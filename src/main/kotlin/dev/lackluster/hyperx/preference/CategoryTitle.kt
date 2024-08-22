package dev.lackluster.hyperx.preference

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import cn.fkj233.ui.R
import dev.lackluster.hyperx.internal.graphics.drawable.TaggingDrawable

class CategoryTitle : FrameLayout {
    var mRootView: View
    var mTitle: TextView
    private var mNoTitle: Boolean = false
    private var mNoLine: Boolean = false
    private var mTitleText: CharSequence? = null

    private var baseState:  IntArray
    companion object {
        private val STATES_TAGS = intArrayOf(R.attr.state_no_title, R.attr.state_no_line)
        private val STATE_NO_TITLE = intArrayOf(R.attr.state_no_title)
        private val STATE_NO_LINE = intArrayOf(R.attr.state_no_line)
    }
    constructor(context: Context) : this(context, null, 0, R.style.HyperX_Preference_Category)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.preferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CategoryTitle, defStyleAttr, defStyleRes)
        val layout = obtainStyledAttributes.getResourceId(R.styleable.CategoryTitle_android_layout, R.layout.hyperx_preference_category_layout)
        val title = obtainStyledAttributes.getString(R.styleable.CategoryTitle_android_title)
        val noTitle = obtainStyledAttributes.getBoolean(R.styleable.CategoryTitle_state_no_title, false)
        val noLine = obtainStyledAttributes.getBoolean(R.styleable.CategoryTitle_state_no_line, false)
        obtainStyledAttributes.recycle()
        val inflate = LayoutInflater.from(context).inflate(layout, this, false)
        mRootView = inflate
        mTitle = inflate.findViewById(android.R.id.title)
        this.addView(mRootView)
        baseState = mRootView.background.state
        setTitle(title)
        setStyle(noTitle, noLine)
    }

    fun setStyle(hideTitle: Boolean = false, hideLine: Boolean = false) {
        mNoTitle = hideTitle
        mNoLine = hideLine
        setTitle(mTitleText)
        mRootView.minimumHeight = if (mNoTitle && mNoLine) 0 else 1
        updateBackground()
    }

    fun setTitle(text: CharSequence?) {
        mTitleText = text
        if (!mNoTitle && text != null && !TextUtils.isEmpty(text)) {
            mTitle.visibility = View.VISIBLE
            mTitle.text = text
        } else {
            mTitle.visibility = View.GONE
        }
    }

//    override fun onCreateDrawableState(extraSpace: Int): IntArray {
//        var drawableState = super.onCreateDrawableState(extraSpace + 2)
////        if (mNoTitle) {
////            drawableState = mergeDrawableStates(drawableState, STATE_NO_TITLE)
////        }
////        if (mNoLine) {
////            drawableState = mergeDrawableStates(drawableState, STATE_NO_LINE)
////        }
//        return drawableState
//    }

    private fun updateBackground() {
        val stateLine: IntArray
        val stateTitle: IntArray
        if (mNoLine) {
            stateLine = IntArray(STATE_NO_LINE.size)
            System.arraycopy(STATE_NO_LINE, 0, stateLine, 0,  STATE_NO_LINE.size)
        } else {
            stateLine = IntArray(0)
        }
        if (mNoTitle) {
            stateTitle = IntArray(STATE_NO_TITLE.size)
            System.arraycopy(STATE_NO_TITLE, 0, stateTitle, 0, STATE_NO_TITLE.size)
        } else {
            stateTitle = IntArray(0)
        }
        val finalState = IntArray(stateLine.size + stateTitle.size)
        System.arraycopy(stateLine, 0, finalState, 0, stateLine.size)
        System.arraycopy(stateTitle, 0, finalState, stateLine.size, stateTitle.size)
        var background = mRootView.background
        if (background is LayerDrawable) {
            background.setLayerInset(0, 0, 0, 0, 0)
            background.colorFilter = null
            val taggingDrawable = TaggingDrawable(background)
            mRootView.background = taggingDrawable
            taggingDrawable.colorFilter = null
            background = taggingDrawable
        }
        if (background is StateListDrawable && TaggingDrawable.containsTagState(background, STATES_TAGS)) {
            val taggingDrawable = TaggingDrawable(background)
            mRootView.background = taggingDrawable
            background = taggingDrawable
        }
        if (background is TaggingDrawable) {
            background.setTaggingState(finalState)
            val rect = Rect()
            if (background.getPadding(rect)) {
                background.colorFilter = null
            }
            mRootView.setPadding(rect.left, rect.top, rect.right, rect.bottom)
        }
    }
}