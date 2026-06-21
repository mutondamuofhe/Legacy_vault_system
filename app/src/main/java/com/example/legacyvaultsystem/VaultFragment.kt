package com.example.legacyvaultsystem

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.legacyvaultsystem.databinding.DialogUploadDocumentBinding
import com.example.legacyvaultsystem.databinding.FragmentVaultBinding
import com.example.legacyvaultsystem.databinding.ItemDocumentBinding

class VaultFragment : Fragment() {
    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.documents.observe(viewLifecycleOwner) { documents ->
            if (documents.isNotEmpty()) {
                binding.layoutEmptyState.visibility = View.GONE
                binding.btnUploadFirst.visibility = View.GONE
                binding.layoutDocumentsList.visibility = View.VISIBLE
                binding.layoutDocumentsList.removeAllViews()
                documents.forEach { doc ->
                    val itemBinding = ItemDocumentBinding.inflate(layoutInflater, binding.layoutDocumentsList, false)
                    itemBinding.tvDocTitle.text = doc.title
                    itemBinding.tvDocCategory.text = doc.category
                    binding.layoutDocumentsList.addView(itemBinding.root)
                }
            } else {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.btnUploadFirst.visibility = View.VISIBLE
                binding.layoutDocumentsList.visibility = View.GONE
            }
        }

        binding.btnUpload.setOnClickListener {
            showUploadDialog()
        }

        binding.btnUploadFirst.setOnClickListener {
            showUploadDialog()
        }
    }

    private fun showUploadDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogUploadDocumentBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        
        dialogBinding.layoutUpload.setOnClickListener {
            Toast.makeText(context, "Opening file picker...", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnUploadConfirm.setOnClickListener {
            val title = dialogBinding.etDocTitle.text.toString()
            val category = dialogBinding.spinnerDocCategory.selectedItem?.toString() ?: ""
            
            if (title.isNotEmpty()) {
                viewModel.addDocument(title, category, "uploaded_file.pdf")
                dialog.dismiss()
            } else {
                dialogBinding.etDocTitle.error = "Title is required"
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}