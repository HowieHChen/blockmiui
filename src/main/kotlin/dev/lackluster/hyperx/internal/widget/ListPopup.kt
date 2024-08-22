package dev.lackluster.hyperx.internal.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.database.DataSetObserver
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.PopupWindow
import cn.fkj233.ui.R
import dev.lackluster.hyperx.core.util.HyperXUIUtils
import dev.lackluster.hyperx.internal.util.AttributeResolver
import dev.lackluster.hyperx.internal.util.DisplayHelper
import dev.lackluster.hyperx.internal.util.SinglePopControl
import dev.lackluster.hyperx.smooth.SmoothFrameLayout2
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

open class ListPopup(context: Context) : PopupWindow(context) {
    private var mAdapter: ListAdapter? = null
    private var mAnchor: WeakReference<View>? = null
    protected val mBackgroundPadding: Rect
    private val mContentSize: ContentSize
    protected var mContentView: View? = null
    private val mContext: Context
    private var mDropDownGravity: Int
    protected val mElevation: Int
    private val mElevationExtra: Int
    private var mHasShadow: Boolean
    private var mListView: ListView? = null
    protected var mMaxAllowedHeight: Int
    private var mMaxAllowedWidth: Int
    private var mMinAllowedWidth: Int
    private val mMinMarginScreen: Int
    private val mObserver: DataSetObserver
    private val mOffsetFromStatusBar: Int
    private var mOffsetX: Int
    private var mOffsetXSet: Boolean = false
    private var mOffsetY: Int
    private var mOffsetYSet: Boolean = false
    private var mOnDismissListener: OnDismissListener? = null
    private var mOnItemClickListener: AdapterView.OnItemClickListener? = null
    protected val mRootView: SmoothFrameLayout2

    init {
        mDropDownGravity = Gravity.END or Gravity.TOP
        mHasShadow = true
        mContentSize = ContentSize()
        mObserver = object : DataSetObserver() {
            override fun onChanged() {
                mContentSize.mHasContentWidth = false
                val anchor = getAnchor()
                if (!isShowing || anchor == null) {
                    return
                }
                anchor.post {
                    val checkMaxHeight = checkMaxHeight()
                    val computePopupContentWidth = computePopupContentWidth()
                    val height = if (checkMaxHeight <= 0 || mContentSize.mHeight <= checkMaxHeight) {
                        mContentSize.mHeight
                    } else {
                        checkMaxHeight
                    }
                    val location = IntArray(2)
                    anchor.getLocationInWindow(location)
                    update(anchor, calculateXoffset(anchor), calculateYoffset(anchor), computePopupContentWidth, height)
                }
            }
        }
        mContext = context
        height = -2
        val resources = context.resources
        val displayHelper = DisplayHelper(mContext)
        mMaxAllowedWidth = min(displayHelper.getWidthPixels(), resources.getDimensionPixelSize(R.dimen.hyperx_list_menu_dialog_maximum_width))
        mMinAllowedWidth = resources.getDimensionPixelSize(R.dimen.hyperx_list_menu_dialog_minimum_width)
        mMaxAllowedHeight = min(displayHelper.getHeightPixels(), resources.getDimensionPixelSize(R.dimen.hyperx_list_menu_dialog_maximum_height))
        val density = (displayHelper.getDensity() * 8.0f).toInt()
        mOffsetX = density
        mOffsetY = density
        mBackgroundPadding = Rect()
        isFocusable = true
        isOutsideTouchable = true
        mRootView = SmoothFrameLayout2(context)
        mRootView.setCornerRadius(context.resources.getDimensionPixelSize(R.dimen.hyperx_immersion_menu_background_radius).toFloat())
        mRootView.setOnClickListener {
            dismiss()
        }
        prepareContentView(context)
        animationStyle = R.style.Animation_PopupWindow_ImmersionMenu
        mElevation = context.resources.getDimensionPixelSize(R.dimen.hyperx_drop_down_menu_elevation)
        super.setOnDismissListener(object : OnDismissListener {
            override fun onDismiss() {
                mOnDismissListener?.onDismiss()
            }
        })
        mMinMarginScreen = context.resources.getDimensionPixelSize(R.dimen.hyperx_context_menu_window_margin_screen)
        mOffsetFromStatusBar = context.resources.getDimensionPixelSize(R.dimen.hyperx_context_menu_window_margin_statusbar)
        mElevationExtra = context.resources.getDimensionPixelSize(R.dimen.hyperx_menu_popup_extra_elevation)
    }
    companion object {
        fun changeWindowBackground(view: View?) {
            val layoutParams: WindowManager.LayoutParams = (view?.layoutParams as? WindowManager.LayoutParams) ?: return
            layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            layoutParams.dimAmount = 0.3f
            (view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).updateViewLayout(view, layoutParams)
        }
    }
    protected fun prepareContentView(context: Context) {
        val drawable = context.getDrawable(R.drawable.hyperx_immersion_window_bg)
        if (drawable != null) {
            drawable.getPadding(mBackgroundPadding)
            mRootView.background = drawable
        }
        setBackgroundDrawable(ColorDrawable(0))
        setPopupWindowContentView(mRootView)
    }
    protected fun setPopupWindowContentView(view: View) {
        super.setContentView(view)
    }
    override fun setOnDismissListener(onDismissListener: OnDismissListener?) {
        mOnDismissListener = onDismissListener
    }
    open fun setAdapter(listAdapter: ListAdapter?) {
        mAdapter?.unregisterDataSetObserver(mObserver)
        mAdapter = listAdapter
        listAdapter?.registerDataSetObserver(mObserver)
    }
    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        mOnItemClickListener = onItemClickListener
    }
    fun show(view: View?, viewGroup: ViewGroup) {
        if (prepareShow(view, viewGroup)) {
            showWithAnchor(view!!)
        }
    }
    protected open fun isNeedScroll(): Boolean {
        return mContentSize.mHeight > checkMaxHeight()
    }
    protected fun prepareShow(view: View?, viewGroup: ViewGroup?): Boolean {
        if (view == null) {
            return false
        }
        if (mContentView == null) {
            val inflate = LayoutInflater.from(mContext).inflate(R.layout.hyperx_list_popup_list, null)
            mContentView = inflate
            inflate.addOnLayoutChangeListener(object : OnLayoutChangeListener {
                private var lastContentHeight: Int = -1
                override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
                    val contentView = mContentView ?: return
                    val measuredHeight = contentView.measuredHeight
                    val i9 = lastContentHeight
                    if (i9 == -1 || i9 != measuredHeight) {
                        val isNeedScroll = if (mListView?.adapter != null) isNeedScroll() else true
                        contentView.isEnabled = isNeedScroll
                        mListView?.isVerticalScrollBarEnabled = isNeedScroll
                        lastContentHeight = measuredHeight
                    }
                }
            })
        }
        if (mRootView.childCount != 1 || mRootView.getChildAt(0) != mContentView) {
            mRootView.removeAllViews()
            mRootView.addView(mContentView)
            val layoutParams = mContentView!!.layoutParams as FrameLayout.LayoutParams
            layoutParams.width = -1
            layoutParams.height = -2
            layoutParams.gravity = Gravity.CENTER_VERTICAL
        }
        if (shouldSetElevation()) {
            mRootView.elevation = mElevation.toFloat()
            elevation = (mElevation + mElevationExtra).toFloat()
            setPopupShadowAlpha(mRootView)
        }
        val listView = mContentView!!.findViewById<ListView>(android.R.id.list)
        mListView = listView
        if (listView == null) {
            return false
        }
        listView.setOnItemClickListener { adapterView, view2, i2, j ->
            val headerViewsCount = i2 - mListView!!.headerViewsCount
            if (mOnItemClickListener == null || headerViewsCount < 0 || headerViewsCount >= (mAdapter?.count ?: 0)) {
                return@setOnItemClickListener
            }
            mOnItemClickListener?.onItemClick(adapterView, view2, i2, j)
        }
        mListView!!.adapter = mAdapter
        width = computePopupContentWidth()
        val checkMaxHeight = checkMaxHeight()
        height = if (checkMaxHeight > 0 && mContentSize.mHeight > checkMaxHeight) {
            checkMaxHeight
        } else {
            -2
        }
        (mContext.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
        return true
    }
    private fun shouldSetElevation(): Boolean {
        return mHasShadow
    }
    open fun setContentWidth(width: Int) {
        mContentSize.updateWidth(width)
    }
    fun setDropDownGravity(i: Int) {
        mDropDownGravity = i
    }
    fun getListView(): ListView? {
        return mListView
    }
    fun setVerticalOffset(offset: Int) {
        mOffsetY = offset
        mOffsetYSet = true
    }
    fun setHorizontalOffset(offset: Int) {
        mOffsetX = offset
        mOffsetXSet = true
    }
    fun setHasShadow(enable: Boolean) {
        mHasShadow = enable
    }
    fun getMinMarginScreen(): Int {
        return mMinMarginScreen
    }
    fun getOffsetFromStatusBar(): Int {
        return mOffsetFromStatusBar
    }
    fun getVerticalOffset(): Int {
        return mOffsetY
    }
    fun getHorizontalOffset(): Int {
        return mOffsetX
    }
    protected fun computePopupContentWidth(): Int {
        if (!mContentSize.mHasContentWidth) {
            measureContentSize(mAdapter!!,null, mContext, mMaxAllowedWidth)
        }
        val max = max(mContentSize.mWidth, mMinAllowedWidth)
        val rect = mBackgroundPadding
        return max + rect.left + rect.right
    }
    private fun showWithAnchor(view: View) {
        showAsDropDown(view, calculateXoffset(view), calculateYoffset(view), mDropDownGravity)
        changeWindowBackground(mRootView.rootView)
    }
    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        mAnchor = WeakReference<View>(anchor)
        SinglePopControl.showPop(mContext, this)
    }
    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        SinglePopControl.showPop(mContext, this)
    }
    override fun dismiss() {
        super.dismiss()
        SinglePopControl.hidePop(mContext, this)
    }
    fun getAnchor(): View? {
        val weakReference = mAnchor
        if (weakReference != null) {
            return weakReference.get()
        }
        return null
    }
    fun calculateYoffset(view: View): Int {
        val f: Float
        var i: Int
        val i2: Int
        var i3: Int = if (mOffsetYSet) mOffsetY else ((-view.height) - mBackgroundPadding.top) + mOffsetY
        val iArr = IntArray(2)
        view.getLocationInWindow(iArr)
        val f2 = iArr[1].toFloat()
        val i4 =mContext.resources.displayMetrics.heightPixels
        val iArr2 = IntArray(2)
        if (AttributeResolver.resolveBoolean(mContext, R.attr.isMiuixFloatingTheme, false)) {
            val appActivity = mContext
            if (appActivity is Activity) {
                val findViewById = appActivity.findViewById<View>(android.R.id.content)
                i = findViewById.height
                findViewById.getLocationInWindow(iArr2)
            } else {
                if (appActivity is ContextWrapper) {
                    val baseContext = appActivity.baseContext
                    if (baseContext is Activity) {
                        val findViewById = baseContext.findViewById<View>(android.R.id.content)
                        i = findViewById.height
                        findViewById.getLocationInWindow(iArr2)
                    }
                }
                i = i4
            }
            f = f2 -iArr2[1]
        } else {
            f = f2
            i = i4
        }
        val checkMaxHeight = checkMaxHeight()
        val min = if (checkMaxHeight > 9) {
            min(mContentSize.mHeight, checkMaxHeight)
        } else {
            mContentSize.mHeight
        }
        if (min < i && f + i3 + min + view.height > i) {
            i3 -= (if (mOffsetYSet) view.height else 0) + min
        }
        val iArr3 = IntArray(2)
        view.rootView.getLocationInWindow(iArr3)
        val height2 = (i3 + f2 + view.height).toInt()
        if (height2 >= iArr3[1] && height2 < (iArr2[1])) {
            i2 = iArr2[1]
            val i5 = i2 - height2
            height = min - i5
            i3 += i5
        }
        val i6 = height2 + min
        if (i6 <= iArr3[1] + i4) {
            val i7 = iArr2[1]
            if (i7 + i < i6) {
                height = min - ((i6 - i7) - i)
            }
        }
        return i3
    }
    fun calculateXoffset(view: View): Int {
        val width: Int
        val width2: Int
        var i: Int
        val iArr = IntArray(2)
        view.getLocationInWindow(iArr)
        var z = true
        if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            if (iArr[0] - mOffsetX + getWidth() + mMinMarginScreen > view.rootView.width) {
                width = view.rootView.width - getWidth() - mMinMarginScreen
                width2 = iArr[0]
                i = width - width2
            }
            i = 0
            z = false
        } else {
            if (iArr[0] + view.width + mOffsetX - getWidth() - mMinMarginScreen < 0) {
                width = getWidth() + mMinMarginScreen
                width2 = iArr[0] + view.width
                i = width - width2
            }
            i = 0
            z = false
        }
        if (z) {
            return i
        }
        val z2 = mOffsetXSet
        val i2 = if (z2) mOffsetX else 0
        if (i2 == 0 || z2) {
            return i2
        }
        if (view.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            return i2 - (mBackgroundPadding.left - mOffsetX)
        }
        return i2 + (mBackgroundPadding.right - mOffsetX)
    }
    fun fastShow(view: View, viewGroup: ViewGroup) {
        width = computePopupContentWidth()
        var h = mContentSize.mHeight
        val checkMaxHeight = checkMaxHeight()
        if (h > checkMaxHeight) {
            h = checkMaxHeight
        }
        height = h
        showWithAnchor(view)
    }
    private fun measureContentSize(listAdapter: ListAdapter, viewGroup: ViewGroup?, context: Context, maxWidth: Int) {
        val makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val count = listAdapter.count
        var viewType = 0
        var width = 0
        var height = 0
        var view: View? = null
        var viewGroup2: ViewGroup? = viewGroup
        for (index in 0 until count) {
            val itemViewType = listAdapter.getItemViewType(index)
            if (itemViewType != viewType) {
                view = null
                viewType = itemViewType
            }
            if (viewGroup2 == null) {
                viewGroup2 = FrameLayout(context)
            }
            view = listAdapter.getView(index, view, viewGroup2)
            view.measure(makeMeasureSpec, makeMeasureSpec2)
            height += view.measuredHeight
            if (!mContentSize.mHasContentWidth) {
                val measuredWidth = view.measuredWidth
                if (measuredWidth >= maxWidth) {
                    mContentSize.updateWidth(maxWidth)
                } else if (measuredWidth > width) {
                    width = measuredWidth
                }
            }
        }
        if (!mContentSize.mHasContentWidth) {
            mContentSize.updateWidth(width)
        }
        mContentSize.mHeight = height
    }
    protected fun setPopupShadowAlpha(view: View) {
        if (HyperXUIUtils.isFreeformMode(mContext)) {
            view.outlineProvider = null
        } else {
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(p0: View?, p1: Outline?) {
                    if (p0 == null || p0.width == 0|| p0.height == 0 || p1 == null) {
                        return
                    }
                    p1.alpha = p0.context.resources.getFloat(R.dimen.drop_down_menu_shadow_alpha)
                    if (p0.background != null) {
                        p0.background.getOutline(p1)
                    }
                }
            }
            view.outlineSpotShadowColor = mContext.getColor(R.color.hyperx_drop_down_menu_spot_shadow_color)
        }
    }
    fun setMaxAllowedHeight(height: Int) {
        mMaxAllowedHeight = height
    }
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    protected fun checkMaxHeight(): Int {
        val identifier = mContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = if (identifier > 0) mContext.resources.getDimensionPixelSize(identifier) else 0
        return min(mMaxAllowedHeight, DisplayHelper(mContext).getHeightPixels() - statusBarHeight)
    }
    class ContentSize {
        var mHasContentWidth: Boolean =false
        var mHeight: Int = 0
        var mWidth: Int = 0
        fun updateWidth(width: Int) {
            mWidth = width
            mHasContentWidth = true
        }
    }
}