package com.datbt.center_tab_layout

import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

abstract class TabIndicationInterpolator {

    companion object {
        val SMART: TabIndicationInterpolator = SmartIndicationInterpolator()
        val LINEAR: TabIndicationInterpolator = LinearIndicationInterpolator()

        const val ID_SMART = 0
        const val ID_LINEAR = 1

        fun of(id: Int): TabIndicationInterpolator {
            return when (id) {
                ID_SMART -> SMART
                ID_LINEAR -> LINEAR
                else -> throw IllegalArgumentException("Unknown id: $id")
            }
        }
    }

    abstract fun getLeftEdge(offset: Float): Float

    abstract fun getRightEdge(offset: Float): Float

    open fun getThickness(offset: Float): Float = 1f //Always the same thickness by default

    class SmartIndicationInterpolator @JvmOverloads constructor(
        factor: Float = DEFAULT_INDICATOR_INTERPOLATION_FACTOR
    ) : TabIndicationInterpolator() {

        companion object {
            private const val DEFAULT_INDICATOR_INTERPOLATION_FACTOR = 3.0f
        }

        private val leftEdgeInterpolator: Interpolator
        private val rightEdgeInterpolator: Interpolator

        init {
            leftEdgeInterpolator = AccelerateInterpolator(factor)
            rightEdgeInterpolator = DecelerateInterpolator(factor)
        }

        override fun getLeftEdge(offset: Float): Float {
            return leftEdgeInterpolator.getInterpolation(offset)
        }

        override fun getRightEdge(offset: Float): Float {
            return rightEdgeInterpolator.getInterpolation(offset)
        }

        override fun getThickness(offset: Float): Float =
            1f / (1.0f - getLeftEdge(offset) + getRightEdge(offset))
    }

    class LinearIndicationInterpolator : TabIndicationInterpolator() {

        override fun getLeftEdge(offset: Float): Float = offset

        override fun getRightEdge(offset: Float): Float = offset
    }
}