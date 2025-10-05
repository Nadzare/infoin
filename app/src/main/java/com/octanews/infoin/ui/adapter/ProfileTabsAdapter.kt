package com.octanews.infoin.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.octanews.infoin.ui.main.ProfileNewsFragment
import com.octanews.infoin.ui.main.ProfileRecentFragment

class ProfileTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2 // Kita punya 2 tab

    override fun createFragment(position: Int): Fragment {
        // Tentukan fragment mana yang akan ditampilkan berdasarkan posisi tab
        return when (position) {
            0 -> ProfileNewsFragment() // Posisi 0 untuk tab "News"
            1 -> ProfileRecentFragment() // Posisi 1 untuk tab "Recent"
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}