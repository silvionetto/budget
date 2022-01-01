package com.silvionetto.budget

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CrudRepository<User, Long> {
    fun findByLogin(login: String): User?
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
    fun findByCategory(category: BudgetCategory): List<BudgetSubCategory>
    fun findByNameAndCategory(name: String, category: BudgetCategory): BudgetSubCategory
    fun findByCategoryType(type: BudgetType): List<BudgetSubCategory>
    fun findByNameAndCategoryType(name: String, type: BudgetType): List<BudgetSubCategory>
}

@Repository
interface StoreRepository : CrudRepository<Store, Long> {
    fun findByName(name: String): Store?
    fun findBySubCategory(budgetSubCategory: BudgetSubCategory): List<Store>
}

@Repository
interface TransactionRepository: CrudRepository<Transaction, Long> {
    fun findByDebitCredit(debitCredit: String): List<Transaction>
    fun findByDate(date: Date): List<Transaction>
    fun findBySubCategory(subCategory: BudgetSubCategory): List<Transaction>
    fun findByStore(store: Store): List<Transaction>
    fun findBySubCategoryCategory(category: BudgetCategory): List<Transaction>
    fun findBySubCategoryCategoryAndDateGreaterThan(category: BudgetCategory, date: Date): List<Transaction>
    fun findBySubCategoryCategoryAndDateBetween(category: BudgetCategory, startDate: Date, endDate: Date): List<Transaction>
    fun findByDebitCreditAndDateBetween(debitCredit: String, startDate: Date, endDate: Date): List<Transaction>
}