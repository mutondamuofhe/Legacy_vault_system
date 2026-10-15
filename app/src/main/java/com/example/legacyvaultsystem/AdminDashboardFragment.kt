package com.example.legacyvaultsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.legacyvaultsystem.databinding.FragmentAdminDashboardBinding
import com.example.legacyvaultsystem.databinding.ItemRecentActivityBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadRealtimeMetrics()
    }

    private fun loadRealtimeMetrics() {
        // Listen to Users collection
        db.collection("users").addSnapshotListener { snapshot, _ ->
            val userCount = snapshot?.size() ?: 0
            _binding?.tvTotalUsers?.text = userCount.toString()
            _binding?.tvTotalVaults?.text = userCount.toString()
        }

        // Listen to Access Requests
        db.collection("access_requests").addSnapshotListener { snapshot, _ ->
            val pendingCount = snapshot?.documents?.count { it.getString("status") == "Pending" } ?: 0
            _binding?.tvPendingRequests?.text = pendingCount.toString()
        }

        // Listen to Broadcasts & Recent System Activity
        db.collection("broadcast_notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                _binding?.let { b ->
                    b.adminRecentActivityList.removeAllViews()
                    snapshot?.documents?.take(5)?.forEach { doc ->
                        val item = ItemRecentActivityBinding.inflate(layoutInflater, b.adminRecentActivityList, false)
                        item.tvActivityTitle.text = doc.getString("title") ?: "System Announcement"
                        item.tvActivitySubtitle.text = doc.getString("message") ?: ""
                        item.ivActivityIcon.setImageResource(R.drawable.ic_shield_outline)
                        b.adminRecentActivityList.addView(item.root)
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}