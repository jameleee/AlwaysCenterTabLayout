package com.example.alwayscentertablayout.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.alwayscentertablayout.R
import com.example.alwayscentertablayout.ui.dashboard.DashboardFragment

class DemoAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    companion object {
        fun tab10(): IntArray = intArrayOf(
            R.string.demo_tab_1,
            R.string.demo_tab_2,
            R.string.demo_tab_3,
            R.string.demo_tab_4,
            R.string.demo_tab_5,
            R.string.demo_tab_6,
            R.string.demo_tab_7,
            R.string.demo_tab_8,
            R.string.demo_tab_9,
            R.string.demo_tab_10
        )
    }

    override fun getItemCount(): Int = tab10().size

    override fun createFragment(position: Int): Fragment = DashboardFragment()
}
