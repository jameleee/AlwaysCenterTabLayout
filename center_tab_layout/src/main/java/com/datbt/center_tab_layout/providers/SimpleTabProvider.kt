package com.datbt.center_tab_layout.providers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.TextView

class SimpleTabProvider(context: Context, layoutResId: Int, textViewId: Int) : TabProvider {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val tabViewLayoutId: Int = layoutResId
    private val tabViewTextViewId: Int = textViewId

    override fun createTabView(container: ViewGroup?, position: Int, tabText: List<String>): View? {
        var tabView: View? = null
        var tabTitleView: TextView? = null
        if (tabViewLayoutId != HorizontalScrollView.NO_ID) {
            tabView = inflater.inflate(tabViewLayoutId, container, false)
        }
        if (tabViewTextViewId != HorizontalScrollView.NO_ID && tabView != null) {
            tabTitleView = tabView.findViewById<View>(tabViewTextViewId) as TextView
        }
        if (tabTitleView == null && TextView::class.java.isInstance(tabView)) {
            tabTitleView = tabView as TextView?
        }
        if (tabTitleView != null) {
            tabTitleView.text = tabText[position]
        }
        return tabView
    }

}
