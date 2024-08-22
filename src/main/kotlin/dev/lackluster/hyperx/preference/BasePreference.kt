package dev.lackluster.hyperx.preference

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.fkj233.ui.R

abstract class BasePreference : LinearLayout {
    lateinit var mRootView: View
    lateinit var mIcon: ImageView
    lateinit var mTitle: TextView
    constructor(context: Context) : this(context, null, 0, R.style.HyperX_Preference)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.preferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes)

    fun setTitle(text: CharSequence?) {
        if (text != null && !TextUtils.isEmpty(text)) {
            mTitle.visibility = View.VISIBLE
            mTitle.text = text
        } else {
            mTitle.visibility = View.GONE
        }
    }

    fun setIcon(drawable: Drawable?) {
        if (drawable != null) {
            mIcon.visibility = View.VISIBLE
            mIcon.setImageDrawable(drawable)
        } else {
            mIcon.visibility = View.GONE
        }
    }

    fun setSingleLineTitle(isSingleLine: Boolean) {
        mTitle.isSingleLine = isSingleLine
    }

    protected open fun initResource(root: ViewGroup, mainLayout: Int, widgetLayout: Int) {
        val inflate = LayoutInflater.from(root.context).inflate(mainLayout, root, false)
        mRootView = inflate
        mIcon = inflate.findViewById(android.R.id.icon)
        mTitle = inflate.findViewById(android.R.id.title)
        this.addView(mRootView)
    }
}