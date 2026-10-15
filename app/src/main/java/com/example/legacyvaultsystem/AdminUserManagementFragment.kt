package com.example.legacyvaultsystem

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legacyvaultsystem.databinding.FragmentAdminUsersBinding
import com.example.legacyvaultsystem.databinding.ItemAdminUserBinding
import com.google.firebase.firestore.FirebaseFirestore

data class AdminUserModel(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val status: String = "Active",
    val role: String = "user"
)

class AdminUserManagementFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val allUsers = mutableListOf<AdminUserModel>()
    private val filteredUsers = mutableListOf<AdminUserModel>()
    private lateinit var adapter: AdminUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminUserAdapter(filteredUsers,
            onToggleStatus = { user -> toggleUserStatus(user) },
            onEditUser = { user -> showEditUserDialog(user) }
        )

        binding.rvAdminUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminUsers.adapter = adapter

        loadUsersFromFirestore()

        // Real-time search filter
        val searchEditText = binding.tilSearchUsers.editText
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadUsersFromFirestore() {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            allUsers.clear()
            snapshot?.documents?.forEach { doc ->
                val user = AdminUserModel(
                    uid = doc.id,
                    fullName = doc.getString("fullName") ?: "Unnamed User",
                    email = doc.getString("email") ?: "",
                    status = doc.getString("status") ?: "Active",
                    role = doc.getString("role") ?: "user"
                )
                allUsers.add(user)
            }
            val currentQuery = binding.tilSearchUsers.editText?.text.toString()
            filterUsers(currentQuery)
        }
    }

    private fun filterUsers(query: String) {
        filteredUsers.clear()
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers)
        } else {
            val lower = query.lowercase()
            allUsers.forEach { user ->
                if (user.fullName.lowercase().contains(lower) || user.email.lowercase().contains(lower)) {
                    filteredUsers.add(user)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun toggleUserStatus(user: AdminUserModel) {
        val newStatus = if (user.status == "Active") "Suspended" else "Active"
        db.collection("users").document(user.uid)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Updated ${user.fullName} to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditUserDialog(user: AdminUserModel) {
        val input = EditText(requireContext())
        input.setText(user.fullName)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit User Full Name")
            .setMessage("User Email: ${user.email}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    db.collection("users").document(user.uid)
                        .update("fullName", newName)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "User updated!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AdminUserAdapter(
        private val users: List<AdminUserModel>,
        private val onToggleStatus: (AdminUserModel) -> Unit,
        private val onEditUser: (AdminUserModel) -> Unit
    ) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

        class UserViewHolder(val binding: ItemAdminUserBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val binding = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return UserViewHolder(binding)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = users[position]
            holder.binding.tvUserName.text = user.fullName
            holder.binding.tvUserEmail.text = user.email
            holder.binding.tvUserStatus.text = user.status

            if (user.status == "Active") {
                holder.binding.tvUserStatus.setTextColor(0xFF2ECC71.toInt())
                holder.binding.btnToggleStatus.text = "Suspend User"
            } else {
                holder.binding.tvUserStatus.setTextColor(0xFFE74C3C.toInt())
                holder.binding.btnToggleStatus.text = "Activate User"
            }

            holder.binding.btnToggleStatus.setOnClickListener { onToggleStatus(user) }
            holder.binding.btnEditUser.setOnClickListener { onEditUser(user) }
        }

        override fun getItemCount(): Int = users.size
    }
}