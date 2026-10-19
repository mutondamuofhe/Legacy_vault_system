package com.example.legacyvaultsystem

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.legacyvaultsystem.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.etSettingsEmail.setText(currentUser.email ?: "")
            binding.etSettingsName.setText(currentUser.displayName ?: "")
        }

        // Load SharedPreferences for switches
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)
        binding.switchBiometrics.isChecked = prefs.getBoolean("biometrics_enabled", false)

        binding.btnSaveProfile.setOnClickListener {
            val newName = binding.etSettingsName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a valid display name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()

                user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Also update in Firestore user record
                        db.collection("users").document(user.uid)
                            .set(mapOf("displayName" to newName, "email" to user.email), com.google.firebase.firestore.SetOptions.merge())

                        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.btnResetPassword.setOnClickListener {
            val email = currentUser?.email
            if (!email.isNullOrEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "No account email found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(requireContext(), "Notifications $status", Toast.LENGTH_SHORT).show()
        }

        binding.switchBiometrics.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("biometrics_enabled", isChecked).apply()
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(requireContext(), "App lock $status", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}