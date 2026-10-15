package com.example.legacyvaultsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legacyvaultsystem.databinding.FragmentAdminVaultsBinding
import com.example.legacyvaultsystem.databinding.ItemDocumentBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminVaultMonitoringFragment : Fragment() {
    private var _binding: FragmentAdminVaultsBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val documentsList = mutableListOf<Document>()
    private lateinit var adapter: VaultMonitoringAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminVaultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VaultMonitoringAdapter(documentsList)
        binding.rvAdminVaults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminVaults.adapter = adapter

        loadVaultRecords()
    }

    private fun loadVaultRecords() {
        db.collectionGroup("documents").addSnapshotListener { snapshot, _ ->
            documentsList.clear()
            snapshot?.documents?.forEach { doc ->
                val document = Document(
                    title = doc.getString("title") ?: "Vault Document",
                    category = doc.getString("category") ?: "General",
                    fileName = doc.getString("fileName") ?: "file.pdf",
                    description = doc.getString("description") ?: "",
                    notes = doc.getString("notes") ?: ""
                )
                documentsList.add(document)
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class VaultMonitoringAdapter(private val list: List<Document>) :
        RecyclerView.Adapter<VaultMonitoringAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val doc = list[position]
            holder.binding.tvDocTitle.text = doc.title
            holder.binding.tvDocCategory.text = "${doc.category} • ${doc.fileName}"
        }

        override fun getItemCount(): Int = list.size
    }
}