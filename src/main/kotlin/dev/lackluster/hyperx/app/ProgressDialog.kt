package dev.lackluster.hyperx.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import cn.fkj233.ui.R
import dev.lackluster.hyperx.widget.ProgressBar
import java.text.NumberFormat


class ProgressDialog : AlertDialog {
    private var mHasStarted = false
    private var mIncrementBy = 0
    private var mIncrementSecondaryBy = 0
    private var mIndeterminate = false
    private var mIndeterminateDrawable: Drawable? = null
    private var mMax = 0
    private var mMessage: CharSequence? = null
    private var mMessageView: TextView? = null
    private var mProgress: ProgressBar? = null
    private var mProgressDrawable: Drawable? = null
    private val mProgressNumberFormat: String
    private var mProgressPercentFormat: NumberFormat? = null
    private var mProgressPercentView: TextView? = null
    private var mProgressStyle = 0
    private var mProgressVal = 0
    private var mSecondaryProgressVal = 0
    private var mViewUpdateHandler: Handler? = null
    constructor(context: Context) : super(context) {
        mProgressStyle = 0
        mProgressNumberFormat = "%1d/%2d"
        val percentInstance = NumberFormat.getPercentInstance()
        mProgressPercentFormat = percentInstance
        percentInstance.maximumFractionDigits = 0
    }
    constructor(context: Context, themeResId: Int): super(context, themeResId) {
        mProgressStyle = 0
        mProgressNumberFormat = "%1d/%2d"
        val percentInstance = NumberFormat.getPercentInstance()
        mProgressPercentFormat = percentInstance
        percentInstance.maximumFractionDigits = 0
    }

    companion object {
        fun show(
            context: Context,
            title: CharSequence?,
            message: CharSequence?
        ): ProgressDialog {
            return show(context, title, message, false)
        }

        fun show(
            context: Context,
            title: CharSequence?,
            message: CharSequence?,
            cancelable: Boolean
        ): ProgressDialog {
            return show(context, title, message, cancelable, false, null)
        }

        fun show(
            context: Context,
            title: CharSequence?,
            message: CharSequence?,
            indeterminate: Boolean,
            cancelable: Boolean,
            onCancelListener: DialogInterface.OnCancelListener?
        ): ProgressDialog {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle(title)
            progressDialog.setMessage(message)
            progressDialog.setIndeterminate(indeterminate)
            progressDialog.setCancelable(cancelable)
            progressDialog.setOnCancelListener(onCancelListener)
            progressDialog.show()
            return progressDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val inflate: View
        val from = LayoutInflater.from(context)
        val obtainStyledAttributes = context.obtainStyledAttributes(null, R.styleable.AlertDialog, android.R.attr.alertDialogStyle, 0)
        val obtainStyledAttributes2 = context.theme.obtainStyledAttributes(intArrayOf(R.attr.dialogProgressPercentColor))
        val color = obtainStyledAttributes2.getColor(0, context.getColor(R.color.hyperx_dialog_default_progress_percent_color))
        obtainStyledAttributes2.recycle()
        if (mProgressStyle == 1) {
            mViewUpdateHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    this@ProgressDialog.mMessageView?.text = (this@ProgressDialog.mMessage)
                    if (this@ProgressDialog.mProgressPercentFormat == null || this@ProgressDialog.mProgressPercentView == null) {
                        return
                    }
                    val spannableStringBuilder = SpannableStringBuilder()
                    val format = this@ProgressDialog.mProgressPercentFormat!!.format(
                        this@ProgressDialog.mProgressVal / this@ProgressDialog.mProgress!!.max
                    )
                    spannableStringBuilder.append(format)
                    spannableStringBuilder.setSpan(ForegroundColorSpan(color), 0, format.length, 34)
                    this@ProgressDialog.mProgress?.progress = this@ProgressDialog.mProgressVal
                    this@ProgressDialog.mProgressPercentView!!.text = spannableStringBuilder
                }
            }
            inflate = from.inflate(
                obtainStyledAttributes.getResourceId(
                    R.styleable.AlertDialog_horizontalProgressLayout,
                    R.layout.hyperx_alert_dialog_progress
                ), null
            )
            mProgressPercentView = inflate.findViewById<TextView>(R.id.progress_percent)
        } else {
            inflate = from.inflate(
                obtainStyledAttributes.getResourceId(
                    R.styleable.AlertDialog_progressLayout,
                    R.layout.hyperx_progress_dialog
                ), null
            )
        }
        mProgress = inflate.findViewById(android.R.id.progress)
        val textView = inflate.findViewById<TextView>(R.id.message)
        mMessageView = textView
        textView.lineHeight = context.resources.getDimensionPixelSize(R.dimen.hyperx_dialog_message_line_height)
        setView(inflate)
        obtainStyledAttributes.recycle()
        val i = mMax
        if (i > 0) {
            setMax(i)
        }
        val i2 = mProgressVal
        if (i2 > 0) {
            setProgress(i2)
        }
        val i3 = mSecondaryProgressVal
        if (i3 > 0) {
            setSecondaryProgress(i3)
        }
        val i4 = mIncrementBy
        if (i4 > 0) {
            incrementProgressBy(i4)
        }
        val i5 = mIncrementSecondaryBy
        if (i5 > 0) {
            incrementSecondaryProgressBy(i5)
        }
        mProgressDrawable?.let { setProgressDrawable(it) }
        mIndeterminateDrawable?.let { setIndeterminateDrawable(it) }
        mMessage?.let { setMessage(it) }
        setIndeterminate(mIndeterminate)
        onProgressChanged()
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mHasStarted = true
    }

    override fun onStop() {
        super.onStop()
        mHasStarted = false
    }

    fun setProgress(i: Int) {
        mProgressVal = i
        if (mHasStarted) {
            onProgressChanged()
        }
    }

    fun setSecondaryProgress(i: Int) {
        val progressBar = mProgress
        if (progressBar != null) {
            progressBar.secondaryProgress = i
            onProgressChanged()
        } else {
            mSecondaryProgressVal = i
        }
    }

    fun setMax(i: Int) {
        val progressBar = mProgress
        if (progressBar != null) {
            progressBar.max = i
            onProgressChanged()
        } else {
            mMax = i
        }
    }

    fun incrementProgressBy(i: Int) {
        val progressBar = mProgress
        if (progressBar != null) {
            progressBar.incrementProgressBy(i)
            onProgressChanged()
        } else {
            mIncrementBy += i
        }
    }

    fun incrementSecondaryProgressBy(i: Int) {
        val progressBar = mProgress
        if (progressBar != null) {
            progressBar.incrementSecondaryProgressBy(i)
            onProgressChanged()
        } else {
            mIncrementSecondaryBy += i
        }
    }

    fun setProgressDrawable(drawable: Drawable?) {
        val progressBar = mProgress
        if (progressBar != null) {
            progressBar.progressDrawable = drawable
        } else {
            mProgressDrawable = drawable
        }
    }

    fun setIndeterminateDrawable(drawable: Drawable?) {
        val progressBar = mProgress
        if (progressBar != null) {
            progressBar.indeterminateDrawable = drawable
        } else {
            mIndeterminateDrawable = drawable
        }
    }

    fun setIndeterminate(z: Boolean) {
        val progressBar = mProgress
        if (progressBar != null) {
            progressBar.isIndeterminate = z
        } else {
            mIndeterminate = z
        }
    }

    override fun setMessage(charSequence: CharSequence?) {
        if (mProgress != null) {
            if (mProgressStyle == 1) {
                mMessage = charSequence
            }
            mMessageView?.text = charSequence
            return
        }
        mMessage = charSequence
    }

    fun setProgressStyle(i: Int) {
        mProgressStyle = i
    }

    private fun onProgressChanged() {
        val handler = mViewUpdateHandler
        if (mProgressStyle != 1 || handler == null || handler.hasMessages(0)) {
            return
        }
        handler.sendEmptyMessage(0)
    }
}