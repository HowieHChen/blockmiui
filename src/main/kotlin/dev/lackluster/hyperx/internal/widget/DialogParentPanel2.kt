package dev.lackluster.hyperx.internal.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import cn.fkj233.ui.R
import dev.lackluster.hyperx.core.util.HyperXUIUtils
import dev.lackluster.hyperx.core.util.WindowUtils
import dev.lackluster.hyperx.internal.util.AttributeResolver
import dev.lackluster.hyperx.smooth.SmoothCornerHelper
import kotlin.math.min


class DialogParentPanel2: LinearLayout {
    private val mClipPath: Path = Path()
    private var mDensityDpi: Int
    private val mFloatingWindowSize: FloatingABOLayoutSpec
    private val mLayer: RectF = RectF()
    private var mRadius: Float = 0.0f

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        setSmoothCornerEnable(true)
        val resources = resources
        setCornerRadius(resources.getDimension(R.dimen.hyperx_dialog_bg_corner_radius))
        mDensityDpi = resources.displayMetrics.densityDpi
        mFloatingWindowSize = FloatingABOLayoutSpec(context, attributeSet)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val newDpi = newConfig?.densityDpi ?: return
        if (newDpi != mDensityDpi) {
            mDensityDpi = newDpi
            setCornerRadius(resources.getDimension(R.dimen.hyperx_dialog_bg_corner_radius))
        }
        notifyConfigurationChanged()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        notifyConfigurationChanged()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mLayer.set(0.0f, 0.0f, w.toFloat(), h.toFloat())
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        clipRoundRect(canvas)
        super.draw(canvas)
        canvas.restoreToCount(save)
    }
    private fun setCornerRadius(radius: Float) {
        mRadius = radius.coerceAtLeast(0.0f)
        refresh()
    }

    private fun refresh() {
        invalidateOutline()
        invalidate()
    }

    private fun setSmoothCornerEnable(enabled: Boolean) {
        SmoothCornerHelper.setViewSmoothCornerEnable(this, enabled)
    }

    private fun clipRoundRect(canvas: Canvas) {
        mClipPath.reset()
        val path = mClipPath
        val rectF = mLayer
        val radius = mRadius
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
        canvas.clipPath(mClipPath)
    }

    private fun notifyConfigurationChanged() {
        mFloatingWindowSize.flushWindowSizeIfNeed(mFloatingWindowSize.getScreenHeightDp())
    }

    inner class FloatingABOLayoutSpec(context: Context, attributeSet: AttributeSet?) {
        private val mContext: Context
        private var mFixedHeightMajor: TypedValue? = null
        private var mFixedHeightMinor: TypedValue? = null
        private var mFixedWidthMajor: TypedValue? = null
        private var mFixedWidthMinor: TypedValue? = null
        private var mIsFreeWindowMode = false
        private var mMaxHeightMajor: TypedValue? = null
        private var mMaxHeightMinor: TypedValue? = null
        private var mMaxWidthMajor: TypedValue? = null
        private var mMaxWidthMinor: TypedValue? = null
        private var mScreenHeightDp = 0
        private var mScreenSize = Point()

        init {
            mContext = context
            parseWindowSize(context, attributeSet)
            mScreenHeightDp = getScreenHeightDp()
            mIsFreeWindowMode = HyperXUIUtils.isFreeformMode(context)
        }

        private fun parseWindowSize(context: Context, attributeSet: AttributeSet?) {
            if (attributeSet == null) return
            val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.Window)
            if (obtainStyledAttributes.hasValue(R.styleable.Window_windowFixedHeightMajor)) {
                val typedValue = TypedValue()
                mFixedHeightMajor = typedValue
                obtainStyledAttributes.getValue(R.styleable.Window_windowFixedHeightMajor, typedValue)
            }
            if (obtainStyledAttributes.hasValue(R.styleable.Window_windowFixedHeightMinor)) {
                val typedValue = TypedValue()
                mFixedHeightMinor = typedValue
                obtainStyledAttributes.getValue(R.styleable.Window_windowFixedHeightMinor, typedValue)
            }
            if (obtainStyledAttributes.hasValue(R.styleable.Window_windowFixedWidthMajor)) {
                val typedValue = TypedValue()
                mFixedWidthMajor = typedValue
                obtainStyledAttributes.getValue(R.styleable.Window_windowFixedWidthMajor, typedValue)
            }
            if (obtainStyledAttributes.hasValue(R.styleable.Window_windowFixedWidthMinor)) {
                val typedValue = TypedValue()
                mFixedWidthMinor = typedValue
                obtainStyledAttributes.getValue(R.styleable.Window_windowFixedWidthMinor, typedValue)
            }
            if (obtainStyledAttributes.hasValue(R.styleable.Window_windowMaxHeightMajor)) {
                val typedValue = TypedValue()
                mMaxHeightMajor = typedValue
                obtainStyledAttributes.getValue(R.styleable.Window_windowMaxHeightMajor, typedValue)
            }
            if (obtainStyledAttributes.hasValue(R.styleable.Window_windowMaxHeightMinor)) {
                val typedValue = TypedValue()
                mMaxHeightMinor = typedValue
                obtainStyledAttributes.getValue(R.styleable.Window_windowMaxHeightMinor, typedValue)
            }
            if (obtainStyledAttributes.hasValue(R.styleable.Window_windowMaxWidthMajor)) {
                val typedValue = TypedValue()
                mMaxWidthMajor = typedValue
                obtainStyledAttributes.getValue(R.styleable.Window_windowMaxWidthMajor, typedValue)
            }
            if (obtainStyledAttributes.hasValue(R.styleable.Window_windowMaxWidthMinor)) {
                val typedValue = TypedValue()
                mMaxWidthMinor = typedValue
                obtainStyledAttributes.getValue(R.styleable.Window_windowMaxWidthMinor, typedValue)
            }
            obtainStyledAttributes.recycle()
        }

        fun getWidthMeasureSpecForDialog(measureSpec: Int): Int {
            return getMeasureSpec(measureSpec, true, mFixedWidthMinor, mFixedWidthMajor, mMaxWidthMinor, mMaxWidthMajor)
        }

        fun getHeightMeasureSpecForDialog(measureSpec: Int): Int {
            return getMeasureSpec(measureSpec, false, mFixedHeightMinor, mFixedHeightMajor, mMaxHeightMinor, mMaxHeightMajor)
        }

        private fun getMeasureSpec(measureSpec: Int, z: Boolean, typedValue: TypedValue?, typedValue2: TypedValue?, typedValue3: TypedValue?, typedValue4: TypedValue?): Int {
            if (MeasureSpec.getMode(measureSpec) != MeasureSpec.AT_MOST) {
                return measureSpec
            }
            if (!z && mIsFreeWindowMode) {
                return measureSpec
            }
            val useMinor = useMinor()
            val typed = if (!useMinor) {
                typedValue2
            } else {
                typedValue
            }
            val resolveDimension = resolveDimension(typed, z)
            if (resolveDimension > 0) {
                return MeasureSpec.makeMeasureSpec(resolveDimension, MeasureSpec.EXACTLY)
            }
            val typed2 = if (!useMinor) {
                typedValue4
            } else {
                typedValue3
            }
            val resolveDimension2 = resolveDimension(typed2, z)
            if (resolveDimension2 > 0) {
                return MeasureSpec.makeMeasureSpec(min(resolveDimension2, MeasureSpec.getSize(measureSpec)), MeasureSpec.AT_MOST)
            }
            return measureSpec
        }

        private fun useMinor(): Boolean {
            return isPortrait() || mScreenHeightDp >= 500
        }

        private fun isPortrait(): Boolean {
            return mContext.resources.configuration.orientation == 1
        }

        private fun resolveDimension(typedValue: TypedValue?, z: Boolean): Int {
            val i: Int = typedValue?.type ?: return 0
            var fraction: Float = 0.0f
            if (i != 0) {
                if (i == TypedValue.TYPE_DIMENSION) {
                    fraction = typedValue.getDimension(mContext.resources.displayMetrics)
                } else if (i == TypedValue.TYPE_FRACTION) {
                    val point = mScreenSize
                    val f = (if (z) point.x else point.y).toFloat()
                    fraction = typedValue.getFraction(f, f)
                }
                return fraction.toInt()
            }
            return 0
        }

        fun flushWindowSizeIfNeed(height: Int) {
            if (mScreenHeightDp != height) {
                mFixedHeightMajor = AttributeResolver.resolveTypedValue(mContext, R.attr.windowFixedHeightMajor)
                mFixedHeightMinor = AttributeResolver.resolveTypedValue(mContext, R.attr.windowFixedHeightMinor)
                mFixedWidthMajor = AttributeResolver.resolveTypedValue(mContext, R.attr.windowFixedWidthMajor)
                mFixedWidthMinor = AttributeResolver.resolveTypedValue(mContext, R.attr.windowFixedWidthMinor)
                mMaxHeightMajor = AttributeResolver.resolveTypedValue(mContext, R.attr.windowMaxHeightMajor)
                mMaxHeightMinor = AttributeResolver.resolveTypedValue(mContext, R.attr.windowMaxHeightMinor)
                mMaxWidthMajor = AttributeResolver.resolveTypedValue(mContext, R.attr.windowMaxWidthMajor)
                mMaxWidthMinor = AttributeResolver.resolveTypedValue(mContext, R.attr.windowMaxWidthMinor)
                mScreenHeightDp = height
            }
        }

        fun getScreenHeightDp(): Int {
            WindowUtils.getScreenSize(mContext, mScreenSize)
            return (mScreenSize.y / mContext.resources.displayMetrics.density).toInt()
        }
    }
}