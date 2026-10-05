package com.silvionetto.budget

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdminOidcUserServiceTests {

    @Test
    fun `allows the configured verified admin email`() {
        assertTrue(isAllowedAdminEmail("silvio.netto@gmail.com", true, "silvio.netto@gmail.com"))
    }

    @Test
    fun `matches configured email without case sensitivity`() {
        assertTrue(isAllowedAdminEmail("Silvio.Netto@gmail.com", true, "silvio.netto@gmail.com"))
    }

    @Test
    fun `rejects other or unverified email addresses`() {
        assertFalse(isAllowedAdminEmail("someone@example.com", true, "silvio.netto@gmail.com"))
        assertFalse(isAllowedAdminEmail("silvio.netto@gmail.com", false, "silvio.netto@gmail.com"))
        assertFalse(isAllowedAdminEmail("silvio.netto@gmail.com", null, "silvio.netto@gmail.com"))
    }
}
