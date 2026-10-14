package com.example.legacyvaultsystem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS?.matcher(email)?.matches() ?: email.contains("@")
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    @Test
    fun testValidEmail_ReturnsTrue() {
        val validEmail = "user@example.com"
        assertTrue(isValidEmail(validEmail))
    }

    @Test
    fun testEmptyEmail_ReturnsFalse() {
        val emptyEmail = ""
        assertFalse(isValidEmail(emptyEmail))
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