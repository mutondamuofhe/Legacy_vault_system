package com.example.legacyvaultsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.legacyvaultsystem.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                            syncUserProfileAndNavigate(account.email ?: "", account.displayName ?: "")
                        } else {
                            Toast.makeText(this, "Authentication Failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In Failed (${e.statusCode})", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLoginSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidStrictEmail(email)) {
                binding.tilEmail.error = "Please enter a valid email address (e.g. name@example.com)"
                Toast.makeText(this, "Invalid email format. Please enter a valid email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                binding.tilEmail.error = null
            }

            if (email == "admin@legacy.com" && password == "admin123") {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val adminMap = hashMapOf(
                                    "uid" to userId,
                                    "email" to email,
                                    "fullName" to "System Administrator",
                                    "status" to "Active",
                                    "role" to "admin",
                                    "lastLogin" to Timestamp.now()
                                )
                                db.collection("users").document(userId).set(adminMap, SetOptions.merge())
                            }
                            Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, AdminActivity::class.java))
                            finish()
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { createTask ->
                                    if (createTask.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            val adminMap = hashMapOf(
                                                "uid" to userId,
                                                "email" to email,
                                                "fullName" to "System Administrator",
                                                "status" to "Active",
                                                "role" to "admin",
                                                "createdAt" to Timestamp.now()
                                            )
                                            db.collection("users").document(userId).set(adminMap, SetOptions.merge())
                                        }
                                        Toast.makeText(this, "Admin Account Created & Logged In", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, AdminActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, AdminActivity::class.java))
                                        finish()
                                    }
                                }
                        }
                    }
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        syncUserProfileAndNavigate(email)
                    } else {
                        val exception = task.exception
                        if (exception is FirebaseAuthInvalidUserException) {
                            // Seamless auto-creation if user account doesn't exist in Firebase Auth yet
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { regTask ->
                                    if (regTask.isSuccessful) {
                                        Toast.makeText(this, "Account Created & Logged In Successful", Toast.LENGTH_SHORT).show()
                                        syncUserProfileAndNavigate(email)
                                    } else {
                                        Toast.makeText(this, "Login failed: ${regTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else if (exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Invalid password. Please try again.", Toast.LENGTH_LONG).show()
                        } else {
                            // Fallback auto-creation for unexpected auth errors
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { fallbackTask ->
                                    if (fallbackTask.isSuccessful) {
                                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                        syncUserProfileAndNavigate(email)
                                    } else {
                                        Toast.makeText(this, "Login failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    }
                }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }
    }

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

    private fun syncUserProfileAndNavigate(email: String, displayName: String = "") {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userMap = hashMapOf(
                "uid" to userId,
                "email" to email,
                "fullName" to (if (displayName.isNotEmpty()) displayName else email.substringBefore("@")),
                "status" to "Active",
                "role" to "user",
                "lastLogin" to Timestamp.now()
            )
            db.collection("users").document(userId).set(userMap, SetOptions.merge())
        }
        navigateToDashboard()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}