package dev.lackluster.hyperx.widget.internal

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.StateSet
import android.view.View
import cn.fkj233.ui.R
import org.xmlpull.v1.XmlPullParser


class FilterSortTabView2ForegroundDrawable : Drawable {
    private var mActivated = false
    private var mActivatedAlpha = 0f
    private var mHovered = false
    private var mHoveredActivatedAlpha = 0f
    private var mHoveredAlpha = 0f
    private var mNormalAlpha = 0f
    private var mPressed = false
    private var mPressedAlpha = 0f
    private var mRadius = 0
    private val mRect = RectF()
    private val mPath: Path = Path()
    private val mPaint = Paint()
    companion object {
        private var mIsCommonLiteStrategy: Boolean = false
        private val TAG = "StateTransitionDrawable"
        private val USE_SMOOTH_ROUND_RECT = true
        private val STATE_PRESSED = intArrayOf(16842919)
        private val STATE_DRAG_HOVERED = intArrayOf(16843625)
        private val STATE_HOVERED_ACTIVATED = intArrayOf(16843623, 16843518)
        private val STATE_HOVERED = intArrayOf(16843623)
        private val STATE_ACTIVATED = intArrayOf(16843518)
    }
    constructor()
    constructor(view: View) {
        init(view.resources, null, null)
    }
    override fun draw(canvas: Canvas) {
        if (USE_SMOOTH_ROUND_RECT) {
            canvas.drawPath(mPath, mPaint)
            return
        }
        val rectF = mRect
        val i = mRadius
        canvas.drawRoundRect(rectF, i.toFloat(), i.toFloat(), mPaint)
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

    fun setAlphaF(f: Float) {
        mPaint.alpha = (f * 255.0f).toInt()
        invalidateSelf()
    }

    fun getAlphaF(): Float {
        return mPaint.alpha / 255.0f
    }

    fun setRadius(i: Int) {
        if (mRadius == i) {
            return
        }
        mRadius = i
        invalidateSelf()
    }

    override fun onStateChange(state: IntArray): Boolean {
        if (StateSet.stateSetMatches(STATE_PRESSED, state) || StateSet.stateSetMatches(STATE_DRAG_HOVERED, state)) {
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

    override fun inflate(
        r: Resources,
        parser: XmlPullParser,
        attrs: AttributeSet,
        theme: Resources.Theme?
    ) {
        super.inflate(r, parser, attrs, theme)
        init(r, attrs, theme)
    }

    private fun init(resources: Resources, attributeSet: AttributeSet?, theme: Theme?) {
        val obtainAttributes: TypedArray =
            theme?.obtainStyledAttributes(attributeSet, R.styleable.StateTransitionDrawable, 0, 0)
                ?: resources.obtainAttributes(attributeSet, R.styleable.StateTransitionDrawable)
        val color = obtainAttributes.getColor(R.styleable.StateTransitionDrawable_tintColor, -16777216)
        mRadius = obtainAttributes.getDimensionPixelSize(R.styleable.StateTransitionDrawable_tintRadius, 0)
        mNormalAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_normalAlpha, 0.0f)
        mPressedAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_pressedAlpha, 0.0f)
        mHoveredAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_hoveredAlpha, 0.0f)
        mActivatedAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_activatedAlpha, 0.0f)
        mHoveredActivatedAlpha = obtainAttributes.getFloat(R.styleable.StateTransitionDrawable_hoveredActivatedAlpha, 0.0f)
        obtainAttributes.recycle()
        mPaint.color = color
        setAlphaF(mNormalAlpha)
        if (USE_SMOOTH_ROUND_RECT) {
            setSmoothCornerEnable(true)
        }
    }

    private fun toPressedState(): Boolean {
        if (mPressed) {
            return false
        }
        setAlphaF(mPressedAlpha)
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
        setAlphaF(mActivatedAlpha)
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

    override fun jumpToCurrentState() {
    }

    override fun onBoundsChange(bounds: Rect) {
        mRect.set(bounds)
        calculatePath()
    }

    private fun calculatePath() {
        mPath.reset()
        val path = mPath
        val rectF = mRect
        val i = mRadius
        path.addRoundRect(rectF, i.toFloat(), i.toFloat(), Path.Direction.CW)
    }

    @SuppressLint("LongLogTag")
    private fun setSmoothCornerEnable(z: Boolean) {
        try {
            Drawable::class.java.getDeclaredMethod("setSmoothCornerEnabled", Boolean::class.java).invoke(this, z)
        } catch (e: Throwable) {
            Log.e(TAG, "setSmoothCornerEnabled failed:" + e.message)
        }
    }
}