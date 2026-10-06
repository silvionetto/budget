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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
        private val uploadedDocumentRepository: UploadedDocumentRepository,
        private val bankMovementRepository: BankMovementRepository,
        private val documentService: DocumentService,
        private val storeRepository: StoreRepository,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val subCategoryRepository: SubCategoryRepository
) {
    @Test
    fun `uploaded CSV movement data can be processed and reprocessed after deleting the source`() {
        val ownerEmail = "${UUID.randomUUID()}@example.com"
        val storeName = "Imported store ${UUID.randomUUID()}"
        val retroactiveStoreName = "Retroactive store ${UUID.randomUUID()}"
        val csv = bankCsv(movement("20200101", storeName, "512,78"))
        val file = MockMultipartFile("file", "statement.csv", "text/csv", csv)
        ensureUnknownExpenseCategory()

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
            assertThat(document.content).isNotEmpty
            assertThat(document.sourceFileAvailable).isTrue()
            val savedMovement = bankMovementRepository
                    .findAllByUploadedDocumentOrderByRowNumber(document)
                    .single()
            assertThat(savedMovement.storeName).isEqualTo(storeName)
            assertThat(savedMovement.date.time).isEqualTo("20200101".toDate().time)
            assertThat(savedMovement.account).isEqualTo("NL91INGB0702811688")
            assertThat(savedMovement.contraAccount).isEqualTo("PT50001800034886484702004")
            assertThat(savedMovement.code).isEqualTo("GT")
            assertThat(savedMovement.debitCredit).isEqualTo("Debit")
            assertThat(savedMovement.amount).isEqualTo(512.78)
            assertThat(savedMovement.transactionType).isEqualTo("Online Banking")
            assertThat(savedMovement.notifications).contains("Frederico Tupinamba Simoes")
            val store = storeRepository.findByName(storeName)
            assertThat(store).isNull()

            mockMvc.perform(get("/documents").with(ownerLogin(ownerEmail)))
                    .andExpect(status().isOk)
                    .andExpect(content().string(containsString("statement.csv")))
                    .andExpect(content().string(containsString("Process")))

            mockMvc.perform(
                    post("/documents/${document.id}/process")
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute(
                            "success",
                            "Document processed: 1 movements imported, 0 duplicates skipped."
                    ))

            val importedStore = storeRepository.findByName(storeName)
            assertThat(importedStore).isNotNull
            val transaction = transactionRepository.findByStore(importedStore!!).single()
            assertThat(transaction.date.time).isEqualTo("20200101".toDate().time)
            assertThat(transaction.account).isEqualTo("NL91INGB0702811688")
            assertThat(transaction.contraAccount).isEqualTo("PT50001800034886484702004")
            assertThat(transaction.code).isEqualTo("GT")
            assertThat(transaction.debitCredit).isEqualTo("Debit")
            assertThat(transaction.amount).isEqualTo(512.78)
            assertThat(transaction.transactionType).isEqualTo("Online Banking")
            assertThat(transaction.notifications).contains("Frederico Tupinamba Simoes")

            mockMvc.perform(get("/documents").with(ownerLogin("other@example.com")))
                    .andExpect(status().isOk)
                    .andExpect(content().string(not(containsString("statement.csv"))))

            mockMvc.perform(get("/documents/${document.id}/download").with(ownerLogin(ownerEmail)))
                    .andExpect(status().isOk)
                    .andExpect(content().bytes(csv))

            mockMvc.perform(get("/documents/${document.id}/download").with(ownerLogin("other@example.com")))
                    .andExpect(status().isNotFound)

            mockMvc.perform(
                    post("/documents/${document.id}/process")
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute(
                            "success",
                            "Document processed: 0 movements imported, 1 duplicates skipped."
                    ))
            mockMvc.perform(
                    post("/documents/${document.id}/delete-source")
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute(
                            "success",
                            "Original CSV file deleted. Saved movements are retained and can still be processed."
                    ))
            val savedDocument = uploadedDocumentRepository.findById(document.id!!).orElseThrow()
            assertThat(savedDocument.content).isNull()
            assertThat(savedDocument.sourceFileAvailable).isFalse()
            assertThat(bankMovementRepository.findAllByUploadedDocumentOrderByRowNumber(savedDocument)).hasSize(1)
            mockMvc.perform(get("/documents").with(ownerLogin(ownerEmail)))
                    .andExpect(status().isOk)
                    .andExpect(content().string(containsString("CSV removed")))
                    .andExpect(content().string(containsString("Process")))
            mockMvc.perform(get("/documents/${document.id}/download").with(ownerLogin(ownerEmail)))
                    .andExpect(status().isNotFound)

            mockMvc.perform(
                    post("/documents/${document.id}/process")
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute(
                            "success",
                            "Document processed: 0 movements imported, 1 duplicates skipped."
                    ))
            assertThat(transactionRepository.findByStore(importedStore).size).isEqualTo(1)

            val retroactiveCsv = bankCsv(movement("20191231", retroactiveStoreName, "15,58"))
            mockMvc.perform(
                    multipart("/documents")
                            .file(MockMultipartFile("file", "retroactive.csv", "text/csv", retroactiveCsv))
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute(
                            "success",
                            "CSV uploaded. 1 movements saved. Process the document to add them to your budget."
                    ))
            val retroactiveDocument = uploadedDocumentRepository
                    .findAllByOwnerEmailOrderByUploadedAtDesc(ownerEmail)
                    .first { it.fileName == "retroactive.csv" }
            assertThat(storeRepository.findByName(retroactiveStoreName)).isNull()
            mockMvc.perform(
                    post("/documents/${retroactiveDocument.id}/process")
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute(
                            "success",
                            "Document processed: 1 movements imported, 0 duplicates skipped."
                    ))
            val retroactiveStore = storeRepository.findByName(retroactiveStoreName)
            assertThat(retroactiveStore).isNotNull
            assertThat(transactionRepository.findByStore(retroactiveStore!!).single().amount).isEqualTo(15.58)
        } finally {
            cleanup(ownerEmail, storeName, retroactiveStoreName)
        }
    }

    @Test
    fun `malformed upload creates no partial documents movement rows stores or transactions`() {
        val ownerEmail = "${UUID.randomUUID()}@example.com"
        val validStoreName = "Valid row ${UUID.randomUUID()}"
        val csv = bankCsv(
                movement("20200101", validStoreName, "10,00"),
                movement("20200102", "Malformed row ${UUID.randomUUID()}", "not-an-amount")
        )

        try {
            mockMvc.perform(
                    multipart("/documents")
                            .file(MockMultipartFile("file", "statement.csv", "text/csv", csv))
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute(
                            "error",
                            "CSV row 3 has an invalid amount."
                    ))

            assertThat(uploadedDocumentRepository.findAllByOwnerEmailOrderByUploadedAtDesc(ownerEmail)).isEmpty()
            assertThat(storeRepository.findByName(validStoreName)).isNull()
        } finally {
            cleanup(ownerEmail, validStoreName)
        }
    }

    @Test
    fun `comma delimited CSV saves quoted decimal amounts and notification text`() {
        val ownerEmail = "${UUID.randomUUID()}@example.com"
        val storeName = "Comma store ${UUID.randomUUID()}"
        ensureUnknownExpenseCategory()

        try {
            mockMvc.perform(
                    multipart("/documents")
                            .file(MockMultipartFile(
                                    "file",
                                    "comma-statement.csv",
                                    "text/csv",
                                    bankCsv(',', movement("20200101", storeName, "512,78"))
                            ))
                            .with(ownerLogin(ownerEmail))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute(
                            "success",
                            "CSV uploaded. 1 movements saved. Process the document to add them to your budget."
                    ))

            val document = uploadedDocumentRepository.findAllByOwnerEmailOrderByUploadedAtDesc(ownerEmail).single()
            val savedMovement = bankMovementRepository
                    .findAllByUploadedDocumentOrderByRowNumber(document)
                    .single()
            assertThat(savedMovement.amount).isEqualTo(512.78)
            assertThat(savedMovement.notifications).contains("IBAN: PT50001800034886484702004")
            assertThat(storeRepository.findByName(storeName)).isNull()
        } finally {
            cleanup(ownerEmail, storeName)
        }
    }

    @Test
    fun `saved documents cannot be processed by another owner`() {
        val ownerEmail = "${UUID.randomUUID()}@example.com"
        val storeName = "Private store ${UUID.randomUUID()}"
        val document = documentService.saveCsv(
                ownerEmail,
                MockMultipartFile("file", "private.csv", "text/csv", bankCsv(movement("20200101", storeName, "1,00")))
        )

        try {
            mockMvc.perform(
                    post("/documents/${document.id}/process")
                            .with(ownerLogin("${UUID.randomUUID()}@example.com"))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(flash().attribute("error", "Document not found."))
            assertThat(storeRepository.findByName(storeName)).isNull()
        } finally {
            cleanup(ownerEmail, storeName)
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

    private fun movement(date: String, description: String, amount: String): List<String> = listOf(
            date,
            description,
            "NL91INGB0702811688",
            "PT50001800034886484702004",
            "GT",
            "Debit",
            amount,
            "Online Banking",
            "Name: Frederico Tupinamba Simoes IBAN: PT50001800034886484702004 Value date: 01/01/2020"
    )

    private fun bankCsv(vararg movements: List<String>): ByteArray {
        return bankCsv(';', *movements)
    }

    private fun bankCsv(delimiter: Char, vararg movements: List<String>): ByteArray {
        val header = listOf(
                "Date",
                "Name / Description",
                "Account",
                "Counterparty",
                "Code",
                "Debit/credit",
                "Amount (EUR)",
                "Transaction type",
                "Notifications"
        ).joinToString(delimiter.toString())
        val rows = movements.map { movement ->
            movement.joinToString(delimiter.toString()) { value -> "\"${value.replace("\"", "\"\"")}\"" }
        }
        return (listOf(header) + rows).joinToString("\n").toByteArray()
    }

    private fun cleanup(ownerEmail: String, vararg storeNames: String) {
        uploadedDocumentRepository.findAllByOwnerEmailOrderByUploadedAtDesc(ownerEmail)
                .forEach { document ->
                    bankMovementRepository.deleteAllByUploadedDocument(document)
                    uploadedDocumentRepository.delete(document)
                }
        storeNames.forEach { storeName ->
            storeRepository.findByName(storeName)?.let { store ->
                transactionRepository.findByStore(store).forEach(transactionRepository::delete)
                storeRepository.delete(store)
            }
        }
    }

    private fun ensureUnknownExpenseCategory() {
        val category = categoryRepository.findByName("Unknown_Expense")
                ?: categoryRepository.save(BudgetCategory("Unknown_Expense", BudgetType.EXPENSE))
        if (subCategoryRepository.findByNameAndCategoryName("Unknown_Expense", category.name) == null) {
            subCategoryRepository.save(BudgetSubCategory("Unknown_Expense", category.name))
        }
    }
}
