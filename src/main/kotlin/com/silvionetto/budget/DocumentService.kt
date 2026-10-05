package com.silvionetto.budget

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.Date
import java.util.Locale

class InvalidDocumentException(message: String) : RuntimeException(message)

@Service
class DocumentService(
        private val uploadedDocumentRepository: UploadedDocumentRepository
) {
    fun listForOwner(ownerEmail: String): List<UploadedDocumentSummary> =
            uploadedDocumentRepository.findAllProjectedByOwnerEmailOrderByUploadedAtDesc(normalizeOwner(ownerEmail))

    fun saveCsv(ownerEmail: String, file: MultipartFile): UploadedDocument {
        val owner = normalizeOwner(ownerEmail)
        if (file.isEmpty) {
            throw InvalidDocumentException("Choose a non-empty CSV file.")
        }
        if (file.size > MAX_FILE_SIZE_BYTES) {
            throw InvalidDocumentException("CSV files must be 10 MB or smaller.")
        }

        val fileName = file.originalFilename
                ?.replace('\\', '/')
                ?.substringAfterLast('/')
                ?.filterNot { it.code < 32 || it.code == 127 }
                ?.trim()
                .orEmpty()
        if (fileName.isBlank()) {
            throw InvalidDocumentException("The uploaded file must have a name.")
        }
        if (fileName.length > 255) {
            throw InvalidDocumentException("File names must be 255 characters or fewer.")
        }
        if (!fileName.endsWith(".csv", ignoreCase = true)) {
            throw InvalidDocumentException("Only CSV files are accepted.")
        }

        val content = file.bytes
        return uploadedDocumentRepository.save(
                UploadedDocument(
                        ownerEmail = owner,
                        fileName = fileName,
                        contentType = CSV_CONTENT_TYPE,
                        fileSize = content.size.toLong(),
                        uploadedAt = Date(),
                        content = content
                )
        )
    }

    fun findForOwner(id: Long, ownerEmail: String): UploadedDocument? =
            uploadedDocumentRepository.findByIdAndOwnerEmail(id, normalizeOwner(ownerEmail))

    private fun normalizeOwner(ownerEmail: String): String {
        val normalized = ownerEmail.trim().lowercase(Locale.ROOT)
        if (normalized.isBlank()) {
            throw IllegalArgumentException("Authenticated owner email must not be blank.")
        }
        return normalized
    }

    companion object {
        const val MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024
        const val CSV_CONTENT_TYPE = "text/csv"
    }
}
