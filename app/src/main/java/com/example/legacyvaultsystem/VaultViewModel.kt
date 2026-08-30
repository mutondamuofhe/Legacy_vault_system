package com.example.legacyvaultsystem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VaultViewModel : ViewModel() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    
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

    private var listeners = mutableListOf<ListenerRegistration>()

    init {
        loadData()
    }

    private fun loadData() {
        // Clear old listeners if any
        listeners.forEach { it.remove() }
        listeners.clear()

        val userId = auth.currentUser?.uid ?: return
        
        // Listen to Assets
        listeners.add(db.collection("users").document(userId).collection("assets")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    DigitalAsset(
                        name = doc.getString("name") ?: "",
                        platform = doc.getString("platform") ?: "",
                        category = doc.getString("category") ?: "",
                        actionAfterDeath = doc.getString("actionAfterDeath") ?: "No Action"
                    )
                } ?: emptyList()
                _assets.value = list
            })

        // Listen to Documents
        listeners.add(db.collection("users").document(userId).collection("documents")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Document(
                        title = doc.getString("title") ?: "",
                        category = doc.getString("category") ?: "",
                        fileName = doc.getString("fileName") ?: ""
                    )
                } ?: emptyList()
                _documents.value = list
            })

        // Listen to Executors
        listeners.add(db.collection("users").document(userId).collection("executors")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Executor(
                        name = doc.getString("name") ?: "",
                        relation = doc.getString("relation") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        accessLevel = doc.getString("accessLevel") ?: "Full Access",
                        isPrimary = doc.getBoolean("isPrimary") ?: false
                    )
                } ?: emptyList()
                _executors.value = list
            })
            
        // Listen to Instructions
        listeners.add(db.collection("users").document(userId).collection("instructions")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Instruction(
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        type = doc.getString("type") ?: "General",
                        priority = doc.getString("priority") ?: "Medium"
                    )
                } ?: emptyList()
                _instructions.value = list
            })

        // Listen to Activities
        listeners.add(db.collection("users").document(userId).collection("activities")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    Activity(
                        title = doc.getString("title") ?: "",
                        subtitle = doc.getString("subtitle") ?: "",
                        date = doc.getString("date") ?: "",
                        iconType = doc.getString("iconType") ?: "asset"
                    )
                } ?: emptyList()
                _activities.value = list
            })
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    fun addAsset(name: String, platform: String, category: String, username: String = "", passwordHint: String = "", url: String = "", action: String = "No Action") {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "name" to name,
            "platform" to platform,
            "category" to category,
            "username" to username,
            "passwordHint" to passwordHint,
            "url" to url,
            "actionAfterDeath" to action,
            "timestamp" to Timestamp.now()
        )
        db.collection("users").document(userId).collection("assets").add(data)
        addActivity(name, "Digital asset added", "asset")
    }

    fun addDocument(title: String, category: String, fileName: String, description: String = "", notes: String = "") {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "title" to title,
            "category" to category,
            "fileName" to fileName,
            "description" to description,
            "notes" to notes,
            "timestamp" to Timestamp.now()
        )
        db.collection("users").document(userId).collection("documents").add(data)
        addActivity(title, "Document uploaded", "doc")
    }

    fun addExecutor(name: String, relation: String, email: String, phone: String, accessLevel: String = "Full Access", isPrimary: Boolean = false) {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "name" to name,
            "relation" to relation,
            "email" to email,
            "phone" to phone,
            "accessLevel" to accessLevel,
            "isPrimary" to isPrimary,
            "timestamp" to Timestamp.now()
        )
        db.collection("users").document(userId).collection("executors").add(data)
        addActivity(name, "Executor assigned", "executor")
    }

    fun addInstruction(title: String, content: String, type: String = "General", priority: String = "Medium") {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "title" to title,
            "content" to content,
            "type" to type,
            "priority" to priority,
            "timestamp" to Timestamp.now()
        )
        db.collection("users").document(userId).collection("instructions").add(data)
        addActivity(title, "Instruction written", "instruction")
    }

    private fun addActivity(title: String, subtitle: String, type: String) {
        val userId = auth.currentUser?.uid ?: return
        val dateStr = getCurrentDate()
        val data = hashMapOf(
            "title" to title,
            "subtitle" to "$subtitle · $dateStr",
            "date" to dateStr,
            "iconType" to type,
            "timestamp" to Timestamp.now()
        )
        db.collection("users").document(userId).collection("activities").add(data)
    }

    override fun onCleared() {
        listeners.forEach { it.remove() }
        super.onCleared()
    }
}