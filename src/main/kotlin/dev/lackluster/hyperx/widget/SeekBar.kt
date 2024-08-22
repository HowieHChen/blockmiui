package dev.lackluster.hyperx.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.widget.SeekBar
import cn.fkj233.ui.R
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
class SeekBar : SeekBar {
    private var mDefaultForegroundPrimaryColor: Int
    private var mDefaultForegroundPrimaryDisableColor: Int
    private var mDefaultIconPrimaryColor: Int
    private var mMiddleEnabled: Boolean
    private var mForegroundPrimaryColor: Int
    private var mForegroundPrimaryDisableColor: Int
    private var mIconPrimaryColor: Int
    private var mDisabledProgressAlpha: Float
    private var mMinMiddle: Float
    private var mMaxMiddle: Float
    private var mIconTransparent: Int
    private var mIsInMiddle: Boolean
    private var mProgress: Int
    private var mProgressColorStateList: ColorStateList? = null
    private var mOnSeekBarChangeListener: OnSeekBarChangeListener? = null
    private val mTrainsOnSeekBarChangeListener: OnSeekBarChangeListener?

    constructor(context: Context) : this(context, null, 0, R.style.Widget_SeekBar)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0, R.style.Widget_SeekBar)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.Widget_SeekBar)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SeekBar, defStyleAttr, defStyleRes)
        mDefaultForegroundPrimaryColor = context.getColor(R.color.hyperx_progress_primary_colors)
        mDefaultForegroundPrimaryDisableColor = context.getColor(R.color.hyperx_progress_disable_color)
        mDefaultIconPrimaryColor = context.getColor(R.color.hyperx_progress_background_icon)
        mMiddleEnabled = obtainStyledAttributes.getBoolean(R.styleable.SeekBar_middleEnabled, false)
        mForegroundPrimaryColor = obtainStyledAttributes.getColor(R.styleable.SeekBar_foregroundPrimaryColor, mDefaultForegroundPrimaryColor)
        mForegroundPrimaryDisableColor = obtainStyledAttributes.getColor(R.styleable.SeekBar_foregroundPrimaryDisableColor, mDefaultForegroundPrimaryDisableColor)
        mIconPrimaryColor = obtainStyledAttributes.getColor(R.styleable.SeekBar_iconPrimaryColor, mDefaultIconPrimaryColor)
        mDisabledProgressAlpha = obtainStyledAttributes.getFloat(R.styleable.SeekBar_disabledProgressAlpha, 0.5f)
        mMinMiddle = obtainStyledAttributes.getFloat(R.styleable.SeekBar_minMiddle, 0.46f)
        mMaxMiddle = obtainStyledAttributes.getFloat(R.styleable.SeekBar_maxMiddle, 0.54f)
        obtainStyledAttributes.recycle()
        mIconTransparent = context.getColor(R.color.hyperx_transparent)
        if (mMinMiddle !in 0.0f..0.5f) {
            mMaxMiddle = 0.46f
        }
        if (mMaxMiddle !in 0.5f..1.0f) {
            mMaxMiddle = 0.54f
        }
        val range = max - getMinWrapper()
        mIsInMiddle =isInMiddle(range, progress)
        mProgress = progress
        if (mIsInMiddle) {
            val round = (range * 0.5f).roundToInt()
            mProgress = round
            progress = round
        }
        mTrainsOnSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mMiddleEnabled && fromUser) {
                    val fRange = max - getMinWrapper()
                    val round = (fRange * 0.5f).roundToInt()
                    val percent = if (fRange > 0) {
                        (progress - getMinWrapper()).toFloat() / fRange
                    } else {
                        0.0f
                    }
                    mProgress = if (percent > mMinMiddle && percent < mMaxMiddle) {
                        round
                    } else {
                        progress
                    }
                    if (getProgress() != mProgress) {
                        setProgress(mProgress)
                    }
                }
                mOnSeekBarChangeListener?.onProgressChanged(p0, progress, fromUser)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                mOnSeekBarChangeListener?.onStartTrackingTouch(p0)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mOnSeekBarChangeListener?.onStopTrackingTouch(p0)
            }
        }
        setOnSeekBarChangeListener(mTrainsOnSeekBarChangeListener)
    }

    fun setMiddleEnabled(enabled: Boolean) {
        if (enabled != mMiddleEnabled) {
            mMiddleEnabled = enabled
            updatePrimaryColor()
        }
    }

    fun setForegroundPrimaryColor(normalColor: Int, disableColor: Int) {
        mForegroundPrimaryColor = normalColor
        mForegroundPrimaryDisableColor = disableColor
        updatePrimaryColor()
    }

    fun setIconPrimaryColor(iconColor: Int) {
        mIconPrimaryColor = iconColor
        updatePrimaryColor()
    }

    override fun setOnSeekBarChangeListener(onSeekBarChangeListener: OnSeekBarChangeListener) {
        if (mTrainsOnSeekBarChangeListener == onSeekBarChangeListener) {
            super.setOnSeekBarChangeListener(mTrainsOnSeekBarChangeListener)
        } else {
            mOnSeekBarChangeListener = onSeekBarChangeListener
        }
    }

    private fun updatePrimaryColor() {
        if (progressDrawable is LayerDrawable) {
            val layerDrawable = progressDrawable as LayerDrawable
            val progress = layerDrawable.findDrawableByLayerId(android.R.id.progress)
            if (progress is ClipDrawable) {
                if (progress.drawable is GradientDrawable) {
                    val gradientDrawable = progress.drawable as GradientDrawable
                    val colorStateList = gradientDrawable.color
                    if (mProgressColorStateList == null && colorStateList != null) {
                        mProgressColorStateList = colorStateList
                    }
                    if (mProgressColorStateList != null &&
                        mProgressColorStateList?.getColorForState(intArrayOf(-16842910), mDefaultForegroundPrimaryDisableColor) != mForegroundPrimaryDisableColor
                        ) {
                        mProgressColorStateList = ColorStateList(
                            arrayOf(
                                intArrayOf(-16842910), intArrayOf()
                            ),
                            intArrayOf(mForegroundPrimaryDisableColor, mForegroundPrimaryColor)
                        )
                        (gradientDrawable.mutate() as GradientDrawable).color = mProgressColorStateList
                    }
                }
            }
            val icon = layerDrawable.findDrawableByLayerId(android.R.id.icon)
            if (icon is GradientDrawable) {
                icon.setColorFilter(
                    if (mMiddleEnabled) mIconPrimaryColor else mIconTransparent,
                    PorterDuff.Mode.SRC
                )
            }
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updatePrimaryColor()
        if (progressDrawable != null) {
            progressDrawable.alpha = if (isEnabled) {
                255
            } else {
                (mDisabledProgressAlpha * 255.0f).toInt()
            }
        }
    }

    private fun getMinWrapper(): Int {
        return super.getMin()
    }
    private fun isInMiddle(range: Int, progress: Int): Boolean {
        val percent = if (range > 0) {
            (progress - getMinWrapper()).toFloat() / range
        } else {
            0.0f
        }
        return percent > mMinMiddle && percent < mMaxMiddle
    }
}