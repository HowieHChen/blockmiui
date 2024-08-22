package dev.lackluster.hyperx.internal.widget

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import cn.fkj233.ui.R
import dev.lackluster.hyperx.view.DensityChangedHelper


class DialogButtonPanel : LinearLayout {
    private var mButtonHeight: Int
    private var mButtonMarginHorizontal: Int
    private var mButtonMarginVertical: Int
    private val mButtonTextSize: Float
    private var mCurrentDensityDpi: Int
    private var mForceVertical: Boolean = false
    private var mLastDensityDpi: Int
    private var mPanelPaddingHorizontal: Int

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        mButtonTextSize = 17.0f
        val resources = resources
        mPanelPaddingHorizontal = resources.getDimensionPixelOffset(R.dimen.hyperx_dialog_button_panel_horizontal_margin)
        mButtonMarginHorizontal = resources.getDimensionPixelOffset(R.dimen.miuix_appcompat_dialog_btn_margin_horizontal)
        mButtonMarginVertical = resources.getDimensionPixelOffset(R.dimen.miuix_appcompat_dialog_btn_margin_vertical)
        mButtonHeight = resources.getDimensionPixelOffset(R.dimen.hyperx_button_height)
        val i2 = resources.configuration.densityDpi
        mCurrentDensityDpi = i2
        mLastDensityDpi = i2
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        handleButtonLayout(MeasureSpec.getSize(widthMeasureSpec))
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun handleButtonLayout(i: Int) {
        val isVerticalNeeded = isVerticalNeeded(i - paddingStart - paddingEnd)
        val childCount = childCount
        if (isVerticalNeeded) {
            orientation = VERTICAL
            setPadding(mPanelPaddingHorizontal, paddingTop, mPanelPaddingHorizontal, paddingBottom)
            var i2 = 0
            for (i3 in 0 until childCount) {
                val childAt = getChildAt(i3)
                val z = childAt.visibility == View.VISIBLE
                val layoutParams = childAt.layoutParams as LayoutParams
                layoutParams.width = -1
                layoutParams.height = mButtonHeight
                layoutParams.weight = 0.0f
                layoutParams.topMargin = if (z) i2 else 0
                layoutParams.rightMargin = 0
                layoutParams.leftMargin = 0
                if (z) {
                    i2 = mButtonMarginVertical
                }
            }
            return
        }
        orientation = HORIZONTAL
        setPadding(mPanelPaddingHorizontal, paddingTop, mPanelPaddingHorizontal, paddingBottom)
        val isLayoutRtl: Boolean = layoutDirection == LAYOUT_DIRECTION_RTL
        var i4 = 0
        for (i5 in 0 until childCount) {
            val childAt2 = getChildAt(i5)
            val z2 = childAt2.visibility == 0
            val layoutParams2 = childAt2.layoutParams as LayoutParams
            layoutParams2.width = 0
            layoutParams2.height = mButtonHeight
            layoutParams2.weight = 1.0f
            layoutParams2.topMargin = 0
            if (!z2) {
                layoutParams2.rightMargin = 0
                layoutParams2.leftMargin = 0
            } else if (isLayoutRtl) {
                layoutParams2.rightMargin = i4
            } else {
                layoutParams2.leftMargin = i4
            }
            if (z2) {
                i4 = mButtonMarginHorizontal
            }
        }
    }

    private fun isVerticalNeeded(i: Int): Boolean {
        if (mForceVertical) {
            return true
        }
        val childCount = childCount
        var i2 = childCount
        for (i3 in childCount - 1 downTo 0) {
            if (getChildAt(i3).visibility == View.GONE) {
                i2--
            }
        }
        if (i2 < 2) {
            return false
        }
        if (i2 >= 3) {
            return true
        }
        val i4 = (i - mButtonMarginHorizontal) / 2
        for (i5 in 0 until childCount) {
            val child = getChildAt(i5)
            if (child is TextView && child.visibility == View.VISIBLE && isEllipsized(child, i4)) {
                return true
            }
        }
        return false
    }

    private fun isEllipsized(textView: TextView, i: Int): Boolean {
        return textView.paint.measureText(textView.text.toString()).toInt() > i - textView.paddingStart - textView.paddingEnd
    }

    fun setForceVertical(enabled: Boolean) {
        mForceVertical = enabled
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig == null) return
        val i = mCurrentDensityDpi
        mLastDensityDpi = i
        val i2 = newConfig.densityDpi
        if (i != i2) {
            mCurrentDensityDpi = i2
            val f = i2 * 1.0f / i
            mPanelPaddingHorizontal = (mPanelPaddingHorizontal * f).toInt()
            mButtonMarginHorizontal = (mButtonMarginHorizontal * f).toInt()
            mButtonMarginVertical = (mButtonMarginVertical * f).toInt()
            mButtonHeight = (mButtonHeight * f).toInt()
            val childCount = childCount
            for (i3 in 0 until childCount) {
                val child = getChildAt(i3)
                if (child is TextView) {
                    DensityChangedHelper.updateTextSizeSpUnit(child, mButtonTextSize)
                }
            }
        }
    }
}