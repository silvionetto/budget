package com.silvionetto.budget

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*
import java.util.function.Supplier
import javax.persistence.EntityNotFoundException

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

    fun update(id: Long, category: String, subCategory: String, name: String): Store {
        var store: Store = storeRepository.findById(id).orElseThrow(Supplier { EntityNotFoundException("Store id $id not found!") })
        store.name = name
        var budgetSubCategory = subCategoryRepository.findByName(subCategory)
        budgetSubCategory?.apply {
            store.subCategory = this
        }
        var transactions = transactionRepository.findByStore(store)
        transactions.forEach { transaction ->
            transaction.subCategory = store.subCategory
            transactionRepository.save(transaction)
        }
        return storeRepository.save(store)
    }

    fun saveStore(storeName: String, transactionSide: String): Store {
        var store = storeRepository.findByName(storeName)

        if (store != null) {
            println(store)
        } else {
            if (TransactionSide.Credit == TransactionSide.valueOf(transactionSide)) {
                var category = categoryRepository.findByNameAndType("Unknown_Income", BudgetType.INCOME)
                var subCategory = subCategoryRepository.findByNameAndCategory("Unknown_Income", category)
                store = Store(storeName, subCategory)
                saveStore(store)
                println("Store: $store, Category: $category, SubCategory: $subCategory")
            } else {
                var category = categoryRepository.findByNameAndType("Unknown_Expense", BudgetType.EXPENSE)
                var subCategory = subCategoryRepository.findByNameAndCategory("Unknown_Expense", category)
                store = Store(storeName, subCategory)
                store = saveStore(store)
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
            if (BudgetType.INCOME == BudgetType.valueOf(budgetType)) {
                var subCategory = subCategoryRepository.findByNameAndCategoryType(subCategoryName, BudgetType.INCOME)
                store = Store(storeName, subCategory.first())
                saveStore(store)
                println("Store: $store, SubCategory: $subCategory, Type: Income")
            } else {
                var subCategory = subCategoryRepository.findByNameAndCategoryType(subCategoryName, BudgetType.EXPENSE)
                store = Store(storeName, subCategory.first())
                saveStore(store)
                println("Store: $store, SubCategory: $subCategory, Type: Expense")
            }
        }

        return store
    }
}

@Service
class CategoryService {
    @Autowired
    lateinit var categoryRepository: CategoryRepository

    fun saveCategory(category: BudgetCategory): BudgetCategory {
        if (categoryRepository.findByName(category.name) == null) {
            return categoryRepository.save(category)
        }
        return category
    }
}

@Service
class SubCategoryService {
    @Autowired
    lateinit var subCategoryRepository: SubCategoryRepository

    fun saveSubCategory(subCategory: BudgetSubCategory): BudgetSubCategory {
        if (subCategoryRepository.findByName(subCategory.name) == null) {
            return subCategoryRepository.save(subCategory)
        }
        return subCategory
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
        val now = Date()
        val startDate = Date(now.year, Month.valueOf(month).value - 1, 1)
        val endDate = Date(now.year, Month.valueOf(month).value, 1)
        var transactions = transactionRepository.findBySubCategoryCategory(category).filter {
            it.date.after(startDate) && it.date.before(endDate)
        }
        return transactions
    }

    fun getBySubCategoryAndMonth(subCategory: BudgetSubCategory, month: String): List<Transaction> {
        val now = Date()
        val startDate = Date(now.year, Month.valueOf(month).value - 1, 1)
        val endDate = Date(now.year, Month.valueOf(month).value, 1)
        var transactions = transactionRepository.findBySubCategory(subCategory).filter {
            it.date.after(startDate) && it.date.before(endDate)
        }
        return transactions
    }

    fun getBySubCategoryAndYearAndMonth(subCategory: BudgetSubCategory, year: String, month: String): List<Transaction> {
        val sdf = SimpleDateFormat("yyyy-M-dd")
        val startDate = sdf.parse("$year-${(Month.valueOf(month).value)}-1")
        val endDate = sdf.parse("$year-${Month.valueOf(month).value + 1}-1")
        var transactions = transactionRepository.findBySubCategory(subCategory).filter {
            it.date.after(startDate) && it.date.before(endDate)
        }
        return transactions
    }
}

@Service
class BudgetService {
    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @Autowired
    lateinit var subCategoryRepository: SubCategoryRepository

    fun getBudgetByType(): List<Budget> {
        val budgets = mutableListOf<Budget>()

        var budgetTotal = Budget(Constants.TOTAL, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        var budget = Budget(Constants.INCOME, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        var transactions = transactionRepository.findByDebitCredit(Constants.DEBIT)

        transactions?.forEach {
            when ((it.date.month + 1)) {
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

        transactions?.apply {
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
        }

        budget = Budget(Constants.EXPENSE, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        transactions = transactionRepository.findByDebitCredit(Constants.CREDIT)

        transactions?.forEach {
            when ((it.date.month + 1)) {
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

        transactions?.apply {
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
        }

        budgets.add(budgetTotal)

        return budgets
    }

    fun getBudget(year: String): List<Budget> {
        // Get all transactions from the year
        // Split by category and subcategory / month

        var budgets = mutableListOf<Budget>()
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
            when ((it.date.month + 1)) {
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
            when ((it.date.month + 1)) {
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
        val transactions = transactionRepository.findBySubCategoryCategoryAndDateBetween(category, startDate, endDate)
        val budget = Budget(category.name, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        transactions.forEach {
            when ((it.date.month + 1)) {
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
