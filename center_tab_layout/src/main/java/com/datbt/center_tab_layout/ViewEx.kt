package com.datbt.center_tab_layout

import android.view.View
import android.view.ViewGroup
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.ViewCompat

fun View.getWidthWithMargin(): Int {
    return width + getMarginHorizontally()
}

fun View.getViewStart(): Int {
    return getViewStart(false)
}

fun View?.getViewStart(withoutPadding: Boolean): Int {
    if (this == null) {
        return 0
    }
    return if (isLayoutRtl()) {
        if (withoutPadding) right - getViewPaddingStart() else right
    } else {
        if (withoutPadding) left + getViewPaddingStart() else left
    }
}

fun View.getViewEnd(): Int {
    return getViewEnd(false)
}

fun View?.getViewEnd(withoutPadding: Boolean): Int {
    if (this == null) {
        return 0
    }
    return if (isLayoutRtl()) {
        if (withoutPadding) left + getViewPaddingEnd() else left
    } else {
        if (withoutPadding) right - getViewPaddingEnd() else right
    }
}

fun View?.getViewPaddingStart(): Int {
    return if (this == null) {
        0
    } else ViewCompat.getPaddingStart(this)
}

fun View?.getViewPaddingEnd(): Int {
    return if (this == null) {
        0
    } else ViewCompat.getPaddingEnd(this)
}

fun View?.getPaddingHorizontally(): Int {
    return if (this == null) {
        0
    } else paddingLeft + paddingRight
}

fun View?.getViewMarginStart(): Int {
    if (this == null) {
        return 0
    }
    val lp = layoutParams as ViewGroup.MarginLayoutParams
    return MarginLayoutParamsCompat.getMarginStart(lp)
}

fun View?.getViewMarginEnd(): Int {
    if (this == null) {
        return 0
    }
    val lp = layoutParams as ViewGroup.MarginLayoutParams
    return MarginLayoutParamsCompat.getMarginEnd(lp)
}

fun View?.getMarginHorizontally(): Int {
    if (this == null) {
        return 0
    }
    val lp = layoutParams as ViewGroup.MarginLayoutParams
    return MarginLayoutParamsCompat.getMarginStart(lp) + MarginLayoutParamsCompat.getMarginEnd(lp)
}

fun View.isLayoutRtl(): Boolean {
    return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
}