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

    fun populateSampleDatabase(onComplete: ((Boolean) -> Unit)? = null) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete?.invoke(false)
            return
        }

        val sampleAssets = listOf(
            DigitalAsset("Online Banking", "First National Bank", "Banking & Finance", "user_fnb", "Mother maiden name", "https://fnb.com", "Transfer to Executor"),
            DigitalAsset("Investment Portfolio", "EasyEquities", "Banking & Finance", "investor_99", "First car model", "https://easyequities.co.za", "Transfer to Executor"),
            DigitalAsset("Crypto Wallet", "Binance", "Banking & Finance", "crypto_holder", "Favorite teacher", "https://binance.com", "Transfer to Executor"),
            DigitalAsset("Facebook Account", "Meta", "Social Media", "fb_user", "High school name", "https://facebook.com", "Memorialize"),
            DigitalAsset("Instagram Account", "Meta", "Social Media", "@insta_life", "Favorite movie", "https://instagram.com", "Memorialize"),
            DigitalAsset("LinkedIn Profile", "LinkedIn", "Social Media", "pro_link", "First pet name", "https://linkedin.com", "Delete Account"),
            DigitalAsset("Primary Gmail", "Google", "Email", "myemail@gmail.com", "Childhood address", "https://gmail.com", "Transfer to Executor"),
            DigitalAsset("Work Email", "Microsoft Outlook", "Email", "work@company.com", "Office room number", "https://outlook.office.com", "Delete Account"),
            DigitalAsset("Netflix Subscription", "Netflix", "Entertainment", "streaming_fan", "Favorite band", "https://netflix.com", "Delete Account"),
            DigitalAsset("Spotify Family Plan", "Spotify", "Entertainment", "music_lover", "Favorite song", "https://spotify.com", "Transfer to Executor")
        )

        val sampleDocs = listOf(
            Document("National ID Document", "Identity", "national_id.pdf", "Official government issued ID card", "Vault Box #1"),
            Document("Passport Copy", "Identity", "passport_2025.pdf", "International travel passport", "Drawer 2"),
            Document("Last Will and Testament", "Legal", "final_will.pdf", "Signed and witnessed legal will", "Attorneys Safe"),
            Document("Living Trust Agreement", "Legal", "trust_agreement.pdf", "Family trust registration", "Safe #102"),
            Document("Power of Attorney", "Legal", "power_of_attorney.pdf", "General power of attorney document", "Safe #102"),
            Document("Title Deed Residence", "Financial", "property_deed.pdf", "Primary property title deed", "Bank Vault"),
            Document("Vehicle Logbook", "Financial", "car_logbook.pdf", "Car registration and ownership papers", "Glove box copy"),
            Document("Life Insurance Policy", "Financial", "life_policy_2025.pdf", "Comprehensive life insurance plan", "Agent file"),
            Document("Medical Aid Card", "Medical", "medical_aid.pdf", "Health insurance details and policy number", "Wallet copy"),
            Document("Living Will Directive", "Medical", "living_will_medical.pdf", "Medical care directives and instructions", "Doctor file")
        )

        val sampleExecutors = listOf(
            Executor("Sarah Johnson", "Spouse", "sarah.j@example.com", "+15550101", "Full Access", true),
            Executor("Michael Johnson", "Brother", "michael.j@example.com", "+15550102", "Documents Only", false),
            Executor("David Smith", "Attorney", "dsmith@lawfirm.com", "+15550103", "Legal & Financial", false),
            Executor("Emma Williams", "Sister", "emma.w@example.com", "+15550104", "Full Access", false),
            Executor("Robert Taylor", "Financial Advisor", "rtaylor@finance.com", "+15550105", "Assets Only", false),
            Executor("Jennifer Davis", "Family Friend", "jenny.d@example.com", "+15550106", "Limited", false),
            Executor("James Wilson", "Estate Administrator", "jwilson@estates.com", "+15550107", "Full Access", false),
            Executor("Amanda Martinez", "Accountant", "amanda@taxservices.com", "+15550108", "Assets Only", false),
            Executor("Christopher Brown", "Uncle", "cbrown@example.com", "+15550109", "Documents Only", false),
            Executor("Jessica Miller", "Cousin", "jessica.m@example.com", "+15550110", "Limited", false)
        )

        val sampleInstructions = listOf(
            Instruction("Social Media Memorialization", "Contact Facebook & Instagram to convert accounts to memorial status.", "Social Media", "High"),
            Instruction("Banking Funds Transfer", "Transfer remaining savings account balance to the designated family trust.", "Financial", "High"),
            Instruction("Crypto Asset Recovery", "Access hardware wallet seed phrase in physical safe and transfer to primary executor.", "Financial", "High"),
            Instruction("Subscription Cancellations", "Cancel Netflix, Spotify, and Adobe Cloud subscriptions.", "General", "Medium"),
            Instruction("Family Photo Archival", "Download Google Photos archive and distribute USB drives to family members.", "Personal Messages", "Medium"),
            Instruction("Personal Letters", "Distribute sealed personal letters stored in the document vault.", "Personal Messages", "High"),
            Instruction("Email Forwarding", "Set up auto-responder on main Gmail account for estate notices.", "General", "Medium"),
            Instruction("Domain Name Management", "Renew web domain names for 1 year before transferring to estate.", "Financial", "Low"),
            Instruction("Physical Safe Key Location", "Physical safe key is located in the master bedroom desk drawer.", "General", "High"),
            Instruction("Medical Directive", "Refer to medical directives in the document vault for preferences.", "General", "High")
        )

        val batch = db.batch()

        sampleAssets.forEach { asset ->
            val ref = db.collection("users").document(userId).collection("assets").document()
            val data = hashMapOf(
                "name" to asset.name,
                "platform" to asset.platform,
                "category" to asset.category,
                "username" to asset.username,
                "passwordHint" to asset.passwordHint,
                "url" to asset.url,
                "actionAfterDeath" to asset.actionAfterDeath,
                "timestamp" to Timestamp.now()
            )
            batch.set(ref, data)
        }

        sampleDocs.forEach { doc ->
            val ref = db.collection("users").document(userId).collection("documents").document()
            val data = hashMapOf(
                "title" to doc.title,
                "category" to doc.category,
                "fileName" to doc.fileName,
                "description" to doc.description,
                "notes" to doc.notes,
                "timestamp" to Timestamp.now()
            )
            batch.set(ref, data)
        }

        sampleExecutors.forEach { executor ->
            val ref = db.collection("users").document(userId).collection("executors").document()
            val data = hashMapOf(
                "name" to executor.name,
                "relation" to executor.relation,
                "email" to executor.email,
                "phone" to executor.phone,
                "accessLevel" to executor.accessLevel,
                "isPrimary" to executor.isPrimary,
                "timestamp" to Timestamp.now()
            )
            batch.set(ref, data)
        }

        sampleInstructions.forEach { ins ->
            val ref = db.collection("users").document(userId).collection("instructions").document()
            val data = hashMapOf(
                "title" to ins.title,
                "content" to ins.content,
                "type" to ins.type,
                "priority" to ins.priority,
                "timestamp" to Timestamp.now()
            )
            batch.set(ref, data)
        }

        batch.commit().addOnCompleteListener { task ->
            onComplete?.invoke(task.isSuccessful)
        }
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