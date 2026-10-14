package com.example.legacyvaultsystem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataModelTest {

    @Test
    fun testDigitalAssetCreation() {
        val asset = DigitalAsset(
            name = "Bank Account",
            platform = "First National Bank",
            category = "Banking & Finance",
            username = "user123",
            passwordHint = "First pet name",
            url = "https://fnb.com",
            actionAfterDeath = "Transfer to Executor"
        )

        assertEquals("Bank Account", asset.name)
        assertEquals("Banking & Finance", asset.category)
        assertEquals("Transfer to Executor", asset.actionAfterDeath)
    }

    @Test
    fun testDocumentCreation() {
        val doc = Document(
            title = "Will and Testament",
            category = "Legal",
            fileName = "will_2025.pdf",
            description = "Final Last Will and Testament",
            notes = "Stored in safe box 102"
        )

        assertEquals("Will and Testament", doc.title)
        assertEquals("Legal", doc.category)
        assertEquals("will_2025.pdf", doc.fileName)
    }

    @Test
    fun testExecutorCreation() {
        val executor = Executor(
            name = "John Doe",
            relation = "Brother",
            email = "john@example.com",
            phone = "+1234567890",
            accessLevel = "Full Access",
            isPrimary = true
        )

        assertEquals("John Doe", executor.name)
        assertEquals("Brother", executor.relation)
        assertTrue(executor.isPrimary)
        assertEquals("Full Access", executor.accessLevel)
    }

    @Test
    fun testInstructionCreation() {
        val instruction = Instruction(
            title = "Social Media Closure",
            content = "Request memorialization of Facebook profile.",
            type = "Social Media",
            priority = "High"
        )

        assertEquals("Social Media Closure", instruction.title)
        assertEquals("High", instruction.priority)
        assertEquals("Social Media", instruction.type)
    }
}