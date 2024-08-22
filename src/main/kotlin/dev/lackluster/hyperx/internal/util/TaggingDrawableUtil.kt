package dev.lackluster.hyperx.internal.util

import android.content.Context
import android.graphics.drawable.StateListDrawable
import android.view.View
import cn.fkj233.ui.R
import dev.lackluster.hyperx.internal.graphics.drawable.TaggingDrawable

object TaggingDrawableUtil {
    private var mPaddingLarge: Int = -1
    private var mPaddingSingle: Int = -1
    private var mPaddingSmall: Int = -1
    private val STATES_TAGS: IntArray = intArrayOf(android.R.attr.state_single, android.R.attr.state_first, android.R.attr.state_middle, android.R.attr.state_last)
    private val STATE_SET_SINGLE: IntArray = intArrayOf(android.R.attr.state_single)
    private val STATE_SET_FIRST: IntArray = intArrayOf(android.R.attr.state_first)
    private val STATE_SET_MIDDLE: IntArray = intArrayOf(android.R.attr.state_middle)
    private val STATE_SET_LAST: IntArray = intArrayOf(android.R.attr.state_last)
    fun updateItemBackground(view: View?, i: Int, i2: Int) {
        updateBackgroundState(view, i, i2)
        updateItemPadding(view, i, i2)
    }
    fun updateBackgroundState(view: View?, i: Int, i2: Int) {
        if (view == null || i2 == 0) {
            return
        }
        var background = view.background
        if (background is StateListDrawable && TaggingDrawable.containsTagState(background, STATES_TAGS)) {
            val taggingDrawable = TaggingDrawable(background)
            view.background = taggingDrawable
            background = taggingDrawable
        }
        if (background is TaggingDrawable) {
            val iArr: IntArray = if (i2 == 1) {
                STATE_SET_SINGLE
            } else if (i == 0) {
                STATE_SET_FIRST
            } else if (i == i2 - 1) {
                STATE_SET_LAST
            } else {
                STATE_SET_MIDDLE
            }
            background.setTaggingState(iArr)
        }
    }
    fun updateItemPadding(view: View?, i: Int, i2: Int) {
        if (view == null || i2 == 0) {
            return
        }
        var i3: Int
        var i4: Int = 0
        var measuredHeight: Int
        var dimen: Int
        val context = view.context
        var paddingStart = view.paddingStart
        view.paddingTop
        var paddingEnd = view.paddingEnd
        view.paddingBottom
        if (i2 == 1) {
            if (mPaddingSingle == -1) {
                mPaddingSingle = getDimen(context, R.dimen.hyperx_drop_down_menu_padding_single_item)
            }
            i3 = mPaddingSingle
            i4 = i3
            measuredHeight = view.measuredHeight
            dimen = getDimen(context, R.dimen.hyperx_drop_down_item_min_height)
            if (measuredHeight > 0) {
                val i5 = (dimen - measuredHeight) / 2
                i3 += i5
                i4 += i5
            }
            view.setPaddingRelative(paddingStart, i3, paddingEnd, i4)
        } else {
            if (mPaddingSmall == -1) {
                mPaddingSmall = getDimen(context, R.dimen.hyperx_drop_down_menu_padding_small)
            }
            if (mPaddingLarge == -1) {
                mPaddingLarge = getDimen(context, R.dimen.hyperx_drop_down_menu_padding_large)
            }
            when (i) {
                0 -> {
                    i3 = mPaddingLarge
                    i4 = mPaddingSmall
                }
                i2 - 1 -> {
                    i3 = mPaddingSmall
                    i4 = mPaddingLarge
                }
                else -> {
                    i3 = mPaddingSmall
                    i4 = mPaddingSmall
                }
            }
            measuredHeight = view.measuredHeight
            dimen = getDimen(context, R.dimen.hyperx_drop_down_item_min_height)
            if (measuredHeight in 1..< dimen) {
                val i5 = (dimen - measuredHeight) / 2
                i3 += i5
                i4 += i5
            }
            view.setPaddingRelative(paddingStart, i3, paddingEnd, i4)
        }
    }
    private fun getDimen(context: Context, resId: Int): Int {
        return context.resources.getDimensionPixelSize(resId)
    }
}