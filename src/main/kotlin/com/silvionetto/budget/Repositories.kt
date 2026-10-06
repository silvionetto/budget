package com.silvionetto.budget

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface UploadedDocumentSummary {
    val id: Long?
    val fileName: String
    val fileSize: Long
    val uploadedAt: Date
    val sourceFileAvailable: Boolean
}

@Repository
interface UserRepository : CrudRepository<User, Long> {
    fun findByLogin(login: String): User?
}

@Repository
interface UploadedDocumentRepository : CrudRepository<UploadedDocument, Long> {
    fun findAllByOwnerEmailOrderByUploadedAtDesc(ownerEmail: String): List<UploadedDocument>
    fun findAllProjectedByOwnerEmailOrderByUploadedAtDesc(ownerEmail: String): List<UploadedDocumentSummary>
    fun findByIdAndOwnerEmail(id: Long, ownerEmail: String): UploadedDocument?
}

@Repository
interface BankMovementRepository : CrudRepository<BankMovement, Long> {
    fun findAllByUploadedDocumentOrderByRowNumber(uploadedDocument: UploadedDocument): List<BankMovement>
    @Transactional
    fun deleteAllByUploadedDocument(uploadedDocument: UploadedDocument)
}

@Repository
interface CategoryRepository : CrudRepository<BudgetCategory, Long> {
    fun findByName(name: String): BudgetCategory?
    fun findByNameAndType(name: String, budgetType: BudgetType): BudgetCategory
    fun findByType(budgetType: BudgetType): List<BudgetCategory>
}

@Repository
interface SubCategoryRepository : CrudRepository<BudgetSubCategory, Long> {
    fun findByName(name: String): BudgetSubCategory?
    fun findByCategoryName(categoryName: String): List<BudgetSubCategory>
    fun findByNameAndCategoryName(name: String, categoryName: String): BudgetSubCategory?
}

@Repository
interface StoreRepository : CrudRepository<Store, Long> {
    fun findByName(name: String): Store?
    fun findByCategoryNameAndSubCategoryName(categoryName: String, subCategoryName: String): List<Store>
}

@Repository
interface TransactionRepository: CrudRepository<Transaction, Long> {
    fun findByDebitCredit(debitCredit: String): List<Transaction>
    fun findByDate(date: Date): List<Transaction>
    fun findByStore(store: Store): List<Transaction>
    fun findByCategoryName(categoryName: String): List<Transaction>
    fun findByCategoryNameAndDateBetween(categoryName: String, startDate: Date, endDate: Date): List<Transaction>
    fun findByDebitCreditAndDateBetween(debitCredit: String, startDate: Date, endDate: Date): List<Transaction>
}