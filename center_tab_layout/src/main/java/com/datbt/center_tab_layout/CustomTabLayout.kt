package com.datbt.center_tab_layout

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.TextUtilsCompat
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.datbt.center_tab_layout.interfaces.OnTabClickListener
import com.datbt.center_tab_layout.interfaces.OnTabGestureListener
import com.datbt.center_tab_layout.interfaces.OnTabScrollChangeListener
import com.datbt.center_tab_layout.interfaces.TabColorizer
import com.datbt.center_tab_layout.providers.SimpleTabProvider
import com.datbt.center_tab_layout.providers.TabProvider
import java.util.*

/**
 * To be used with ViewPager2 to provide a tab indicator component which give constant feedback as
 * to
 * the user's scroll progress.
 *
 *
 * To use the component, simply add it to your view hierarchy. Then in your
 * [android.app.Activity] or [androidx.fragment.app.Fragment] call
 * [.setViewPager] providing it the ViewPager this layout is being used for.
 *
 *
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via [.setSelectedIndicatorColors] and [.setDividerColors]. The
 * alternative is via the [TabColorizer] interface which provides you complete control over
 * which color is used for any individual position.
 *
 *
 * The views used as tabs can be customized by calling [.setCustomTabView],
 * providing the layout ID of your custom layout.
 */
internal class CustomTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    HorizontalScrollView(context, attrs, defStyle) {

    companion object {
        private const val DEFAULT_DISTRIBUTE_EVENLY = false
        private const val TITLE_OFFSET_DIPS = 24
        private const val TAB_VIEW_PADDING_DIPS = 16
        private const val TAB_VIEW_TEXT_ALL_CAPS = false
        private const val TAB_VIEW_TEXT_SIZE_SP = 12
        private const val TAB_VIEW_TEXT_MIN_WIDTH = 0
        private const val TAB_CLICKABLE = true
        private const val TAB_CLICK_INTERVAL = 500L
    }

    internal val tabStrip: TabStrip

    private val titleOffset: Int
    private var distributeEvenly: Boolean
    private var mDetector: GestureDetectorCompat
    private var childView: View? = null
    private var isClicked: Boolean = false

    // Tab params
    private val tabViewBackgroundResId: Int
    private val tabViewTextAllCaps: Boolean
    private var tabViewTextColors: ColorStateList?
    private val tabViewTextSize: Float
    private val tabViewTextHorizontalPadding: Int
    private val tabViewTextMinWidth: Int
    private var tabProvider: TabProvider? = null
    private val isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL

    private val ViewPager2.isHorizontal: Boolean
        get() {
            return orientation == ViewPager2.ORIENTATION_HORIZONTAL
        }

    private var lastX: Float = 0f
    private var lastTimeClicked: Long = 0
    private var clickInterval = TAB_CLICK_INTERVAL

    // View pager
    private var viewPager: ViewPager2? = null

    // Callback
    private var onTabClickListener: OnTabClickListener? = null
    private var onTabGestureListener: OnTabGestureListener? = null
    private var onScrollChangeListener: OnTabScrollChangeListener? = null
    private var viewPagerPageChangeListener: ViewPager2.OnPageChangeCallback? = null
    private var onDisableTab: (Boolean) -> Unit = {}

    init {
        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false
        val dm = resources.displayMetrics
        val density = dm.density
        var tabBackgroundResId = NO_ID
        var textAllCaps = TAB_VIEW_TEXT_ALL_CAPS
        val textColors: ColorStateList?
        var textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP.toFloat(), dm
        )
        var textHorizontalPadding = (TAB_VIEW_PADDING_DIPS * density).toInt()
        var textMinWidth = (TAB_VIEW_TEXT_MIN_WIDTH * density).toInt()
        var distributeEvenly = DEFAULT_DISTRIBUTE_EVENLY
        var customTabLayoutId = NO_ID
        var customTabTextViewId = NO_ID
        var clickable = TAB_CLICKABLE
        var titleOffset = (TITLE_OFFSET_DIPS * density).toInt()
        val a = context.obtainStyledAttributes(attrs, R.styleable.acl_CenterTabLayout, defStyle, 0)
        tabBackgroundResId = a.getResourceId(
            R.styleable.acl_CenterTabLayout_acl_defaultTabBackground, tabBackgroundResId
        )
        textAllCaps = a.getBoolean(
            R.styleable.acl_CenterTabLayout_acl_defaultTabTextAllCaps, textAllCaps
        )
        textColors = a.getColorStateList(R.styleable.acl_CenterTabLayout_acl_defaultTabTextColor)
        textSize = a.getDimension(
            R.styleable.acl_CenterTabLayout_acl_defaultTabTextSize, textSize
        )
        textHorizontalPadding = a.getDimensionPixelSize(
            R.styleable.acl_CenterTabLayout_acl_defaultTabTextHorizontalPadding, textHorizontalPadding
        )
        textMinWidth = a.getDimensionPixelSize(
            R.styleable.acl_CenterTabLayout_acl_defaultTabTextMinWidth, textMinWidth
        )
        customTabLayoutId = a.getResourceId(
            R.styleable.acl_CenterTabLayout_acl_customTabTextLayoutId, customTabLayoutId
        )
        customTabTextViewId = a.getResourceId(
            R.styleable.acl_CenterTabLayout_acl_customTabTextViewId, customTabTextViewId
        )
        distributeEvenly = a.getBoolean(
            R.styleable.acl_CenterTabLayout_acl_distributeEvenly, distributeEvenly
        )
        clickable = a.getBoolean(
            R.styleable.acl_CenterTabLayout_acl_clickable, clickable
        )
        titleOffset = a.getLayoutDimension(
            R.styleable.acl_CenterTabLayout_acl_titleOffset, titleOffset
        )
        a.recycle()
        this.titleOffset = titleOffset
        tabViewBackgroundResId = tabBackgroundResId
        tabViewTextAllCaps = textAllCaps
        tabViewTextColors = textColors
        tabViewTextSize = textSize
        tabViewTextHorizontalPadding = textHorizontalPadding
        tabViewTextMinWidth = textMinWidth
        onTabGestureListener = InternalTabGestureListener()
        this.distributeEvenly = distributeEvenly
        if (customTabLayoutId != NO_ID) {
            setCustomTabView(customTabLayoutId, customTabTextViewId)
        }
        tabStrip = TabStrip(context, attrs)
        if (distributeEvenly) {
            throw UnsupportedOperationException(
                "'distributeEvenly' and 'indicatorAlwaysInCenter' both use does not support"
            )
        }

        // Make sure that the Tab Strips fills this View
        isFillViewport = false
        addView(tabStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        mDetector = GestureDetectorCompat(context, onTabGestureListener)
        mDetector.setOnDoubleTapListener(onTabGestureListener)

        setOnTouchListener { _, motionEvent ->
            handleOnTouchEvent(motionEvent)
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (onScrollChangeListener != null) {
            onScrollChangeListener?.onScrollChanged(l, oldl)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (tabStrip.childCount > 0) {
            val firstTab: View = tabStrip.getChildAt(0)
            val lastTab: View = tabStrip.getChildAt(tabStrip.childCount - 1)
            val start: Int = (w - Utils.getMeasuredWidth(firstTab)) / 2 - Utils.getMarginStart(firstTab)
            val end: Int = (w - Utils.getMeasuredWidth(lastTab)) / 2 - Utils.getMarginEnd(lastTab)
            tabStrip.minimumWidth = tabStrip.measuredWidth
            ViewCompat.setPaddingRelative(this, start, paddingTop, end, paddingBottom)
            clipToPadding = false
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        // Ensure first scroll
        if (changed && viewPager != null) {
            scrollToTab(viewPager!!.currentItem, 0f)
        }
    }

    /**
     * Set the behavior of the Indicator scrolling feedback.
     *
     * @param interpolator [com.datbt.center_tab_layout.TabIndicationInterpolator]
     */
    fun setIndicationInterpolator(interpolator: TabIndicationInterpolator?) {
        tabStrip.setIndicationInterpolator(interpolator)
    }

    /**
     * Set the custom [TabColorizer] to be used.
     *
     * If you only require simple customisation then you can use
     * [.setSelectedIndicatorColors] and [.setDividerColors] to achieve
     * similar effects.
     */
    fun setCustomTabColorizer(tabColorizer: TabColorizer?) {
        tabStrip.setCustomTabColorizer(tabColorizer)
    }

    /**
     * Set the color used for styling the tab text. This will need to be called prior to calling
     * [.setViewPager] otherwise it will not get set
     *
     * @param color to use for tab text
     */
    fun setDefaultTabTextColor(color: Int) {
        tabViewTextColors = ColorStateList.valueOf(color)
    }

    /**
     * Sets the colors used for styling the tab text. This will need to be called prior to calling
     * [.setViewPager] otherwise it will not get set
     *
     * @param colors ColorStateList to use for tab text
     */
    fun setDefaultTabTextColor(colors: ColorStateList) {
        tabViewTextColors = colors
    }

    /**
     * Set the same weight for tab
     */
    fun setDistributeEvenly(distributeEvenly: Boolean) {
        this.distributeEvenly = distributeEvenly
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    fun setSelectedIndicatorColors(vararg colors: Int) {
        tabStrip.setSelectedIndicatorColors(*colors)
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    fun setDividerColors(vararg colors: Int) {
        tabStrip.setDividerColors(*colors)
    }

    /**
     * Set the [ViewPager2.OnPageChangeCallback]. When using [CustomTabLayout] you are
     * required to set any [ViewPager2.OnPageChangeCallback] through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager2.OnPageChangeCallback
     */
    fun setOnPageChangeListener(listener: ViewPager2.OnPageChangeCallback?) {
        viewPagerPageChangeListener = listener
    }

    /**
     * Set [OnTabScrollChangeListener] for obtaining values of scrolling.
     *
     * @param listener the [OnTabScrollChangeListener] to set
     */
    fun setOnScrollChangeListener(listener: OnTabScrollChangeListener?) {
        onScrollChangeListener = listener
    }

    /**
     * Set [OnTabClickListener] for obtaining click event.
     *
     * @param listener the [OnTabClickListener] to set
     */
    fun setOnTabClickListener(listener: OnTabClickListener?) {
        onTabClickListener = listener
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId id of the [TextView] in the inflated view
     */
    fun setCustomTabView(layoutResId: Int, textViewId: Int) {
        tabProvider = SimpleTabProvider(context, layoutResId, textViewId)
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param provider [TabProvider]
     */
    fun setCustomTabView(provider: TabProvider?) {
        tabProvider = provider
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    fun setUpWithViewPager(viewPager: ViewPager2?, tabText: List<String>) {
        tabStrip.removeAllViews()
        this.viewPager = viewPager
        if (viewPager != null && viewPager.adapter != null) {
            viewPager.registerOnPageChangeCallback(InternalViewPagerListener())
            populateTabStrip(tabText)
        }
    }

    /**
     * Sets the interval time of click to avoid rapid click
     */
    fun setClickInterval(interval: Long) {
        clickInterval = interval
    }

    /**
     * Returns the view at the specified position in the tabs.
     *
     * @param position the position at which to get the view from
     * @return the view at the specified position or null if the position does not exist within the
     * tabs
     */
    fun getTabAt(position: Int): View {
        return tabStrip.getChildAt(position)
    }

    fun goToPreviousPage() {
        if (viewPager != null) {
            viewPager?.endFakeDrag()
            viewPager!!.currentItem--
        }
    }

    fun goToNextPage() {
        if (viewPager != null) {
            viewPager?.endFakeDrag()
            viewPager!!.currentItem++
        }
    }

    fun setTabDisable(onDisable: (Boolean) -> Unit) {
        onDisableTab = onDisable
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * [.setCustomTabView].
     */
    protected fun createDefaultTabView(title: CharSequence?): TextView {
        val textView = TextView(context)
        textView.gravity = Gravity.CENTER
        textView.text = title
        textView.setTextColor(tabViewTextColors)
        textView.setBackgroundColor(Color.BLUE)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSize)
        textView.typeface = Typeface.DEFAULT
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(
                tabViewTextHorizontalPadding, 0,
                tabViewTextHorizontalPadding, 0
            )
        }
        if (tabViewBackgroundResId != NO_ID) {
            textView.setBackgroundResource(tabViewBackgroundResId)
        } else {
            // If we're running on Honeycomb or newer, then we can use the Theme's
            // selectableItemBackground to ensure that the View has a pressed state
            val outValue = TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue, true
            )
            textView.setBackgroundResource(outValue.resourceId)
        }
        textView.isAllCaps = tabViewTextAllCaps
//        textView.setPadding(
//            tabViewTextHorizontalPadding, 0,
//            tabViewTextHorizontalPadding, 0
//        )
        if (tabViewTextMinWidth > 0) {
            textView.minWidth = tabViewTextMinWidth
        }
        return textView
    }

    private fun populateTabStrip(tabText: List<String>) {
        if (viewPager == null) return
        val adapter = viewPager?.adapter
        for (i in 0 until (adapter?.itemCount ?: 0)) {
            val tabView =
                (if (tabProvider == null) createDefaultTabView(tabText[i]) else tabProvider?.createTabView(tabStrip, i, tabText))
                    ?: throw IllegalStateException("Tab view is null.")
            if (distributeEvenly) {
                val lp = tabView.layoutParams as LinearLayout.LayoutParams
                lp.width = 0
                lp.weight = 1f
            }
            tabView.setOnTouchListener { view, motionEvent ->
                childView = view
                if (!mDetector.onTouchEvent(motionEvent)) {
                    viewPager?.beginFakeDrag()
                }
                true
            }
            tabStrip.addView(tabView)
            if (i == viewPager!!.currentItem) {
                tabView.isSelected = true
            }
        }
    }

    private fun scrollToTab(tabIndex: Int, positionOffset: Float) {
        val tabStripChildCount: Int = tabStrip.childCount
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return
        }
        val isLayoutRtl: Boolean = Utils.isLayoutRtl(this)
        val selectedTab: View = tabStrip.getChildAt(tabIndex)
        val widthPlusMargin: Int = Utils.getWidth(selectedTab) + Utils.getMarginHorizontally(selectedTab)
        var extraOffset = (positionOffset * widthPlusMargin).toInt()
        var deltaCenter: Int
        if (0f < positionOffset && positionOffset < 1f) {
            val nextTab: View = tabStrip.getChildAt(tabIndex + 1)
            val selectHalfWidth: Int = Utils.getWidth(selectedTab) / 2 + Utils.getMarginEnd(selectedTab)
            val nextHalfWidth: Int = Utils.getWidth(nextTab) / 2 + Utils.getMarginStart(nextTab)
            extraOffset = Math.round(positionOffset * (selectHalfWidth + nextHalfWidth))
        }
        val firstTab: View = tabStrip.getChildAt(0)
        if (isLayoutRtl) {
            val first: Int = Utils.getWidth(firstTab) + Utils.getMarginEnd(firstTab)
            val selected: Int = Utils.getWidth(selectedTab) + Utils.getMarginEnd(selectedTab)
            deltaCenter = Utils.getEnd(selectedTab) - Utils.getMarginEnd(selectedTab) - extraOffset
            deltaCenter -= (first - selected) / 2
        } else {
            val first: Int = Utils.getWidth(firstTab) + Utils.getMarginStart(firstTab)
            val selected: Int = Utils.getWidth(selectedTab) + Utils.getMarginStart(selectedTab)
            deltaCenter = Utils.getStart(selectedTab) - Utils.getMarginStart(selectedTab) + extraOffset
            deltaCenter -= (first - selected) / 2
        }
        scrollTo(deltaCenter, 0)
    }

    /**
     * Create custom [OnPageChangeCallback] of [ViewPager2]
     */
    private inner class InternalViewPagerListener : ViewPager2.OnPageChangeCallback() {
        private var scrollState = 0
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val tabStripChildCount: Int = tabStrip.childCount
            if (tabStripChildCount == 0 || position < 0 || position >= tabStripChildCount) {
                return
            }
            tabStrip.onViewPagerPageChanged(position, positionOffset)
            scrollToTab(position, positionOffset)
            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener?.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (isClicked && scrollState == ViewPager2.SCROLL_STATE_SETTLING && state == ViewPager2.SCROLL_STATE_IDLE) {
                isClicked = false
                onDisableTab(false)
            }
            scrollState = state
            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener?.onPageScrollStateChanged(state)
            }
        }

        override fun onPageSelected(position: Int) {
            if (scrollState == ViewPager2.SCROLL_STATE_IDLE) {
                tabStrip.onViewPagerPageChanged(position, 0f)
                scrollToTab(position, 0f)
            }
            var i = 0
            val size: Int = tabStrip.childCount
            while (i < size) {
                tabStrip.getChildAt(i).isSelected = position == i
                i++
            }
            if (viewPagerPageChangeListener != null) {
                viewPagerPageChangeListener?.onPageSelected(position)
            }
        }
    }

    /**
     * Gesture listener of the tab when clicked.
     */
    private inner class InternalTabGestureListener : OnTabGestureListener() {

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            if (isSingleClick()) {
                isClicked = true
                onDisableTab(true)
                // Get child visibility rect
                val rect = Rect()
                childView?.getGlobalVisibleRect(rect)
                val coordinateX = event.rawX
                if (rect.left < coordinateX && rect.right > coordinateX) {
                    val indexChild = tabStrip.indexOfChild(childView)
                    if (onTabClickListener != null) {
                        onTabClickListener?.onTabClicked(indexChild)
                    }
                    viewPager?.endFakeDrag()
                    viewPager?.currentItem = indexChild
                    childView?.performClick()
                }
            }
            return super.onSingleTapUp(event)
        }

        override fun onDown(event: MotionEvent): Boolean {
            val rect = Rect()
            childView?.getGlobalVisibleRect(rect)
            lastX = event.rawX
            return super.onDown(event)
        }
    }

    private fun isSingleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < clickInterval) {
            return false
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        return true
    }

    private fun mirrorInRtl(f: Float): Float {
        return if (isRtl) -f else f
    }

    private fun getValue(event: MotionEvent): Float {
        return mirrorInRtl(event.x)
    }

    private fun handleOnTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = getValue(event)
                viewPager?.beginFakeDrag()
            }

            MotionEvent.ACTION_MOVE -> {
                val value = getValue(event)
                val delta = value - lastX
                viewPager?.fakeDragBy(if (viewPager?.isHorizontal == true) mirrorInRtl(delta) else delta)
                lastX = value
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                viewPager?.endFakeDrag()
            }
        }
        return true
    }
}