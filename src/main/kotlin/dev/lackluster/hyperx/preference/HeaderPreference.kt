package dev.lackluster.hyperx.preference

import android.content.Context
import android.graphics.Outline
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.TextView
import cn.fkj233.ui.R
import cn.fkj233.ui.activity.dp2px
import kotlin.math.min

class HeaderPreference : BaseWidgetPreference {
    private val mValue: TextView
    private var mLargeIcon: Boolean = false
    private var mCornerRadius: Int = 0
    private val accountIconSize: Int
    private val headerIconSize: Int

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.headerPreferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attributeSet, defStyleAttr, defStyleRes) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.HeaderPreference, defStyleAttr, defStyleRes)
        val layout = obtainStyledAttributes.getResourceId(R.styleable.HeaderPreference_android_layout, R.layout.hyperx_preference_main_layout)
        val widgetLayout = obtainStyledAttributes.getResourceId(R.styleable.HeaderPreference_android_widgetLayout, R.layout.hyperx_preference_widget_text)
        val icon = obtainStyledAttributes.getDrawable(R.styleable.HeaderPreference_android_icon)
        val title = obtainStyledAttributes.getString(R.styleable.HeaderPreference_android_title)
        val summary = obtainStyledAttributes.getString(R.styleable.HeaderPreference_android_summary)
        val singleLineTitle = obtainStyledAttributes.getBoolean(R.styleable.HeaderPreference_singleLineTitle, true)
        val showRightArrow = obtainStyledAttributes.getBoolean(R.styleable.HeaderPreference_showRightArrow, true)
        val largeIcon = obtainStyledAttributes.getBoolean(R.styleable.HeaderPreference_largeIcon, false)
        val cornerRadius = obtainStyledAttributes.getDimensionPixelSize(R.styleable.HeaderPreference_iconCornerRadius, 0)
        obtainStyledAttributes.recycle()
        accountIconSize = context.resources.getDimensionPixelSize(R.dimen.preference_icon_account_size)
        headerIconSize = context.resources.getDimensionPixelSize(R.dimen.header_icon_size)
        initResource(this, layout, widgetLayout)
        if (mIcon.parent != null) {
            val linearLayout = mIcon.parent as LinearLayout
            mIcon.minimumWidth = mIcon.context.resources.getDimensionPixelSize(R.dimen.header_icon_size)
            (mIcon.layoutParams as MarginLayoutParams).marginEnd = 0
            linearLayout.minimumWidth = 0
            (linearLayout.layoutParams as MarginLayoutParams).marginEnd = 0
        }
        mValue = mWidgetFrame.findViewById(R.id.text_right)
        mValue.maxWidth = context.resources.getDimensionPixelSize(R.dimen.preference_text_right_max_width)
        mValue.background = null
        mValue.gravity = Gravity.END
        mValue.setTextAppearance(R.style.HyperX_TextAppearance_PreferenceRight)
        mValue.visibility = View.GONE
        setIconCornerRadius(cornerRadius)
        setSingleLineTitle(singleLineTitle)
        setShowRightArrow(showRightArrow)
        setLargeIconEnabled(largeIcon)
        setIcon(icon)
        setTitle(title)
        setSummary(summary)
    }

    fun getValueLabel(): TextView {
        return mValue
    }

    fun setValue(text: CharSequence?) {
        if (!TextUtils.isEmpty(text)) {
            mValue.visibility = View.VISIBLE
            mValue.text = text
        } else {
            mValue.visibility = View.GONE
        }
    }

    fun setLargeIconEnabled(enabled: Boolean) {
        mLargeIcon = enabled
        mIcon.maxHeight = if (mLargeIcon) {
            accountIconSize
        } else {
            headerIconSize
        }
        setExtraPadding()
    }

    fun setIconCornerRadius(size: Int) {
        mCornerRadius = size
        setIcon(mIcon.drawable)
        if (size > 0) {
            mIcon.clipToOutline = true
            mIcon.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(p0: View?, p1: Outline?) {
                    if (p0 == null || p1 == null) return
                    val minLength = min(p0.width, p0.height)
                    if (mCornerRadius * 2 < minLength) {
                        p1.setRoundRect(p0.paddingLeft, 0, p0.paddingLeft + minLength, minLength, mCornerRadius.toFloat())
                    } else {
                        p1.setOval(p0.paddingLeft, 0, p0.paddingLeft + minLength, minLength)
                    }
                }
            }
            mIcon.invalidateOutline()
        } else {
            mIcon.clipToOutline = false
        }
    }

    private fun setExtraPadding() {
        val accountPadding = (accountIconSize * 1.0f / 2.0f).toInt()
        val normalPadding = (headerIconSize * 1.0f / 2.0f).toInt()
        val paddingIcon = dp2px(context, 16.0f)
        val paddingS = context.resources.getDimensionPixelSize(R.dimen.hyperx_preference_item_padding_start)
        val paddingE = context.resources.getDimensionPixelSize(R.dimen.hyperx_preference_item_padding_end)
        if (mLargeIcon) {
            mIcon.setPaddingRelative(
                paddingS + normalPadding - accountPadding,
                0,
                paddingIcon + normalPadding - accountPadding,
                0
            )
            mRootView.setPaddingRelative(
                0,
                paddingTop,
                paddingE,
                paddingBottom
            )
        } else {
            mIcon.setPaddingRelative(0, 0, paddingIcon, 0)
            mRootView.setPaddingRelative(paddingS, paddingTop, paddingE, paddingBottom)
        }
    }
}