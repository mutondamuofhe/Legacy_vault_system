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
import com.example.legacyvaultsystem.databinding.DialogAddAssetBinding
import com.example.legacyvaultsystem.databinding.FragmentAssetsBinding
import com.example.legacyvaultsystem.databinding.ItemAssetBinding
import com.google.android.material.tabs.TabLayout

class AssetsFragment : Fragment() {
    private var _binding: FragmentAssetsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultViewModel by activityViewModels()
    private var currentFilter: String = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.assets.observe(viewLifecycleOwner) { assets ->
            updateAssetsList(assets)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilter = tab?.text.toString()
                viewModel.assets.value?.let { updateAssetsList(it) }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnAddAsset.setOnClickListener {
            showAddAssetDialog()
        }
    }

    private fun updateAssetsList(assets: List<DigitalAsset>) {
        val filteredAssets = if (currentFilter == "All") {
            assets
        } else {
            assets.filter { it.category.contains(currentFilter, ignoreCase = true) }
        }

        if (filteredAssets.isNotEmpty()) {
            binding.layoutEmptyState.visibility = View.GONE
            binding.layoutAssetsList.visibility = View.VISIBLE
            binding.layoutAssetsList.removeAllViews()
            filteredAssets.forEach { asset ->
                val itemBinding = ItemAssetBinding.inflate(layoutInflater, binding.layoutAssetsList, false)
                itemBinding.tvAssetName.text = asset.name
                itemBinding.tvAssetPlatform.text = asset.platform
                itemBinding.tvAssetAction.text = asset.actionAfterDeath
                binding.layoutAssetsList.addView(itemBinding.root)
            }
        } else {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.layoutAssetsList.visibility = View.GONE
        }
    }

    private fun showAddAssetDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogAddAssetBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnAddAssetConfirm.setOnClickListener {
            val name = dialogBinding.etAssetName.text.toString()
            val platform = dialogBinding.etPlatform.text.toString()
            val category = dialogBinding.spinnerCategory.selectedItem?.toString() ?: ""
            
            if (name.isNotEmpty()) {
                viewModel.addAsset(name, platform, category)
                dialog.dismiss()
            } else {
                dialogBinding.etAssetName.error = "Name is required"
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}