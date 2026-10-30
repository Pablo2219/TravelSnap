package com.industriaop.travelsnap.ui.common

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.industriaop.travelsnap.MainActivity
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.ui.map.MapActivity
import com.industriaop.travelsnap.ui.profile.ProfileActivity

/** Wires the bottom navigation bar shared by the places, map and profile screens. */
object BottomNav {

    fun setup(activity: Activity, nav: BottomNavigationView, selectedItemId: Int) {
        nav.selectedItemId = selectedItemId
        nav.setOnItemSelectedListener { item ->
            if (item.itemId == selectedItemId) return@setOnItemSelectedListener true

            val target: Class<out Activity> = when (item.itemId) {
                R.id.nav_places -> MainActivity::class.java
                R.id.nav_map -> MapActivity::class.java
                R.id.nav_profile -> ProfileActivity::class.java
                else -> return@setOnItemSelectedListener false
            }
            val intent = Intent(activity, target)
            if (target == MainActivity::class.java) {
                // Go back to the existing places screen instead of opening a new one.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            activity.startActivity(intent)
            if (activity !is MainActivity) activity.finish()
            true
        }
    }
}
