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
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.Year
import java.util.function.Supplier
import jakarta.persistence.EntityNotFoundException

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
    lateinit var categoryService: CategoryService

    @Autowired
    lateinit var subCategoryService: SubCategoryService

    @Autowired
    lateinit var budgetService: BudgetService

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var transactionService: TransactionService

    @Value("title")
    lateinit var title: String

    private val year: String
        get() = Year.now().value.toString()

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
        val categories = categoryRepository.findAll().toList().sortedBy { it.name }
        model["categories"] = categories.map { category ->
            category to subCategoryRepository.findByCategoryName(category.name).size
        }
        return "categories"
    }

    @PostMapping("/categories")
    fun createCategory(
            @RequestParam name: String,
            @RequestParam type: BudgetType,
            redirectAttributes: RedirectAttributes
    ): String {
        try {
            categoryService.saveCategory(BudgetCategory(name.trim(), type))
            redirectAttributes.addFlashAttribute("success", "Category created.")
        } catch (exception: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Could not create category.")
        }
        return "redirect:/categories"
    }

    @PostMapping("/categories/{id}/update")
    fun updateCategory(
            @PathVariable id: Long,
            @RequestParam name: String,
            @RequestParam type: BudgetType,
            redirectAttributes: RedirectAttributes
    ): String {
        try {
            categoryService.update(id, name.trim(), type)
            redirectAttributes.addFlashAttribute("success", "Category updated.")
        } catch (exception: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Could not update category.")
        } catch (exception: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Category not found.")
        }
        return "redirect:/categories"
    }

    @PostMapping("/categories/{id}/delete")
    fun deleteCategory(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String {
        try {
            categoryService.delete(id)
            redirectAttributes.addFlashAttribute("success", "Category and its subcategory definitions deleted.")
        } catch (exception: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Category not found.")
        }
        return "redirect:/categories"
    }

    @PostMapping("/categories/{id}/subcategories")
    fun createSubCategory(
            @PathVariable id: Long,
            @RequestParam name: String,
            redirectAttributes: RedirectAttributes
    ): String {
        try {
            val category = categoryRepository.findById(id).orElseThrow(
                    Supplier { EntityNotFoundException("Category id $id not found!") }
            )
            subCategoryService.saveSubCategory(BudgetSubCategory(name.trim(), category.name))
            redirectAttributes.addFlashAttribute("success", "Subcategory created.")
            return "redirect:/categories/${category.id}/subcategories"
        } catch (exception: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Could not create subcategory.")
        } catch (exception: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Category not found.")
        }
        return "redirect:/categories"
    }

    @PostMapping("/subcategories/{id}/update")
    fun updateSubCategory(
            @PathVariable id: Long,
            @RequestParam name: String,
            @RequestParam categoryName: String,
            redirectAttributes: RedirectAttributes
    ): String {
        var redirectUrl = "redirect:/categories"
        subCategoryRepository.findById(id).ifPresent { current ->
            categoryRepository.findByName(current.categoryName)?.id?.let {
                redirectUrl = "redirect:/categories/$it/subcategories"
            }
        }
        try {
            val subCategory = subCategoryService.update(id, name.trim(), categoryName)
            redirectAttributes.addFlashAttribute("success", "Subcategory updated.")
            val categoryId = categoryRepository.findByName(subCategory.categoryName)?.id
            if (categoryId != null) {
                redirectUrl = "redirect:/categories/$categoryId/subcategories"
            }
        } catch (exception: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Could not update subcategory.")
        } catch (exception: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Subcategory not found.")
        }
        return redirectUrl
    }

    @PostMapping("/subcategories/{id}/delete")
    fun deleteSubCategory(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String {
        var redirectUrl = "redirect:/categories"
        subCategoryRepository.findById(id).ifPresent { current ->
            categoryRepository.findByName(current.categoryName)?.id?.let {
                redirectUrl = "redirect:/categories/$it/subcategories"
            }
        }
        try {
            val subcategory = subCategoryRepository.findById(id)
                    .orElseThrow(Supplier { EntityNotFoundException("Subcategory id $id not found!") })
            val categoryId = categoryRepository.findByName(subcategory.categoryName)?.id
            subCategoryService.delete(id)
            redirectAttributes.addFlashAttribute("success", "Subcategory deleted.")
            if (categoryId != null) {
                redirectUrl = "redirect:/categories/$categoryId/subcategories"
            }
        } catch (exception: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("error", exception.message ?: "Subcategory not found.")
        }
        return redirectUrl
    }

    @GetMapping("/categories/{id}/subcategories")
    fun manageSubCategories(@PathVariable id: Long, model: Model): String {
        val category = categoryRepository.findById(id).orElse(null)
                ?: return "redirect:/categories"
        model["title"] = "Subcategories: ${category.name}"
        model["category"] = category
        model["subcategories"] = subCategoryRepository.findByCategoryName(category.name).sortedBy { it.name }
        model["allCategories"] = categoryRepository.findAll().toList().sortedBy { it.name }
        return "subcategories"
    }

    @GetMapping("/stores")
    fun stores(model: Model): String {
        model["title"] = "Stores"
        model["stores"] = storeRepository.findAll().toList().sortedBy { it.name }
        return "stores"
    }

    @GetMapping("/categories/{name}")
    fun category(@PathVariable name: String, model: Model): String {
        model["previousYear"] = getPreviousYear(year)
        model["year"] = year
        model["nextYear"] = getNextYear(year)
        val category = categoryRepository.findByName(name) ?: return "redirect:/categories"
        model["year"] = year
        model["title"] = category.name
        model["category"] = category
        model["subcategories"] = subCategoryRepository.findByCategoryName(category.name)
        model["transactions"] = transactionRepository.findByCategoryName(category.name)
        model["budgets"] = budgetService.getBudget(year, category)
        return "category"
    }

    @GetMapping("/subcategories/{type}/{category}/{name}")
    fun subcategory(@PathVariable type: String, @PathVariable category: String,
                    @PathVariable name: String, model: Model): String {
        val subCategory = subCategoryRepository.findByNameAndCategoryName(name, category)
                ?: return "redirect:/categories"
        model["title"] = name
        model["category"] = category
        model["subcategory"] = subCategory
        model["stores"] = storeRepository.findByCategoryNameAndSubCategoryName(category, name)
        return "subcategory"
    }

    @GetMapping("/transactions/{category}/{year}/{month}")
    fun transactions(@PathVariable category: String,
                     @PathVariable year: String,
                     @PathVariable month: String,
                     model: Model): String {
        val transactions = transactionService.getByCategoryAndYearAndMonth(category, year, month)
        transactions.apply {
            model["title"] = month
            model["category"] = category
            model["transactions"] = this
        }
        return "transactions"
    }

    @GetMapping("/store/{id}")
    fun store(@PathVariable id: Long, model: Model): String {
        val store = storeRepository.findById(id.toLong()).orElseThrow(Supplier { EntityNotFoundException("Store id $id not found!") })
        store.apply {
            model["store"] = this
            model["title"] = name
            model["subcategory"] = subCategoryName
            model["category"] = categoryName
            model["categoryType"] = categoryRepository.findByName(categoryName)?.type?.name ?: "ARCHIVED"
            model["categories"] = categoryRepository.findAll().toList().sortedBy { it.name }
            model["subcategories"] = subCategoryRepository.findAll().toList()
                    .sortedWith(compareBy<BudgetSubCategory> { it.categoryName }.thenBy { it.name })
            model["hasStoredCategoryDefinition"] = categoryRepository.findByName(categoryName) != null
            model["hasStoredSubcategoryDefinition"] =
                    subCategoryRepository.findByNameAndCategoryName(subCategoryName, categoryName) != null
            model["transactions"] = transactionRepository.findByStore(this)
        }

        return "store"
    }

    @PostMapping("/store/{id}")
    fun addStore(@RequestParam name: String,
                 @RequestParam categoryName: String,
                 @RequestParam subCategoryName: String,
                 @PathVariable id: Long,
                 model: Model,
                 redirectAttributes: RedirectAttributes): String {
        val store: Store = try {
            storeService.update(id, categoryName, subCategoryName, name)
        } catch (_: IllegalArgumentException) {
            redirectAttributes.addFlashAttribute("error", "Could not update store. Please select a valid category and subcategory.")
            return "redirect:/store/$id"
        } catch (_: EntityNotFoundException) {
            redirectAttributes.addFlashAttribute("error", "Could not update store. Please select a valid category and subcategory.")
            return "redirect:/store/$id"
        }
        model["title"] = store.name
        model["categories"] = categoryRepository.findAll().toList().sortedBy { it.name }
        model["subcategories"] = subCategoryRepository.findAll().toList()
                .sortedWith(compareBy<BudgetSubCategory> { it.categoryName }.thenBy { it.name })
        model["category"] = store.categoryName
        model["categoryType"] = categoryRepository.findByName(store.categoryName)?.type?.name ?: "ARCHIVED"
        model["subcategory"] = store.subCategoryName
        model["hasStoredCategoryDefinition"] = categoryRepository.findByName(store.categoryName) != null
        model["hasStoredSubcategoryDefinition"] =
                subCategoryRepository.findByNameAndCategoryName(store.subCategoryName, store.categoryName) != null
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
        val previousYear = year.toInt() - 1
        return previousYear.toString()
    }

    fun getNextYear(year: String): String {
        val next = year.toInt() + 1
        return next.toString()
    }

//    @DeleteMapping("/store/{id}")
//    fun deleteStore(@PathVariable id: Long) {
//        storeRepository.deleteById(id)
//    }

    /*@PostMapping("/store/{id}")
    fun updateStore(store: Store,
                    @PathVariable id: Long,
                    model: Model) : Store {
        if (store.id != id) {
            throw InputMismatchException("Store id: $store.id is different than $id")
        }
        storeRepository.findById(id).orElseThrow(Supplier { EntityNotFoundException("Store id $store.id not found!") })
        return storeRepository.save(store)
    }*/

}