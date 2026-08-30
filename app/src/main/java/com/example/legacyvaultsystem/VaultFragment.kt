package com.example.legacyvaultsystem

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.legacyvaultsystem.databinding.DialogUploadDocumentBinding
import com.example.legacyvaultsystem.databinding.FragmentVaultBinding
import com.example.legacyvaultsystem.databinding.ItemDocumentBinding

class VaultFragment : Fragment() {
    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultViewModel by activityViewModels()
    private var selectedFileName: String? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileName = context?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                } ?: "file_uploaded"
                selectedFileName = fileName
                Toast.makeText(context, "Selected: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            filePickerLauncher.launch(intent)
        }

        dialogBinding.btnUploadConfirm.setOnClickListener {
            val title = dialogBinding.etDocTitle.text.toString()
            val category = dialogBinding.spinnerDocCategory.selectedItem?.toString() ?: ""
            
            if (title.isNotEmpty()) {
                viewModel.addDocument(title, category, selectedFileName ?: "unknown_file")
                dialog.dismiss()
                selectedFileName = null
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