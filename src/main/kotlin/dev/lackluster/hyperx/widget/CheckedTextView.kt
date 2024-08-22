package dev.lackluster.hyperx.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.CheckedTextView
import cn.fkj233.ui.R


class CheckedTextView : CheckedTextView {
    private companion object val CHECKED_STATE_SET = intArrayOf(16842912)
    private var mCheckMarkDrawable: Drawable? = null
    private var mCheckMarkMarginToText: Int = 0
    private var mDrawCheckMark: Boolean = false
    private var mDrawTextOffsetInRtl: Boolean = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        mDrawCheckMark = true
        mDrawTextOffsetInRtl = false
        mCheckMarkMarginToText = context.resources.getDimension(R.dimen.hyperx_checked_text_view_addition_margin).toInt()
    }

    override fun getCheckMarkDrawable(): Drawable? {
        return mCheckMarkDrawable
    }

    override fun setCheckMarkDrawable(d: Drawable?) {
        mCheckMarkDrawable?.let {
            it.callback = null
            unscheduleDrawable(it)
        }
        d?.let {
            it.callback = this
            it.setVisible(visibility == View.VISIBLE, false)
            it.setState(CHECKED_STATE_SET)
            minHeight = it.intrinsicHeight
            it.setState(drawableState)
        }
        mCheckMarkDrawable = d
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mCheckMarkMarginToText = context.resources.getDimension(R.dimen.hyperx_checked_text_view_addition_margin).toInt()
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        var checkWidth = getCheckWidth()
        if (checkWidth > 0) {
            if (TextUtils.isEmpty(text)) {
                this.mDrawCheckMark = true
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                setMeasuredDimension(checkWidth, measuredHeight)
                return
            } else if (size - paddingEnd < checkWidth * 2) {
                checkWidth = 0
                mDrawCheckMark = false
            } else {
                mDrawCheckMark = true
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (checkWidth == 0) {
            return
        }
        setMeasuredDimension(measuredWidth + checkWidth, measuredHeight)
    }

    private fun getCheckWidth(): Int {
        return checkMarkDrawable?.current?.intrinsicWidth ?: 0
    }

    override fun onDraw(canvas: Canvas) {
        val isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
        if (mDrawCheckMark) {
            drawCheckMark(canvas, isRtl)
        }
        mDrawTextOffsetInRtl = checkMarkDrawable?.javaClass?.isAssignableFrom(StateListDrawable::class.java) ?: false
        if (isRtl && mDrawCheckMark && mDrawTextOffsetInRtl) {
            canvas.save()
            canvas.translate(getCheckWidth().toFloat(), 0.0f)
        }
        super.onDraw(canvas)
        if (isRtl && mDrawCheckMark && mDrawTextOffsetInRtl) {
            canvas.restore()
        }
    }

    private fun drawCheckMark(canvas: Canvas?, isRtl: Boolean) {
        val width: Int
        val scrollX: Int
        val checkMarkDrawable = checkMarkDrawable
        if (checkMarkDrawable != null) {
            val intrinsicWidth = checkMarkDrawable.current.intrinsicWidth
            if (isRtl) {
                width = paddingLeft
                scrollX = getScrollX()
            } else {
                width = getWidth() - paddingRight - intrinsicWidth
                scrollX = getScrollX()
            }
            val i = width + scrollX
            var paddingTop = paddingTop
            val paddingTop2 = getPaddingTop()
            val paddingBottom = paddingBottom
            var intrinsicHeight = checkMarkDrawable.intrinsicHeight
            if (checkMarkDrawable.current is NinePatchDrawable) {
                intrinsicHeight = height - paddingTop - getPaddingBottom()
            } else {
                val gravity = gravity and 112
                if (gravity == 16) {
                    paddingTop =
                        getCheckMarkPositionY(height, intrinsicHeight, paddingTop2, paddingBottom)
                } else if (gravity == 80) {
                    paddingTop = height - intrinsicHeight
                }
            }
            checkMarkDrawable.setBounds(
                i,
                paddingTop,
                intrinsicWidth + i,
                intrinsicHeight + paddingTop
            )
            checkMarkDrawable.draw(canvas!!)
        }
    }

    private fun getCheckMarkPositionY(i: Int, i2: Int, i3: Int, i4: Int): Int {
        return (i - i3 - i4 - i2) / 2 + i3
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val onCreateDrawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            mergeDrawableStates(onCreateDrawableState, CHECKED_STATE_SET)
        }
        return onCreateDrawableState
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        mCheckMarkDrawable?.let {
            it.setState(drawableState)
            invalidate()
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == mCheckMarkDrawable
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        mCheckMarkDrawable?.jumpToCurrentState()
    }
}