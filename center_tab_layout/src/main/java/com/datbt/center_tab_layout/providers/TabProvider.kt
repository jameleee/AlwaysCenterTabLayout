package com.datbt.center_tab_layout.providers

import android.view.View
import android.view.ViewGroup

/**
 * Create the custom tabs in the tab layout. Set with
 * [.setCustomTabView]
 */
interface TabProvider {

    /**
     * @return Return the View of `position` for the Tabs
     */
    fun createTabView(container: ViewGroup?, position: Int, tabText: List<String>): View?
}