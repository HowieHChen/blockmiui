package dev.lackluster.hyperx.theme

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.util.StateSet
import cn.fkj233.ui.R
import org.xmlpull.v1.XmlPullParser

class ActionIconDrawable : VectorDrawable() {
    companion object {
        private val STATE_DISABLED = intArrayOf(-android.R.attr.state_enabled)
        private val STATE_PRESSED = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed)
    }
    private var mActionIconHeight = 0
    private var mActionIconWidth = 0
    private var mNormalAlpha = 0.8f
    private var mPressedAlpha = 0.5f
    private var mDisabledAlpha = 0.3f

    override fun getConstantState(): ConstantState? {
        return null
    }
    override fun isStateful(): Boolean {
        return true
    }
    override fun inflate(
        r: Resources,
        parser: XmlPullParser,
        attrs: AttributeSet,
        theme: Resources.Theme?
    ) {
        init(r, attrs, theme)
        super.inflate(r, parser, attrs, theme)
    }
    private fun init(r: Resources, attrs: AttributeSet, theme: Resources.Theme?) {
        val obtainAttributes = theme?.obtainStyledAttributes(attrs, R.styleable.ActionIconDrawable, 0, 0) ?: r.obtainAttributes(attrs, R.styleable.ActionIconDrawable)
        mNormalAlpha = obtainAttributes.getFloat(R.styleable.ActionIconDrawable_actionIconNormalAlpha, 0.0f)
        mPressedAlpha = obtainAttributes.getFloat(R.styleable.ActionIconDrawable_actionIconPressedAlpha, 0.0f)
        mDisabledAlpha = obtainAttributes.getFloat(R.styleable.ActionIconDrawable_actionIconDisabledAlpha, 0.0f)
        mActionIconWidth = obtainAttributes.getDimensionPixelSize(R.styleable.ActionIconDrawable_actionIconWidth, 0)
        mActionIconHeight = obtainAttributes.getDimensionPixelSize(R.styleable.ActionIconDrawable_actionIconHeight, 0)
        obtainAttributes.recycle()
        setAlphaF(mNormalAlpha)
    }
    override fun getIntrinsicHeight(): Int {
        val width = mActionIconWidth
        return if (width == 0) super.getIntrinsicHeight() else width
    }
    override fun getIntrinsicWidth(): Int {
        val height = mActionIconHeight
        return if (height == 0) super.getIntrinsicHeight() else height
    }
    override fun draw(canvas: Canvas) {
        if (mActionIconWidth != 0 && mActionIconHeight != 0) {
            canvas.translate(
                (mActionIconWidth - super.getIntrinsicWidth() shr 1).toFloat(),
                (mActionIconHeight - super.getIntrinsicHeight() shr 1).toFloat()
            )
            canvas.scale(
                super.getIntrinsicWidth() / mActionIconWidth.toFloat(),
                super.getIntrinsicHeight() / mActionIconHeight.toFloat(),
                0.5f,
                0.5f
            )
        }
        super.draw(canvas)
    }
    override fun onStateChange(stateSet: IntArray): Boolean {
        super.onStateChange(stateSet)
        if (StateSet.stateSetMatches(STATE_DISABLED, stateSet)) {
            return toDisabledState()
        }
        if (StateSet.stateSetMatches(STATE_PRESSED, stateSet)) {
            return toPressedState()
        }
        return toNormalState()
    }
    private fun toDisabledState(): Boolean {
        setAlphaF(mDisabledAlpha)
        return true
    }
    private fun toPressedState(): Boolean {
        setAlphaF(mPressedAlpha)
        return true
    }
    private fun toNormalState(): Boolean {
        setAlphaF(mNormalAlpha)
        return true
    }
    private fun setAlphaF(f: Float) {
        alpha = (f * 255.0f).toInt()
    }
}