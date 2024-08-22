package dev.lackluster.hyperx.preference

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.fkj233.ui.R

abstract class BaseWidgetPreference : BasePreference {
    lateinit var mSummary: TextView
    lateinit var mWidgetFrame: ViewGroup
    lateinit var mArrowRight: ImageView

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

    override fun initResource(root: ViewGroup, mainLayout: Int, widgetLayout: Int) {
        super.initResource(root, mainLayout, widgetLayout)
        mSummary = mRootView.findViewById(android.R.id.summary)
        mWidgetFrame = mRootView.findViewById(android.R.id.widget_frame)
        mArrowRight = mRootView.findViewById(R.id.arrow_right)
        LayoutInflater.from(this.context).inflate(widgetLayout, mWidgetFrame, true)
        this.background = mRootView.background
        mRootView.background = context.getDrawable(R.drawable.hyperx_preference_touch_background)
    }
}