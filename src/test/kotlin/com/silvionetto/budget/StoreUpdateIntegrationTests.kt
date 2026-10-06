package com.silvionetto.budget

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class StoreUpdateIntegrationTests @Autowired constructor(
        private val mockMvc: MockMvc,
        private val categoryRepository: CategoryRepository,
        private val subCategoryRepository: SubCategoryRepository,
        private val storeRepository: StoreRepository
) {

    @Test
    fun `store update supports duplicate subcategory names across categories`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val categoryA = categoryRepository.save(BudgetCategory("Category A $suffix", BudgetType.EXPENSE))
        val categoryB = categoryRepository.save(BudgetCategory("Category B $suffix", BudgetType.EXPENSE))
        val sharedSubCategoryName = "Shared $suffix"
        val subCategoryA = subCategoryRepository.save(BudgetSubCategory(sharedSubCategoryName, categoryA))
        val subCategoryB = subCategoryRepository.save(BudgetSubCategory(sharedSubCategoryName, categoryB))
        val store = storeRepository.save(Store("Store $suffix", subCategoryA))

        try {
            mockMvc.perform(
                    post("/store/${store.id}")
                            .param("name", store.name)
                            .param("category", categoryB.name)
                            .param("subCategoryId", subCategoryB.id.toString())
                            .with(user("admin").roles("USER"))
                            .with(csrf())
            )
                    .andExpect(status().isOk)

            val updatedStore = storeRepository.findById(store.id!!).orElseThrow()
            assertThat(updatedStore.subCategory.id).isEqualTo(subCategoryB.id)
            assertThat(updatedStore.subCategory.category.id).isEqualTo(categoryB.id)
            assertThat(updatedStore.subCategory.name).isEqualTo(sharedSubCategoryName)
        } finally {
            storeRepository.findById(store.id!!).ifPresent(storeRepository::delete)
            subCategoryRepository.findById(subCategoryA.id!!).ifPresent(subCategoryRepository::delete)
            subCategoryRepository.findById(subCategoryB.id!!).ifPresent(subCategoryRepository::delete)
            categoryRepository.findById(categoryA.id!!).ifPresent(categoryRepository::delete)
            categoryRepository.findById(categoryB.id!!).ifPresent(categoryRepository::delete)
        }
    }

    @Test
    fun `store update redirects with friendly flash error for invalid category subcategory combination`() {
        val suffix = UUID.randomUUID().toString().substring(0, 8)
        val categoryA = categoryRepository.save(BudgetCategory("Category A $suffix", BudgetType.EXPENSE))
        val categoryB = categoryRepository.save(BudgetCategory("Category B $suffix", BudgetType.EXPENSE))
        val sharedSubCategoryName = "Shared $suffix"
        val subCategoryA = subCategoryRepository.save(BudgetSubCategory(sharedSubCategoryName, categoryA))
        val subCategoryB = subCategoryRepository.save(BudgetSubCategory(sharedSubCategoryName, categoryB))
        val store = storeRepository.save(Store("Store $suffix", subCategoryA))

        try {
            mockMvc.perform(
                    post("/store/${store.id}")
                            .param("name", store.name)
                            .param("category", categoryA.name)
                            .param("subCategoryId", subCategoryB.id.toString())
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
            assertThat(unchangedStore.subCategory.id).isEqualTo(subCategoryA.id)
        } finally {
            storeRepository.findById(store.id!!).ifPresent(storeRepository::delete)
            subCategoryRepository.findById(subCategoryA.id!!).ifPresent(subCategoryRepository::delete)
            subCategoryRepository.findById(subCategoryB.id!!).ifPresent(subCategoryRepository::delete)
            categoryRepository.findById(categoryA.id!!).ifPresent(categoryRepository::delete)
            categoryRepository.findById(categoryB.id!!).ifPresent(categoryRepository::delete)
        }
    }
}


