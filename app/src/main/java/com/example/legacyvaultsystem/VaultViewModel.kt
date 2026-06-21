package com.example.legacyvaultsystem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VaultViewModel : ViewModel() {
    private val _assets = MutableLiveData<List<DigitalAsset>>(emptyList())
    val assets: LiveData<List<DigitalAsset>> = _assets

    private val _documents = MutableLiveData<List<Document>>(emptyList())
    val documents: LiveData<List<Document>> = _documents

    private val _executors = MutableLiveData<List<Executor>>(emptyList())
    val executors: LiveData<List<Executor>> = _executors

    private val _instructions = MutableLiveData<List<Instruction>>(emptyList())
    val instructions: LiveData<List<Instruction>> = _instructions

    private val _activities = MutableLiveData<List<Activity>>(emptyList())
    val activities: LiveData<List<Activity>> = _activities

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    fun addAsset(name: String, platform: String, category: String, username: String = "", passwordHint: String = "", url: String = "", action: String = "No Action") {
        val newAsset = DigitalAsset(name, platform, category, username, passwordHint, url, action)
        _assets.value = (_assets.value ?: emptyList()) + newAsset
        addActivity(name, "Digital asset added", "asset")
    }

    fun addDocument(title: String, category: String, fileName: String, description: String = "", notes: String = "") {
        val newDoc = Document(title, category, fileName, description, notes)
        _documents.value = (_documents.value ?: emptyList()) + newDoc
        addActivity(title, "Document uploaded", "doc")
    }

    fun addExecutor(name: String, relation: String, email: String, phone: String, accessLevel: String = "Full Access", isPrimary: Boolean = false) {
        val newExecutor = Executor(name, relation, email, phone, accessLevel, isPrimary)
        _executors.value = (_executors.value ?: emptyList()) + newExecutor
        addActivity(name, "Executor assigned", "executor")
    }

    fun addInstruction(title: String, content: String, type: String = "General", priority: String = "Medium") {
        val newInstruction = Instruction(title, content, type, priority)
        _instructions.value = (_instructions.value ?: emptyList()) + newInstruction
        addActivity(title, "Instruction written", "instruction")
    }

    private fun addActivity(title: String, subtitle: String, type: String) {
        val newActivity = Activity(title, "$subtitle · ${getCurrentDate()}", getCurrentDate(), type)
        _activities.value = listOf(newActivity) + (_activities.value ?: emptyList())
    }
}