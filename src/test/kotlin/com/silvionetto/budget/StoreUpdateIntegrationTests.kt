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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Date
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class StoreUpdateIntegrationTests @Autowired constructor(
        private val mockMvc: MockMvc,
        private val categoryRepository: CategoryRepository,
        private val subCategoryRepository: SubCategoryRepository,
        private val storeRepository: StoreRepository,
        private val transactionRepository: TransactionRepository
) {

    @Test
    fun `new store page loads and creating a store validates subcategory belongs to category`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val categoryA = categoryRepository.save(BudgetCategory("Category A $suffix", BudgetType.EXPENSE))
        val categoryB = categoryRepository.save(BudgetCategory("Category B $suffix", BudgetType.EXPENSE))
        val subA = subCategoryRepository.save(BudgetSubCategory("Sub A $suffix", categoryA.name))
        val subB = subCategoryRepository.save(BudgetSubCategory("Sub B $suffix", categoryB.name))
        val storeName = "New store $suffix"

        try {
            mockMvc.perform(get("/stores/new").with(user("admin").roles("USER")))
                    .andExpect(status().isOk)

            val existing = storeRepository.save(Store("Existing $suffix", categoryA.name, subA.name))
            try {
                mockMvc.perform(get("/store/${existing.id}").with(user("admin").roles("USER")))
                        .andExpect(status().isOk)
            } finally {
                storeRepository.delete(existing)
            }

            mockMvc.perform(
                    post("/stores/new")
                            .param("name", storeName)
                            .param("categoryName", categoryA.name)
                            .param("subCategoryName", subB.name)
                            .with(user("admin").roles("USER")).with(csrf())
            ).andExpect(redirectedUrl("/stores/new"))
            assertThat(storeRepository.findByName(storeName)).isNull()

            mockMvc.perform(
                    post("/stores/new")
                            .param("name", storeName)
                            .param("categoryName", categoryA.name)
                            .param("subCategoryName", subA.name)
                            .with(user("admin").roles("USER")).with(csrf())
            ).andExpect(status().is3xxRedirection)
            val created = storeRepository.findByName(storeName)!!
            assertThat(created.categoryName).isEqualTo(categoryA.name)
            assertThat(created.subCategoryName).isEqualTo(subA.name)
        } finally {
            storeRepository.findByName(storeName)?.let(storeRepository::delete)
            subCategoryRepository.delete(subA)
            subCategoryRepository.delete(subB)
            categoryRepository.delete(categoryA)
            categoryRepository.delete(categoryB)
        }
    }

    @Test
    fun `store update supports duplicate subcategory names across categories`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val categoryA = categoryRepository.save(BudgetCategory("Category A $suffix", BudgetType.EXPENSE))
        val categoryB = categoryRepository.save(BudgetCategory("Category B $suffix", BudgetType.EXPENSE))
        val sharedSubCategoryName = "Shared $suffix"
        val subCategoryA = subCategoryRepository.save(BudgetSubCategory(sharedSubCategoryName, categoryA.name))
        val subCategoryB = subCategoryRepository.save(BudgetSubCategory(sharedSubCategoryName, categoryB.name))
        val store = storeRepository.save(Store("Store $suffix", categoryA.name, subCategoryA.name))
        val transaction = transactionRepository.save(
                Transaction(Date(), store, "account", "contra", "code", "Debit", 10.0, "purchase", "",
                        categoryA.name, subCategoryA.name)
        )

        try {
            mockMvc.perform(
                    post("/store/${store.id}")
                            .param("name", store.name)
                            .param("categoryName", categoryB.name)
                            .param("subCategoryName", subCategoryB.name)
                            .with(user("admin").roles("USER"))
                            .with(csrf())
            )
                    .andExpect(status().isOk)

            val updatedStore = storeRepository.findById(store.id!!).orElseThrow()
            assertThat(updatedStore.categoryName).isEqualTo(categoryB.name)
            assertThat(updatedStore.subCategoryName).isEqualTo(sharedSubCategoryName)
            val reclassifiedTransaction = transactionRepository.findById(transaction.id!!).orElseThrow()
            assertThat(reclassifiedTransaction.categoryName).isEqualTo(categoryB.name)
            assertThat(reclassifiedTransaction.subCategoryName).isEqualTo(sharedSubCategoryName)
        } finally {
            transactionRepository.findById(transaction.id!!).ifPresent(transactionRepository::delete)
            storeRepository.findById(store.id!!).ifPresent(storeRepository::delete)
            subCategoryRepository.findById(subCategoryA.id!!).ifPresent(subCategoryRepository::delete)
            subCategoryRepository.findById(subCategoryB.id!!).ifPresent(subCategoryRepository::delete)
            categoryRepository.findById(categoryA.id!!).ifPresent(categoryRepository::delete)
            categoryRepository.findById(categoryB.id!!).ifPresent(categoryRepository::delete)
        }
    }

    @Test
    fun `reclassifying unknown incoming store moves its existing transaction to selected category`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val unknownCategory = categoryRepository.save(
                BudgetCategory("Unknown_Income $suffix", BudgetType.INCOME)
        )
        val unknownSubCategory = subCategoryRepository.save(
                BudgetSubCategory("Unknown_Income $suffix", unknownCategory.name)
        )
        val targetCategory = categoryRepository.save(BudgetCategory("Income $suffix", BudgetType.INCOME))
        val targetSubCategory = subCategoryRepository.save(BudgetSubCategory("Salary $suffix", targetCategory.name))
        val store = storeRepository.save(
                Store("Incoming store $suffix", unknownCategory.name, unknownSubCategory.name)
        )
        val transaction = transactionRepository.save(
                Transaction(Date(), store, "account", "contra", "code", "Credit", 125.0, "income", "",
                        unknownCategory.name, unknownSubCategory.name)
        )

        try {
            mockMvc.perform(
                    post("/store/${store.id}")
                            .param("name", store.name)
                            .param("categoryName", targetCategory.name)
                            .param("subCategoryName", targetSubCategory.name)
                            .with(user("admin").roles("USER"))
                            .with(csrf())
            ).andExpect(status().isOk)

            val updatedStore = storeRepository.findById(store.id!!).orElseThrow()
            val updatedTransaction = transactionRepository.findById(transaction.id!!).orElseThrow()
            assertThat(updatedStore.categoryName).isEqualTo(targetCategory.name)
            assertThat(updatedStore.subCategoryName).isEqualTo(targetSubCategory.name)
            assertThat(updatedTransaction.categoryName).isEqualTo(targetCategory.name)
            assertThat(updatedTransaction.subCategoryName).isEqualTo(targetSubCategory.name)
        } finally {
            transactionRepository.findById(transaction.id!!).ifPresent(transactionRepository::delete)
            storeRepository.findById(store.id!!).ifPresent(storeRepository::delete)
            subCategoryRepository.findById(unknownSubCategory.id!!).ifPresent(subCategoryRepository::delete)
            subCategoryRepository.findById(targetSubCategory.id!!).ifPresent(subCategoryRepository::delete)
            categoryRepository.findById(unknownCategory.id!!).ifPresent(categoryRepository::delete)
            categoryRepository.findById(targetCategory.id!!).ifPresent(categoryRepository::delete)
        }
    }

    @Test
    fun `store update redirects with friendly flash error for invalid category subcategory combination`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val categoryA = categoryRepository.save(BudgetCategory("Category A $suffix", BudgetType.EXPENSE))
        val categoryB = categoryRepository.save(BudgetCategory("Category B $suffix", BudgetType.EXPENSE))
        val subCategoryNameA = "Subcategory A $suffix"
        val subCategoryNameB = "Subcategory B $suffix"
        val subCategoryA = subCategoryRepository.save(BudgetSubCategory(subCategoryNameA, categoryA.name))
        val subCategoryB = subCategoryRepository.save(BudgetSubCategory(subCategoryNameB, categoryB.name))
        val store = storeRepository.save(Store("Store $suffix", categoryA.name, subCategoryA.name))

        try {
            mockMvc.perform(
                    post("/store/${store.id}")
                            .param("name", store.name)
                            .param("categoryName", categoryA.name)
                            .param("subCategoryName", subCategoryB.name)
                            .with(user("admin").roles("USER"))
                            .with(csrf())
            )
                    .andExpect(status().is3xxRedirection)
                    .andExpect(redirectedUrl("/store/${store.id}"))
                    .andExpect(flash().attribute(
                            "error",
                            "Could not update store. Please select a valid category and subcategory."
                    ))

            val unchangedStore = storeRepository.findById(store.id!!).orElseThrow()
            assertThat(unchangedStore.categoryName).isEqualTo(categoryA.name)
            assertThat(unchangedStore.subCategoryName).isEqualTo(subCategoryA.name)
        } finally {
            storeRepository.findById(store.id!!).ifPresent(storeRepository::delete)
            subCategoryRepository.findById(subCategoryA.id!!).ifPresent(subCategoryRepository::delete)
            subCategoryRepository.findById(subCategoryB.id!!).ifPresent(subCategoryRepository::delete)
            categoryRepository.findById(categoryA.id!!).ifPresent(categoryRepository::delete)
            categoryRepository.findById(categoryB.id!!).ifPresent(categoryRepository::delete)
        }
    }
}
