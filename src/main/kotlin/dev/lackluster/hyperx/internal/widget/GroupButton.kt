package dev.lackluster.hyperx.internal.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import cn.fkj233.ui.R

class GroupButton: Button {
    private var mAttrsCache: AttributeSet? = null
    private var mPrimary: Boolean = false
    companion object {
        private val STATE_FIRST_V: IntArray = intArrayOf(R.attr.state_first_v)
        private val STATE_MIDDLE_V: IntArray = intArrayOf(R.attr.state_middle_v)
        private val STATE_LAST_V: IntArray = intArrayOf(R.attr.state_last_v)
        private val STATE_FIRST_H: IntArray = intArrayOf(R.attr.state_first_h)
        private val STATE_MIDDLE_H: IntArray = intArrayOf(R.attr.state_middle_h)
        private val STATE_LAST_H: IntArray = intArrayOf(R.attr.state_last_h)
        private val STATE_SINGLE_H: IntArray = intArrayOf(R.attr.state_single_h)
    }
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        initAttr(context, attributeSet, defStyleAttr)
    }

    private fun initAttr(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) {
        mAttrsCache = attributeSet
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.GroupButton, defStyleAttr, 0)
        try {
            if (obtainStyledAttributes.hasValue(R.styleable.GroupButton_primaryButton)) {
                mPrimary = obtainStyledAttributes.getBoolean(R.styleable.GroupButton_primaryButton, false)
            }
        } finally {
            obtainStyledAttributes.recycle()
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val viewGroup = parent ?: return super.onCreateDrawableState(extraSpace)
        if (viewGroup is LinearLayout) {
            val orientation = viewGroup.orientation
            val indexOfChild = viewGroup.indexOfChild(this)
            var visibleCount = 0
            var first = true
            var last = true
            for (index in 0 until viewGroup.childCount) {
                if (viewGroup.getChildAt(index).visibility == View.VISIBLE) {
                    visibleCount++
                    if (index < indexOfChild) {
                        first = false
                    }
                    if (index > indexOfChild) {
                        last = false
                    }
                }
            }
            val single = visibleCount == 1
            if (orientation == 1) {
                val onCreateDrawableState = super.onCreateDrawableState(extraSpace + 2)
                mergeDrawableStates(onCreateDrawableState, STATE_SINGLE_H)
                if (!single) {
                    if (first) {
                        mergeDrawableStates(onCreateDrawableState, STATE_FIRST_V)
                    } else if (last) {
                        mergeDrawableStates(onCreateDrawableState, STATE_LAST_V)
                    } else {
                        mergeDrawableStates(onCreateDrawableState, STATE_MIDDLE_V)
                    }
                }
                return onCreateDrawableState
            } else {
                val isLayoutRtl = layoutDirection == LAYOUT_DIRECTION_RTL
                val onCreateDrawableState = super.onCreateDrawableState(extraSpace + 1)
                if (single) {
                    mergeDrawableStates(onCreateDrawableState, STATE_SINGLE_H)
                } else if (first) {
                    mergeDrawableStates(onCreateDrawableState, if (isLayoutRtl) STATE_LAST_H else STATE_FIRST_H)
                } else if (last) {
                    mergeDrawableStates(onCreateDrawableState, if (isLayoutRtl) STATE_FIRST_H else STATE_LAST_H)
                } else {
                    mergeDrawableStates(onCreateDrawableState, STATE_MIDDLE_H)
                }
                return onCreateDrawableState
            }
        }
        return super.onCreateDrawableState(extraSpace)
    }

    fun isPrimary(): Boolean {
        return mPrimary
    }
}