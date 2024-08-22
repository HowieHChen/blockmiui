package dev.lackluster.hyperx.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import dev.lackluster.hyperx.core.util.MiuiBlurUtils.addBackgroundBlenderColor
import dev.lackluster.hyperx.core.util.MiuiBlurUtils.clearBackgroundBlenderColor
import dev.lackluster.hyperx.core.util.MiuiBlurUtils.clearBackgroundBlur
import dev.lackluster.hyperx.core.util.MiuiBlurUtils.setBackgroundBlur
import dev.lackluster.hyperx.internal.util.AttributeResolver.resolveDrawable


class MiuiBlurUiHelper(
    private val mContext: Context,
    private val mViewApplyBlur: View,
    private val mIsSpecialShape: Boolean,
    private val mCallback: BlurStateCallback
) : BlurableWidget {
    private var mIsSupportBlur = false
    private var mIsEnableBlur = false
    private var mApplyBlur = false
    private var mBlurBlendColors: IntArray? = null
    private var mBlurBlendColorModes: IntArray? = null
    private var mBlurEffect = 0

    companion object {
        fun getFinalBlendColorForViewByBackgroundColor(
            context: Context,
            drawable: Drawable?,
            iArr: IntArray
        ): IntArray {
            var i = 0
            val iArr2 = intArrayOf(iArr[0], iArr[1])
            if (drawable != null && drawable is ColorDrawable) {
                if (drawable.color == 0) {
                    val resolveDrawable = resolveDrawable(
                        context, android.R.attr.windowBackground
                    )
                    if (resolveDrawable is ColorDrawable) {
                        i = resolveDrawable.color
                    }
                } else {
                    i = drawable.color
                }
                iArr2[1] = 16777215 and i or (-16777216 and iArr[1])
            }
            return iArr2
        }
    }

    override fun setSupportBlur(support: Boolean) {
        mIsSupportBlur = support
    }
    fun isSupportBlur(): Boolean {
        return mIsSupportBlur
    }
    override fun setEnableBlur(enabled: Boolean) {
        if (mIsSupportBlur && mIsEnableBlur != enabled) {
            mIsEnableBlur = enabled
            mCallback.onBlurEnableStateChanged(enabled)
            if (enabled) {
                return
            }
            applyBlur(false)
        }
    }
    fun isEnableBlur(): Boolean {
        return mIsEnableBlur
    }
    override fun applyBlur(enabled: Boolean) {
        val density: Float
        if (!mIsSupportBlur || !mIsEnableBlur || mApplyBlur == enabled) {
            return
        }
        mApplyBlur = enabled
        var i = 0
        if (enabled) {
            if (mBlurBlendColors == null) {
                mCallback.onCreateBlurParams(this)
            }
            mCallback.onBlurApplyStateChanged(true)
            density = try {
                mViewApplyBlur.context.resources.displayMetrics.density
            } catch (unused: Exception) {
                2.75f
            }
            setBackgroundBlur(
                mViewApplyBlur,
                (mBlurEffect * density).toInt(),
                mIsSpecialShape
            )
            while (true) {
                val iArr = mBlurBlendColors
                if (i >= iArr!!.size) {
                    return
                }
                addBackgroundBlenderColor(
                    mViewApplyBlur, iArr[i],
                    mBlurBlendColorModes!![i]
                )
                i++
            }
        } else {
            clearBackgroundBlur(mViewApplyBlur)
            clearBackgroundBlenderColor(mViewApplyBlur)
            mCallback.onBlurApplyStateChanged(false)
        }
    }
    fun isApplyBlur(): Boolean {
        return mApplyBlur
    }
    fun setBlurParams(iArr: IntArray?, iArr2: IntArray?, i: Int) {
        mBlurBlendColors = iArr
        mBlurBlendColorModes = iArr2
        mBlurEffect = i
    }
    fun onConfigChanged() {
        resetBlurParams()
    }
    fun resetBlurParams() {
        mBlurBlendColors = null
        mBlurBlendColorModes = null
        mBlurEffect = 0
    }
    fun refreshBlur() {
        if (!mApplyBlur) {
            return
        }
        if (mBlurBlendColors == null) {
            clearBackgroundBlur(mViewApplyBlur)
            clearBackgroundBlenderColor(mViewApplyBlur)
            mCallback.onCreateBlurParams(this)
        }
        val density: Float = try {
            mViewApplyBlur.context.resources.displayMetrics.density
        } catch (unused: java.lang.Exception) {
            2.75f
        }
        mCallback.onBlurApplyStateChanged(true)
        setBackgroundBlur(mViewApplyBlur, (mBlurEffect * density).toInt(), mIsSpecialShape)
        var i = 0
        while (true) {
            val iArr = mBlurBlendColors
            if (i >= (iArr?.size ?: 0)) {
                return
            }
            addBackgroundBlenderColor(
                mViewApplyBlur, iArr!![i],
                mBlurBlendColorModes!![i]
            )
            i++
        }
    }

    interface BlurStateCallback {
        fun onBlurApplyStateChanged(enabled: Boolean)
        fun onBlurEnableStateChanged(enabled: Boolean)
        fun onCreateBlurParams(miuiBlurUiHelper: MiuiBlurUiHelper)
    }
}