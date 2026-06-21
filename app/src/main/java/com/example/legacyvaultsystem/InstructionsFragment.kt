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
import com.example.legacyvaultsystem.databinding.DialogAddInstructionBinding
import com.example.legacyvaultsystem.databinding.FragmentInstructionsBinding
import com.example.legacyvaultsystem.databinding.ItemInstructionBinding

class InstructionsFragment : Fragment() {
    private var _binding: FragmentInstructionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.instructions.observe(viewLifecycleOwner) { instructions ->
            if (instructions.isNotEmpty()) {
                binding.layoutEmptyState.visibility = View.GONE
                binding.btnAddFirst.visibility = View.GONE
                binding.layoutInstructionsList.visibility = View.VISIBLE
                binding.layoutInstructionsList.removeAllViews()
                instructions.forEach { instruction ->
                    val itemBinding = ItemInstructionBinding.inflate(layoutInflater, binding.layoutInstructionsList, false)
                    itemBinding.tvInstructionTitle.text = instruction.title
                    itemBinding.tvInstructionType.text = instruction.type
                    itemBinding.tvInstructionContent.text = instruction.content
                    itemBinding.tvPriority.text = instruction.priority
                    binding.layoutInstructionsList.addView(itemBinding.root)
                }
            } else {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.btnAddFirst.visibility = View.VISIBLE
                binding.layoutInstructionsList.visibility = View.GONE
            }
        }

        binding.btnAddInstruction.setOnClickListener { showAddInstructionDialog() }
        binding.btnAddFirst.setOnClickListener { showAddInstructionDialog() }
    }

    private fun showAddInstructionDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogAddInstructionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnAddInstructionConfirm.setOnClickListener {
            val title = dialogBinding.etInstructionTitle.text.toString()
            val content = dialogBinding.etInstructionContent.text.toString()
            val type = dialogBinding.spinnerInstructionCategory.selectedItem.toString()
            val priority = dialogBinding.spinnerPriority.selectedItem.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                viewModel.addInstruction(title, content, type, priority)
                dialog.dismiss()
            } else {
                if (title.isEmpty()) dialogBinding.etInstructionTitle.error = "Title required"
                if (content.isEmpty()) dialogBinding.etInstructionContent.error = "Directive required"
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}