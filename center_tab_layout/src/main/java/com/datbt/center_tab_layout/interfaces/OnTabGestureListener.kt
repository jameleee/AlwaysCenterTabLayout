package com.datbt.center_tab_layout.interfaces

import android.view.GestureDetector
import android.view.MotionEvent

open class OnTabGestureListener : GestureDetector.SimpleOnGestureListener() {

    override fun onDown(event: MotionEvent): Boolean = false

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false

    override fun onLongPress(event: MotionEvent) = Unit

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false

    override fun onShowPress(event: MotionEvent) = Unit

    override fun onSingleTapUp(event: MotionEvent): Boolean = true

    override fun onDoubleTap(event: MotionEvent): Boolean = false

    override fun onDoubleTapEvent(event: MotionEvent): Boolean = false

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean = false

    override fun onContextClick(event: MotionEvent?): Boolean = super.onContextClick(event)
}
