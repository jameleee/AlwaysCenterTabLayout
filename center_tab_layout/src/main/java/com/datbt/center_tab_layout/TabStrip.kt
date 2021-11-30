package com.datbt.center_tab_layout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import com.datbt.center_tab_layout.interfaces.TabColorizer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *
 *
 * Forked from Google Samples &gt; SlidingTabsBasic &gt;
 * [SlidingTabStrip](https://developer.android.com/samples/SlidingTabsBasic/src/com.example.android.common/view/SlidingTabLayout.html)
 */
internal class TabStrip(context: Context, attrs: AttributeSet?) : LinearLayout(context) {

    companion object {
        private const val GRAVITY_BOTTOM = 0
        private const val GRAVITY_TOP = 1
        private const val GRAVITY_CENTER = 2
        private const val AUTO_WIDTH = -1
        private const val DEFAULT_TOP_BORDER_THICKNESS_DIPS = 0
        private const val DEFAULT_TOP_BORDER_COLOR_ALPHA: Byte = 0x26
        private const val DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 2
        private const val DEFAULT_BOTTOM_BORDER_COLOR_ALPHA: Byte = 0x26
        private const val SELECTED_INDICATOR_THICKNESS_DIPS = 8
        private const val DEFAULT_SELECTED_INDICATOR_COLOR = -0xcc4a1b
        private const val DEFAULT_INDICATOR_CORNER_RADIUS = 0f
        private const val DEFAULT_DIVIDER_THICKNESS_DIPS = 1
        private const val DEFAULT_DIVIDER_COLOR_ALPHA: Byte = 0x20
        private const val DEFAULT_DIVIDER_HEIGHT = 0.5f
        private const val DEFAULT_INDICATOR_IN_CENTER = false
        private const val DEFAULT_INDICATOR_IN_FRONT = false
        private const val DEFAULT_INDICATOR_WITHOUT_PADDING = false
        private const val DEFAULT_INDICATOR_GRAVITY = GRAVITY_BOTTOM
        private const val DEFAULT_DRAW_DECORATION_AFTER_TAB = false

        /**
         * Set the alpha value of the `color` to be the given `alpha` value.
         */
        private fun setColorAlpha(color: Int, alpha: Byte): Int {
            return Color.argb(alpha.toInt(), Color.red(color), Color.green(color), Color.blue(color))
        }

        /**
         * Blend `color1` and `color2` using the given ratio.
         *
         * @param ratio of which to blend. 1.0 will return `color1`, 0.5 will give an even blend,
         * 0.0 will return `color2`.
         */
        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRation = 1f - ratio
            val r = Color.red(color1) * ratio + Color.red(color2) * inverseRation
            val g = Color.green(color1) * ratio + Color.green(color2) * inverseRation
            val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRation
            return Color.rgb(r.toInt(), g.toInt(), b.toInt())
        }
    }

    private val topBorderThickness: Int
    private val topBorderColor: Int
    private val bottomBorderThickness: Int
    private val bottomBorderColor: Int
    private val borderPaint: Paint
    private val indicatorRectF = RectF()
    private val indicatorWithoutPadding: Boolean
    private val indicatorInFront: Boolean
    private val indicatorThickness: Int
    private val indicatorWidth: Int
    private val indicatorGravity: Int
    private val indicatorCornerRadius: Float
    private val indicatorPaint: Paint
    private val dividerThickness: Int
    private val dividerPaint: Paint
    private val dividerHeight: Float
    private val defaultTabColorizer: SimpleTabColorizer
    private val drawDecorationAfterTab: Boolean
    private var lastPosition = 0
    private var selectedPosition = 0
    private var selectionOffset = 0f
    private var indicationInterpolator: TabIndicationInterpolator?
    private var customTabColorizer: TabColorizer? = null

    init {
        setWillNotDraw(false)
        val density = resources.displayMetrics.density
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorForeground, outValue, true)
        val themeForegroundColor = outValue.data
        var indicatorWithoutPadding = DEFAULT_INDICATOR_WITHOUT_PADDING
        var indicatorInFront = DEFAULT_INDICATOR_IN_FRONT
        var indicationInterpolatorId: Int = TabIndicationInterpolator.ID_SMART
        var indicatorGravity = DEFAULT_INDICATOR_GRAVITY
        var indicatorColor = DEFAULT_SELECTED_INDICATOR_COLOR
        var indicatorColorsId = NO_ID
        var indicatorThickness = (SELECTED_INDICATOR_THICKNESS_DIPS * density).toInt()
        var indicatorWidth = AUTO_WIDTH
        var indicatorCornerRadius = DEFAULT_INDICATOR_CORNER_RADIUS * density
        var overlineColor = setColorAlpha(themeForegroundColor, DEFAULT_TOP_BORDER_COLOR_ALPHA)
        var overlineThickness = (DEFAULT_TOP_BORDER_THICKNESS_DIPS * density).toInt()
        var underlineColor = setColorAlpha(themeForegroundColor, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA)
        var underlineThickness = (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density).toInt()
        var dividerColor = setColorAlpha(themeForegroundColor, DEFAULT_DIVIDER_COLOR_ALPHA)
        var dividerColorsId = NO_ID
        var dividerThickness = (DEFAULT_DIVIDER_THICKNESS_DIPS * density).toInt()
        var drawDecorationAfterTab = DEFAULT_DRAW_DECORATION_AFTER_TAB

        val a = context.obtainStyledAttributes(attrs, R.styleable.acl_CenterTabLayout)
        indicatorWithoutPadding = a.getBoolean(
            R.styleable.acl_CenterTabLayout_acl_indicatorWithoutPadding, indicatorWithoutPadding
        )
        indicatorInFront = a.getBoolean(
            R.styleable.acl_CenterTabLayout_acl_indicatorInFront, indicatorInFront
        )
        indicationInterpolatorId = a.getInt(
            R.styleable.acl_CenterTabLayout_acl_indicatorInterpolation, indicationInterpolatorId
        )
        indicatorGravity = a.getInt(
            R.styleable.acl_CenterTabLayout_acl_indicatorGravity, indicatorGravity
        )
        indicatorColor = a.getColor(
            R.styleable.acl_CenterTabLayout_acl_indicatorColor, indicatorColor
        )
        indicatorColorsId = a.getResourceId(
            R.styleable.acl_CenterTabLayout_acl_indicatorColors, indicatorColorsId
        )
        indicatorThickness = a.getDimensionPixelSize(
            R.styleable.acl_CenterTabLayout_acl_indicatorThickness, indicatorThickness
        )
        indicatorWidth = a.getLayoutDimension(
            R.styleable.acl_CenterTabLayout_acl_indicatorWidth, indicatorWidth
        )
        indicatorCornerRadius = a.getDimension(
            R.styleable.acl_CenterTabLayout_acl_indicatorCornerRadius, indicatorCornerRadius
        )
        overlineColor = a.getColor(
            R.styleable.acl_CenterTabLayout_acl_overlineColor, overlineColor
        )
        overlineThickness = a.getDimensionPixelSize(
            R.styleable.acl_CenterTabLayout_acl_overlineThickness, overlineThickness
        )
        underlineColor = a.getColor(
            R.styleable.acl_CenterTabLayout_acl_underlineColor, underlineColor
        )
        underlineThickness = a.getDimensionPixelSize(
            R.styleable.acl_CenterTabLayout_acl_underlineThickness, underlineThickness
        )
        dividerColor = a.getColor(
            R.styleable.acl_CenterTabLayout_acl_dividerColor, dividerColor
        )
        dividerColorsId = a.getResourceId(
            R.styleable.acl_CenterTabLayout_acl_dividerColors, dividerColorsId
        )
        dividerThickness = a.getDimensionPixelSize(
            R.styleable.acl_CenterTabLayout_acl_dividerThickness, dividerThickness
        )
        drawDecorationAfterTab = a.getBoolean(
            R.styleable.acl_CenterTabLayout_acl_drawDecorationAfterTab, drawDecorationAfterTab
        )
        a.recycle()
        val indicatorColors = if (indicatorColorsId == NO_ID) intArrayOf(indicatorColor) else resources.getIntArray(indicatorColorsId)
        val dividerColors = if (dividerColorsId == NO_ID) intArrayOf(dividerColor) else resources.getIntArray(dividerColorsId)
        defaultTabColorizer = SimpleTabColorizer()
        defaultTabColorizer.setIndicatorColors(*indicatorColors)
        defaultTabColorizer.setDividerColors(*dividerColors)
        topBorderThickness = overlineThickness
        topBorderColor = overlineColor
        bottomBorderThickness = underlineThickness
        bottomBorderColor = underlineColor
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        this.indicatorWithoutPadding = indicatorWithoutPadding
        this.indicatorInFront = indicatorInFront
        this.indicatorThickness = indicatorThickness
        this.indicatorWidth = indicatorWidth
        indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        this.indicatorCornerRadius = indicatorCornerRadius
        this.indicatorGravity = indicatorGravity
        dividerHeight = DEFAULT_DIVIDER_HEIGHT
        dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dividerPaint.strokeWidth = dividerThickness.toFloat()
        this.dividerThickness = dividerThickness
        this.drawDecorationAfterTab = drawDecorationAfterTab
        indicationInterpolator = TabIndicationInterpolator.of(indicationInterpolatorId)
    }

    fun setIndicationInterpolator(interpolator: TabIndicationInterpolator?) {
        indicationInterpolator = interpolator
        invalidate()
    }

    fun setCustomTabColorizer(customTabColorizer: TabColorizer?) {
        this.customTabColorizer = customTabColorizer
        invalidate()
    }

    fun setSelectedIndicatorColors(vararg colors: Int) {
        // Make sure that the custom colorizer is removed
        customTabColorizer = null
        defaultTabColorizer.setIndicatorColors(*colors)
        invalidate()
    }

    fun setDividerColors(vararg colors: Int) {
        // Make sure that the custom colorizer is removed
        customTabColorizer = null
        defaultTabColorizer.setDividerColors(*colors)
        invalidate()
    }

    fun onViewPagerPageChanged(position: Int, positionOffset: Float) {
        selectedPosition = position
        selectionOffset = positionOffset
        if (positionOffset == 0f && lastPosition != selectedPosition) {
            lastPosition = selectedPosition
        }
        invalidate()
    }

    val tabColorizer: TabColorizer
        get() = customTabColorizer ?: defaultTabColorizer

    override fun onDraw(canvas: Canvas) {
        if (!drawDecorationAfterTab) {
            drawDecoration(canvas)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (drawDecorationAfterTab) {
            drawDecoration(canvas)
        }
    }

    private fun drawDecoration(canvas: Canvas) {
        val tabCount = childCount
        val tabColorizer: TabColorizer = tabColorizer
        val isLayoutRtl: Boolean = Utils.isLayoutRtl(this)
        if (indicatorInFront) {
            drawOverline(canvas, 0, width)
            drawUnderline(canvas, 0, width, height)
        }

        // Thick colored underline below the current selection
        if (tabCount > 0) {
            val selectedTab = getChildAt(selectedPosition)
            val selectedStart: Int = Utils.getStart(selectedTab, indicatorWithoutPadding)
            val selectedEnd: Int = Utils.getEnd(selectedTab, indicatorWithoutPadding)
            var left: Int
            var right: Int
            if (isLayoutRtl) {
                left = selectedEnd
                right = selectedStart
            } else {
                left = selectedStart
                right = selectedEnd
            }
            var color: Int = tabColorizer.getIndicatorColor(selectedPosition)
            var thickness = indicatorThickness.toFloat()
            if (selectionOffset > 0f && selectedPosition < childCount - 1) {
                val nextColor: Int = tabColorizer.getIndicatorColor(selectedPosition + 1)
                if (color != nextColor) {
                    color = blendColors(nextColor, color, selectionOffset)
                }

                // Draw the selection partway between the tabs
                val startOffset: Float = indicationInterpolator?.getLeftEdge(selectionOffset) ?: 0f
                val endOffset: Float = indicationInterpolator?.getRightEdge(selectionOffset) ?: 0f
                val thicknessOffset: Float = indicationInterpolator?.getThickness(selectionOffset) ?: 0f
                val nextTab = getChildAt(selectedPosition + 1)
                val nextStart: Int = Utils.getStart(nextTab, indicatorWithoutPadding)
                val nextEnd: Int = Utils.getEnd(nextTab, indicatorWithoutPadding)
                if (isLayoutRtl) {
                    left = (endOffset * nextEnd + (1.0f - endOffset) * left).toInt()
                    right = (startOffset * nextStart + (1.0f - startOffset) * right).toInt()
                } else {
                    left = (startOffset * nextStart + (1.0f - startOffset) * left).toInt()
                    right = (endOffset * nextEnd + (1.0f - endOffset) * right).toInt()
                }
                thickness *= thicknessOffset
            }
            drawIndicator(canvas, left, right, height, thickness, color)
        }
        if (!indicatorInFront) {
            drawOverline(canvas, 0, width)
            drawUnderline(canvas, 0, width, height)
        }

        // Vertical separators between the titles
        drawSeparator(canvas, height, tabCount)
    }

    private fun drawSeparator(canvas: Canvas, height: Int, tabCount: Int) {
        if (dividerThickness <= 0) {
            return
        }
        val dividerHeightPx = (min(max(0f, dividerHeight), 1f) * height).toInt()
        val tabColorizer: TabColorizer = tabColorizer

        // Vertical separators between the titles
        val separatorTop = (height - dividerHeightPx) / 2
        val separatorBottom = separatorTop + dividerHeightPx
        val isLayoutRtl: Boolean = Utils.isLayoutRtl(this)
        for (i in 0 until tabCount - 1) {
            val child = getChildAt(i)
            val end: Int = Utils.getEnd(child)
            val endMargin: Int = Utils.getMarginEnd(child)
            val separatorX = if (isLayoutRtl) end - endMargin else end + endMargin
            dividerPaint.color = tabColorizer.getDividerColor(i)
            canvas.drawLine(separatorX.toFloat(), separatorTop.toFloat(), separatorX.toFloat(), separatorBottom.toFloat(), dividerPaint)
        }
    }

    private fun drawIndicator(
        canvas: Canvas, left: Int, right: Int, height: Int, thickness: Float,
        color: Int
    ) {
        if (indicatorThickness <= 0 || indicatorWidth == 0) {
            return
        }
        val center: Float
        val top: Float
        val bottom: Float
        when (indicatorGravity) {
            GRAVITY_TOP -> {
                center = indicatorThickness / 2f
                top = center - thickness / 2f
                bottom = center + thickness / 2f
            }
            GRAVITY_CENTER -> {
                center = height / 2f
                top = center - thickness / 2f
                bottom = center + thickness / 2f
            }
            GRAVITY_BOTTOM -> {
                center = height - indicatorThickness / 2f
                top = center - thickness / 2f
                bottom = center + thickness / 2f
            }
            else -> {
                center = height - indicatorThickness / 2f
                top = center - thickness / 2f
                bottom = center + thickness / 2f
            }
        }
        indicatorPaint.color = color
        if (indicatorWidth == AUTO_WIDTH) {
            indicatorRectF[left.toFloat(), top, right.toFloat()] = bottom
        } else {
            val padding = (abs(left - right) - indicatorWidth) / 2f
            indicatorRectF[left + padding, top, right - padding] = bottom
        }
        if (indicatorCornerRadius > 0f) {
            canvas.drawRoundRect(
                indicatorRectF, indicatorCornerRadius,
                indicatorCornerRadius, indicatorPaint
            )
        } else {
            canvas.drawRect(indicatorRectF, indicatorPaint)
        }
    }

    private fun drawOverline(canvas: Canvas, left: Int, right: Int) {
        if (topBorderThickness <= 0) {
            return
        }
        // Thin overline along the entire top edge
        borderPaint.color = topBorderColor
        canvas.drawRect(left.toFloat(), 0f, right.toFloat(), topBorderThickness.toFloat(), borderPaint)
    }

    private fun drawUnderline(canvas: Canvas, left: Int, right: Int, height: Int) {
        if (bottomBorderThickness <= 0) {
            return
        }
        // Thin underline along the entire bottom edge
        borderPaint.color = bottomBorderColor
        canvas.drawRect(left.toFloat(), (height - bottomBorderThickness).toFloat(), right.toFloat(), height.toFloat(), borderPaint)
    }

    private class SimpleTabColorizer : TabColorizer {

        private lateinit var indicatorColors: IntArray
        private lateinit var dividerColors: IntArray

        override fun getIndicatorColor(position: Int): Int {
            return indicatorColors[position % indicatorColors.size]
        }

        override fun getDividerColor(position: Int): Int {
            return dividerColors[position % dividerColors.size]
        }

        fun setIndicatorColors(vararg colors: Int) {
            indicatorColors = colors
        }

        fun setDividerColors(vararg colors: Int) {
            dividerColors = colors
        }
    }
}