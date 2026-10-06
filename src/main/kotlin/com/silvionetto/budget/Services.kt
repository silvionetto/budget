package com.silvionetto.budget

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*
import java.util.function.Supplier
import jakarta.persistence.EntityNotFoundException
import org.springframework.transaction.annotation.Transactional

@Service
class UserService {

    @Autowired
    lateinit var userRepository: UserRepository

    fun saveUser(user: User): User {
        if (userRepository.findByLogin(user.login) == null) {
            return userRepository.save(user)
        }
        return user
    }
}

@Service
class StoreService {
    @Autowired
    lateinit var storeRepository: StoreRepository

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @Autowired
    lateinit var subCategoryRepository: SubCategoryRepository

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    fun saveStore(store: Store): Store {
        if (storeRepository.findByName(store.name) == null) {
            return storeRepository.save(store)
        }
        return store
    }

    @Transactional
    fun update(id: Long, categoryName: String, subCategoryName: String, name: String): Store {
        val store: Store = storeRepository.findById(id).orElseThrow(Supplier { EntityNotFoundException("Store id $id not found!") })
        val category = categoryRepository.findByName(categoryName)
                ?: throw EntityNotFoundException("Category $categoryName not found!")
        val subCategory = subCategoryRepository.findByNameAndCategoryName(subCategoryName, categoryName)
                ?: throw IllegalArgumentException("Invalid category/subcategory combination")
        store.name = name
        store.categoryName = category.name
        store.subCategoryName = subCategory.name
        val updatedStore = storeRepository.save(store)
        val transactions = transactionRepository.findByStore(store).onEach { transaction ->
            transaction.categoryName = category.name
            transaction.subCategoryName = subCategory.name
        }
        transactionRepository.saveAll(transactions)
        return updatedStore
    }

    fun saveStore(storeName: String, transactionSide: String): Store {
        var store = storeRepository.findByName(storeName)

        if (store != null) {
            println(store)
        } else {
            if (TransactionSide.Credit == TransactionSide.valueOf(transactionSide)) {
                val category = categoryRepository.findByName("Unknown_Income")
                        ?: throw EntityNotFoundException("Category Unknown_Income not found!")
                val subCategory = subCategoryRepository.findByNameAndCategoryName("Unknown_Income", category.name)
                        ?: throw EntityNotFoundException("Subcategory Unknown_Income not found!")
                store = saveStore(Store(storeName, category.name, subCategory.name))
                println("Store: $store, Category: $category, SubCategory: $subCategory")
            } else {
                val category = categoryRepository.findByName("Unknown_Expense")
                        ?: throw EntityNotFoundException("Category Unknown_Expense not found!")
                val subCategory = subCategoryRepository.findByNameAndCategoryName("Unknown_Expense", category.name)
                        ?: throw EntityNotFoundException("Subcategory Unknown_Expense not found!")
                store = saveStore(Store(storeName, category.name, subCategory.name))
                println("Store: $store, Category: $category, SubCategory: $subCategory")
            }
        }

        return store
    }

    fun saveStore(storeName: String, budgetType: String, subCategoryName: String): Store {
        var store = storeRepository.findByName(storeName)

        if (store != null) {
            println(store)
        } else {
            val type = BudgetType.valueOf(budgetType)
            val categoryNames = categoryRepository.findByType(type).map { it.name }
            val subCategory = categoryNames.flatMap(subCategoryRepository::findByCategoryName)
                    .firstOrNull { it.name == subCategoryName }
                    ?: throw EntityNotFoundException("Subcategory $subCategoryName not found for type $type!")
            store = saveStore(Store(storeName, subCategory.categoryName, subCategory.name))
            println("Store: $store, SubCategory: $subCategory, Type: $type")
        }

        return store
    }
}

@Service
class CategoryService {
    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @Autowired
    lateinit var subCategoryRepository: SubCategoryRepository

    fun saveCategory(category: BudgetCategory): BudgetCategory {
        require(category.name.isNotBlank()) { "Category name is required." }
        require(category.name.length <= 255) { "Category name must be 255 characters or fewer." }
        if (categoryRepository.findByName(category.name) != null) {
            throw IllegalArgumentException("A category with this name already exists.")
        }
        return categoryRepository.save(category)
    }

    @Transactional
    fun update(id: Long, name: String, type: BudgetType): BudgetCategory {
        require(name.isNotBlank()) { "Category name is required." }
        require(name.length <= 255) { "Category name must be 255 characters or fewer." }
        val category = categoryRepository.findById(id)
                .orElseThrow(Supplier { EntityNotFoundException("Category id $id not found!") })
        val duplicate = categoryRepository.findByName(name)
        if (duplicate != null && duplicate.id != id) {
            throw IllegalArgumentException("A category with this name already exists.")
        }
        val oldName = category.name
        category.name = name
        category.type = type
        if (oldName != name) {
            subCategoryRepository.findByCategoryName(oldName).forEach { it.categoryName = name }
        }
        return categoryRepository.save(category)
    }

    @Transactional
    fun delete(id: Long) {
        val category = categoryRepository.findById(id)
                .orElseThrow(Supplier { EntityNotFoundException("Category id $id not found!") })
        subCategoryRepository.findByCategoryName(category.name).forEach(subCategoryRepository::delete)
        categoryRepository.delete(category)
    }
}

@Service
class SubCategoryService {
    @Autowired
    lateinit var subCategoryRepository: SubCategoryRepository

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    fun saveSubCategory(subCategory: BudgetSubCategory): BudgetSubCategory {
        require(subCategory.name.isNotBlank()) { "Subcategory name is required." }
        require(subCategory.name.length <= 255) { "Subcategory name must be 255 characters or fewer." }
        require(categoryRepository.findByName(subCategory.categoryName) != null) { "Select an existing category." }
        if (subCategoryRepository.findByNameAndCategoryName(subCategory.name, subCategory.categoryName) != null) {
            throw IllegalArgumentException("A subcategory with this name already exists in this category.")
        }
        return subCategoryRepository.save(subCategory)
    }

    fun update(id: Long, name: String, categoryName: String): BudgetSubCategory {
        require(name.isNotBlank()) { "Subcategory name is required." }
        require(name.length <= 255) { "Subcategory name must be 255 characters or fewer." }
        require(categoryRepository.findByName(categoryName) != null) { "Select an existing category." }
        val subCategory = subCategoryRepository.findById(id)
                .orElseThrow(Supplier { EntityNotFoundException("Subcategory id $id not found!") })
        val duplicate = subCategoryRepository.findByNameAndCategoryName(name, categoryName)
        if (duplicate != null && duplicate.id != id) {
            throw IllegalArgumentException("A subcategory with this name already exists in the selected category.")
        }
        subCategory.name = name
        subCategory.categoryName = categoryName
        return subCategoryRepository.save(subCategory)
    }

    fun delete(id: Long) {
        val subCategory = subCategoryRepository.findById(id)
                .orElseThrow(Supplier { EntityNotFoundException("Subcategory id $id not found!") })
        subCategoryRepository.delete(subCategory)
    }
}

@Service
class TransactionService {
    @Autowired
    lateinit var transactionRepository: TransactionRepository

    fun saveTransaction(transaction: Transaction): Transaction {
        if (!exists(transaction)) {
            return transactionRepository.save(transaction)
        }
        return transaction
    }

    fun exists(transaction: Transaction): Boolean {
        val transactions = transactionRepository.findByDate(transaction.date)
        return transactions.any { it.store.name == transaction.store.name && it.amount == transaction.amount }
    }

    fun getByCategoryAndMonth(category: BudgetCategory, month: String): List<Transaction> {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val monthValue = Month.valueOf(month).value
        
        val startDate = GregorianCalendar(year, monthValue - 1, 1).time
        val endDate = GregorianCalendar(year, monthValue, 1).time
        
        return transactionRepository.findByCategoryNameAndDateBetween(category.name, startDate, endDate)
    }

    fun getByCategoryAndYearAndMonth(categoryName: String, year: String, month: String): List<Transaction> {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val monthValue = Month.valueOf(month).value
        val startDate = sdf.parse("$year-$monthValue-1")
        val endDate = sdf.parse("$year-${monthValue + 1}-1")
        return transactionRepository.findByCategoryNameAndDateBetween(categoryName, startDate, endDate)
    }
}

@Service
class BudgetService {
    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    fun getBudgetByType(): List<Budget> {
        val budgets = mutableListOf<Budget>()

        val budgetTotal = Budget(Constants.TOTAL, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        var budget = Budget(Constants.INCOME, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        var transactions = transactionRepository.findByDebitCredit(Constants.DEBIT)

        transactions.forEach {
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            when (calendar.get(Calendar.MONTH) + 1) {
                1 -> budget.january += it.amount
                2 -> budget.february += it.amount
                3 -> budget.march += it.amount
                4 -> budget.april += it.amount
                5 -> budget.may += it.amount
                6 -> budget.june += it.amount
                7 -> budget.july += it.amount
                8 -> budget.august += it.amount
                9 -> budget.september += it.amount
                10 -> budget.october += it.amount
                11 -> budget.november += it.amount
                12 -> budget.december += it.amount
            }
        }

        budgets.add(budget)
        budgetTotal.january += budget.january
        budgetTotal.february += budget.february
        budgetTotal.march += budget.march
        budgetTotal.april += budget.april
        budgetTotal.may += budget.may
        budgetTotal.june += budget.june
        budgetTotal.july += budget.july
        budgetTotal.august += budget.august
        budgetTotal.september += budget.september
        budgetTotal.october += budget.october
        budgetTotal.november += budget.november
        budgetTotal.december += budget.december

        budget = Budget(Constants.EXPENSE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        transactions = transactionRepository.findByDebitCredit(Constants.CREDIT)

        transactions.forEach {
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            when (calendar.get(Calendar.MONTH) + 1) {
                1 -> budget.january += it.amount
                2 -> budget.february += it.amount
                3 -> budget.march += it.amount
                4 -> budget.april += it.amount
                5 -> budget.may += it.amount
                6 -> budget.june += it.amount
                7 -> budget.july += it.amount
                8 -> budget.august += it.amount
                9 -> budget.september += it.amount
                10 -> budget.october += it.amount
                11 -> budget.november += it.amount
                12 -> budget.december += it.amount
            }
        }

        budgets.add(budget)
        budgetTotal.january -= budget.january
        budgetTotal.february -= budget.february
        budgetTotal.march -= budget.march
        budgetTotal.april -= budget.april
        budgetTotal.may -= budget.may
        budgetTotal.june -= budget.june
        budgetTotal.july -= budget.july
        budgetTotal.august -= budget.august
        budgetTotal.september -= budget.september
        budgetTotal.october -= budget.october
        budgetTotal.november -= budget.november
        budgetTotal.december -= budget.december

        budgets.add(budgetTotal)

        return budgets
    }

    fun getBudget(year: String): List<Budget> {
        // Get all transactions from the year
        // Split by category and subcategory / month

        val budgets = mutableListOf<Budget>()
        val categories = categoryRepository.findAll()
        categories.forEach { category ->
            budgets.add(getBudget(year, category))
        }
        return budgets
    }

    fun getBalance(year: String): List<Budget> {
        val balance = mutableListOf<Budget>()
        val incomes = getIncome(year)
        val exponses = getExpense(year)
        balance.add(incomes)
        balance.add(exponses)
        val yieldBudget = Budget(Constants.YIELD,
                incomes.january - exponses.january,
                incomes.february - exponses.february,
                incomes.march - exponses.march,
                incomes.april - exponses.april,
                incomes.may - exponses.may,
                incomes.june - exponses.june,
                incomes.july - exponses.july,
                incomes.august - exponses.august,
                incomes.september - exponses.september,
                incomes.october - exponses.october,
                incomes.november - exponses.november,
                incomes.december - exponses.december
        )
        balance.add(yieldBudget)
        val openingBalance = 0.0 // TODO
        val jan = openingBalance.plus(incomes.january).minus(exponses.january)
        val fev = jan.plus(incomes.february).minus(exponses.february)
        val mar = fev.plus(incomes.march).minus(exponses.march)
        val apr = mar.plus(incomes.april).minus(exponses.april)
        val may = apr.plus(incomes.may).minus(exponses.may)
        val jun = may.plus(incomes.june).minus(exponses.june)
        val jul = jun.plus(incomes.july).minus(exponses.july)
        val aug = jul.plus(incomes.august).minus(exponses.august)
        val sep = aug.plus(incomes.september).minus(exponses.september)
        val oct = sep.plus(incomes.october).minus(exponses.october)
        val nov = oct.plus(incomes.november).minus(exponses.november)
        val dez = nov.plus(incomes.december).minus(exponses.december)

        val projected = Budget(Constants.PROJECTED_BALANCE, jan, fev, mar, apr, may, jun, jul, aug, sep, oct, nov, dez)
        balance.add(projected)
        return balance
    }

    fun getIncome(year: String): Budget {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val startDate = sdf.parse("$year-01-01")
        val endDate = sdf.parse(String.format("%s-01-01", Integer.valueOf(year) + 1))
        val transactions = transactionRepository.findByDebitCreditAndDateBetween(Constants.CREDIT, startDate, endDate)
        val budget = Budget(Constants.INCOME, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        transactions.forEach {
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            when (calendar.get(Calendar.MONTH) + 1) {
                1 -> budget.january += it.amount
                2 -> budget.february += it.amount
                3 -> budget.march += it.amount
                4 -> budget.april += it.amount
                5 -> budget.may += it.amount
                6 -> budget.june += it.amount
                7 -> budget.july += it.amount
                8 -> budget.august += it.amount
                9 -> budget.september += it.amount
                10 -> budget.october += it.amount
                11 -> budget.november += it.amount
                12 -> budget.december += it.amount
            }
        }

        return budget
    }

    fun getExpense(year: String): Budget {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val startDate = sdf.parse("$year-01-01")
        val endDate = sdf.parse(String.format("%s-01-01", Integer.valueOf(year) + 1))
        val transactions = transactionRepository.findByDebitCreditAndDateBetween(Constants.DEBIT, startDate, endDate)
        val budget = Budget(Constants.EXPENSE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        transactions.forEach {
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            when (calendar.get(Calendar.MONTH) + 1) {
                1 -> budget.january += it.amount
                2 -> budget.february += it.amount
                3 -> budget.march += it.amount
                4 -> budget.april += it.amount
                5 -> budget.may += it.amount
                6 -> budget.june += it.amount
                7 -> budget.july += it.amount
                8 -> budget.august += it.amount
                9 -> budget.september += it.amount
                10 -> budget.october += it.amount
                11 -> budget.november += it.amount
                12 -> budget.december += it.amount
            }
        }

        return budget
    }

    fun getBudget(year: String, category: BudgetCategory): Budget {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val startDate = sdf.parse("$year-01-01")
        val endDate = sdf.parse(String.format("%s-01-01", Integer.valueOf(year) + 1))
        val transactions = transactionRepository.findByCategoryNameAndDateBetween(category.name, startDate, endDate)
        val budget = Budget(category.name, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        transactions.forEach {
            val calendar = Calendar.getInstance()
            calendar.time = it.date
            when (calendar.get(Calendar.MONTH) + 1) {
                1 -> budget.january += it.amount
                2 -> budget.february += it.amount
                3 -> budget.march += it.amount
                4 -> budget.april += it.amount
                5 -> budget.may += it.amount
                6 -> budget.june += it.amount
                7 -> budget.july += it.amount
                8 -> budget.august += it.amount
                9 -> budget.september += it.amount
                10 -> budget.october += it.amount
                11 -> budget.november += it.amount
                12 -> budget.december += it.amount
            }
        }

        return budget
    }
}
