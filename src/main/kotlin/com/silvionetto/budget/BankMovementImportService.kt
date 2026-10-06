package com.silvionetto.budget

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeParseException

data class BankMovementImportResult(val imported: Int, val duplicatesSkipped: Int)

@Service
class BankMovementImportService(
        private val storeService: StoreService,
        private val transactionService: TransactionService,
        private val bankMovementRepository: BankMovementRepository,
        private val documentService: DocumentService,
        private val uploadedDocumentRepository: UploadedDocumentRepository
) {
    @Transactional
    fun saveDocument(ownerEmail: String, file: org.springframework.web.multipart.MultipartFile): Pair<UploadedDocument, Int> {
        val document = documentService.saveCsv(ownerEmail, file)
        val movements = parse(document.content ?: throw InvalidDocumentException("The uploaded CSV file is unavailable."))
        bankMovementRepository.saveAll(movements.mapIndexed { index, movement ->
            BankMovement(
                    uploadedDocument = document,
                    rowNumber = index + 1,
                    date = movement.date,
                    storeName = movement.storeName,
                    account = movement.account,
                    contraAccount = movement.contraAccount,
                    code = movement.code,
                    debitCredit = movement.transactionSide,
                    amount = movement.amount,
                    transactionType = movement.transactionType,
                    notifications = movement.notifications
            )
        })
        return document to movements.size
    }

    @Transactional
    fun processDocument(document: UploadedDocument): BankMovementImportResult {
        val movements = movementRowsOrBackfill(document).map { movement ->
            ParsedMovement(
                    date = movement.date,
                    storeName = movement.storeName,
                    account = movement.account,
                    contraAccount = movement.contraAccount,
                    code = movement.code,
                    transactionSide = movement.debitCredit,
                    amount = movement.amount,
                    transactionType = movement.transactionType,
                    notifications = movement.notifications
            )
        }
        return importMovements(movements)
    }

    @Transactional
    fun importCsv(content: ByteArray): BankMovementImportResult = importMovements(parse(content))

    private fun importMovements(movements: List<ParsedMovement>): BankMovementImportResult {
        var imported = 0
        var duplicatesSkipped = 0

        movements.forEach { movement ->
            val store = storeService.saveStore(movement.storeName, movement.transactionSide)
            val transaction = Transaction(
                    movement.date,
                    store,
                    movement.account,
                    movement.contraAccount,
                    movement.code,
                    movement.transactionSide,
                    movement.amount,
                    movement.transactionType,
                    movement.notifications,
                    store.categoryName,
                    store.subCategoryName
            )
            if (transactionService.exists(transaction)) {
                duplicatesSkipped++
            } else {
                transactionService.saveTransaction(transaction)
                imported++
            }
        }

        return BankMovementImportResult(imported, duplicatesSkipped)
    }

    @Transactional
    fun deleteSourceFile(document: UploadedDocument) {
        movementRowsOrBackfill(document)
        document.content = null
        document.sourceFileAvailable = false
        uploadedDocumentRepository.save(document)
    }

    private fun movementRowsOrBackfill(document: UploadedDocument): List<BankMovement> {
        val storedMovements = bankMovementRepository.findAllByUploadedDocumentOrderByRowNumber(document)
        if (storedMovements.isNotEmpty()) {
            return storedMovements
        }

        val content = document.content
                ?: throw InvalidDocumentException("This document has no saved movement data or source CSV file.")
        val parsedMovements = parse(content)
        return bankMovementRepository.saveAll(parsedMovements.mapIndexed { index, movement ->
            BankMovement(
                    uploadedDocument = document,
                    rowNumber = index + 1,
                    date = movement.date,
                    storeName = movement.storeName,
                    account = movement.account,
                    contraAccount = movement.contraAccount,
                    code = movement.code,
                    debitCredit = movement.transactionSide,
                    amount = movement.amount,
                    transactionType = movement.transactionType,
                    notifications = movement.notifications
            )
        }).toList()
    }

    private fun parse(content: ByteArray): List<ParsedMovement> {
        val text = try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(content))
                    .toString()
                    .removePrefix("\uFEFF")
        } catch (exception: CharacterCodingException) {
            throw InvalidDocumentException("The CSV file must be valid UTF-8.")
        }

        val parsedByDelimiter = mutableListOf<Pair<Char, List<List<String>>>>()
        var formatError: CsvFormatException? = null
        listOf(';', ',').forEach { delimiter ->
            try {
                parsedByDelimiter.add(delimiter to parseRecords(text, delimiter))
            } catch (exception: CsvFormatException) {
                if (formatError == null) {
                    formatError = exception
                }
            }
        }

        val rows = parsedByDelimiter
                .firstOrNull { (_, records) -> records.firstOrNull()?.size == COLUMN_COUNT }
                ?.second
                ?: throw InvalidDocumentException(
                        formatError?.message ?: "The CSV file must contain exactly $COLUMN_COUNT columns."
                )

        if (rows.isEmpty() || !rows.first()[0].trim().equals("Date", ignoreCase = true)) {
            throw InvalidDocumentException("The CSV file must start with a bank movement header row.")
        }
        if (rows.size == 1) {
            throw InvalidDocumentException("The CSV file does not contain any movements.")
        }

        return rows.drop(1).mapIndexed { index, columns ->
            val rowNumber = index + 2
            if (columns.size != COLUMN_COUNT) {
                throw InvalidDocumentException("CSV row $rowNumber must contain exactly $COLUMN_COUNT columns.")
            }

            val dateValue = columns[0].trim()
            val date = try {
                if (!dateValue.matches(Regex("\\d{8}"))) {
                    throw DateTimeParseException("Expected yyyyMMdd.", dateValue, 0)
                }
                dateValue.toDate()
            } catch (exception: DateTimeParseException) {
                throw InvalidDocumentException("CSV row $rowNumber has an invalid date.")
            }
            val storeName = columns[1].trim()
            if (storeName.isBlank()) {
                throw InvalidDocumentException("CSV row $rowNumber has no movement description.")
            }
            val transactionSide = columns[5].trim()
            if (transactionSide != TransactionSide.Debit.name && transactionSide != TransactionSide.Credit.name) {
                throw InvalidDocumentException("CSV row $rowNumber has an invalid debit/credit value.")
            }
            val amount = parseAmount(columns[6], rowNumber)
            val movementFields = listOf(
                    "account" to columns[2],
                    "counterparty" to columns[3],
                    "code" to columns[4],
                    "transaction type" to columns[7]
            )
            movementFields.forEach { (fieldName, value) ->
                if (value.length > 255) {
                    throw InvalidDocumentException("CSV row $rowNumber has a $fieldName longer than 255 characters.")
                }
            }
            if (columns[8].length > 512) {
                throw InvalidDocumentException("CSV row $rowNumber has notifications longer than 512 characters.")
            }

            ParsedMovement(
                    date = date,
                    storeName = storeName,
                    account = columns[2].trim(),
                    contraAccount = columns[3].trim(),
                    code = columns[4].trim(),
                    transactionSide = transactionSide,
                    amount = amount,
                    transactionType = columns[7].trim(),
                    notifications = columns[8].trim()
            )
        }
    }

    private fun parseAmount(value: String, rowNumber: Int): Double {
        val normalized = value.trim().replace(" ", "").replace("\u00A0", "")
        val decimalValue = when {
            normalized.contains(',') && normalized.contains('.') &&
                    normalized.lastIndexOf(',') > normalized.lastIndexOf('.') ->
                normalized.replace(".", "").replace(',', '.')
            normalized.contains(',') -> normalized.replace(',', '.')
            else -> normalized
        }
        val amount = decimalValue.toDoubleOrNull()
        if (amount == null || !amount.isFinite()) {
            throw InvalidDocumentException("CSV row $rowNumber has an invalid amount.")
        }
        return amount
    }

    private fun parseRecords(text: String, delimiter: Char): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var fields = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var afterQuote = false
        var index = 0

        fun finishField() {
            fields.add(field.toString())
            field.setLength(0)
            afterQuote = false
        }

        fun finishRecord() {
            finishField()
            if (fields.any { it.isNotEmpty() }) {
                records.add(fields)
            }
            fields = mutableListOf()
        }

        while (index < text.length) {
            val character = text[index]
            if (inQuotes) {
                when {
                    character == '"' && index + 1 < text.length && text[index + 1] == '"' -> {
                        field.append('"')
                        index++
                    }
                    character == '"' -> {
                        inQuotes = false
                        afterQuote = true
                    }
                    else -> field.append(character)
                }
            } else {
                if (afterQuote && character != delimiter && character != '\r' && character != '\n') {
                    throw CsvFormatException("The CSV contains unexpected text after a quoted value.")
                }
                when (character) {
                    '"' -> {
                        if (field.isNotEmpty()) {
                            throw CsvFormatException("The CSV contains a quote inside an unquoted value.")
                        }
                        inQuotes = true
                    }
                    delimiter -> finishField()
                    '\r', '\n' -> {
                        finishRecord()
                        if (character == '\r' && index + 1 < text.length && text[index + 1] == '\n') {
                            index++
                        }
                    }
                    else -> field.append(character)
                }
            }
            index++
        }

        if (inQuotes) {
            throw CsvFormatException("The CSV contains an unterminated quoted value.")
        }
        if (field.isNotEmpty() || fields.isNotEmpty() || afterQuote) {
            finishRecord()
        }
        return records
    }

    private data class ParsedMovement(
            val date: java.util.Date,
            val storeName: String,
            val account: String,
            val contraAccount: String,
            val code: String,
            val transactionSide: String,
            val amount: Double,
            val transactionType: String,
            val notifications: String
    )

    private class CsvFormatException(message: String) : IllegalArgumentException(message)

    companion object {
        private const val COLUMN_COUNT = 9
    }
}
