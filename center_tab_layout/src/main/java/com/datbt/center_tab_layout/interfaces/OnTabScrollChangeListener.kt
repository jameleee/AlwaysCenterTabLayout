package com.datbt.center_tab_layout.interfaces

/**
 * Interface definition for a callback to be invoked when the scroll position of a view changes.
 */
interface OnTabScrollChangeListener {
    /**
     * Called when the scroll position of a view changes.
     *
     * @param scrollX Current horizontal scroll origin.
     * @param oldScrollX Previous horizontal scroll origin.
     */
    fun onScrollChanged(scrollX: Int, oldScrollX: Int)
}
