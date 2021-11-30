package com.datbt.center_tab_layout.interfaces

/**
 * Interface definition for a callback to be invoked when a tab is clicked.
 */
interface OnTabClickListener {
    /**
     * Called when a tab is clicked.
     *
     * @param position tab's position
     */
    fun onTabClicked(position: Int)
}
