package com.example.legacyvaultsystem

data class DigitalAsset(
    val name: String,
    val platform: String = "",
    val category: String = "",
    val username: String = "",
    val passwordHint: String = "",
    val url: String = "",
    val actionAfterDeath: String = "No Action"
)

data class Document(
    val title: String,
    val category: String = "",
    val fileName: String = "",
    val description: String = "",
    val notes: String = ""
)

data class Executor(
    val name: String,
    val relation: String,
    val email: String,
    val phone: String,
    val accessLevel: String = "Full Access",
    val isPrimary: Boolean = false
)

data class Instruction(
    val title: String,
    val content: String,
    val type: String = "General", // Social Media, Financial, etc.
    val priority: String = "Medium" // High, Medium, Low
)

data class Activity(
    val title: String,
    val subtitle: String,
    val date: String,
    val iconType: String // "asset", "doc", "executor", "instruction"
)