package com.silvionetto.budget

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class DocumentUploadIntegrationTests @Autowired constructor(
        private val mockMvc: MockMvc,
        private val uploadedDocumentRepository: UploadedDocumentRepository
) {
    @Test
    fun `uploaded CSV is listed and downloadable only by its owner`() {
        val ownerEmail = "${UUID.randomUUID()}@example.com"
        val csv = "date,amount\n2026-01-01,12.34\n".toByteArray()
        val file = MockMultipartFile("file", "statement.csv", "text/csv", csv)

        try {
            mockMvc.perform(
                    multipart("/documents")
                            .file(file)
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(redirectedUrl("/documents"))

            val document = uploadedDocumentRepository
                    .findAllByOwnerEmailOrderByUploadedAtDesc(ownerEmail)
                    .single()
            assertThat(document.content).containsExactly(*csv)
            assertThat(document.fileName).isEqualTo("statement.csv")

            mockMvc.perform(get("/documents").with(ownerLogin(ownerEmail)))
                    .andExpect(status().isOk)
                    .andExpect(content().string(containsString("statement.csv")))

            mockMvc.perform(get("/documents").with(ownerLogin("other@example.com")))
                    .andExpect(status().isOk)
                    .andExpect(content().string(not(containsString("statement.csv"))))

            mockMvc.perform(get("/documents/${document.id}/download").with(ownerLogin(ownerEmail)))
                    .andExpect(status().isOk)
                    .andExpect(content().bytes(csv))

            mockMvc.perform(get("/documents/${document.id}/download").with(ownerLogin("other@example.com")))
                    .andExpect(status().isNotFound)
        } finally {
            uploadedDocumentRepository
                    .findAllByOwnerEmailOrderByUploadedAtDesc(ownerEmail)
                    .forEach(uploadedDocumentRepository::delete)
        }
    }

    @Test
    fun `non CSV uploads are rejected without saving the file`() {
        val ownerEmail = "${UUID.randomUUID()}@example.com"
        val file = MockMultipartFile("file", "statement.txt", "text/plain", "not a CSV".toByteArray())

        mockMvc.perform(
                multipart("/documents")
                        .file(file)
                        .with(ownerLogin(ownerEmail))
                        .with(csrf())
        )
                .andExpect(status().is3xxRedirection)
                .andExpect(flash().attribute("error", "Only CSV files are accepted."))

        assertThat(uploadedDocumentRepository.findAllByOwnerEmailOrderByUploadedAtDesc(ownerEmail)).isEmpty()
    }

    @Test
    fun `uploads larger than 10 MB are rejected`() {
        val oversizedCsv = ByteArray(DocumentService.MAX_FILE_SIZE_BYTES.toInt() + 1)
        val file = MockMultipartFile("file", "large.csv", "text/csv", oversizedCsv)

        mockMvc.perform(
                multipart("/documents")
                        .file(file)
                        .with(ownerLogin("large-file@example.com"))
                        .with(csrf())
        )
                .andExpect(status().is3xxRedirection)
                .andExpect(flash().attribute("error", "CSV files must be 10 MB or smaller."))

        assertThat(
                uploadedDocumentRepository.findAllByOwnerEmailOrderByUploadedAtDesc("large-file@example.com")
        ).isEmpty()
    }

    private fun ownerLogin(email: String) =
            oidcLogin().idToken { token -> token.claim("email", email) }
}
