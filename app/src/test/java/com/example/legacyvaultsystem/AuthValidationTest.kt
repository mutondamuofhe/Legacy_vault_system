package com.example.legacyvaultsystem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    private fun isValidStrictEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val pattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!pattern.matches(email)) return false

        val domainPart = email.substringAfter("@", "")
        val domainName = domainPart.substringBeforeLast(".", "")
        if (domainName.isEmpty() || domainName.matches(Regex("^[0-9]+$"))) {
            return false
        }
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    @Test
    fun testInvalidNumericDomainEmail_ReturnsFalse() {
        val invalidEmail = "liftarke@1.com"
        assertFalse(isValidStrictEmail(invalidEmail))
    }

    @Test
    fun testValidEmail_ReturnsTrue() {
        val validEmail = "user@example.com"
        assertTrue(isValidStrictEmail(validEmail))
    }

    @Test
    fun testEmptyEmail_ReturnsFalse() {
        val emptyEmail = ""
        assertFalse(isValidStrictEmail(emptyEmail))
    }

    @Test
    fun testPasswordTooShort_ReturnsFalse() {
        val shortPassword = "123"
        assertFalse(isValidPassword(shortPassword))
    }

    @Test
    fun testValidPassword_ReturnsTrue() {
        val validPassword = "securePassword123"
        assertTrue(isValidPassword(validPassword))
    }
}