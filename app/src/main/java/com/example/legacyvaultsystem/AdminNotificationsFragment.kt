package com.example.legacyvaultsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legacyvaultsystem.databinding.FragmentAdminNotificationsBinding
import com.example.legacyvaultsystem.databinding.ItemAdminNotificationBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotificationModel(
    val title: String = "",
    val message: String = "",
    val date: String = ""
)

class AdminNotificationsFragment : Fragment() {
    private var _binding: FragmentAdminNotificationsBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val notificationsList = mutableListOf<NotificationModel>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter(notificationsList)
        binding.rvNotificationHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotificationHistory.adapter = adapter

        loadNotificationHistory()

        binding.btnSendNotif.setOnClickListener {
            val title = binding.etNotifTitle.text.toString().trim()
            val message = binding.etNotifMessage.text.toString().trim()

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both title and message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date())

            val notifMap = hashMapOf(
                "title" to title,
                "message" to message,
                "date" to dateStr,
                "timestamp" to Timestamp.now()
            )

            db.collection("broadcast_notifications").add(notifMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Notification sent to all users!", Toast.LENGTH_SHORT).show()
                    binding.etNotifTitle.text?.clear()
                    binding.etNotifMessage.text?.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadNotificationHistory() {
        db.collection("broadcast_notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                notificationsList.clear()
                snapshot?.documents?.forEach { doc ->
                    val notif = NotificationModel(
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        date = doc.getString("date") ?: ""
                    )
                    notificationsList.add(notif)
                }
                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class NotificationAdapter(private val list: List<NotificationModel>) :
        RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

        class NotifViewHolder(val binding: ItemAdminNotificationBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
            val binding = ItemAdminNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return NotifViewHolder(binding)
        }

        override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvNotifTitle.text = item.title
            holder.binding.tvNotifMessage.text = item.message
            holder.binding.tvNotifDate.text = item.date
        }

        override fun getItemCount(): Int = list.size
    }
}