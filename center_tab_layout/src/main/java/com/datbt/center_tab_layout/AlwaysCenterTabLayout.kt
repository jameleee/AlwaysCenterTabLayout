package com.datbt.center_tab_layout

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.datbt.center_tab_layout.interfaces.OnTabClickListener
import com.datbt.center_tab_layout.interfaces.OnTabScrollChangeListener
import com.datbt.center_tab_layout.interfaces.TabColorizer
import com.datbt.center_tab_layout.providers.TabProvider

class AlwaysCenterTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    FrameLayout(context, attrs) {

    private var tabLayout: CustomTabLayout = CustomTabLayout(context, attrs, defStyle)
    private var isDisable = false

    init {
        addView(tabLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isDisable
    }

    fun setIndicationInterpolator(interpolator: TabIndicationInterpolator?) {
        tabLayout.setIndicationInterpolator(interpolator)
    }

    fun setCustomTabColorizer(tabColorizer: TabColorizer?) {
        tabLayout.setCustomTabColorizer(tabColorizer)
    }

    fun setDefaultTabTextColor(color: Int) {
        tabLayout.setDefaultTabTextColor(color)
    }

    fun setDefaultTabTextColor(colors: ColorStateList) {
        tabLayout.setDefaultTabTextColor(colors)
    }

    fun setDistributeEvenly(distributeEvenly: Boolean) {
        tabLayout.setDistributeEvenly(distributeEvenly)
    }

    fun setSelectedIndicatorColors(vararg colors: Int) {
        tabLayout.setSelectedIndicatorColors(*colors)
    }

    fun setDividerColors(vararg colors: Int) {
        tabLayout.setDividerColors(*colors)
    }

    fun setOnPageChangeListener(listener: ViewPager2.OnPageChangeCallback?) {
        tabLayout.setOnPageChangeListener(listener)
    }

    fun setOnScrollChangeListener(listener: OnTabScrollChangeListener?) {
        tabLayout.setOnScrollChangeListener(listener)
    }

    fun setOnTabClickListener(listener: OnTabClickListener?) {
        tabLayout.setOnTabClickListener(listener)
    }

    fun setCustomTabView(layoutResId: Int, textViewId: Int) {
        tabLayout.setCustomTabView(layoutResId, textViewId)
    }

    fun setCustomTabView(provider: TabProvider?) {
        tabLayout.setCustomTabView(provider)
    }

    fun setUpWithViewPager(viewPager: ViewPager2?, tabText: List<String>) {
        tabLayout.setTabDisable { isDisable ->
            this@AlwaysCenterTabLayout.isDisable = isDisable
            isClickable = !isDisable
            isFocusable = !isDisable
            isFocusableInTouchMode = !isDisable
        }
        tabLayout.setUpWithViewPager(viewPager, tabText)
    }

    fun setClickInterval(interval: Long) {
        tabLayout.setClickInterval(interval)
    }

    fun getTabAt(position: Int): View = tabLayout.getChildAt(position)

    fun goToPreviousPage() {
        tabLayout.goToPreviousPage()
    }

    fun goToNextPage() {
        tabLayout.goToNextPage()
    }
}
