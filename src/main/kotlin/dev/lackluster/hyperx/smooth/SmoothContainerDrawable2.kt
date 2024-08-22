package dev.lackluster.hyperx.smooth

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import cn.fkj233.ui.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


open class SmoothContainerDrawable2 : Drawable, Drawable.Callback {
    companion object {
        fun obtainAttributes(resources: Resources, theme: Theme?, attrs: AttributeSet, arrs: IntArray): TypedArray {
            return theme?.obtainStyledAttributes(attrs, arrs, 0, 0) ?: resources.obtainAttributes(attrs, arrs)
        }
    }
    private val mClipPath: Path
    private val mContainerState: ContainerState
    private val mLayer: RectF
    private var mRadii: FloatArray? = null
    private var mRadius = 0f
    private var mStrokeColor = 0
    private val mStrokePaint: Paint
    private var mStrokeWidth = 0
    private var mTempRadii: FloatArray = FloatArray(0)
    private var mUseSmooth = false
    constructor() {
        mLayer = RectF()
        mClipPath = Path()
        mContainerState = ContainerState()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        mStrokePaint = paint
        paint.style = Paint.Style.STROKE
        mStrokePaint.color = mStrokeColor
        mStrokePaint.strokeWidth = mStrokeWidth.toFloat()
    }
    constructor(resources: Resources?, theme: Theme?, containerState: ContainerState) {
        mLayer = RectF()
        mClipPath = Path()
        mContainerState = ContainerState(containerState, this, resources, theme)
        mStrokeWidth = containerState.mStrokeWidth
        mStrokeColor = containerState.mStrokeColor
        mRadii = containerState.mRadii
        mRadius = containerState.mRadius
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        mStrokePaint = paint
        paint.style = Paint.Style.STROKE
        mStrokePaint.color = mStrokeColor
        mStrokePaint.strokeWidth = mStrokeWidth.toFloat()
    }

    override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Theme?) {
        super.inflate(r, parser, attrs, theme)
        val obtainAttributes = obtainAttributes(r, theme, attrs, R.styleable.HyperXSmoothContainerDrawable2)
        setCornerRadius(obtainAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothContainerDrawable2_android_radius, 0).toFloat())
        if (
            obtainAttributes.hasValue(R.styleable.HyperXSmoothContainerDrawable2_android_topLeftRadius) ||
            obtainAttributes.hasValue(R.styleable.HyperXSmoothContainerDrawable2_android_topRightRadius) ||
            obtainAttributes.hasValue(R.styleable.HyperXSmoothContainerDrawable2_android_bottomLeftRadius) ||
            obtainAttributes.hasValue(R.styleable.HyperXSmoothContainerDrawable2_android_bottomRightRadius)
        ) {
            val topLeft = obtainAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothContainerDrawable2_android_topLeftRadius, 0).toFloat()
            val topRight = obtainAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothContainerDrawable2_android_topRightRadius, 0).toFloat()
            val bottomLeft = obtainAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothContainerDrawable2_android_bottomLeftRadius, 0).toFloat()
            val bottomRight = obtainAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothContainerDrawable2_android_bottomRightRadius, 0).toFloat()
            setCornerRadii(floatArrayOf(topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft))
        }
        setStrokeWidth(obtainAttributes.getDimensionPixelSize(R.styleable.HyperXSmoothContainerDrawable2_hyperx_strokeWidth, 0))
        setStrokeColor(obtainAttributes.getColor(R.styleable.HyperXSmoothContainerDrawable2_hyperx_strokeColor, 0))
        setLayerType(obtainAttributes.getInt(R.styleable.HyperXSmoothContainerDrawable2_android_layerType, 0))
        mUseSmooth = obtainAttributes.getBoolean(R.styleable.HyperXSmoothContainerDrawable2_hyperx_useSmooth, true)
        if (SmoothCornerHelper.FORCE_USE_SMOOTH != null) {
            mUseSmooth = SmoothCornerHelper.FORCE_USE_SMOOTH
        }
        if (mUseSmooth) {
            setSmoothCornerEnable(true)
        }
        obtainAttributes.recycle()
        inflateInnerDrawable(r, parser, attrs, theme)
    }
    @Throws(IOException::class, XmlPullParserException::class)
    private fun inflateInnerDrawable(
        resources: Resources,
        xmlPullParser: XmlPullParser,
        attributeSet: AttributeSet,
        theme: Theme?
    ) {
        var next: Int
        val depth = xmlPullParser.depth + 1
        while (true) {
            val next2 = xmlPullParser.next()
            if (next2 == 1) {
                return
            }
            val depth2 = xmlPullParser.depth
            if (depth2 < depth && next2 == 3) {
                return
            }
            if (next2 == 2 && depth2 <= depth && xmlPullParser.name == "child") {
                do {
                    next = xmlPullParser.next()
                } while (next == 4)
                if (next != 2) {
                    throw XmlPullParserException(xmlPullParser.positionDescription + ": <child> tag requires a 'drawable' attribute or child tag defining a drawable")
                }
                val childDrawableWrapper = ChildDrawableWrapper()
                val createFromXmlInner =
                    createFromXmlInner(resources, xmlPullParser, attributeSet, theme)
                childDrawableWrapper.mDrawable = createFromXmlInner
                createFromXmlInner.callback = this
                mContainerState.mChildDrawableWrapper = childDrawableWrapper
                return
            }
        }
    }
    override fun applyTheme(theme: Theme) {
        super.applyTheme(theme)
        mContainerState.mChildDrawableWrapper!!.mDrawable!!.applyTheme(theme)
    }
    override fun canApplyTheme(): Boolean {
        val containerState = mContainerState
        return containerState.canApplyTheme() || super.canApplyTheme()
    }
    fun setChildDrawable(drawable: Drawable) {
        val childDrawableWrapper = ChildDrawableWrapper()
        childDrawableWrapper.mDrawable = drawable
        drawable.callback = this
        mContainerState.mChildDrawableWrapper = childDrawableWrapper
    }
    fun setStrokeWidth(i: Int) {
        val containerState = mContainerState
        if (containerState.mStrokeWidth != i) {
            containerState.mStrokeWidth = i
            mStrokeWidth = i
            mStrokePaint.strokeWidth = i.toFloat()
            invalidateSelf()
        }
    }
    fun setStrokeColor(i: Int) {
        val containerState = mContainerState
        if (containerState.mStrokeColor != i) {
            containerState.mStrokeColor = i
            mStrokeColor = i
            mStrokePaint.color = i
            invalidateSelf()
        }
    }
    fun setCornerRadii(fArr: FloatArray?) {
        val containerState = mContainerState
        containerState.mRadii = fArr
        mRadii = fArr
        if (fArr == null) {
            containerState.mRadius = 0.0f
            mRadius = 0.0f
        }
        invalidateSelf()
    }
    fun setCornerRadius(f: Float) {
        if (java.lang.Float.isNaN(f)) {
            return
        }
        var f2 = f
        if (f2 < 0.0f) {
            f2 = 0.0f
        }
        val containerState = mContainerState
        containerState.mRadius = f2
        containerState.mRadii = null
        mRadius = f2
        mRadii = null
        invalidateSelf()
    }
    @Throws(IllegalArgumentException::class)
    fun setLayerType(type: Int) {
        if (type < 0 || type > 2) {
            throw IllegalArgumentException("Layer type can only be one of: LAYER_TYPE_NONE, LAYER_TYPE_SOFTWARE or LAYER_TYPE_HARDWARE")
        }
        val containerState = mContainerState
        if (containerState.mLayerType != type) {
            containerState.mLayerType = type
            invalidateSelf()
        }
    }
    override fun invalidateDrawable(drawable: Drawable) {
        invalidateSelf()
    }
    override fun scheduleDrawable(drawable: Drawable, runnable: Runnable, j: Long) {
        scheduleSelf(runnable, j)
    }
    override fun unscheduleDrawable(drawable: Drawable, runnable: Runnable) {
        unscheduleSelf(runnable)
    }
    override fun getIntrinsicWidth(): Int {
        return mContainerState.getIntrinsicWidth()
    }
    override fun getIntrinsicHeight(): Int {
        return mContainerState.getIntrinsicHeight()
    }
    override fun onBoundsChange(rect: Rect) {
        mContainerState.onBoundsChange(rect)
    }
    override fun getDirtyBounds(): Rect {
        return mContainerState.getDirtyBounds()
    }
    override fun setChangingConfigurations(i: Int) {
        mContainerState.changingConfigurations = i
    }
    @Deprecated("Deprecated in Java")
    override fun setDither(z: Boolean) {
        mContainerState.setDither(z)
    }
    override fun setFilterBitmap(z: Boolean) {
        mContainerState.setFilterBitmap(z)
    }
    override fun getPadding(rect: Rect): Boolean {
        return mContainerState.getPadding(rect)
    }
    override fun draw(p0: Canvas) {
        drawRoundRect(p0)
    }
    private fun drawRoundRect(canvas: Canvas) {
        val bounds = bounds
        val save = canvas.save()
        mClipPath.reset()
        val rectF = mLayer
        rectF.left = bounds.left.toFloat()
        rectF.top = bounds.top.toFloat()
        rectF.right = bounds.right.toFloat()
        rectF.bottom = bounds.bottom.toFloat()
        val fArr = mRadii
        if (fArr == null) {
            val path = mClipPath
            val f = mRadius
            path.addRoundRect(rectF, f, f, Path.Direction.CW)
        } else {
            mClipPath.addRoundRect(rectF, fArr, Path.Direction.CW)
        }
        canvas.clipPath(mClipPath)
        val i = mStrokeWidth
        val f2 = i * 0.5f
        if (i != 0) {
            val save2 = canvas.save()
            mLayer.inset(f2, f2)
            mClipPath.reset()
            val fArr2 = mRadii
            if (fArr2 == null) {
                val path2 = mClipPath
                val rectF2 = mLayer
                val f3 = mRadius
                path2.addRoundRect(rectF2, f3 + f2, f3 + f2, Path.Direction.CW)
            } else {
                val fArr3 = fArr2.clone()
                mTempRadii = fArr3
                val fArr4 = mRadii!!
                fArr3[0] = fArr4[0] + f2
                fArr3[1] = fArr4[1] + f2
                fArr3[2] = fArr4[2] + f2
                fArr3[3] = fArr4[3] + f2
                mClipPath.addRoundRect(mLayer, fArr3, Path.Direction.CCW)
            }
            canvas.clipPath(mClipPath)
            mContainerState.mChildDrawableWrapper!!.mDrawable!!.draw(canvas)
            canvas.restoreToCount(save2)
            canvas.drawPath(mClipPath, mStrokePaint)
        } else {
            mContainerState.mChildDrawableWrapper!!.mDrawable!!.draw(canvas)
        }
        canvas.restoreToCount(save)
    }
    private fun setSmoothCornerEnable(enabled: Boolean) {
        SmoothCornerHelper.setDrawableSmoothCornerEnable(this, enabled)
    }
    override fun getAlpha(): Int {
        return mContainerState.getAlpha()
    }
    override fun setAlpha(i: Int) {
        mContainerState.setAlpha(i)
        mStrokePaint.alpha = i
        invalidateSelf()
    }
    override fun setColorFilter(colorFilter: ColorFilter?) {
        mContainerState.setColorFilter(colorFilter)
    }
    override fun getOpacity(): Int {
        return mContainerState.getOpacity()
    }
    override fun getConstantState(): ConstantState {
        return mContainerState
    }
    override fun jumpToCurrentState() {
        mContainerState.jumpToCurrentState()
    }
    override fun onStateChange(state: IntArray): Boolean {
        return mContainerState.onStateChange(state)
    }
    override fun isStateful(): Boolean {
        return mContainerState.isStateful()
    }
    
    
    class ContainerState : ConstantState {
        var mChildDrawableWrapper: ChildDrawableWrapper? = null
        var mLayerType = 0
        var mRadii: FloatArray? = null
        var mRadius = 0f
        var mStrokeColor = 0
        var mStrokeWidth = 0   
        constructor() {
            mLayerType = 0
            mChildDrawableWrapper = ChildDrawableWrapper()
        }
        constructor(
            containerState: ContainerState,
            smoothContainerDrawable2: SmoothContainerDrawable2?,
            resources: Resources?,
            theme: Theme?
        ) {
            mLayerType = 0
            mChildDrawableWrapper = ChildDrawableWrapper(containerState.mChildDrawableWrapper, smoothContainerDrawable2, resources, theme)
            mRadius = containerState.mRadius
            mRadii = containerState.mRadii
            mStrokeWidth = containerState.mStrokeWidth
            mStrokeColor = containerState.mStrokeColor
            mLayerType = containerState.mLayerType
        }
        override fun canApplyTheme(): Boolean {
            return true
        }
        override fun newDrawable(): Drawable {
            return SmoothContainerDrawable2(null, null, this)
        }
        override fun newDrawable(res: Resources?): Drawable {
            return SmoothContainerDrawable2(res, null, this)
        }
        override fun newDrawable(res: Resources?, theme: Theme?): Drawable {
            return SmoothContainerDrawable2(res, theme, this)
        }
        override fun getChangingConfigurations(): Int {
            return mChildDrawableWrapper!!.mDrawable!!.changingConfigurations
        }
        fun isStateful(): Boolean {
            return mChildDrawableWrapper!!.mDrawable!!.isStateful
        }
        fun onStateChange(iArr: IntArray?): Boolean {
            return isStateful() && mChildDrawableWrapper!!.mDrawable!!.setState(iArr!!)
        }
        fun getIntrinsicWidth(): Int {
            return mChildDrawableWrapper!!.mDrawable!!.intrinsicWidth
        }
        fun getIntrinsicHeight(): Int {
            return mChildDrawableWrapper!!.mDrawable!!.intrinsicHeight
        }
        fun onBoundsChange(rect: Rect?) {
            mChildDrawableWrapper!!.mDrawable!!.bounds = rect!!
        }
        fun jumpToCurrentState() {
            mChildDrawableWrapper!!.mDrawable!!.jumpToCurrentState()
        }
        fun getOpacity(): Int {
            return mChildDrawableWrapper!!.mDrawable!!.opacity
        }
        fun setAlpha(i: Int) {
            mChildDrawableWrapper!!.mDrawable!!.alpha = i
            mChildDrawableWrapper!!.mDrawable!!.invalidateSelf()
        }
        fun setColorFilter(colorFilter: ColorFilter?) {
            mChildDrawableWrapper!!.mDrawable!!.colorFilter = colorFilter
        }
        fun getBounds(): Rect {
            return mChildDrawableWrapper!!.mDrawable!!.bounds
        }
        fun setBounds(rect: Rect?) {
            mChildDrawableWrapper!!.mDrawable!!.bounds = rect!!
        }
        fun setBounds(i: Int, i2: Int, i3: Int, i4: Int) {
            mChildDrawableWrapper!!.mDrawable!!.setBounds(i, i2, i3, i4)
        }
        fun getDirtyBounds(): Rect {
            return mChildDrawableWrapper!!.mDrawable!!.dirtyBounds
        }
        fun setChangingConfigurations(i: Int) {
            mChildDrawableWrapper!!.mDrawable!!.changingConfigurations = i
        }
        fun setDither(z: Boolean) {
            mChildDrawableWrapper!!.mDrawable!!.setDither(z)
        }
        fun setFilterBitmap(z: Boolean) {
            mChildDrawableWrapper!!.mDrawable!!.isFilterBitmap = z
        }
        fun getAlpha(): Int {
            return mChildDrawableWrapper!!.mDrawable!!.alpha
        }
        fun getPadding(rect: Rect): Boolean {
            return mChildDrawableWrapper!!.mDrawable!!.getPadding(rect)
        }
    }
    
    class ChildDrawableWrapper {
        var mDrawable: Drawable?
        constructor() {
            mDrawable = GradientDrawable()
        }
        constructor(
            childDrawableWrapper: ChildDrawableWrapper?, 
            smoothContainerDrawable2: SmoothContainerDrawable2?, 
            resources: Resources?,
            theme: Theme?
        ) {
            val drawable: Drawable?
            val drawable2 = childDrawableWrapper?.mDrawable
            if (drawable2 != null) {
                val constantState = drawable2.constantState
                drawable = if (constantState == null) {
                    drawable2
                } else if (resources == null) {
                    constantState.newDrawable()
                } else if (theme == null) {
                    constantState.newDrawable(resources)
                } else {
                    constantState.newDrawable(resources, theme)
                }
                drawable.layoutDirection = drawable2.layoutDirection
                drawable.bounds = drawable2.bounds
                drawable.level = drawable2.level
                drawable.callback = smoothContainerDrawable2
            } else {
                drawable = null
            }
            mDrawable = drawable2
        }
    }
}