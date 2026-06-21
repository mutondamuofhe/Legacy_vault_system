package com.example.legacyvaultsystem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.legacyvaultsystem.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set initial fragment
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
            binding.navView.setCheckedItem(R.id.nav_dashboard)
        }

        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    replaceFragment(DashboardFragment())
                }
                R.id.nav_assets -> {
                    replaceFragment(AssetsFragment())
                }
                R.id.nav_vault -> {
                    replaceFragment(VaultFragment())
                }
                R.id.nav_executors -> {
                    replaceFragment(ExecutorsFragment())
                }
                R.id.nav_instructions -> {
                    replaceFragment(InstructionsFragment())
                }
                R.id.nav_sign_out -> {
                    finish()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}