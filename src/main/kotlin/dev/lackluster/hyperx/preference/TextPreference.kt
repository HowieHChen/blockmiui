package dev.lackluster.hyperx.preference

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextView
import cn.fkj233.ui.R

class TextPreference : BaseWidgetPreference {
    private val mValue: TextView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.textPreferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attributeSet, defStyleAttr, defStyleRes) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.TextPreference, defStyleAttr, defStyleRes)
        val layout = obtainStyledAttributes.getResourceId(R.styleable.TextPreference_android_layout, R.layout.hyperx_preference_text_layout)
        val widgetLayout = obtainStyledAttributes.getResourceId(R.styleable.TextPreference_android_widgetLayout, R.layout.hyperx_preference_widget_text)
        val icon = obtainStyledAttributes.getDrawable(R.styleable.TextPreference_android_icon)
        val title = obtainStyledAttributes.getString(R.styleable.TextPreference_android_title)
        val summary = obtainStyledAttributes.getString(R.styleable.TextPreference_android_summary)
        val singleLineTitle = obtainStyledAttributes.getBoolean(R.styleable.TextPreference_singleLineTitle, true)
        val showRightArrow = obtainStyledAttributes.getBoolean(R.styleable.TextPreference_showRightArrow, true)
        obtainStyledAttributes.recycle()
        initResource(this, layout, widgetLayout)
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
    }

    fun getValueLabel(): TextView {
        return mValue
    }

    fun getValue(): CharSequence? {
        return mValue.text
    }

    fun setValue(text: CharSequence?) {
        if (text != null && !TextUtils.isEmpty(text)) {
            mValue.visibility = View.VISIBLE
            mValue.text = text
        } else {
            mValue.visibility = View.GONE
        }
    }
}