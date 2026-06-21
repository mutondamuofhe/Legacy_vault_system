package com.example.legacyvaultsystem

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.legacyvaultsystem.databinding.DialogAddExecutorBinding
import com.example.legacyvaultsystem.databinding.FragmentExecutorsBinding
import com.example.legacyvaultsystem.databinding.ItemExecutorBinding

class ExecutorsFragment : Fragment() {
    private var _binding: FragmentExecutorsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExecutorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.executors.observe(viewLifecycleOwner) { executors ->
            if (executors.isNotEmpty()) {
                binding.layoutEmptyState.visibility = View.GONE
                binding.btnAssignFirst.visibility = View.GONE
                binding.layoutExecutorsList.visibility = View.VISIBLE
                binding.layoutExecutorsList.removeAllViews()
                
                executors.forEach { executor ->
                    val itemBinding = ItemExecutorBinding.inflate(layoutInflater, binding.layoutExecutorsList, false)
                    itemBinding.tvExecutorName.text = executor.name
                    itemBinding.tvRelation.text = executor.relation
                    itemBinding.tvEmail.text = executor.email
                    itemBinding.tvPhone.text = executor.phone
                    itemBinding.tvAccessLevel.text = executor.accessLevel
                    itemBinding.ivPrimaryStar.visibility = if (executor.isPrimary) View.VISIBLE else View.GONE
                    
                    val initials = executor.name.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("")
                    itemBinding.tvInitials.text = initials.uppercase()
                    
                    binding.layoutExecutorsList.addView(itemBinding.root)
                }
            } else {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.btnAssignFirst.visibility = View.VISIBLE
                binding.layoutExecutorsList.visibility = View.GONE
            }
        }

        binding.btnAssign.setOnClickListener { showAddExecutorDialog() }
        binding.btnAssignFirst.setOnClickListener { showAddExecutorDialog() }
    }
    
    private fun showAddExecutorDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogAddExecutorBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnAddExecutorConfirm.setOnClickListener {
            val name = dialogBinding.etExecutorName.text.toString()
            val relation = dialogBinding.etExecutorRelation.text.toString()
            val email = dialogBinding.etExecutorEmail.text.toString()
            val phone = dialogBinding.etExecutorPhone.text.toString()
            val access = dialogBinding.spinnerAccess.selectedItem.toString()
            val isPrimary = dialogBinding.cbPrimary.isChecked

            if (name.isNotEmpty() && relation.isNotEmpty() && email.isNotEmpty()) {
                viewModel.addExecutor(name, relation, email, phone, access, isPrimary)
                dialog.dismiss()
            } else {
                if (name.isEmpty()) dialogBinding.etExecutorName.error = "Name required"
                if (relation.isEmpty()) dialogBinding.etExecutorRelation.error = "Relation required"
                if (email.isEmpty()) dialogBinding.etExecutorEmail.error = "Email required"
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}