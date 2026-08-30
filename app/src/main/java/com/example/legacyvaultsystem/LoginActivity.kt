package com.example.legacyvaultsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.legacyvaultsystem.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLoginSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email == "admin@legacy.com" && password == "admin123") {
                Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
                finish()
                return@setOnClickListener
            }

                auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val exception = task.exception
                        if (exception is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, "User not found. Please register first.", Toast.LENGTH_LONG).show()
                        } else if (exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Invalid password. Please try again.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Login failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }
}