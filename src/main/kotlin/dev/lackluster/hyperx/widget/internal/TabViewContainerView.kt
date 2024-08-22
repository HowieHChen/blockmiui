package dev.lackluster.hyperx.widget.internal

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import cn.fkj233.ui.R
import dev.lackluster.hyperx.core.util.HyperXUIUtils
import dev.lackluster.hyperx.internal.util.ViewUtils.layoutChildView


class TabViewContainerView : FrameLayout {
    private var mChildrenTotalWidth = 0
    private var mDensityDpi = 0
    private var mGapBetweenTabs = 0
    private var mLayoutCenter = false
    private var mLayoutMode = 0
    private val mOverSizeViews: ArrayList<View?>
    private val mSmallSizeViews: ArrayList<View?>
    private var mSpaciousLessThanTwoItemMinWidth = 0
    private var mSpaciousMoreThanFourItemMinWidth = 0
    private var mSpaciousThreeItemMinWidth = 0
    private var mVerticalPadding = 0
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes) {
        mLayoutCenter = false
        mLayoutMode = 0
        mOverSizeViews = ArrayList()
        mSmallSizeViews = ArrayList()
        updateLayoutParams()
    }

    private fun updateLayoutParams() {
        mGapBetweenTabs = resources.getDimensionPixelSize(R.dimen.hyperx_filter_sort_view2_tab_gap)
        mVerticalPadding = resources.getDimensionPixelSize(R.dimen.hyperx_filter_sort_view2_vertical_padding)
        mSpaciousLessThanTwoItemMinWidth = HyperXUIUtils.dp2px(context, 220.0f)
        mSpaciousThreeItemMinWidth = HyperXUIUtils.dp2px(context, 180.0f)
        mSpaciousMoreThanFourItemMinWidth = HyperXUIUtils.dp2px(context, 150.0f)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val dpi: Int = newConfig?.densityDpi ?: return
        if (dpi != mDensityDpi) {
            mDensityDpi = dpi
            updateLayoutParams()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mLayoutCenter = false
        mChildrenTotalWidth = 0
        val childCount = childCount
        var i3 = 0
        for (i4 in 0 until childCount) {
            if (!isViewGone(getChildAt(i4))) {
                i3++
            }
        }
        for (i5 in 0 until childCount) {
            val childAt = getChildAt(i5)
            if (mLayoutMode != 1) {
                childAt.minimumWidth = 0
            } else if (i3 <= 2) {
                childAt.minimumWidth = mSpaciousLessThanTwoItemMinWidth
            } else if (i3 == 3) {
                childAt.minimumWidth = mSpaciousThreeItemMinWidth
            } else {
                childAt.minimumWidth = mSpaciousMoreThanFourItemMinWidth
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (i3 <= 0) {
            return
        }
        mOverSizeViews.clear()
        mSmallSizeViews.clear()
        val size = MeasureSpec.getSize(widthMeasureSpec)
        val paddingStart = paddingStart + paddingEnd
        val i6 = if (i3 > 1) (i3 - 1) * mGapBetweenTabs else 0
        val i7 = size - paddingStart - i6
        val i8 = i7 / i3
        var i9 = 0
        var i10 = 0
        var i11 = 0
        for (i12 in 0 until childCount) {
            val childAt2 = getChildAt(i12)
            if (!isViewGone(childAt2)) {
                val measuredWidth = childAt2.measuredWidth
                i9 += measuredWidth
                if (measuredWidth > i8) {
                    mOverSizeViews.add(childAt2)
                    i11 += measuredWidth
                } else {
                    mSmallSizeViews.add(childAt2)
                    i10 += measuredWidth
                }
                childAt2.measure(
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(
                        measuredHeight, MeasureSpec.EXACTLY
                    )
                )
            }
        }
        val measuredHeight = measuredHeight + mVerticalPadding * 2
        if (i9 > i7) {
            setMeasuredDimension(i9 + i6 + paddingStart, measuredHeight)
            return
        }
        val i13 = mLayoutMode
        if (i13 != 0) {
            if (i13 == 1) {
                mLayoutCenter = true
                mChildrenTotalWidth = i9 + i6
                setMeasuredDimension(size, measuredHeight)
                return
            }
            throw IllegalStateException("Illegal layout mode: $mLayoutMode")
        }
        if (mOverSizeViews.isEmpty()) {
            for (i14 in 0 until childCount) {
                val childAt3 = getChildAt(i14)
                if (!isViewGone(childAt3)) {
                    childAt3.measure(
                        MeasureSpec.makeMeasureSpec(i8, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(childAt3.measuredHeight, MeasureSpec.EXACTLY)
                    )
                }
            }
        } else if (i10 > 0) {
            val size2 = mSmallSizeViews.size
            val i15 = i7 - i11
            for (i16 in 0 until size2) {
                val view = mSmallSizeViews[i16]!!
                val measuredWidth2 = (view.measuredWidth * 1.0f / i10 * i15).toInt()
                if (!isViewGone(view)) {
                    view.measure(
                        MeasureSpec.makeMeasureSpec(measuredWidth2, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(view.measuredHeight, MeasureSpec.EXACTLY)
                    )
                }
            }
        }
        setMeasuredDimension(size, measuredHeight)
    }

    private fun isViewGone(view: View): Boolean {
        return view.visibility == 8
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val paddingStart: Int
        val i5: Int = right - left
        val childCount = childCount
        val i6 = mVerticalPadding
        paddingStart = if (mLayoutCenter) {
            getPaddingStart() + (i5 - mChildrenTotalWidth) / 2
        } else {
            getPaddingStart()
        }
        var i7 = paddingStart
        for (i8 in 0 until childCount) {
            val childAt = getChildAt(i8)
            if (!isViewGone(childAt)) {
                val measuredWidth = childAt.measuredWidth + i7
                layoutChildView(this, childAt, i7, i6, measuredWidth, i6 + childAt.measuredHeight)
                i7 = measuredWidth + mGapBetweenTabs
            }
        }
    }

    fun setTabViewLayoutMode(i: Int) {
        if (mLayoutMode != i) {
            mLayoutMode = i
            requestLayout()
        }
    }
}