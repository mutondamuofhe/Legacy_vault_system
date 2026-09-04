package com.example.legacyvaultsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.legacyvaultsystem.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegisterSubmit.setOnClickListener {
            val email = binding.etRegEmail.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()
            val fullName = binding.etFullName.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                        // Instead of going to Dashboard, go back to Login
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}