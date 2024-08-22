package dev.lackluster.hyperx.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.DialogInterface.OnShowListener
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.DefaultTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import cn.fkj233.ui.R
import dev.lackluster.hyperx.widget.DialogAnimHelper
import java.lang.reflect.InvocationTargetException
import kotlin.concurrent.Volatile


open class AlertDialog : Dialog, DialogInterface {
    val mAlert: AlertController
    private val mOnDismiss: DialogAnimHelper.OnDismiss
    private var mOriginalExecutor: Any? = null
    @SuppressLint("RestrictedApi")
    private var mSpecialUiExecutor: TaskExecutor? = null
    companion object {
        fun resolveDialogTheme(context: Context, themeResId: Int): Int {
            if (themeResId ushr 24 and 255 >= 1) {
                return themeResId
            }
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.hyperAlertDialogTheme, typedValue, true)
            return typedValue.resourceId
        }
    }
    constructor(context: Context): this(context, 0)
    constructor(context: Context, themeResId: Int) : super(context, resolveDialogTheme(context, themeResId)) {
        mOnDismiss = object : DialogAnimHelper.OnDismiss {
            override fun end() {
                val decorView = window?.decorView
                if (decorView?.isAttachedToWindow != true) {
                    return
                }
                realDismiss()
            }
        }
        mAlert = AlertController(parseContext(context), this, window!!)
    }
    fun onLayoutReload() {
    }
    private fun parseContext(context: Context?): Context {
        if (context == null) {
            return getContext()
        }
        return if (context is ContextThemeWrapper) context else getContext()
    }
    fun getButton(i: Int): Button? {
        return mAlert.getButton(i)
    }
    fun getListView(): ListView? {
        return mAlert.getListView()
    }
    override fun setTitle(charSequence: CharSequence?) {
        super.setTitle(charSequence)
        mAlert.setTitle(charSequence)
    }
    fun setCustomTitle(view: View?) {
        mAlert.setCustomTitle(view!!)
    }
    open fun setMessage(charSequence: CharSequence?) {
        mAlert.setMessage(charSequence)
    }
    fun getMessageView(): TextView? {
        return mAlert.getMessageView()
    }
    fun setView(view: View?) {
        mAlert.setView(view)
    }
    fun setButton(
        i: Int,
        charSequence: CharSequence?,
        onClickListener: DialogInterface.OnClickListener?
    ) {
        mAlert.setButton(i, charSequence, onClickListener, null)
    }
    fun setIcon(resId: Int) {
        mAlert.setIcon(resId)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        if (isSystemSpecialUiThread()) {
            processSpecialUiOnCreate()
        }
        if (mAlert.isDialogImmersive() || !mAlert.mEnableEnterAnim) {
            window?.setWindowAnimations(0)
        }
        super.onCreate(savedInstanceState)
        mAlert.installContent(savedInstanceState)
    }
    fun isChecked(): Boolean {
        return mAlert.isChecked()
    }
    override fun dispatchKeyEvent(keyEvent: KeyEvent): Boolean {
        return if (mAlert.dispatchKeyEvent(keyEvent)) {
            true
        } else super.dispatchKeyEvent(keyEvent)
    }
    override fun onStart() {
        super.onStart()
        mAlert.onStart()
    }
    override fun onStop() {
        if (isSystemSpecialUiThread()) {
            processSpecialUiOnStopBeforeSuper()
        }
        super.onStop()
        mAlert.onStop()
        if (isSystemSpecialUiThread()) {
            processSpecialUiOnStopAfterSuper()
        }
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mAlert.onAttachedToWindow()
    }
    override fun setCancelable(flag: Boolean) {
        super.setCancelable(flag)
        mAlert.setCancelable(flag)
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAlert.onDetachedFromWindow()
    }
    override fun dismiss() {
        val decorView = window?.decorView
        if (mAlert.isDialogImmersive()) {
            val associatedActivity = getAssociatedActivity()
            if (associatedActivity != null && associatedActivity.isFinishing) {
                dismissIfAttachedToWindow(decorView)
                return
            } else {
                dismissWithAnimationOrNot(decorView)
                return
            }
        }
        dismissIfAttachedToWindow(decorView)
    }
    fun dismissWithAnimationOrNot(view: View?) {
        if (view != null) {
            if (view.handler != null) {
                dismissWithAnimationExistDecorView(view)
                return
            } else {
                dismissIfAttachedToWindow(view)
                return
            }
        }
        super.dismiss()
    }
    fun dismissWithAnimationExistDecorView(view: View) {
        if (Thread.currentThread() === view.handler.looper.thread) {
            mAlert.dismiss(mOnDismiss)
        } else {
            view.post {
                mAlert.dismiss(mOnDismiss)
            }
        }
    }
    fun dismissIfAttachedToWindow(view: View?) {
        if (view == null || view.isAttachedToWindow) {
            super.dismiss()
        }
    }
    fun realDismiss() {
        super.dismiss()
    }
    fun getAssociatedActivity(): Activity? {
        var ownerActivity = ownerActivity
        var context: Any? = context
        while (ownerActivity == null && context != null) {
            if (context is Activity) {
                ownerActivity = context
            } else {
                context = if (context is ContextWrapper) context.baseContext else null
            }
        }
        return ownerActivity
    }
    fun dismissWithoutAnimation() {
        if (window?.decorView?.isAttachedToWindow == true) {
            realDismiss()
        }
    }
    fun setEnableImmersive(z: Boolean) {
        mAlert.setEnableImmersive(z)
    }
    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
        mAlert.setCanceledOnTouchOutside(cancel)
    }
    fun setOnShowAnimListener(onDialogShowAnimListener: OnDialogShowAnimListener?) {
        mAlert.setShowAnimListener(onDialogShowAnimListener)
    }
    private fun isSystemSpecialUiThread(): Boolean {
        return TextUtils.equals("android.ui", Thread.currentThread().name) ||
                TextUtils.equals("android.imms", Thread.currentThread().name) ||
                TextUtils.equals("system_server", Thread.currentThread().name)
    }
    @SuppressLint("RestrictedApi")
    private fun processSpecialUiOnCreate() {
        try {
            try {
                try {
                    val field = ArchTaskExecutor::class.java.getDeclaredField("mDelegate")
                    val fieldValue = field.get(ArchTaskExecutor.getInstance())
                    if (fieldValue != null) {
                        mOriginalExecutor = fieldValue
                    }
                } catch (e: IllegalAccessException) {
                    Log.d(
                        "MiuixDialog",
                        "onCreate() taskExecutor get failed IllegalAccessException $e"
                    )
                }
            } catch (e2: NoSuchMethodException) {
                Log.d(
                    "MiuixDialog",
                    "onCreate() taskExecutor get failed NoSuchMethodException $e2"
                )
            } catch (e3: InvocationTargetException) {
                Log.d(
                    "MiuixDialog",
                    "onCreate() taskExecutor get failed InvocationTargetException $e3"
                )
            }
        } finally {
            mSpecialUiExecutor = createSpecialUiTaskExecutor()
            ArchTaskExecutor.getInstance().setDelegate(mSpecialUiExecutor)
        }
    }
    @SuppressLint("RestrictedApi")
    private fun processSpecialUiOnStopBeforeSuper() {
        try {
            try {
                val field = ArchTaskExecutor::class.java.getDeclaredField("mDelegate")
                val fieldValue = field.get(ArchTaskExecutor.getInstance())
                if (fieldValue != null && fieldValue !== mOriginalExecutor) {
                    mOriginalExecutor = fieldValue
                }
                if (fieldValue === mSpecialUiExecutor && ArchTaskExecutor.getInstance().isMainThread) {
                    return
                }
            } catch (e: IllegalAccessException) {
                Log.d(
                    "MiuixDialog",
                    "onStop() taskExecutor get failed IllegalAccessException $e"
                )
                if (mSpecialUiExecutor == null && ArchTaskExecutor.getInstance().isMainThread) {
                    return
                }
            } catch (e2: NoSuchMethodException) {
                Log.d(
                    "MiuixDialog",
                    "onStop() taskExecutor get failed NoSuchMethodException $e2"
                )
                if (mSpecialUiExecutor == null && ArchTaskExecutor.getInstance().isMainThread) {
                    return
                }
            } catch (e3: InvocationTargetException) {
                Log.d(
                    "MiuixDialog",
                    "onStop() taskExecutor get failed InvocationTargetException $e3"
                )
                if (mSpecialUiExecutor == null && ArchTaskExecutor.getInstance().isMainThread) {
                    return
                }
            }
            ArchTaskExecutor.getInstance().setDelegate(mSpecialUiExecutor)
        } catch (th: Throwable) {
            if (mSpecialUiExecutor != null || !ArchTaskExecutor.getInstance().isMainThread) {
                ArchTaskExecutor.getInstance().setDelegate(mSpecialUiExecutor)
            }
            throw th
        }
    }
    @SuppressLint("RestrictedApi")
    private fun processSpecialUiOnStopAfterSuper() {
        if (mOriginalExecutor is TaskExecutor) {
            ArchTaskExecutor.getInstance().setDelegate(mOriginalExecutor as TaskExecutor?)
        }
    }
    @SuppressLint("RestrictedApi")
    private fun createSpecialUiTaskExecutor(): TaskExecutor {
        return object : DefaultTaskExecutor() {
            private val mLock = Any()
            @Volatile
            private var mSpecialMainHandler: Handler? = null

            override fun isMainThread(): Boolean {
                return true
            }
            override fun postToMainThread(runnable: Runnable) {
                if (mSpecialMainHandler == null) {
                    synchronized(this.mLock) {
                        if (mSpecialMainHandler == null) {
                            mSpecialMainHandler = createAsync(Looper.myLooper()!!)
                        }
                    }
                }
                mSpecialMainHandler!!.post(runnable)
            }
            private fun createAsync(looper: Looper): Handler {
                return Handler.createAsync(looper)
            }
        }
    }

    class Builder {
        private val P: AlertController.AlertParams
        private val mTheme: Int
        constructor(context: Context): this(context, resolveDialogTheme(context, 0))
        constructor(context: Context, themeResId: Int) {
            P = AlertController.AlertParams(ContextThemeWrapper(context, resolveDialogTheme(context, themeResId)))
            mTheme = themeResId
        }
        fun getContext(): Context {
            return P.mContext
        }
        fun setTitle(textResId: Int): Builder {
            val alertParams = P
            alertParams.mTitle = alertParams.mContext.getText(textResId)
            return this
        }
        fun setHapticFeedbackEnabled(z: Boolean): Builder {
            P.mHapticFeedbackEnabled = z
            return this
        }
        fun setTitle(charSequence: CharSequence?): Builder {
            P.mTitle = charSequence
            return this
        }
        fun setCustomTitle(view: View?): Builder {
            P.mCustomTitleView = view
            return this
        }
        fun setMessage(textResId: Int): Builder {
            val alertParams = P
            alertParams.mMessage = alertParams.mContext.getText(textResId)
            return this
        }
        fun setMessage(charSequence: CharSequence?): Builder {
            P.mMessage = charSequence
            return this
        }
        fun setComment(charSequence: CharSequence?): Builder {
            P.mComment = charSequence
            return this
        }
        fun setCheckBox(z: Boolean, charSequence: CharSequence?): Builder {
            val alertParams = P
            alertParams.mIsChecked = z
            alertParams.mCheckBoxMessage = charSequence
            return this
        }
        fun setIcon(resId: Int): Builder {
            P.mIconId = resId
            return this
        }
        fun setIcon(drawable: Drawable?): Builder {
            P.mIcon = drawable
            return this
        }
        fun setIconAttribute(resId: Int): Builder {
            val typedValue = TypedValue()
            P.mContext.theme.resolveAttribute(resId, typedValue, true)
            P.mIconId = typedValue.resourceId
            return this
        }
        fun setPositiveButton(
            textResId: Int,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mPositiveButtonText = alertParams.mContext.getText(textResId)
            P.mPositiveButtonListener = onClickListener
            return this
        }
        fun setPositiveButton(
            charSequence: CharSequence?,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mPositiveButtonText = charSequence
            alertParams.mPositiveButtonListener = onClickListener
            return this
        }
        fun setNegativeButton(
            textResId: Int,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mNegativeButtonText = alertParams.mContext.getText(textResId)
            P.mNegativeButtonListener = onClickListener
            return this
        }
        fun setNegativeButton(
            charSequence: CharSequence?,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mNegativeButtonText = charSequence
            alertParams.mNegativeButtonListener = onClickListener
            return this
        }
        fun setNeutralButton(
            textResId: Int,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mNeutralButtonText = alertParams.mContext.getText(textResId)
            P.mNeutralButtonListener = onClickListener
            return this
        }
        fun setNeutralButton(
            charSequence: CharSequence?,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mNeutralButtonText = charSequence
            alertParams.mNeutralButtonListener = onClickListener
            return this
        }
        fun setCancelable(z: Boolean): Builder {
            P.mCancelable = z
            return this
        }
        fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener?): Builder {
            P.mOnCancelListener = onCancelListener
            return this
        }
        fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener?): Builder {
            P.mOnDismissListener = onDismissListener
            return this
        }
        fun setOnShowListener(onShowListener: OnShowListener?): Builder {
            P.mOnShowListener = onShowListener
            return this
        }
        fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?): Builder {
            P.mOnKeyListener = onKeyListener
            return this
        }
        fun setItems(
            charSequenceArr: Array<CharSequence?>?,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mItems = charSequenceArr
            alertParams.mOnClickListener = onClickListener
            return this
        }
        fun setAdapter(
            listAdapter: ListAdapter?,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mAdapter = listAdapter
            alertParams.mOnClickListener = onClickListener
            return this
        }
        fun setMultiChoiceItems(
            charSequenceArr: Array<CharSequence?>?,
            zArr: BooleanArray?,
            onMultiChoiceClickListener: OnMultiChoiceClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mItems = charSequenceArr
            alertParams.mOnCheckboxClickListener = onMultiChoiceClickListener
            alertParams.mCheckedItems = zArr
            alertParams.mIsMultiChoice = true
            return this
        }
        fun setSingleChoiceItems(
            charSequenceArr: Array<CharSequence?>?,
            i: Int,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mItems = charSequenceArr
            alertParams.mOnClickListener = onClickListener
            alertParams.mCheckedItem = i
            alertParams.mIsSingleChoice = true
            return this
        }
        fun setSingleChoiceItems(
            listAdapter: ListAdapter?,
            i: Int,
            onClickListener: DialogInterface.OnClickListener?
        ): Builder {
            val alertParams = P
            alertParams.mAdapter = listAdapter
            alertParams.mOnClickListener = onClickListener
            alertParams.mCheckedItem = i
            alertParams.mIsSingleChoice = true
            return this
        }
        fun setView(i: Int): Builder {
            val alertParams = P
            alertParams.mView = null
            alertParams.mViewLayoutResId = i
            return this
        }
        fun setView(view: View?): Builder {
            val alertParams = P
            alertParams.mView = view
            alertParams.mViewLayoutResId = 0
            return this
        }
        fun create(): AlertDialog {
            val alertDialog = AlertDialog(P.mContext, mTheme)
            P.apply(alertDialog.mAlert)
            alertDialog.setCancelable(P.mCancelable)
            if (P.mCancelable) {
                alertDialog.setCanceledOnTouchOutside(true)
            }
            alertDialog.setOnCancelListener(P.mOnCancelListener)
            alertDialog.setOnDismissListener(P.mOnDismissListener)
            alertDialog.setOnShowListener(P.mOnShowListener)
            alertDialog.setOnShowAnimListener(P.mOnDialogShowAnimListener)
            val onKeyListener = P.mOnKeyListener
            if (onKeyListener != null) {
                alertDialog.setOnKeyListener(onKeyListener)
            }
            return alertDialog
        }
        fun show(): AlertDialog {
            val create = create()
            create.show()
            return create
        }
    }

    interface OnDialogShowAnimListener {
        fun onShowAnimComplete()
        fun onShowAnimStart()
    }

    interface OnPanelSizeChangedListener {
    }
}