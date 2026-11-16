package com.example.legacyvaultsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.legacyvaultsystem.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
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
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val userMap = hashMapOf(
                                    "uid" to userId,
                                    "fullName" to (account.displayName ?: account.email?.substringBefore("@") ?: "Google User"),
                                    "email" to (account.email ?: ""),
                                    "status" to "Active",
                                    "role" to "user",
                                    "createdAt" to Timestamp.now()
                                )
                                db.collection("users").document(userId).set(userMap, SetOptions.merge())
                            }

                            Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
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
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnRegisterSubmit.setOnClickListener {
            val email = binding.etRegEmail.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()
            val fullName = binding.etFullName.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidStrictEmail(email)) {
                binding.tilRegEmail.error = "Please enter a valid email address (e.g. name@example.com)"
                Toast.makeText(this, "Invalid email format. Please enter a valid email.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                binding.tilRegEmail.error = null
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val userMap = hashMapOf(
                                "uid" to userId,
                                "fullName" to fullName,
                                "email" to email,
                                "phone" to binding.etPhone.text.toString().trim(),
                                "idNumber" to binding.etIdNumber.text.toString().trim(),
                                "status" to "Active",
                                "role" to "user",
                                "createdAt" to Timestamp.now()
                            )
                            db.collection("users").document(userId).set(userMap, SetOptions.merge())
                        }

                        auth.currentUser?.sendEmailVerification()
                        Toast.makeText(this, "Registration successful! Verification link sent to $email", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.btnRegGoogleSignIn.setOnClickListener {
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
}