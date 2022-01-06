package com.silvionetto.budget

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.Month
import java.util.*
import java.util.function.Supplier
import javax.persistence.EntityNotFoundException

@Controller
@PropertySource("classpath:app.properties")
@ConfigurationProperties("app")
class HtmlController() {

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @Autowired
    lateinit var subCategoryRepository: SubCategoryRepository

    @Autowired
    lateinit var storeRepository: StoreRepository

    @Autowired
    lateinit var storeService: StoreService

    @Autowired
    lateinit var budgetService: BudgetService

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var transactionService: TransactionService

    @Value("title")
    lateinit var title: String

    @Value("year")
    lateinit var year: String

    @Value("previousYear")
    lateinit var previousYear: String

    @Value("nextYear")
    lateinit var nextYear: String

    @GetMapping("/")
    fun home(model: Model): String {
        model["previousYear"] = getPreviousYear(year)
        model["year"] = year
        model["nextYear"] = getNextYear(year)
        model["title"] = title
        model["budgets"] = budgetService.getBudget(year)
        return "body"
    }

    @GetMapping("/year/{year}")
    fun year(@PathVariable year: String, model: Model): String {
        model["previousYear"] = getPreviousYear(year)
        model["year"] = year
        model["nextYear"] = getNextYear(year)
        model["title"] = title
        model["budgets"] = budgetService.getBudget(year)
        return "body"
    }

    @GetMapping("/types")
    fun types(model: Model): String {
        model["title"] = title
        model["budgets"] = budgetService.getBudgetByType()
        return "types"
    }

    @GetMapping("/categories")
    fun categories(model: Model): String {
        model["previousYear"] = getPreviousYear(year)
        model["year"] = year
        model["nextYear"] = getNextYear(year)
        model["title"] = title
        model["categories"] = categoryRepository.findAll()
        return "categories"
    }

    @GetMapping("/categories/{name}")
    fun category(@PathVariable name: String, model: Model): String {
        model["previousYear"] = getPreviousYear(year)
        model["year"] = year
        model["nextYear"] = getNextYear(year)
        val category = categoryRepository.findByName(name)
        category?.apply {
            model["year"] = year
            model["title"] = category.name
            model["category"] = category
            model["subcategories"] = subCategoryRepository.findByCategory(category)
            model["transactions"] = transactionRepository.findBySubCategoryCategory(category)
            model["budgets"] = budgetService.getBudget(year, category)
        }
        return "category"
    }

    @GetMapping("/subcategories/{type}/{category}/{name}")
    fun subcategory(@PathVariable type: String, @PathVariable category: String,
                    @PathVariable name: String, model: Model): String {
        val subCategory = subCategoryRepository.findByName(name)
        subCategory?.apply {
            model["title"] = name
            model["category"] = category
            model["subcategory"] = this
            model["stores"] = storeRepository.findBySubCategory(subCategory)
        }
        return "subcategory"
    }

    @GetMapping("/transactions/{category}/{year}/{month}")
    fun transactions(@PathVariable category: String,
                     @PathVariable year: String,
                     @PathVariable month: String,
                     model: Model): String {
        val budgetCategory = categoryRepository.findByNameAndType(category, BudgetType.EXPENSE)
        val budgetSubCategory = subCategoryRepository.findByCategory(budgetCategory)
        val transactions = mutableListOf<Transaction>()
        budgetSubCategory.forEach {
            transactions.addAll(transactionService.getBySubCategoryAndYearAndMonth(it, year, month))
        }
        transactions.apply {
            model["title"] = month
            model["category"] = budgetCategory
            model["transactions"] = this
        }
        return "transactions"
    }

    @GetMapping("/store/{id}")
    fun store(@PathVariable id: Long, model: Model): String {
        var store = storeRepository.findById(id.toLong()).orElseThrow(Supplier { EntityNotFoundException("Store id $id not found!") })
        store.apply {
            model["store"] = this
            model["title"] = name
            model["subcategory"] = subCategory
            model["category"] = subCategory.category
            model["categories"] = categoryRepository.findByType(subCategory.category.type)
            model["subcategories"] = subCategoryRepository.findByCategoryType(subCategory.category.type)
            model["transactions"] = transactionRepository.findByStore(this)
        }

        return "store"
    }

    @PostMapping("/store/{id}")
    fun addStore(@RequestParam name: String,
                 @RequestParam category: String,
                 @RequestParam subCategory: String,
                 @PathVariable id: Long, model: Model): String {
        var store: Store = storeService.update(id, category, subCategory, name)
        var budgetSubCategory: BudgetSubCategory = store.subCategory
        var budgetCategory: BudgetCategory = budgetSubCategory.category
        model["title"] = store.name
        model["categories"] = categoryRepository.findByType(budgetCategory.type)
        model["subcategories"] = subCategoryRepository.findByCategoryType(budgetCategory.type)
        model["category"] = budgetCategory
        model["subcategory"] = budgetSubCategory
        model["store"] = store
        return "store"
    }

    @GetMapping("/balance/{year}")
    fun balance(@PathVariable year: String, model: Model): String {
        model["previousYear"] = getPreviousYear(year)
        model["year"] = year
        model["nextYear"] = getNextYear(year)
        model["title"] = title
        model["budget"] = budgetService.getBalance(year)
        return "balance"
    }

    @GetMapping("/reports/{year}")
    fun reports(@PathVariable year: String, model: Model): String {
        model["previousYear"] = getPreviousYear(year)
        model["year"] = year
        model["nextYear"] = getNextYear(year)
        model["title"] = title
        return "reports"
    }

    fun getPreviousYear(year: String): String {
        var previousYear = year.toInt() - 1
        return previousYear.toString()
    }

    fun getNextYear(year: String): String {
        var next = year.toInt() + 1
        return next.toString()
    }

    @GetMapping("/stores")
    fun stores(model: Model): String {
        model["stores"] = storeRepository.findAll()
        return "stores"
    }
}