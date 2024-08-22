package dev.lackluster.hyperx.preference

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import cn.fkj233.ui.R

class EditTextPreference : BaseDialogPreference {
    private val mValue: TextView
    private var mValueInternal: Any? = null
    private var mEditText: EditText? = null
    private var mHintText: CharSequence? = null
    private var mConvertor: ValueConvertor = object : ValueConvertor {
        override fun toString(value: Any): String {
            return value.toString()
        }
        override fun fromString(string: String): Any {
            return string
        }
    }
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.editTextPreferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attributeSet, defStyleAttr, defStyleRes) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.DialogPreference, defStyleAttr, defStyleRes)
        val layout = obtainStyledAttributes.getResourceId(R.styleable.DialogPreference_android_layout, R.layout.hyperx_preference_text_layout)
        val widgetLayout = obtainStyledAttributes.getResourceId(R.styleable.DialogPreference_android_widgetLayout, R.layout.hyperx_preference_widget_text)
        val icon = obtainStyledAttributes.getDrawable(R.styleable.DialogPreference_android_icon)
        val title = obtainStyledAttributes.getString(R.styleable.DialogPreference_android_title)
        val summary = obtainStyledAttributes.getString(R.styleable.DialogPreference_android_summary)
        val singleLineTitle = obtainStyledAttributes.getBoolean(R.styleable.DialogPreference_singleLineTitle, true)
        val showRightArrow = obtainStyledAttributes.getBoolean(R.styleable.DialogPreference_showRightArrow, true)
        obtainStyledAttributes.recycle()
        initResource(this, layout, widgetLayout)
        setDialogLayoutRes(R.layout.hyperx_alert_dialog_edit_text)
        mValue = mWidgetFrame.findViewById(R.id.text_right)
        mValue.maxWidth = context.resources.getDimensionPixelSize(R.dimen.preference_text_right_max_width)
        mValue.background = null
        mValue.gravity = Gravity.END
        mValue.setTextAppearance(R.style.HyperX_TextAppearance_PreferenceRight)
        mValue.visibility = View.GONE
        setIcon(icon)
        setTitle(title)
        setSummary(summary)
        setSingleLineTitle(singleLineTitle)
        setShowRightArrow(showRightArrow)
        setOnClickListener { _ ->
            createDialog().show()
        }
    }

    override fun needInputMethod(): Boolean {
        return true
    }

    override fun onBindDialogView(view: View): Boolean {
        val superResult = super.onBindDialogView(view)
        val editText = view.findViewById<EditText>(android.R.id.edit)
        mEditText = editText
        editText.requestFocus()
        editText.setText(getValue().toString())
        editText.setSelection(editText.text.length)
        editText.hint = mHintText
        return superResult && editText != null
    }

    fun getValueView(): TextView {
        return mValue
    }

    fun getEditText(): EditText? {
        return mEditText
    }

    fun getValue(): Any? {
        return mValueInternal
    }

    fun getValueText(): CharSequence? {
        return mValue.text
    }

    fun setValue(value: Any?) {
        mValueInternal = value
        val text = value?.let { mConvertor.toString(it) }
        if (text != null && !TextUtils.isEmpty(text)) {
            mValue.visibility = View.VISIBLE
            mValue.text = text
        } else {
            mValue.visibility = View.GONE
        }
    }

    fun setHintText(text: CharSequence?) {
        mHintText = text
    }

    fun setValueText(text: CharSequence?) {
        if (text != null && !TextUtils.isEmpty(text)) {
            mValueInternal = mConvertor.fromString(text.toString())
            mValue.visibility = View.VISIBLE
            mValue.text = text
        } else {
            mValueInternal = null
            mValue.visibility = View.GONE
        }
    }

    fun setValueInternal(value: Any?) {
        mValueInternal = value
    }
    fun setValueConvertor(convertor: ValueConvertor) {
        mConvertor = convertor
    }

    fun getValueConvertor(): ValueConvertor {
        return mConvertor
    }

    interface ValueConvertor {
        fun toString(value: Any): String
        fun fromString(string: String): Any?
    }
}