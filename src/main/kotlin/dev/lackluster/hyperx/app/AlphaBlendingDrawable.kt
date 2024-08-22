package dev.lackluster.hyperx.app

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.StateSet
import android.view.View
import cn.fkj233.ui.R
import org.xmlpull.v1.XmlPullParser

open class AlphaBlendingDrawable : Drawable {
    private var mRadius: Int = 0
    private var mNormalAlpha: Float = 0.0f
    private var mPressedAlpha: Float = 0.0f
    private var mHoveredAlpha: Float = 0.0f
    private var mActivatedAlpha: Float = 0.0f
    private var mHoveredActivatedAlpha: Float = 0.0f
    private var mInsetL = 0
    private var mInsetT = 0
    private var mInsetR = 0
    private var mInsetB = 0
    private val mPaint: Paint = Paint()
    private val mRect: RectF = RectF()
    private var mPressed: Boolean = false
    private var mHovered: Boolean = false
    private var mActivated: Boolean = false
    private val STATE_PRESSED = intArrayOf(16842919)
    private val STATE_DRAG_HOVERED = intArrayOf(16843625)
    private val STATE_SELECTED = intArrayOf(16842913)
    private val STATE_HOVERED_ACTIVATED = intArrayOf(16843623, 16843518)
    private val STATE_HOVERED = intArrayOf(16843623)
    private val STATE_ACTIVATED = intArrayOf(16843518)
    constructor()
    constructor(view: View) {
        init(view.resources, null, null)
    }
    private fun init(resources: Resources, attributeSet: AttributeSet?, theme: Theme?) {
        val obtainAttributes =
            theme?.obtainStyledAttributes(attributeSet, R.styleable.StateTransitionDrawable, 0, 0) ?:
            resources.obtainAttributes(attributeSet, R.styleable.StateTransitionDrawable)
        val color = obtainAttributes.getColor(R.styleable.StateTransitionDrawable_tintColor, Color.BLACK)
        mRadius = obtainAttributes.getDimensionPixelSize(R.styleable.StateTransitionDrawable_tintRadius, 0)
        mNormalAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_normalAlpha, 0.0f)
        mPressedAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_pressedAlpha, 0.0f)
        mHoveredAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_hoveredAlpha, 0.0f)
        mActivatedAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_activatedAlpha, 0.0f)
        mHoveredActivatedAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_hoveredActivatedAlpha, 0.0f)
        obtainAttributes.recycle()
        mPaint.color = color
        setAlphaF(mNormalAlpha)
    }

    fun setAlphaF(f: Float) {
        mPaint.alpha = (f * 255.0f).toInt()
        invalidateSelf()
    }
    fun getAlphaF(): Float {
        return (mPaint.alpha / 255.0f)
    }
    fun setRadius(radius: Int) {
        if (mRadius == radius) {
            return
        }
        mRadius = radius
        invalidateSelf()
    }
    fun setInset(l: Int, t: Int, r: Int, b: Int) {
        mInsetL = l
        mInsetT = t
        mInsetR = r
        mInsetB = b
    }
    override fun draw(p0: Canvas) {
        if (isVisible) {
            p0.drawRoundRect(mRect, mRadius.toFloat(), mRadius.toFloat(), mPaint)
        }
    }

    override fun setAlpha(p0: Int) {
    }

    override fun setColorFilter(p0: ColorFilter?) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    override fun isStateful(): Boolean {
        return true
    }

    override fun onStateChange(state: IntArray): Boolean {
        if (StateSet.stateSetMatches(STATE_PRESSED, state) || StateSet.stateSetMatches(STATE_DRAG_HOVERED, state) || StateSet.stateSetMatches(STATE_SELECTED, state)) {
            return toPressedState()
        }
        if (StateSet.stateSetMatches(STATE_HOVERED_ACTIVATED, state)) {
            return toHoveredActivatedState()
        }
        if (StateSet.stateSetMatches(STATE_HOVERED, state)) {
            return toHoveredState()
        }
        if (StateSet.stateSetMatches(STATE_ACTIVATED, state)) {
            return toActivatedState()
        }
        return toNormalState()
    }

    override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Theme?) {
        super.inflate(r, parser, attrs, theme)
        init(r, attrs, theme)
    }

    override fun onBoundsChange(bounds: Rect) {
        mRect.set(bounds)
        mRect.left += mInsetL
        mRect.top += mInsetT
        mRect.right += mInsetR
        mRect.bottom += mInsetB
    }

    private fun toPressedState(): Boolean {
        if (mPressed) {
            return false
        }
        setAlphaF(this.mPressedAlpha)
        mPressed = true
        mHovered = false
        mActivated = false
        return true
    }
    private fun toHoveredActivatedState(): Boolean {
        if (mPressed) {
            mPressed = false
            mHovered = true
            mActivated = true
            setAlphaF(mHoveredActivatedAlpha)
            return true
        }
        val hovered = mHovered
        if (hovered && mActivated) {
            return false
        }
        if (hovered) {
            mActivated = true
            setAlphaF(mHoveredActivatedAlpha)
            return true
        }
        if (mActivated) {
            mHovered = true
            setAlphaF(mHoveredActivatedAlpha)
            return true
        }
        mActivated = true
        mHovered = true
        setAlphaF(mHoveredActivatedAlpha)
        return true
    }
    private fun toHoveredState(): Boolean {
        if (mPressed) {
            mPressed = false
            mHovered = true
            mActivated = false
            setAlphaF(mHoveredAlpha)
            return true
        }
        if (mHovered) {
            if (!mActivated) {
                return false
            }
            setAlphaF(mHoveredAlpha)
            return true
        }
        mHovered = true
        mActivated = false
        setAlphaF(mHoveredAlpha)
        return true
    }
    private fun toActivatedState(): Boolean {
        if (mPressed) {
            mPressed = false
            mHovered = false
            mActivated = true
            setAlphaF(mActivatedAlpha)
            return true
        }
        if (mHovered) {
            mHovered = false
            mActivated = true
            setAlphaF(mActivatedAlpha)
            return true
        }
        if (mActivated) {
            return false
        }
        mActivated = true
        setAlphaF(this.mActivatedAlpha)
        return true
    }
    private fun toNormalState(): Boolean {
        if (mPressed) {
            mPressed = false
            mHovered = false
            mActivated = false
            setAlphaF(mNormalAlpha)
            return true
        }
        if (mHovered) {
            mHovered = false
            mActivated = false
            setAlphaF(mNormalAlpha)
            return true
        }
        if (!mActivated) {
            return false
        }
        mActivated = false
        setAlphaF(mNormalAlpha)
        return true
    }


}