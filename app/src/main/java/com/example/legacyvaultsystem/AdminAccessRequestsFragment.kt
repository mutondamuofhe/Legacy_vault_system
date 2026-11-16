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
            onApprove = { req -> updateRequestStatus(req.id, "Accepted") },
            onReject = { req -> updateRequestStatus(req.id, "Declined") }
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
            }

            // Ensure 3 requests exist
            val defaultRequests = listOf(
                AccessRequestModel("req_1", "Estate Access: Johnson Estate", "Requested by Sarah Johnson (Primary Executor)", "Pending"),
                AccessRequestModel("req_2", "Document Access: Nkanyane Trust", "Requested by Rifa Nkanyane (Legal Executor)", "Pending"),
                AccessRequestModel("req_3", "Asset Transfer: Moganedi Vault", "Requested by Bridget Moganedi (Secondary Executor)", "Pending")
            )

            defaultRequests.forEach { defReq ->
                if (requestsList.none { it.id == defReq.id }) {
                    requestsList.add(defReq)
                }
            }

            adapter.notifyDataSetChanged()
        }
    }

    private fun updateRequestStatus(reqId: String, newStatus: String) {
        val idx = requestsList.indexOfFirst { it.id == reqId }
        if (idx != -1) {
            requestsList[idx] = requestsList[idx].copy(status = newStatus)
            adapter.notifyDataSetChanged()
        }

        if (!reqId.startsWith("req_")) {
            db.collection("access_requests").document(reqId).update("status", newStatus)
        } else {
            val data = hashMapOf(
                "title" to (requestsList.getOrNull(idx)?.title ?: "Estate Request"),
                "details" to (requestsList.getOrNull(idx)?.details ?: "Requested by Executor"),
                "status" to newStatus,
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            db.collection("access_requests").document(reqId).set(data)
        }

        Toast.makeText(requireContext(), "Request $newStatus", Toast.LENGTH_SHORT).show()
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
                "Accepted", "Approved" -> {
                    holder.binding.tvRequestStatus.text = "Accepted"
                    holder.binding.tvRequestStatus.setTextColor(0xFF2ECC71.toInt())
                }
                "Declined", "Rejected" -> {
                    holder.binding.tvRequestStatus.text = "Declined"
                    holder.binding.tvRequestStatus.setTextColor(0xFFE74C3C.toInt())
                }
                else -> {
                    holder.binding.tvRequestStatus.text = "Pending"
                    holder.binding.tvRequestStatus.setTextColor(0xFFFF9800.toInt())
                }
            }

            holder.binding.btnApproveRequest.setOnClickListener { onApprove(req) }
            holder.binding.btnRejectRequest.setOnClickListener { onReject(req) }
        }

        override fun getItemCount(): Int = requests.size
    }
}