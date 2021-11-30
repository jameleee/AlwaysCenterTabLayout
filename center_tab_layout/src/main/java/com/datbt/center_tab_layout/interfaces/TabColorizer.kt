package com.datbt.center_tab_layout.interfaces

/**
 * Allows complete control over the colors drawn in the tab layout. Set with
 * [.setCustomTabColorizer].
 */
interface TabColorizer {

    /**
     * @return return the color of the indicator used when `position` is selected.
     */
    fun getIndicatorColor(position: Int): Int

    /**
     * @return return the color of the divider drawn to the right of `position`.
     */
    fun getDividerColor(position: Int): Int
}
