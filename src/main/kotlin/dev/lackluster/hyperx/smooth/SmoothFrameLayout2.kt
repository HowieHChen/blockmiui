package dev.lackluster.hyperx.smooth

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import cn.fkj233.ui.R

class SmoothFrameLayout2 : FrameLayout {
    private var mClip: Boolean
    private val mClipPath: Path
    private val mLayer: RectF
    private val mPaintSolid: Paint
    private val mPaintStroke: Paint
    private var mRadii: FloatArray? = null
    private var mRadius: Float
    private var mStrokeColor: Int
    private var mStrokeWidth: Int
    private var mTempRadii: FloatArray? = null
    private var mUseSmooth: Boolean
    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        mLayer = RectF()
        mClipPath = Path()
        mClip = false
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.HyperXSmoothFrameLayout2)
        mRadius = obtainStyledAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothFrameLayout2_android_radius, 0).toFloat()
        if (
            obtainStyledAttributes.hasValue(R.styleable.HyperXSmoothFrameLayout2_android_topLeftRadius) ||
            obtainStyledAttributes.hasValue(R.styleable.HyperXSmoothFrameLayout2_android_topRightRadius) ||
            obtainStyledAttributes.hasValue(R.styleable.HyperXSmoothFrameLayout2_android_bottomLeftRadius) ||
            obtainStyledAttributes.hasValue(R.styleable.HyperXSmoothFrameLayout2_android_bottomRightRadius)
        ) {
            val topLeft = obtainStyledAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothFrameLayout2_android_topLeftRadius, 0).toFloat()
            val topRight = obtainStyledAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothFrameLayout2_android_topRightRadius, 0).toFloat()
            val bottomLeft = obtainStyledAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothFrameLayout2_android_bottomLeftRadius, 0).toFloat()
            val bottomRight = obtainStyledAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothFrameLayout2_android_bottomRightRadius, 0).toFloat()
            setCornerRadii(floatArrayOf(topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft))
        }
        mStrokeWidth = obtainStyledAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothFrameLayout2_hyperx_strokeWidth, 0)
        mStrokeColor = obtainStyledAttributes.getColor(R.styleable.HyperXSmoothFrameLayout2_hyperx_strokeColor, 0)
        mUseSmooth = obtainStyledAttributes.getBoolean(R.styleable.HyperXSmoothFrameLayout2_hyperx_useSmooth, true)
        if (SmoothCornerHelper.FORCE_USE_SMOOTH != null) {
            mUseSmooth = SmoothCornerHelper.FORCE_USE_SMOOTH
        }
        if (mUseSmooth) {
            setSmoothCornerEnable(true)
        }
        obtainStyledAttributes.recycle()
        mPaintSolid = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintStroke.style = Paint.Style.STROKE
        mPaintStroke.strokeWidth = mStrokeWidth.toFloat()
        mPaintStroke.color = mStrokeColor
    }

    fun setStrokeWidth(width: Int) {
        mStrokeWidth = width
        updateBackground()
    }
    fun setUseSmooth(use: Boolean) {
        mUseSmooth = use
        setSmoothCornerEnable(use)
    }
    fun getUseSmooth(): Boolean {
        return mUseSmooth
    }
    fun setSmoothCornerEnable(enable: Boolean) {
        SmoothCornerHelper.setViewSmoothCornerEnable(this, enable)
    }
    fun getStrokeWidth(): Int {
        return mStrokeWidth
    }
    fun setStrokeColor(color: Int) {
        mStrokeColor = color
        updateBackground()
    }
    fun getStrokeColor(): Int {
        return mStrokeColor
    }
    fun setCornerRadii(array: FloatArray) {
        mRadii = array
        updateBackground()
    }
    fun getCornerRadii(): FloatArray {
        return mRadii?.clone() ?: FloatArray(0)
    }
    fun setCornerRadius(radius: Float) {
        mRadius = radius.coerceAtLeast(0.0f)
        mRadii = null
        updateBackground()
    }
    fun getCornerRadius(): Float {
        return mRadius
    }
    private fun updateBackground() {
        invalidateOutline()
        invalidate()
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mLayer.set(0.0f, 0.0f, w.toFloat(), h.toFloat())
    }
    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        clipRoundRect(canvas)
        mClip = true
        if (mStrokeWidth > 0) {
            val save2 = canvas.save()
            clipInnerRoundRect(canvas)
            super.draw(canvas)
            canvas.restoreToCount(save2)
        } else {
            super.draw(canvas)
        }
        if (mStrokeWidth > 0) {
            drawRoundRectStroke(canvas)
        }
        mClip = false
        canvas.restoreToCount(save)
    }
    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        if (!mClip) {
            clipRoundRect(canvas)
        }
        if (mStrokeWidth > 0) {
            val save2 = canvas.save()
            clipInnerRoundRect(canvas)
            super.dispatchDraw(canvas)
            canvas.restoreToCount(save2)
        } else {
            super.dispatchDraw(canvas)
        }
        if (!mClip && mStrokeWidth > 0) {
            drawRoundRectStroke(canvas)
        }
        canvas.restoreToCount(save)
    }
    private fun clipRoundRect(canvas: Canvas) {
        mClipPath.reset()
        val fArr = mRadii
        if (fArr == null) {
            val path = mClipPath
            val rectF = mLayer
            val f = mRadius
            path.addRoundRect(rectF, f, f, Path.Direction.CW)
        } else {
            mClipPath.addRoundRect(mLayer, fArr, Path.Direction.CW)
        }
        canvas.clipPath(mClipPath)
    }
    private fun clipInnerRoundRect(canvas: Canvas) {
        mClipPath.reset()
        val f = mStrokeWidth * 0.5f
        val fArr = mRadii
        if (fArr == null) {
            val path = mClipPath
            val rectF = mLayer
            val f2 = rectF.left + f
            val f3 = rectF.top + f
            val f4 = rectF.right - f
            val f5 = rectF.bottom - f
            val f6 = mRadius
            path.addRoundRect(f2, f3, f4, f5, f6 + f, f6 + f, Path.Direction.CW)
        } else {
            val fArr2 = fArr.clone()
            mTempRadii = fArr2
            fArr2[0] = fArr[0] + f
            fArr2[1] = fArr[1] + f
            fArr2[2] = fArr[2] + f
            fArr2[3] = fArr[3] + f
            val path2 = mClipPath
            val rectF2 = mLayer
            path2.addRoundRect(rectF2.left + f, rectF2.top + f, rectF2.right - f, rectF2.bottom - f, fArr2, Path.Direction.CW)
        }
        canvas.clipPath(mClipPath)
    }
    private fun drawRoundRectStroke(canvas: Canvas) {
        mClipPath.reset()
        val f = mStrokeWidth * 0.5f
        val fArr = mRadii
        if (fArr == null) {
            val path = mClipPath
            val rectF = mLayer
            val f2 = rectF.left + f
            val f3 = rectF.top + f
            val f4 = rectF.right - f
            val f5 = rectF.bottom - f
            val f6 = mRadius
            path.addRoundRect(f2, f3, f4, f5, f6 + f, f6 + f, Path.Direction.CW)
        } else {
            val fArr2 = fArr.clone()
            mTempRadii = fArr2
            fArr2[0] = fArr[0] + f
            fArr2[1] = fArr[1] + f
            fArr2[2] = fArr[2] + f
            fArr2[3] = fArr[3] + f
            val path2 = mClipPath
            val rectF2 = mLayer
            path2.addRoundRect(rectF2.left + f, rectF2.top + f, rectF2.right - f, rectF2.bottom - f, fArr2, Path.Direction.CW)
        }
        canvas.drawPath(mClipPath, mPaintStroke)
    }
}