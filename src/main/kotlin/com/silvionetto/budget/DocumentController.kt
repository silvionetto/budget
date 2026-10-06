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
        private val documentService: DocumentService,
        private val bankMovementImportService: BankMovementImportService
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
        val saved = try {
            bankMovementImportService.saveDocument(ownerEmail(principal), file)
        } catch (exception: InvalidDocumentException) {
            redirectAttributes.addFlashAttribute("error", exception.message)
            return "redirect:/documents"
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "CSV uploaded. ${saved.second} movements saved. Process the document to add them to your budget."
        )
        return "redirect:/documents"
    }

    @PostMapping("/documents/{id}/process")
    fun reprocess(
            @PathVariable id: Long,
            @AuthenticationPrincipal principal: OidcUser,
            redirectAttributes: RedirectAttributes
    ): String {
        val document = documentService.findForOwner(id, ownerEmail(principal))
        if (document == null) {
            redirectAttributes.addFlashAttribute("error", "Document not found.")
            return "redirect:/documents"
        }

        return try {
            val result = bankMovementImportService.processDocument(document)
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Document processed: ${result.imported} movements imported, " +
                            "${result.duplicatesSkipped} duplicates skipped."
            )
            "redirect:/documents"
        } catch (exception: InvalidDocumentException) {
            redirectAttributes.addFlashAttribute("error", "Document was not processed: ${exception.message}")
            "redirect:/documents"
        }
    }

    @PostMapping("/documents/{id}/delete-source")
    fun deleteSourceFile(
            @PathVariable id: Long,
            @AuthenticationPrincipal principal: OidcUser,
            redirectAttributes: RedirectAttributes
    ): String {
        val document = documentService.findForOwner(id, ownerEmail(principal))
        if (document == null) {
            redirectAttributes.addFlashAttribute("error", "Document not found.")
            return "redirect:/documents"
        }

        return try {
            bankMovementImportService.deleteSourceFile(document)
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Original CSV file deleted. Saved movements are retained and can still be processed."
            )
            "redirect:/documents"
        } catch (exception: InvalidDocumentException) {
            redirectAttributes.addFlashAttribute("error", "CSV file was not deleted: ${exception.message}")
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
        val content = document.content ?: return ResponseEntity.notFound().build()

        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType(document.contentType)
            contentLength = content.size.toLong()
            contentDisposition = ContentDisposition.attachment().filename(document.fileName).build()
        }
        return ResponseEntity(content, headers, HttpStatus.OK)
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
