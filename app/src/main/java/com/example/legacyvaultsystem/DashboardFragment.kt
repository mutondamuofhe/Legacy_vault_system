package com.example.legacyvaultsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.legacyvaultsystem.databinding.FragmentDashboardBinding
import com.example.legacyvaultsystem.databinding.ItemRecentActivityBinding

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.assets.observe(viewLifecycleOwner) { assets ->
            binding.tvAssetCount.text = assets.size.toString()
            updateVaultHealth()
        }

        viewModel.documents.observe(viewLifecycleOwner) { docs ->
            binding.tvDocumentCount.text = docs.size.toString()
            updateVaultHealth()
        }

        viewModel.executors.observe(viewLifecycleOwner) { executors ->
            binding.tvExecutorCount.text = executors.size.toString()
            updateVaultHealth()
        }

        viewModel.instructions.observe(viewLifecycleOwner) { instructions ->
            binding.tvInstructionCount.text = instructions.size.toString()
            updateVaultHealth()
        }

        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            if (activities.isNotEmpty()) {
                binding.tvRecentActivityEmpty.visibility = View.GONE
                binding.layoutRecentActivity.visibility = View.VISIBLE
                binding.layoutRecentActivity.removeAllViews()
                
                activities.take(3).forEach { activity ->
                    val itemBinding = ItemRecentActivityBinding.inflate(layoutInflater, binding.layoutRecentActivity, false)
                    itemBinding.tvActivityTitle.text = activity.title
                    itemBinding.tvActivitySubtitle.text = activity.subtitle
                    
                    val iconRes = when(activity.iconType) {
                        "asset" -> R.drawable.ic_shield_outline
                        "doc" -> R.drawable.ic_document_outline
                        "executor" -> R.drawable.ic_people_outline
                        else -> R.drawable.ic_list_outline
                    }
                    itemBinding.ivActivityIcon.setImageResource(iconRes)
                    
                    binding.layoutRecentActivity.addView(itemBinding.root)
                }
            } else {
                binding.tvRecentActivityEmpty.visibility = View.VISIBLE
                binding.layoutRecentActivity.visibility = View.GONE
            }
        }
    }

    private fun updateVaultHealth() {
        val hasAssets = if ((viewModel.assets.value?.size ?: 0) > 0) 1 else 0
        val hasDocs = if ((viewModel.documents.value?.size ?: 0) > 0) 1 else 0
        val hasExecutors = if ((viewModel.executors.value?.size ?: 0) > 0) 1 else 0
        val hasInstructions = if ((viewModel.instructions.value?.size ?: 0) > 0) 1 else 0

        val total = hasAssets + hasDocs + hasExecutors + hasInstructions
        val percent = (total * 25)

        binding.tvHealthPercent.text = "$percent% Complete"
        binding.tvHealthRatio.text = "$total/4"
        binding.progressHealth.progress = percent

        // Update checklist icons
        updateChecklistIcon(binding.tvHealthAssets, hasAssets > 0)
        updateChecklistIcon(binding.tvHealthDocs, hasDocs > 0)
        updateChecklistIcon(binding.tvHealthExecutors, hasExecutors > 0)
        updateChecklistIcon(binding.tvHealthInstructions, hasInstructions > 0)
    }

    private fun updateChecklistIcon(textView: android.widget.TextView, isComplete: Boolean) {
        val iconRes = if (isComplete) android.R.drawable.presence_online else android.R.drawable.presence_busy
        val tintColor = if (isComplete) 0xFF4CAF50.toInt() else 0xFFFFC107.toInt() // Green vs Amber
        
        val drawable = androidx.core.content.ContextCompat.getDrawable(requireContext(), iconRes)
        drawable?.let {
            androidx.core.graphics.drawable.DrawableCompat.setTint(it, tintColor)
            textView.setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}