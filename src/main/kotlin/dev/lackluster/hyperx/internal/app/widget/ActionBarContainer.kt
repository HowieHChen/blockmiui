package dev.lackluster.hyperx.internal.app.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import cn.fkj233.ui.R
import dev.lackluster.hyperx.core.util.MiuiBlurUtils
import dev.lackluster.hyperx.internal.util.AttributeResolver
import dev.lackluster.hyperx.theme.token.BlurToken
import dev.lackluster.hyperx.view.BlurableWidget
import dev.lackluster.hyperx.view.MiuiBlurUiHelper


class ActionBarContainer : FrameLayout, BlurableWidget {
    private val mBlurHelper: MiuiBlurUiHelper
    private var mBackground: Drawable? = null
    private var mDrawBackground: Boolean = true
//    private var mActionBarView: View? = null
//    private var mIsSplit: Boolean = false
    private var mUserApplyBgBlur: Boolean? = null
    private var mUserApplySplitActionBarBgBlur: Boolean? = false
    private var mInternalApplyBgBlur: Boolean = false
//    private var mInternalApplySpiltBgBlur: Boolean = false
    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ActionBar)
        mBackground = obtainStyledAttributes.getDrawable(R.styleable.ActionBar_android_background)
        obtainStyledAttributes.recycle()
        mBlurHelper = MiuiBlurUiHelper(context, this, false, object : MiuiBlurUiHelper.BlurStateCallback {
            override fun onBlurApplyStateChanged(enabled: Boolean) {
                mDrawBackground = !enabled
                this@ActionBarContainer.invalidate()
            }
            override fun onBlurEnableStateChanged(enabled: Boolean) {
            }
            override fun onCreateBlurParams(miuiBlurUiHelper: MiuiBlurUiHelper) {
                val resolveBoolean = AttributeResolver.resolveBoolean(this@ActionBarContainer.context, R.attr.isLightTheme, true)
                miuiBlurUiHelper.setBlurParams(
                    MiuiBlurUiHelper.getFinalBlendColorForViewByBackgroundColor(
                        this@ActionBarContainer.context,
                        this@ActionBarContainer.mBackground,
                        if (resolveBoolean) BlurToken.BlendColor.Light.DEFAULT else BlurToken.BlendColor.Dark.DEFAULT
                    ),
                    if (resolveBoolean) BlurToken.BlendMode.Light.DEFAULT else BlurToken.BlendMode.Dark.DEFAULT,
                    66
                )
            }
        })
        setWillNotDraw(mBackground != null)
        mDrawBackground = true
        if (MiuiBlurUtils.isEnable()) {
            setSupportBlur(true)
        }
        if (MiuiBlurUtils.isEffectEnable(context)) {
            setEnableBlur(true)
        }
        setActionBarBlur(true)
    }
    override fun applyBlur(enabled: Boolean) {
        mBlurHelper.applyBlur(enabled)
    }
    override fun setEnableBlur(enabled: Boolean) {
        mBlurHelper.setEnableBlur(enabled)
    }
    override fun setSupportBlur(support: Boolean) {
        mBlurHelper.setSupportBlur(support)
    }
    fun isSupportBlur(): Boolean {
        return mBlurHelper.isSupportBlur()
    }
    fun isEnableBlur(): Boolean {
        return mBlurHelper.isEnableBlur()
    }
    fun isApplyBlur(): Boolean {
        return mBlurHelper.isApplyBlur()
    }
    fun setActionBarBlur(enabled: Boolean) {
        mInternalApplyBgBlur = enabled
        if (mUserApplyBgBlur != null) {
            return
        }
        applyBlur(enabled)
    }
    override fun onDraw(canvas: Canvas) {
        val drawable = mBackground
        if (width == 0 || height == 0 || drawable == null || !mDrawBackground) {
            return
        }
        drawable.draw(canvas)
    }
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mBlurHelper.onConfigChanged()
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState())
        val mserApplyBgBlur = mUserApplyBgBlur
        savedState.userActionBarApplyBlur = if (mserApplyBgBlur == null) -1 else if (mserApplyBgBlur) 1 else 0
        val userApplySplitActionBarBgBlur = mUserApplySplitActionBarBgBlur
        savedState.userSplitActionBarApplyBlur = if (userApplySplitActionBarBgBlur == null) -1 else if (userApplySplitActionBarBgBlur) 1 else 0
        savedState.actionBarSupportBlur = isSupportBlur()
        savedState.actionBarEnableBlur = isEnableBlur()
        savedState.actionBarApplyBlur = isApplyBlur()
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        val savedState = state as SavedState
        mUserApplyBgBlur = when (savedState.userActionBarApplyBlur) {
            0 -> false
            1 -> true
            else -> null
        }
        mUserApplySplitActionBarBgBlur = when (savedState.userActionBarApplyBlur) {
            0 -> false
            1 -> true
            else -> null
        }
        if (savedState.actionBarSupportBlur) {
            setSupportBlur(true)
        }
        if (savedState.actionBarEnableBlur && MiuiBlurUtils.isEffectEnable(context)) {
            setEnableBlur(true)
        }
        if (savedState.actionBarApplyBlur && isEnableBlur()) {
            applyBlur(true)
        }
    }

    class SavedState: BaseSavedState {
        var actionBarApplyBlur = false
        var actionBarEnableBlur = false
        var actionBarSupportBlur = false
        var userActionBarApplyBlur = 0
        var userSplitActionBarApplyBlur = 0
        constructor(parcelable: Parcelable?): super(parcelable)
        constructor(parcel: Parcel?): super(parcel) {
            if (parcel == null) return
            userActionBarApplyBlur = parcel.readInt()
            userSplitActionBarApplyBlur = parcel.readInt()
            actionBarSupportBlur = parcel.readInt() != 0
            actionBarEnableBlur = parcel.readInt() != 0
            actionBarApplyBlur = parcel.readInt() != 0
        }
        constructor(parcel: Parcel, classLoader: ClassLoader): super(parcel, classLoader) {
            userActionBarApplyBlur = parcel.readInt()
            userSplitActionBarApplyBlur = parcel.readInt()
            actionBarSupportBlur = parcel.readInt() != 0
            actionBarEnableBlur = parcel.readInt() != 0
            actionBarApplyBlur = parcel.readInt() != 0
        }
        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(userActionBarApplyBlur)
            out.writeInt(userSplitActionBarApplyBlur)
            out.writeInt(if (actionBarSupportBlur) 1 else 0)
            out.writeInt(if (actionBarEnableBlur) 1 else 0)
            out.writeInt(if (actionBarApplyBlur) 1 else 0)
        }
        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(p0: Parcel?): SavedState {
               return SavedState(p0)
            }
            override fun newArray(p0: Int): Array<SavedState?> {
                return arrayOfNulls<SavedState>(p0)
            }
        }
    }
}