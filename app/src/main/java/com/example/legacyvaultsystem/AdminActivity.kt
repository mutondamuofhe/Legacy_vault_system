package com.example.legacyvaultsystem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.legacyvaultsystem.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(AdminDashboardFragment())
            binding.adminNavView.setCheckedItem(R.id.admin_nav_dashboard)
        }

        binding.ivAdminMenu.setOnClickListener {
            binding.adminDrawerLayout.openDrawer(GravityCompat.START)
        }

        binding.adminNavView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.admin_nav_dashboard -> replaceFragment(AdminDashboardFragment())
                R.id.admin_nav_users -> replaceFragment(AdminUserManagementFragment())
                R.id.admin_nav_vaults -> replaceFragment(AdminVaultMonitoringFragment())
                R.id.admin_nav_requests -> replaceFragment(AdminAccessRequestsFragment())
                R.id.admin_nav_notifications -> replaceFragment(AdminNotificationsFragment())
                R.id.admin_nav_reports -> replaceFragment(AdminReportsFragment())
                R.id.admin_nav_exit -> finish()
            }
            binding.adminDrawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }
}