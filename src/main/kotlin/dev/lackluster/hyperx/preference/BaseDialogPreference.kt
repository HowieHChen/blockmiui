package dev.lackluster.hyperx.preference

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.TextView
import cn.fkj233.ui.R
import dev.lackluster.hyperx.app.AlertDialog


abstract class BaseDialogPreference : BasePreference {
    lateinit var mSummary: TextView
    lateinit var mWidgetFrame: ViewGroup
    lateinit var mArrowRight: ImageView
    private var mMessageView: TextView? = null
    protected var mDialog: AlertDialog? = null
    protected var mDialogIcon: Drawable? = null
    protected var mDialogTitle: CharSequence? = null
    protected var mDialogMessage: CharSequence? = null
    protected var mDialogLayoutRes = 0
    protected var mDialogCancelable: Boolean = true
    protected var mCheckBoxChecked: Boolean = false
    protected var mCheckBoxMsg: CharSequence? = null
    protected var mNegativeButtonText: CharSequence? = null
    protected var mPositiveButtonText: CharSequence? = null
    protected var mNegativeButtonOnClickListener: DialogInterface.OnClickListener? = null
    protected var mPositiveButtonOnClickListener: DialogInterface.OnClickListener? = null

    constructor(context: Context) : this(context, null, 0, R.style.HyperX_Preference)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.preferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes)

    fun setSummary(text: CharSequence?) {
        if (text != null && !TextUtils.isEmpty(text)) {
            mSummary.visibility = View.VISIBLE
            mSummary.text = text
        } else {
            mSummary.visibility = View.GONE
        }
    }

    fun setShowRightArrow(isShow: Boolean) {
        mArrowRight.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    fun setDialogIcon(drawable: Drawable?) {
        mDialogIcon = drawable
    }

    fun setDialogIcon(resId: Int) {
        mDialogIcon = context.getDrawable(resId)
    }

    fun setDialogTitle(charSequence: CharSequence?) {
        mDialogTitle = charSequence
    }

    fun setDialogTitle(resId: Int) {
        mDialogTitle = context.getText(resId)
    }

    fun setDialogMessage(charSequence: CharSequence?) {
        mDialogMessage = charSequence
    }

    fun setDialogMessage(resId: Int) {
        mDialogMessage = context.getText(resId)
    }

    fun setDialogLayoutRes(resId: Int) {
        mDialogLayoutRes = resId
    }

    fun setDialogCancelable(cancelable: Boolean) {
        mDialogCancelable = cancelable
    }

    fun setCheckBoxChecked(checked: Boolean) {
        mCheckBoxChecked = checked
    }

    fun setCheckBoxMessage(charSequence: CharSequence?) {
        mCheckBoxMsg = charSequence
    }

    fun setCheckBoxMessage(resId: Int) {
        mCheckBoxMsg = context.getText(resId)
    }

    fun setNegativeButtonText(charSequence: CharSequence?) {
        mNegativeButtonText = charSequence
    }

    fun setNegativeButtonText(resId: Int) {
        mNegativeButtonText = context.getText(resId)
    }

    fun setPositiveButtonText(charSequence: CharSequence?) {
        mPositiveButtonText = charSequence
    }

    fun setPositiveButtonText(resId: Int) {
        mPositiveButtonText = context.getText(resId)
    }

    fun setNegativeButtonOnClickListener(listener: DialogInterface.OnClickListener?) {
        mNegativeButtonOnClickListener = listener
    }

    fun setPositiveButtonOnClickListener(listener: DialogInterface.OnClickListener?) {
        mPositiveButtonOnClickListener = listener
    }

    fun createCustomView(): View? {
        val layoutResId = mDialogLayoutRes
        return if (layoutResId != 0) {
            View.inflate(context, layoutResId, null)
        } else {
            null
        }
    }

    open fun needInputMethod(): Boolean {
        return false
    }

    open fun onBindDialogView(view: View): Boolean {
        val messageView = view.findViewById<TextView>(android.R.id.message)
        mMessageView = messageView
        val msg = mDialogMessage
        mMessageView?.let {
            if (!msg.isNullOrEmpty()) {
                it.text = msg
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
        return messageView != null
    }

    protected fun createDialog(): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setIcon(mDialogIcon)
            .setTitle(mDialogTitle)
            .setCancelable(mDialogCancelable)
            .setCheckBox(mCheckBoxChecked, mCheckBoxMsg)
            .setNegativeButton(mNegativeButtonText, mNegativeButtonOnClickListener)
            .setPositiveButton(mPositiveButtonText, mPositiveButtonOnClickListener)
        val customView = createCustomView()
        if (customView != null && onBindDialogView(customView)) {
            builder.setView(customView)
        } else {
            builder.setMessage(mDialogMessage)
        }
        val dialog = builder.create()
        if (needInputMethod()) {
            requestInputMethod(dialog)
        }
        mDialog = dialog
        return dialog
    }

    private fun requestInputMethod(dialog: AlertDialog) {
        dialog.window?.decorView?.windowInsetsController?.show(WindowInsets.Type.ime())
    }

    override fun initResource(root: ViewGroup, mainLayout: Int, widgetLayout: Int) {
        super.initResource(root, mainLayout, widgetLayout)
        mSummary = mRootView.findViewById(android.R.id.summary)
        mWidgetFrame = mRootView.findViewById(android.R.id.widget_frame)
        mArrowRight = mRootView.findViewById(R.id.arrow_right)
        LayoutInflater.from(this.context).inflate(widgetLayout, mWidgetFrame, true)
        this.background = mRootView.background
        mRootView.background = context.getDrawable(R.drawable.hyperx_preference_touch_background)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val dialog = mDialog
        if (dialog != null) {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
            mDialog = null
        }
    }
}