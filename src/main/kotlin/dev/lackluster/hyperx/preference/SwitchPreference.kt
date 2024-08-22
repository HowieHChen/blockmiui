package dev.lackluster.hyperx.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton.OnCheckedChangeListener
import cn.fkj233.ui.R
import dev.lackluster.hyperx.widget.Switch

class SwitchPreference : BaseWidgetPreference {
    private val mSwitch: Switch
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null
    private val mTrainsOnCheckedChangeListener: OnCheckedChangeListener?

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.switchPreferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attributeSet, defStyleAttr, defStyleRes) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SwitchPreference, defStyleAttr, defStyleRes)
        val layout = obtainStyledAttributes.getResourceId(R.styleable.SwitchPreference_android_layout, R.layout.hyperx_preference_layout)
        val widgetLayout = obtainStyledAttributes.getResourceId(R.styleable.SwitchPreference_android_widgetLayout, R.layout.hyperx_preference_widget_switch)
        val icon = obtainStyledAttributes.getDrawable(R.styleable.SwitchPreference_android_icon)
        val title = obtainStyledAttributes.getString(R.styleable.SwitchPreference_android_title)
        val summary = obtainStyledAttributes.getString(R.styleable.SwitchPreference_android_summary)
        val singleLineTitle = obtainStyledAttributes.getBoolean(R.styleable.SwitchPreference_singleLineTitle, true)
        val showRightArrow = obtainStyledAttributes.getBoolean(R.styleable.SwitchPreference_showRightArrow, false)
        obtainStyledAttributes.recycle()
        initResource(this, layout, widgetLayout)
        mSwitch = mWidgetFrame.findViewById(android.R.id.switch_widget)
        setIcon(icon)
        setTitle(title)
        setSummary(summary)
        setSingleLineTitle(singleLineTitle)
        setShowRightArrow(showRightArrow)
        mTrainsOnCheckedChangeListener = OnCheckedChangeListener { p0, p1 ->
            mOnCheckedChangeListener?.onCheckedChanged(p0, p1)
        }
        setOnCheckedChangeListener(mTrainsOnCheckedChangeListener)
        setOnClickListener {
            if (mSwitch.isEnabled) {
                mSwitch.toggle()
            }
        }
    }

    fun getSwitch(): Switch {
        return mSwitch
    }

    fun setChecked(checked: Boolean) {
        mSwitch.isChecked = checked
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        if (mTrainsOnCheckedChangeListener == listener) {
            mSwitch.setOnCheckedChangeListener(mTrainsOnCheckedChangeListener)
        } else {
            mOnCheckedChangeListener = listener
        }
    }
}