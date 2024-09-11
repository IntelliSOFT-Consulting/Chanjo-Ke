package com.intellisoft.chanjoke.detail.ui.main.registration

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonScrollableViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        // Disable touch events
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        // Disable touch events
        return false
    }
}