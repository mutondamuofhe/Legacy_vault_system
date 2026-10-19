package com.example.legacyvaultsystem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultSecurityTest {

    @Test
    fun testAccessLevelPermissions() {
        val executor = Executor("Sarah", "Spouse", "sarah@example.com", "1234567890", "Full Access", true)
        assertEquals("Full Access", executor.accessLevel)
        assertTrue(executor.isPrimary)
    }

    @Test
    fun testInstructionPrioritySorting() {
        val instructions = listOf(
            Instruction("Cancel Subscription", "Cancel Netflix", "General", "Low"),
            Instruction("Banking Directive", "Transfer funds", "Financial", "High"),
            Instruction("Email Autoresponder", "Set forwarder", "Email", "Medium")
        )

        val highPriority = instructions.filter { it.priority == "High" }
        assertEquals(1, highPriority.size)
        assertEquals("Banking Directive", highPriority[0].title)
    }
}