package dev.lackluster.hyperx.internal.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class NestedScrollViewExpander : ViewGroup {
    private var mExpandView: View? = null
    private var mParentHeightMeasureSpec: Int = 0
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    fun setParentHeightMeasureSpec(spec: Int) {
        mParentHeightMeasureSpec = spec
    }

    fun setExpandView(view: View?) {
        mExpandView = view
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var mode = MeasureSpec.getMode(mParentHeightMeasureSpec)
        if (mode == 0) {
            mode = MeasureSpec.AT_MOST
        }
        val size = MeasureSpec.getSize(widthMeasureSpec)
        val childCount = childCount
        var i4: Int = 0
        var i5: Int = 0
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child.visibility != View.GONE && mExpandView != child) {
                val marginLayoutParams = child.layoutParams as MarginLayoutParams
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                i5 += child.measuredHeight + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin
            }
        }
        var size2 = MeasureSpec.getSize(mParentHeightMeasureSpec) - i5
        val view = mExpandView
        if (view != null && view.visibility != View.GONE) {
            if (size2 < view.minimumHeight) {
                size2 = view.minimumHeight
            }
            val marginLayoutParams2 = view.layoutParams as MarginLayoutParams
            measureChildWithMargins(view, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(size2, mode), 0)
            i4 = view.measuredHeight + marginLayoutParams2.topMargin + marginLayoutParams2.bottomMargin
        }
        setMeasuredDimension(size, i4 + i5)
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val childCount = childCount
        var i2 = p2
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val marginLayoutParams = child.layoutParams as MarginLayoutParams
            val measuredWidth = child.measuredWidth
            val measuredHeight = child.measuredHeight
            val i6: Int = (p3 - p1 - measuredWidth) / 2 + p1 + marginLayoutParams.leftMargin - marginLayoutParams.rightMargin
            val i7: Int = marginLayoutParams.topMargin + i2
            child.layout(i6, i7, measuredWidth + i6, measuredHeight + i7)
            i2 += marginLayoutParams.topMargin + measuredHeight + marginLayoutParams.bottomMargin
        }
    }
}