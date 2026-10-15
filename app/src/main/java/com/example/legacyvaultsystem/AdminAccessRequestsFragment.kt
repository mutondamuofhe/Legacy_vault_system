package com.example.legacyvaultsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legacyvaultsystem.databinding.FragmentAdminRequestsBinding
import com.example.legacyvaultsystem.databinding.ItemAdminRequestBinding
import com.google.firebase.firestore.FirebaseFirestore

data class AccessRequestModel(
    val id: String = "",
    val title: String = "",
    val details: String = "",
    val status: String = "Pending"
)

class AdminAccessRequestsFragment : Fragment() {
    private var _binding: FragmentAdminRequestsBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val requestsList = mutableListOf<AccessRequestModel>()
    private lateinit var adapter: AccessRequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AccessRequestsAdapter(requestsList,
            onApprove = { req -> updateRequestStatus(req.id, "Approved") },
            onReject = { req -> updateRequestStatus(req.id, "Rejected") }
        )

        binding.rvAdminRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminRequests.adapter = adapter

        loadRequestsFromFirestore()
    }

    private fun loadRequestsFromFirestore() {
        db.collection("access_requests").addSnapshotListener { snapshot, _ ->
            requestsList.clear()
            if (snapshot != null && !snapshot.isEmpty) {
                snapshot.documents.forEach { doc ->
                    val req = AccessRequestModel(
                        id = doc.id,
                        title = doc.getString("title") ?: "Estate Access Request",
                        details = doc.getString("details") ?: "Requested by Executor",
                        status = doc.getString("status") ?: "Pending"
                    )
                    requestsList.add(req)
                }
            } else {
                // Default sample request for initial demonstration if none exists yet
                requestsList.add(
                    AccessRequestModel("sample_1", "Estate Access: Johnson Estate", "Requested by Sarah Johnson (Primary Executor)", "Pending")
                )
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateRequestStatus(reqId: String, status: String) {
        if (reqId == "sample_1") {
            val data = hashMapOf(
                "title" to "Estate Access: Johnson Estate",
                "details" to "Requested by Sarah Johnson (Primary Executor)",
                "status" to status,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            db.collection("access_requests").document().set(data)
        } else {
            db.collection("access_requests").document(reqId).update("status", status)
        }
        Toast.makeText(requireContext(), "Request set to $status", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AccessRequestsAdapter(
        private val requests: List<AccessRequestModel>,
        private val onApprove: (AccessRequestModel) -> Unit,
        private val onReject: (AccessRequestModel) -> Unit
    ) : RecyclerView.Adapter<AccessRequestsAdapter.RequestViewHolder>() {

        class RequestViewHolder(val binding: ItemAdminRequestBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
            val binding = ItemAdminRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return RequestViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
            val req = requests[position]
            holder.binding.tvRequestTitle.text = req.title
            holder.binding.tvRequestDetails.text = req.details
            holder.binding.tvRequestStatus.text = req.status

            when (req.status) {
                "Approved" -> holder.binding.tvRequestStatus.setTextColor(0xFF2ECC71.toInt())
                "Rejected" -> holder.binding.tvRequestStatus.setTextColor(0xFFE74C3C.toInt())
                else -> holder.binding.tvRequestStatus.setTextColor(0xFFFF9800.toInt())
            }

            holder.binding.btnApproveRequest.setOnClickListener { onApprove(req) }
            holder.binding.btnRejectRequest.setOnClickListener { onReject(req) }
        }

        override fun getItemCount(): Int = requests.size
    }
}