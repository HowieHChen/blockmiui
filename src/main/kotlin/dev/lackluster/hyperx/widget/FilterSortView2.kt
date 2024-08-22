package dev.lackluster.hyperx.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import cn.fkj233.ui.R
import dev.lackluster.hyperx.core.util.EnvStateManager.getWindowSize
import dev.lackluster.hyperx.os.DeviceHelper
import dev.lackluster.hyperx.widget.internal.TabViewContainerView


class FilterSortView2 : HorizontalScrollView {
    private val mDeviceType: Int
    private var mEnabled = false
    private var mFilteredId: Int
    protected var mIsParentApplyBlur: Boolean
    private val mLayoutConfig: Int
    private var mTabCount: Int
    private val mTabViewChildIds: ArrayList<Int>
    private var mTabViewContainerView: TabViewContainerView? = null

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, R.attr.filterSortView2Style)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        mTabViewChildIds = ArrayList()
        mFilteredId = -1
        mIsParentApplyBlur = false
        mTabCount = 0
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.FilterSortView2, defStyleAttr, R.style.Widget_FilterSortView2)
        val drawable = obtainStyledAttributes.getDrawable(R.styleable.FilterSortView2_filterSortViewBackground)
        mEnabled = obtainStyledAttributes.getBoolean(R.styleable.FilterSortView2_android_enabled, true)
        mLayoutConfig = obtainStyledAttributes.getInt(R.styleable.FilterSortView2_layoutConfig, 0)
        obtainStyledAttributes.recycle()
        initContentView()
        background = drawable
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mDeviceType = DeviceHelper.detectType(context)
        overScrollMode = 2
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        val density = this.context.resources.displayMetrics.density
        val mLayoutConfig = mLayoutConfig
        var tabViewLayoutMode = 1
        when (mLayoutConfig) {
            0 -> {
                val n3 = (size * 1.0f / density).toInt()
                val n4 = (getWindowSize(this.context).x * 1.0f / density).toInt()
                if (!(mDeviceType == 2 && n3 > 410 && n4 > 670)) {
                    tabViewLayoutMode = 0
                }
            }
            1 -> {
                val n5 = (getWindowSize(this.context).x * 1.0f / density).toInt()
                if (!(mDeviceType == 2 && n5 > 670)) {
                    tabViewLayoutMode = 0
                }
            }
            3 -> {
            }
            else -> {
                tabViewLayoutMode = 0
            }
        }
        mTabViewContainerView!!.setTabViewLayoutMode(tabViewLayoutMode)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
    override fun addView(child: View?) {
        addView(child, -1)
    }
    override fun addView(child: View?, index: Int) {
        requireNotNull(child) { "Cannot add a null child view to a ViewGroup" }
        var layoutParams: ViewGroup.LayoutParams? = child.layoutParams
        if (layoutParams == null) {
            layoutParams = generateDefaultLayoutParams()
        }
        addView(child, index, layoutParams)
    }
    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        addView(child, -1, null)
    }
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (mTabViewContainerView == child) {
            super.addView(child, index, params)
        } else {
            checkView(child)
            addTab(child as TabView, index)
        }
    }
    private fun checkView(view: View?) {
        require(view is TabView) { "Illegal View! Only support TabView!" }
    }
    fun setParentApplyBlur(z: Boolean) {
        if (mIsParentApplyBlur != z) {
            mIsParentApplyBlur = z
        }
        val tabViewContainerView = mTabViewContainerView
        if (tabViewContainerView != null) {
            val childCount = tabViewContainerView.childCount
            for (i in 0 until childCount) {
                val childAt = tabViewContainerView.getChildAt(i)
                if (childAt is TabView) {
                    childAt.isSelected = z
                }
            }
        }
    }
    private fun initContentView() {
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val tabViewContainerView = TabViewContainerView(context)
        tabViewContainerView.layoutParams = layoutParams
        tabViewContainerView.isHorizontalScrollBarEnabled = false
        mTabViewContainerView = tabViewContainerView
        addView(mTabViewContainerView)
    }
    fun getEnabled(): Boolean {
        return mEnabled
    }
    fun addTabViewChildId(i: Int) {
        mTabViewChildIds.add(i)
    }
    fun clearTabViewChildIds() {
        mTabViewChildIds.clear()
    }
    private fun addTab(tabView: TabView, i: Int) {
        tabView.isEnabled = mEnabled
        tabView.isSelected = mIsParentApplyBlur
        addTabViewAt(tabView, i)
        mTabViewChildIds.add(tabView.id)
    }
    fun setFilteredTab(tabView: TabView) {
        if (mFilteredId != tabView.id) {
            mFilteredId = tabView.id
        }
        tabView.setFiltered(true)
        updateChildIdsFromXml()
    }
    protected fun addTabViewAt(tabView: TabView?, i: Int) {
        if (tabView != null) {
            if (i > mTabCount || i < 0) {
                mTabViewContainerView!!.addView(tabView, -1, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            } else {
                mTabViewContainerView!!.addView(tabView, i, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            }
            mTabCount++
        }
    }
    protected fun getTabViewAt(i: Int): TabView? {
        if (i <= -1) {
            return null
        }
        val childAt = mTabViewContainerView!!.getChildAt(
            mTabViewContainerView!!.childCount - mTabCount + i
        )
        return if (childAt is TabView) {
            childAt
        } else null
    }
    protected fun removeAllTabViews() {
        for (i in 0 until mTabViewContainerView!!.childCount) {
            val childAt = mTabViewContainerView!!.getChildAt(i)
            if (childAt is TabView) {
                mTabViewContainerView!!.removeView(childAt)
            }
        }
        clearTabViewChildIds()
        mTabCount = 0
    }
    protected fun getTabCount(): Int {
        return mTabCount
    }
    fun setFilteredTab(i: Int) {
        val tabViewAt = getTabViewAt(i)
        if (tabViewAt != null) {
            if (mFilteredId != tabViewAt.id) {
                mFilteredId = tabViewAt.id
            }
            tabViewAt.setFiltered(true)
        }
        updateChildIdsFromXml()
    }
    protected fun updateChildIdsFromXml() {
        if (mTabViewChildIds.isEmpty()) {
            val childCount = mTabViewContainerView!!.childCount
            for (i in 0 until childCount) {
                val childAt = mTabViewContainerView!!.getChildAt(i)
                if (childAt is TabView) {
                    mTabViewChildIds.add(childAt.id)
                }
            }
            requestLayout()
        }
    }
    override fun setEnabled(z: Boolean) {
        super.setEnabled(z)
        if (mEnabled != z) {
            mEnabled = z
            refreshTabState()
        }
    }
    private fun refreshTabState() {
        for (i in 0 until mTabViewContainerView!!.childCount) {
            val childAt = mTabViewContainerView!!.getChildAt(i)
            if (childAt is TabView) {
                childAt.setEnabled(mEnabled)
            }
        }
    }

    class TabView : FrameLayout {
        private var mActivatedTextAppearanceId: Int = 0
        private var mArrow: ImageView? = null
        private var mArrowIcon: Drawable? = null
        private var mDescending: Boolean = false
        private var mDescendingEnabled: Boolean
        private var mFiltered: Boolean = false
        private var mIndicatorVisibility: Int = 0
        private var mTextAppearanceId: Int = 0
        private var mTextView: TextView? = null
        constructor(context: Context): this(context, null)
        constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, R.attr.filterSortTabView2Style)
        constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
            mDescendingEnabled = true
            LayoutInflater.from(context).inflate(getTabLayoutResource(), this, true)
            val textView = findViewById<TextView>(android.R.id.text1)
            mTextView = textView
            textView?.maxLines = 1
            textView?.ellipsize = TextUtils.TruncateAt.END
            mArrow = findViewById(R.id.arrow)
            if (attributeSet != null) {
                val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.FilterSortTabView2, defStyleAttr, R.style.Widget_FilterSortTabView2)
                val string = obtainStyledAttributes.getString(R.styleable.FilterSortTabView2_android_text)
                val z = obtainStyledAttributes.getBoolean(R.styleable.FilterSortTabView2_descending, true)
                mIndicatorVisibility = obtainStyledAttributes.getInt(R.styleable.FilterSortTabView2_indicatorVisibility, 0)
                mArrowIcon = obtainStyledAttributes.getDrawable(R.styleable.FilterSortTabView2_arrowFilterSortTabView)
                background = obtainStyledAttributes.getDrawable(R.styleable.FilterSortTabView2_filterSortTabViewBackground)
                foreground = obtainStyledAttributes.getDrawable(R.styleable.FilterSortTabView2_filterSortTabViewForeground)
                val dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(
                    R.styleable.FilterSortTabView2_filterSortTabViewHorizontalPadding,
                    R.dimen.hyperx_filter_sort_tab_view2_padding_horizontal
                )
                val dimensionPixelSize2 = obtainStyledAttributes.getDimensionPixelSize(
                    R.styleable.FilterSortTabView2_filterSortTabViewVerticalPadding,
                    R.dimen.hyperx_filter_sort_tab_view2_padding_vertical
                )
                findViewById<View>(R.id.container).setPadding(
                    dimensionPixelSize,
                    dimensionPixelSize2,
                    dimensionPixelSize,
                    dimensionPixelSize2
                )
                mTextAppearanceId = obtainStyledAttributes.getResourceId(R.styleable.FilterSortTabView2_filterSortTabViewTabTextAppearance, 0)
                mActivatedTextAppearanceId = obtainStyledAttributes.getResourceId(R.styleable.FilterSortTabView2_filterSortTabViewTabActivatedTextAppearance, 0)
                obtainStyledAttributes.recycle()
                initView(string, z)
            }
            if (id == -1) {
                id = generateViewId()
            }
        }
        fun setTextAppearance(i: Int) {
            mTextAppearanceId = i
            updateTextAppearance()
        }
        fun setActivatedTextAppearance(i: Int) {
            mActivatedTextAppearanceId = i
            updateTextAppearance()
        }
        private fun updateTextAppearance() {
            if (mTextView != null) {
                if (isFiltered()) {
                    TextViewCompat.setTextAppearance(mTextView!!, mActivatedTextAppearanceId)
                } else {
                    TextViewCompat.setTextAppearance(mTextView!!, mTextAppearanceId)
                }
            }
        }
        protected fun getTabLayoutResource(): Int {
            return R.layout.hyperx_filter_sort_tab_view_2
        }
        fun getTextView(): TextView? {
            return mTextView
        }
        fun setTextView(textView: TextView?) {
            mTextView = textView
        }
        fun getIconView(): ImageView? {
            return mArrow
        }
        fun setIconView(imageView: ImageView?) {
            mArrow = imageView
        }
        protected fun initView(charSequence: CharSequence?, z: Boolean) {
            mArrow?.background = mArrowIcon
            mTextView?.text = charSequence
            mArrow?.visibility = mIndicatorVisibility
            setDescending(z)
            updateTextAppearance()
        }
        fun setFiltered(z: Boolean) {
            var tabView: TabView
            val viewGroup = parent as? ViewGroup
            if (z && viewGroup != null) {
                val childCount = viewGroup.childCount
                for (i in 0 until childCount) {
                    val childAt = viewGroup.getChildAt(i)
                    if (childAt is TabView) {
                        tabView = childAt
                        if (tabView != this && tabView.mFiltered) {
                            tabView.setFiltered(false)
                        }
                    }
                }
            }
            mFiltered = z
            updateTextAppearance()
            mTextView?.isActivated = z
            mArrow?.isActivated = z
            isActivated = z
            if (viewGroup != null && z) {
                viewGroup.post {
                }
            }
        }
        fun isFiltered(): Boolean {
            return mFiltered
        }
        private fun setDescending(z: Boolean) {
            mDescending = z
            if (z) {
                mArrow?.rotationX = 0.0f
            } else {
                mArrow?.rotationX = 180.0f
            }
        }
        override fun setOnClickListener(l: OnClickListener?) {
            super.setOnClickListener {
                if (this.mFiltered) {
                    if (this.mDescendingEnabled) {
                        setDescending(!this.mDescending)
                    }
                } else {
                    setFiltered(true)
                }
                l?.onClick(it)
            }
        }
        override fun setEnabled(z: Boolean) {
            super.setEnabled(z)
            mTextView?.isEnabled = z
        }
    }
}