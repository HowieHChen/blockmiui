package dev.lackluster.hyperx.internal.app.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageView
import cn.fkj233.ui.R
import dev.lackluster.hyperx.internal.util.ViewUtils
import kotlin.math.max
import kotlin.math.min


class ActionBarViewHomeView : FrameLayout {
    private var mDefaultUpIndicator: Drawable? = null
    private var mIconView: ImageView? = null
    private var mUpIndicatorRes = 0
    private var mUpView: ImageView? = null

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet)

    fun getStartOffset(): Int {
        return 0
    }
    fun setUp(z: Boolean) {
        mUpView?.visibility = if (z) VISIBLE else GONE
    }
    fun setIcon(drawable: Drawable?) {
        mIconView?.setImageDrawable(drawable)
    }
    fun setUpIndicator(drawable: Drawable?) {
        mUpView?.setImageDrawable(drawable ?: mDefaultUpIndicator)
        mUpIndicatorRes = 0
    }

    fun setUpIndicator(i: Int) {
        mUpIndicatorRes = i
        mUpView?.setImageDrawable(
            if (i != 0) context.getDrawable(i)
            else null
        )
    }
    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        val contentDescription = contentDescription
        if (TextUtils.isEmpty(contentDescription)) {
            return true
        }
        event?.text?.add(contentDescription)
        return true
    }
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val i = mUpIndicatorRes
        if (i != 0) {
            setUpIndicator(i)
        }
    }
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val upView = mUpView!!
        val iconView = mIconView!!
        var i = left
        var i3 = right
        val i5: Int
        val i6: Int = (bottom - top) / 2
        val isLayoutRtl: Boolean = ViewUtils.isLayoutRtl(this)
        if (upView.visibility != 8) {
            val layoutParams = upView.layoutParams as LayoutParams
            val measuredHeight = upView.measuredHeight
            val measuredWidth = upView.measuredWidth
            val i7 = i6 - measuredHeight / 2
            ViewUtils.layoutChildView(this, upView, 0, i7, measuredWidth, i7 + measuredHeight)
            i5 = layoutParams.leftMargin + measuredWidth + layoutParams.rightMargin
            if (isLayoutRtl) {
                i3 -= i5
            } else {
                i += i5
            }
        } else {
            i5 = 0
        }
        val layoutParams2 = iconView.layoutParams as LayoutParams
        val measuredHeight2 = iconView.measuredHeight
        val measuredWidth2 = iconView.measuredWidth
        val max = i5 + max(
            layoutParams2.marginStart,
            (i3 - i) / 2 - measuredWidth2 / 2
        )
        val max2 = max(layoutParams2.topMargin, (i6 - measuredHeight2 / 2))
        ViewUtils.layoutChildView(
            this,
            iconView,
            max,
            max2,
            max + measuredWidth2,
            max2 + measuredHeight2
        )
    }
    override fun onFinishInflate() {
        super.onFinishInflate()
        mUpView = findViewById(R.id.up)
        mIconView = findViewById(R.id.home)
        val imageView = mUpView
        if (imageView != null) {
            mDefaultUpIndicator = imageView.drawable
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val upView = mUpView!!
        val iconView = mIconView!!
        measureChildWithMargins(upView, widthMeasureSpec, 0, heightMeasureSpec, 0)
        val layoutParams = upView.layoutParams as LayoutParams
        val measuredWidth =
            if (upView.visibility == GONE) {
                0
            } else {
                layoutParams.leftMargin + upView.measuredWidth + layoutParams.rightMargin
            }
        val measuredHeight = layoutParams.topMargin + upView.measuredHeight + layoutParams.bottomMargin
        measureChildWithMargins(iconView, widthMeasureSpec, measuredWidth, heightMeasureSpec, 0)
        val layoutParams2 = iconView.layoutParams as LayoutParams
        var measuredWidth2 = measuredWidth + if (iconView.visibility != GONE) layoutParams2.leftMargin + iconView.measuredWidth + layoutParams2.rightMargin else 0
        var max = max(
            measuredHeight,
            (layoutParams2.topMargin + iconView.measuredHeight + layoutParams2.bottomMargin)
        )
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val mode2 = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)
        val size2 = MeasureSpec.getSize(heightMeasureSpec)
        if (mode == MeasureSpec.AT_MOST) {
            measuredWidth2 = min(measuredWidth2, size)
        } else if (mode == MeasureSpec.EXACTLY) {
            measuredWidth2 = size
        }
        if (mode2 == MeasureSpec.AT_MOST) {
            max = min(max, size2)
        } else if (mode2 == MeasureSpec.EXACTLY) {
            max = size2
        }
        setMeasuredDimension(measuredWidth2, max)
    }
}