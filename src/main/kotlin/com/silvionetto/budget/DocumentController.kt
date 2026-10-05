package com.silvionetto.budget

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.Locale

@Controller
class DocumentController(
        private val documentService: DocumentService
) {
    @GetMapping("/documents")
    fun documents(
            @AuthenticationPrincipal principal: OidcUser,
            model: Model
    ): String {
        model["title"] = "Documents"
        model["documents"] = documentService.listForOwner(ownerEmail(principal))
        return "documents"
    }

    @PostMapping("/documents")
    fun upload(
            @RequestParam("file") file: MultipartFile,
            @AuthenticationPrincipal principal: OidcUser,
            redirectAttributes: RedirectAttributes
    ): String {
        return try {
            documentService.saveCsv(ownerEmail(principal), file)
            redirectAttributes.addFlashAttribute("success", "CSV file uploaded.")
            "redirect:/documents"
        } catch (exception: InvalidDocumentException) {
            redirectAttributes.addFlashAttribute("error", exception.message)
            "redirect:/documents"
        }
    }

    @GetMapping("/documents/{id}/download")
    fun download(
            @PathVariable id: Long,
            @AuthenticationPrincipal principal: OidcUser
    ): ResponseEntity<ByteArray> {
        val document = documentService.findForOwner(id, ownerEmail(principal))
                ?: return ResponseEntity.notFound().build()

        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType(document.contentType)
            contentLength = document.fileSize
            contentDisposition = ContentDisposition.attachment().filename(document.fileName).build()
        }
        return ResponseEntity(document.content, headers, HttpStatus.OK)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleOversizedUpload(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .contentType(MediaType.TEXT_PLAIN)
                .body("CSV files must be 10 MB or smaller. Return to /documents to try again.")
    }

    private fun ownerEmail(principal: OidcUser): String =
            principal.getAttribute<String>("email")
                    ?.trim()
                    ?.lowercase(Locale.ROOT)
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("The authenticated account has no verified email address.")
}
