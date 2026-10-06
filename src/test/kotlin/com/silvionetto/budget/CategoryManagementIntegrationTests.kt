package com.silvionetto.budget

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Date
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class CategoryManagementIntegrationTests @Autowired constructor(
        private val mockMvc: MockMvc,
        private val categoryRepository: CategoryRepository,
        private val subCategoryRepository: SubCategoryRepository,
        private val storeRepository: StoreRepository,
        private val transactionRepository: TransactionRepository
) {

    @Test
    fun `category and subcategory management are presented on separate pages`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val category = categoryRepository.save(BudgetCategory("Category $suffix", BudgetType.EXPENSE))
        val subcategory = subCategoryRepository.save(BudgetSubCategory("Subcategory $suffix", category.name))
        val otherCategory = categoryRepository.save(BudgetCategory("Other category $suffix", BudgetType.INCOME))
        val otherSubcategory = subCategoryRepository.save(BudgetSubCategory("Other subcategory $suffix", otherCategory.name))
        try {
            mockMvc.perform(get("/categories").with(user("admin").roles("USER")))
                    .andExpect(status().isOk)
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Manage categories")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Manage subcategories")))
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Add subcategory"))))
            mockMvc.perform(get("/categories/${category.id}/subcategories").with(user("admin").roles("USER")))
                    .andExpect(status().isOk)
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Subcategories")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(subcategory.name)))
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(otherSubcategory.name))))
        } finally {
            subCategoryRepository.delete(subcategory)
            subCategoryRepository.delete(otherSubcategory)
            categoryRepository.delete(category)
            categoryRepository.delete(otherCategory)
        }
    }

    @Test
    fun `category rename and deletion preserve stored labels and records`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val originalName = "Category $suffix"
        val renamedName = "Renamed $suffix"
        val subcategoryName = "Subcategory $suffix"

        mockMvc.perform(
                post("/categories")
                        .param("name", originalName)
                        .param("type", BudgetType.EXPENSE.name)
                        .with(user("admin").roles("USER"))
                        .with(csrf())
        ).andExpect(status().is3xxRedirection).andExpect(redirectedUrl("/categories"))

        val category = categoryRepository.findByName(originalName)!!
        mockMvc.perform(
                post("/categories/${category.id}/subcategories")
                        .param("name", subcategoryName)
                        .with(user("admin").roles("USER"))
                        .with(csrf())
        ).andExpect(status().is3xxRedirection)
                .andExpect(redirectedUrl("/categories/${category.id}/subcategories"))

        val subcategory = subCategoryRepository.findByNameAndCategoryName(subcategoryName, originalName)!!
        val store = storeRepository.save(Store("Store $suffix", originalName, subcategoryName))
        val transaction = transactionRepository.save(
                Transaction(Date(), store, "account", "contra", "code", "Debit", 10.0, "purchase", "",
                        originalName, subcategoryName)
        )

        try {
            mockMvc.perform(
                    post("/categories/${category.id}/update")
                            .param("name", renamedName)
                            .param("type", BudgetType.INCOME.name)
                            .with(user("admin").roles("USER"))
                            .with(csrf())
            ).andExpect(status().is3xxRedirection)

            assertThat(subCategoryRepository.findById(subcategory.id!!).orElseThrow().categoryName).isEqualTo(renamedName)

            val renamedCategory = categoryRepository.findByName(renamedName)!!
            mockMvc.perform(
                    post("/subcategories/${subcategory.id}/update")
                            .param("name", "Updated $suffix")
                            .param("categoryName", renamedName)
                            .with(user("admin").roles("USER"))
                            .with(csrf())
            ).andExpect(status().is3xxRedirection)

            assertThat(storeRepository.findById(store.id!!).orElseThrow().categoryName).isEqualTo(originalName)
            assertThat(storeRepository.findById(store.id!!).orElseThrow().subCategoryName).isEqualTo(subcategoryName)
            assertThat(transactionRepository.findById(transaction.id!!).orElseThrow().categoryName).isEqualTo(originalName)
            assertThat(transactionRepository.findById(transaction.id!!).orElseThrow().subCategoryName).isEqualTo(subcategoryName)

            mockMvc.perform(
                    post("/categories/${renamedCategory.id}/delete")
                            .with(user("admin").roles("USER"))
                            .with(csrf())
            ).andExpect(status().is3xxRedirection)

            assertThat(categoryRepository.findByName(renamedName)).isNull()
            assertThat(subCategoryRepository.findById(subcategory.id!!)).isEmpty
            assertThat(storeRepository.findById(store.id!!).orElseThrow().categoryName).isEqualTo(originalName)
            assertThat(transactionRepository.findById(transaction.id!!).orElseThrow().subCategoryName).isEqualTo(subcategoryName)
        } finally {
            transactionRepository.findById(transaction.id!!).ifPresent(transactionRepository::delete)
            storeRepository.findById(store.id!!).ifPresent(storeRepository::delete)
            subCategoryRepository.findById(subcategory.id!!).ifPresent(subCategoryRepository::delete)
            categoryRepository.findById(category.id!!).ifPresent(categoryRepository::delete)
            categoryRepository.findByName(renamedName)?.let(categoryRepository::delete)
        }
    }

    @Test
    fun `subcategory deletion preserves store and transaction records`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val category = categoryRepository.save(BudgetCategory("Category $suffix", BudgetType.EXPENSE))
        val subcategory = subCategoryRepository.save(BudgetSubCategory("Subcategory $suffix", category.name))
        val store = storeRepository.save(Store("Store $suffix", category.name, subcategory.name))
        val transaction = transactionRepository.save(
                Transaction(Date(), store, "account", "contra", "code", "Debit", 10.0, "purchase", "",
                        category.name, subcategory.name)
        )

        try {
            mockMvc.perform(
                    post("/subcategories/${subcategory.id}/delete")
                            .with(user("admin").roles("USER"))
                            .with(csrf())
            ).andExpect(status().is3xxRedirection)

            assertThat(subCategoryRepository.findById(subcategory.id!!)).isEmpty
            assertThat(storeRepository.findById(store.id!!).orElseThrow().subCategoryName).isEqualTo(subcategory.name)
            assertThat(transactionRepository.findById(transaction.id!!).orElseThrow().categoryName).isEqualTo(category.name)
        } finally {
            transactionRepository.findById(transaction.id!!).ifPresent(transactionRepository::delete)
            storeRepository.findById(store.id!!).ifPresent(storeRepository::delete)
            subCategoryRepository.findById(subcategory.id!!).ifPresent(subCategoryRepository::delete)
            categoryRepository.findById(category.id!!).ifPresent(categoryRepository::delete)
        }
    }
}
