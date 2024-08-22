package dev.lackluster.hyperx.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.content.res.Resources.Theme
import android.database.DataSetObserver
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.ThemedSpinnerAdapter
import cn.fkj233.ui.R
import dev.lackluster.hyperx.adapter.SpinnerDoubleLineContentAdapter
import dev.lackluster.hyperx.app.AlertDialog
import dev.lackluster.hyperx.core.util.WindowUtils
import dev.lackluster.hyperx.internal.adapter.SpinnerCheckableArrayAdapter
import dev.lackluster.hyperx.internal.util.TaggingDrawableUtil
import dev.lackluster.hyperx.internal.widget.ListPopup
import java.lang.reflect.Field
import kotlin.math.max
import kotlin.math.min

class Spinner : Spinner {
    var mDropDownMaxWidth: Int = 0
    var mDropDownMinWidth: Int = 0
    var mDropDownWidth: Int = 0
    private var mOnSpinnerDismissListener: OnSpinnerDismissListener? = null
    private var mPopup: SpinnerPopup? = null
    private val mPopupContext: Context
    private val mPopupSet: Boolean
    private var mPressAnimAdded: Boolean
    private var mTempAdapter: SpinnerAdapter? = null
    val mTempRect: Rect
    private var mXRelative: Float = 0.0f
    private var mYRelative: Float = 0.0f

    @SuppressLint("DiscouragedPrivateApi")
    companion object {
        private var FORWARDING_LISTENER: Field?
        init {
            try {
                val field = android.widget.Spinner::class.java.getDeclaredField("mForwardingListener")
                FORWARDING_LISTENER = field
                field.isAccessible = true
            } catch (_: NoSuchFieldException) {
                FORWARDING_LISTENER = null
            }
        }
    }

    constructor(context: Context) : this(context, null, 0, R.style.Widget_Spinner)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0, R.style.Widget_Spinner)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.Widget_Spinner)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr) {
        mPressAnimAdded = false
        mTempRect = Rect()
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.Spinner, defStyleAttr, defStyleRes)
        val popupTheme = obtainStyledAttributes.getResourceId(R.styleable.Spinner_popupTheme, 0)
        mPopupContext = if (popupTheme != 0) {
            ContextThemeWrapper(context,popupTheme)
        } else {
            context
        }
        val mode = obtainStyledAttributes.getInt(R.styleable.Spinner_spinnerModeCompat, 0)
        val prompt = obtainStyledAttributes.getString(R.styleable.Spinner_android_prompt)
        if (mode == 0) {
            val dialogPopup = DialogPopup()
            mPopup = dialogPopup
            dialogPopup.setPromptText(prompt)
        } else if (mode == 1) {
            val dropdownPopup = DropdownPopup(mPopupContext, attributeSet, defStyleAttr)
            val obtainStyledAttributes2 = mPopupContext.obtainStyledAttributes(attributeSet, R.styleable.Spinner, defStyleAttr, 0)
            mDropDownWidth = obtainStyledAttributes2.getLayoutDimension(R.styleable.Spinner_android_dropDownWidth, -2)
            mDropDownMinWidth = obtainStyledAttributes2.getLayoutDimension(R.styleable.Spinner_dropDownMinWidth, -2)
            mDropDownMaxWidth = obtainStyledAttributes2.getLayoutDimension(R.styleable.Spinner_dropDownMaxWidth, -2)
            val res2 = obtainStyledAttributes2.getResourceId(R.styleable.Spinner_android_popupBackground, 0)
            if (res2 != 0) {
                setPopupBackgroundResource(res2)
            } else {
                dropdownPopup.setBackgroundDrawable(obtainStyledAttributes2.getDrawable(R.styleable.Spinner_android_popupBackground))
            }
            dropdownPopup.setPromptText(obtainStyledAttributes.getString(R.styleable.Spinner_android_prompt))
            obtainStyledAttributes2.recycle()
            mPopup = dropdownPopup
        }
        makeSupperForwardingListenerInvalid()
        val textArray = obtainStyledAttributes.getTextArray(R.styleable.Spinner_android_entries)
        if (textArray != null) {
            val arrayAdapter = ArrayAdapter(context, R.layout.hyperx_simple_spinner_layout, android.R.id.text1, textArray)
            arrayAdapter.setDropDownViewResource(R.layout.hyperx_simple_spinner_dropdown_item)
            adapter = arrayAdapter
        }
        obtainStyledAttributes.recycle()
        mPopupSet = true
        val spinnerAdapter = mTempAdapter
        if (spinnerAdapter != null) {
            adapter = spinnerAdapter
            mTempAdapter = null
        }
    }
    fun onSpinnerDismiss() {
        notifySpinnerDismiss()
    }
    private fun notifySpinnerDismiss() {
        val onSpinnerDismissListener = mOnSpinnerDismissListener
        onSpinnerDismissListener?.onSpinnerDismiss()
    }
    private fun makeSupperForwardingListenerInvalid() {
        val field = FORWARDING_LISTENER ?: return
        try {
            field.set(this, null)
        } catch (_: Throwable){
        }
    }
    override fun getPopupContext(): Context {
        return mPopupContext
    }
    override fun setPopupBackgroundDrawable(background: Drawable?) {
        val spinnerPopup = mPopup
        if (spinnerPopup != null) {
            spinnerPopup.setBackgroundDrawable(background)
        } else {
            super.setPopupBackgroundDrawable(background)
        }
    }
    override fun setBackgroundResource(resid: Int) {
        setPopupBackgroundDrawable(popupContext.getDrawable(resid))
    }
    override fun getPopupBackground(): Drawable? {
        val spinnerPopup = mPopup
        if (spinnerPopup != null) {
            return spinnerPopup.getBackground()
        }
        return super.getPopupBackground()
    }
    override fun setDropDownVerticalOffset(pixels: Int) {
        val spinnerPopup = mPopup
        if (spinnerPopup != null) {
            spinnerPopup.setVerticalOffset(pixels)
        } else {
            super.setDropDownVerticalOffset(pixels)
        }
    }
    override fun getDropDownVerticalOffset(): Int {
        val spinnerPopup = mPopup
        if (spinnerPopup != null) {
            return spinnerPopup.getVerticalOffset()
        }
        return super.getDropDownVerticalOffset()
    }
    override fun setDropDownHorizontalOffset(pixels: Int) {
        val spinnerPopup = mPopup
        if (spinnerPopup != null) {
            spinnerPopup.setHorizontalOriginalOffset(pixels)
            spinnerPopup.setHorizontalOffset(pixels)
        } else {
            super.setDropDownHorizontalOffset(pixels)
        }
    }
    override fun getDropDownHorizontalOffset(): Int {
        val spinnerPopup = mPopup
        if (spinnerPopup != null) {
            return spinnerPopup.getHorizontalOffset()
        }
        return super.getDropDownHorizontalOffset()
    }
    override fun setDropDownWidth(pixels: Int) {
        if (mPopup != null) {
            mDropDownWidth = pixels
        } else {
            super.setDropDownWidth(pixels)
        }
    }
    override fun getDropDownWidth(): Int {
        return if (mPopup != null) {
            mDropDownWidth
        } else {
            super.getDropDownWidth()
        }
    }
    fun setDoubleLineContentAdapter(spinnerDoubleLineContentAdapter: SpinnerDoubleLineContentAdapter) {
        adapter = SpinnerCheckableArrayAdapter(
            context,
            R.layout.hyperx_simple_spinner_layout,
            spinnerDoubleLineContentAdapter,
            SpinnerCheckedProvider()
        )
    }
    override fun setAdapter(adapter: SpinnerAdapter?) {
        if (!mPopupSet) {
            mTempAdapter = adapter
            return
        }
        super.setAdapter(adapter)
        val spinnerPopup = mPopup
        if (spinnerPopup is DialogPopup) {
            spinnerPopup.setAdapter(DialogPopupAdapter(adapter, popupContext.theme))
        } else if (spinnerPopup is DropdownPopup) {
            spinnerPopup.setAdapter(DropDownPopupAdapter(adapter, popupContext.theme))
        }
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val popup = mPopup
        if (popup == null || !popup.isShowing()) {
            return
        }
        popup.dismiss()
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mPopup == null || MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.AT_MOST) {
            return
        }
        setMeasuredDimension(
            min(min(measuredWidth, compatMeasureSelectItemWidth(adapter,background)), MeasureSpec.getSize(widthMeasureSpec)),
            measuredHeight
        )
    }
    fun setOnSpinnerDismissListener(onSpinnerDismissListener: OnSpinnerDismissListener?) {
        mOnSpinnerDismissListener = onSpinnerDismissListener
    }
    override fun performClick(): Boolean {
        val point = IntArray(2)
        getLocationInWindow(point)
        return performClick(point[0].toFloat(), point[1].toFloat())
    }
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        post {
            val spinnerPopup = mPopup
            if (spinnerPopup != null && spinnerPopup.isShowing() && spinnerPopup is DropdownPopup) {
                val windowsSize = WindowUtils.getWindowSize(popupContext)
                showPopup(windowsSize.x * mXRelative, windowsSize.y + mYRelative)
            }
        }
    }
    fun performClick(x: Float, y: Float): Boolean {
        val windowSize = WindowUtils.getWindowSize(popupContext)
        mXRelative = x / windowSize.x
        mYRelative = y / windowSize.y
        if (superViewPerformClick()) {
            return true
        }
        if (mPopup != null) {
            clearCachedSize()
            if (mPopup?.isShowing() == false) {
                showPopup(x, y)
            }
            return true
        }
        return super.performClick()
    }
    fun setFenceX(i: Int) {
        val dropdownPopup = mPopup
        if (dropdownPopup is DropdownPopup) {
            dropdownPopup.setFenceX(i)
        }
    }
    fun setFenceXFromView(view: View) {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        setFenceX(location[0])
    }
    private fun clearCachedSize() {
        val dropdownPopup = mPopup
        if (dropdownPopup !is DropdownPopup || dropdownPopup.height <= 0) {
            return
        }
        dropdownPopup.height = -2
        dropdownPopup.width = -2
    }
    private fun superViewPerformClick(): Boolean {
        sendAccessibilityEvent(0)
        return false
    }
    override fun setPrompt(prompt: CharSequence?) {
        val spinnerPopup = mPopup
        if (spinnerPopup != null) {
            spinnerPopup.setPromptText(prompt)
        } else {
            super.setPrompt(prompt)
        }
    }
    override fun getPrompt(): CharSequence? {
        val spinnerPopup = mPopup
        return if (spinnerPopup != null) {
            spinnerPopup.getHintText()
        } else {
            super.getPrompt()
        }
    }
//    override fun setSelection(position: Int) {
//        super.setSelection(position)
//    }
    private fun compatMeasureSelectItemWidth(adapter: SpinnerAdapter?, drawable: Drawable?): Int {
        if (adapter == null || adapter.count == 0) {
            return 0
        }
        val makeMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.UNSPECIFIED)
        val makeMeasureSpec2 = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.UNSPECIFIED)
        val view = adapter.getView(max(0, selectedItemPosition), null, this)
        if (view.layoutParams == null) {
            view.layoutParams = LayoutParams(-2, -2)
        }
        view.measure(makeMeasureSpec, makeMeasureSpec2)
        val max = max(0, view.measuredWidth)
        if (drawable == null) {
            return max
        }
        drawable.getPadding(mTempRect)
        return max + mTempRect.left + mTempRect.right
    }
    fun compatMeasureContentWidth(spinnerAdapter: SpinnerAdapter?, drawable: Drawable?): Int {
        if (spinnerAdapter == null) {
            return 0
        }
        val makeMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.UNSPECIFIED)
        val makeMeasureSpec2 = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.UNSPECIFIED)
        val max = max(0, selectedItemPosition)
        val min = min(spinnerAdapter.count, max + 15)
        var i = 0
        var i2 = 0
        var view: View? = null
        for (max2 in max(0, (max - (15 - (min - max)))) until min) {
            val itemViewType = spinnerAdapter.getItemViewType(max2)
            if (itemViewType != i) {
                view = null
                i = itemViewType
            }
            view = spinnerAdapter.getView(max2, view, this)
            if (view.layoutParams == null) {
                view.layoutParams = LayoutParams(-2, -2)
            }
            view.measure(makeMeasureSpec, makeMeasureSpec2)
            i2 = max(i2, view.measuredWidth)
        }
        if (drawable == null) {
            return i2
        }
        drawable.getPadding(mTempRect)
        return i2 + mTempRect.left + mTempRect.right
    }
    fun showPopup() {
        val location = IntArray(2)
        getLocationInWindow(location)
        showPopup(location[0].toFloat(), location[1].toFloat())
    }
    fun showPopup(x: Float, y: Float) {
        mPopup?.show(textDirection, textAlignment, x, y)
    }
    fun dismissPopup() {
        mPopup?.dismiss()
    }
    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        val spinnerPopup = mPopup
        savedState.mShowDropdown = spinnerPopup != null && spinnerPopup.isShowing()
        return savedState
    }
    override fun onRestoreInstanceState(state: Parcelable?) {
        val observer = viewTreeObserver
        val savedState = state as SavedState
        super.onRestoreInstanceState(state.superState)
        if (!savedState.mShowDropdown || observer == null) {
            return
        }
        observer.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (mPopup?.isShowing() == false) {
                    showPopup()
                }
                viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })
    }

    inner class SpinnerCheckedProvider : SpinnerCheckableArrayAdapter.CheckedStateProvider {
        override fun isChecked(index: Int): Boolean {
            return this@Spinner.selectedItemPosition == index
        }
    }
    private class SavedState : BaseSavedState {
        var mShowDropdown: Boolean = false
        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(p0: Parcel?): SavedState {
                return SavedState(p0)
            }
            override fun newArray(p0: Int): Array<SavedState> {
                return newArray(p0)
            }
        }
        constructor(parcelable: Parcelable?): super(parcelable)
        constructor(parcel: Parcel?): super(parcel) {
            mShowDropdown = parcel?.readByte() != 0.toByte()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeByte(if (mShowDropdown) 1.toByte() else 0.toByte())
        }
    }
    interface OnSpinnerDismissListener {
        fun onSpinnerDismiss()
    }

    interface SpinnerPopup {
        fun dismiss()
        fun getBackground(): Drawable?
        fun getHintText(): CharSequence?
        fun getHorizontalOffset(): Int
        fun getVerticalOffset(): Int
        fun isShowing(): Boolean
        fun setAdapter(listAdapter: ListAdapter?)
        fun setBackgroundDrawable(drawable: Drawable?)
        fun setHorizontalOffset(offset: Int)
        fun setHorizontalOriginalOffset(offset: Int)
        fun setPromptText(charSequence: CharSequence?)
        fun setVerticalOffset(offset: Int)
        fun show(i: Int, i2: Int, f: Float, f2: Float)
    }
    inner class DialogPopup : SpinnerPopup, DialogInterface.OnClickListener {
        private var mListAdapter: ListAdapter? = null
        var mPopup: AlertDialog? = null
        private var mPrompt: CharSequence? = null
        override fun dismiss() {
            val alertDialog = mPopup
            if (alertDialog != null) {
                alertDialog.dismiss()
                mPopup = null
            }
        }
        override fun getBackground(): Drawable? {
            return null
        }
        override fun getHorizontalOffset(): Int {
            return 0
        }
        override fun getVerticalOffset(): Int {
            return 0
        }
        override fun isShowing(): Boolean {
            val alertDialog = mPopup
            return alertDialog != null && alertDialog.isShowing
        }
        override fun setAdapter(listAdapter: ListAdapter?) {
            mListAdapter = listAdapter
        }
        override fun setBackgroundDrawable(drawable: Drawable?) {
        }
        override fun setHorizontalOffset(offset: Int) {
        }
        override fun setHorizontalOriginalOffset(offset: Int) {
        }
        override fun setPromptText(charSequence: CharSequence?) {
            mPrompt = charSequence
        }
        override fun setVerticalOffset(offset: Int) {
        }
        override fun show(i: Int, i2: Int, f: Float, f2: Float) {
            show(i, i2)
        }
        override fun onClick(p0: DialogInterface?, p1: Int) {
            this@Spinner.setSelection(p1)
            if (this@Spinner.onItemClickListener != null) {
                this@Spinner.performItemClick(null, p1, mListAdapter?.getItemId(p1) ?: return)
            }
            dismiss()
        }
        override fun getHintText(): CharSequence? {
            return mPrompt
        }
        fun show(i: Int, i2: Int) {
            if (mListAdapter == null) {
                return
            }
            val builder = AlertDialog.Builder(this@Spinner.popupContext)
            mPrompt?.let { builder.setTitle(it) }
            val dialog = builder
                .setSingleChoiceItems(mListAdapter, this@Spinner.selectedItemPosition, this)
                .setOnDismissListener {
                    this@Spinner.onSpinnerDismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            mPopup = dialog
            val listView = dialog.getListView()
            listView?.textDirection = i
            listView?.textAlignment = i2
            mPopup?.show()
        }
    }
    inner class DropdownPopup(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int
    ) : ListPopup(context), SpinnerPopup {
        var mAdapter: ListAdapter? = null
        private var mFenceView: View? = null
        private var mFenceX: Int
        private var mHintText: CharSequence? = null
        private val mMarginScreen: Int
        private val mMarginScreenVertical: Int
        private var mMaxListHeight: Int = 0
        private var mMaxListWidth: Int
        private var mOriginalHorizontalOffset: Int = 0
        private val mVisibleRect: Rect

        init {
            mVisibleRect = Rect()
            mFenceX = -1
            mMarginScreen = context.resources.getDimensionPixelSize(R.dimen.hyperx_spinner_margin_screen_horizontal)
            mMaxListWidth = context.resources.getDimensionPixelSize(R.dimen.hyperx_spinner_max_width)
            mMarginScreenVertical = context.resources.getDimensionPixelSize(R.dimen.hyperx_spinner_margin_screen_vertical)
            setDropDownGravity(Gravity.START or Gravity.TOP)
            setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(adapterView: AdapterView<*>?, view: View?, i: Int, j: Long) {
                    this@Spinner.setSelection(i)
                    if (this@Spinner.onItemClickListener != null) {
                        this@Spinner.performItemClick(view, i, mAdapter?.getItemId(i) ?: return)
                    }
                    dismiss()
                }
            })
        }
        override fun setAdapter(listAdapter: ListAdapter?) {
            super.setAdapter(listAdapter)
            mAdapter = listAdapter
        }
        override fun getHintText(): CharSequence? {
            return mHintText
        }
        override fun setPromptText(charSequence: CharSequence?) {
            mHintText = charSequence
        }
        override fun setContentWidth(width: Int) {
            super.setContentWidth(max(min(width, this@Spinner.mDropDownMaxWidth), this@Spinner.mDropDownMinWidth))
        }
        override fun isNeedScroll(): Boolean {
            val listView = getListView() ?: return false
            if (listView.firstVisiblePosition != 0 || listView.lastVisiblePosition != listView.adapter.count - 1) {
                return true
            }
            var h = 0
            for (index in 0..listView.lastVisiblePosition) {
                h += listView.getChildAt(index).measuredHeight
            }
            return listView.measuredHeight < h
        }
        fun computeContentWidth() {
            val i: Int
            val horizontalOriginalOffset: Int
            val background = getBackground()
            if (background != null) {
                background.getPadding(this@Spinner.mTempRect)
                i = if (this@Spinner.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    this@Spinner.mTempRect.right
                } else {
                    this@Spinner.mTempRect.left
                }
            } else {
                val rect = this@Spinner.mTempRect
                rect.left = 0
                rect.right = 0
                i = 0
            }
            val paddingLeft = this@Spinner.paddingLeft
            val paddingRight = this@Spinner.paddingRight
            val width = this@Spinner.width
            when (val i2 = this@Spinner.mDropDownWidth) {
                -2 -> {
                    var compatMeasureContentWidth = this@Spinner.compatMeasureContentWidth(mAdapter as SpinnerAdapter, getBackground())
                    val i3 = this@Spinner.context.resources.displayMetrics.widthPixels
                    val rect2 = this@Spinner.mTempRect
                    val i4 = (i3 -rect2.left) - rect2.right
                    val i5 = mMarginScreen
                    val i6 = i4 - (i5 * 2)
                    if (compatMeasureContentWidth > i6) {
                        compatMeasureContentWidth = i6
                    }
                    setContentWidth(max(compatMeasureContentWidth, ((width - paddingLeft) - paddingRight) - (i5 * 2)))
                }
                -1 -> {
                    setContentWidth(((width - paddingLeft) - paddingRight) - (mMarginScreen * 2))
                }
                else -> {
                    setContentWidth(i2)
                }
            }
            horizontalOriginalOffset = if (this@Spinner.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                i + (((width - paddingRight) - getWidth()) - getHorizontalOriginalOffset())
            } else {
                i + paddingLeft + getHorizontalOriginalOffset()
            }
            setHorizontalOffset(horizontalOriginalOffset)
        }
        private fun checkInFloatingWindowMode() {
            if (mFenceView == null) {
                return
            }
        }
        private fun isInSplitScreenDown(i: Int, i2: Int, i3: Int, i4: Int): Boolean {
            return i2 != i && (i - i2) + i4 > (i3 * 3) / 4
        }
        private fun isInSplitScreenRight(i: Int, i2: Int, i3: Int, i4: Int): Boolean {
            if (i == i2) {
                return false
            }
            val i5 = i - i2
            val i6 = i3 - i4
            return i5 == i6 || i5 > i6
        }
        private fun showWithAnchor(view: View) {
            val iArr = IntArray(2)
            view.getLocationOnScreen(iArr)
            var i = iArr[0]
            var i2 = iArr[1]
            view.getLocationInWindow(iArr)
            val i3 = iArr[0]
            val i4 = iArr[1]
            val view2 = mFenceView ?: view.rootView
            view2.getLocationInWindow(iArr)
            val i5 = iArr[0]
            val i6 = iArr[1]
            val point = Point()
            WindowUtils.getScreenSize(this@Spinner.context, point)
            val i7 = point.x
            val i8 = point.y
            WindowUtils.getWindowSize(this@Spinner.context, point)
            val i9 = point.x
            val i10 = point.y
            if (i7 != i9 || i8 != i10) {
                if (isInSplitScreenRight(i, i3, i7, i9)) {
                    i -= i7 - i9
                }
                if (isInSplitScreenDown(i2, i4, i8, view2.height)) {
                    i2 -= i8 - i10
                }
            }
            val i11 = getxInWindow(i3, view.width, i5, view.width, i9, i)
            val f = getyInWindow(i4, view.height, i6, view.height, i10, i2)
            if (!isShowing) {
                showAtLocation(view, 0, i11, f.toInt())
                changeWindowBackground(mRootView.rootView)
            } else {
                update(i11, f.toInt(), width, height)
            }
        }
        private fun getxInWindow(i: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int): Int {
            val width = width
            val mMaxListWidth = mMaxListWidth
            var mMaxListWidth2 = width
            if (width > mMaxListWidth) {
                setWidth(mMaxListWidth)
                mMaxListWidth2 = mMaxListWidth
            }
            val i7 = i6 + i2
            val mMarginScreen = mMarginScreen
            var b = true
            val b2 = i6 + mMaxListWidth2 + mMarginScreen <= i5
            if (i7 - mMaxListWidth2 - mMarginScreen < 0) {
                b = false
            }
            if (!b2) {
                if (b) {
                    if (i7 <= i5 - mMarginScreen) {
                        return i + i2 - mMaxListWidth2
                    }
                } else if (i5 - i7 >= (i5 - i2) / 2) {
                    return i3 + mMarginScreen
                }
                return i4 + i3 - mMarginScreen - mMaxListWidth2
            }
            if (i6 >= mMarginScreen) {
                return i
            }
            return i3 + mMarginScreen
        }
        private fun getyInWindow(height: Int, mMarginScreenVertical: Int, i: Int, i2: Int, i3: Int, i4: Int): Float {
            val totalListHeight = getTotalListHeight()
            val mMaxListHeight = mMaxListHeight
            var height2 = totalListHeight
            if (totalListHeight > mMaxListHeight) {
                height2 = mMaxListHeight
            }
            setHeight(height2)
            val n2 = i + i2
            val i5 = height + mMarginScreenVertical
            val i6 = mMarginScreenVertical + i4
            val marginScreenVertical = this.mMarginScreenVertical
            var i7: Float
            if (i6 + height2 < i3 - marginScreenVertical) {
                i7 = i5.toFloat()
                if (i6 < marginScreenVertical){
                    i7 = (i + marginScreenVertical).toFloat()
                }
            } else if (i4 - height2 > marginScreenVertical) {
                i7 = (height - height2).toFloat()
                if (i4 > i3 - marginScreenVertical) {
                    i7 = (n2 - marginScreenVertical - height2).toFloat()
                }
            } else if (i6 < marginScreenVertical) {
                i7 = (i + marginScreenVertical).toFloat()
                setHeight(i3 - mMaxListHeight * 2)
            } else if (i4 > i3 -marginScreenVertical) {
                i7 = (n2 - marginScreenVertical - height2).toFloat()
                setHeight(i3 - mMaxListHeight * 2)
            } else if (i4 < i3 / 2) {
                i7 = i5.toFloat()
                setHeight(i3 - marginScreenVertical - i6)
            } else {
                val i8 = i4 - marginScreenVertical
                setHeight(i8)
                i7 = (height - i8).toFloat()
            }
            return i7
        }
        private fun getTotalListHeight(): Int {
            val listView = getListView()
            val width = width
            if (listView != null && listView.adapter != null) {
                val adapter = listView.adapter
                val count = adapter.count
                val i = count.coerceAtMost(8)
                var i2 = 0
                for (i3 in 0 until count) {
                    val view = adapter.getView(i3, null, listView)
                    view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                    i2 += view.measuredHeight
                    if (i3 == i - 1) {
                        mMaxListHeight = i2
                    }
                }
                return i2
            }
            mContentView?.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val measuredHeight = mContentView?.measuredHeight ?: 0
            mMaxListHeight = measuredHeight
            return measuredHeight
        }

        override fun show(i: Int, i2: Int, f: Float, f2: Float) {
            checkInFloatingWindowMode()
            val isShowing = isShowing
            computeContentWidth()
            inputMethodMode = 2
            if (prepareShow(this@Spinner, null)) {
                showWithAnchor(this@Spinner)
            }
            initListView(i, i2)
            if (isShowing) {
                return
            }
            setOnDismissListener {
                this@Spinner.onSpinnerDismiss()
            }
        }

        private fun initListView(i: Int, i2: Int) {
            val listView = getListView()
            listView?.let {
                it.choiceMode = 1
                it.textDirection = i
                it.textAlignment = i2
                val selected = this@Spinner.selectedItemPosition
                it.setSelection(selected)
                it.setItemChecked(selected, true)
            }
        }
        fun setFenceX(i: Int) {
            mFenceX = i
        }
        fun setFenceView(view: View) {
            mFenceView = view
        }
        override fun setHorizontalOriginalOffset(offset: Int) {
            mOriginalHorizontalOffset = offset
        }
        fun getHorizontalOriginalOffset(): Int {
            return mOriginalHorizontalOffset
        }
    }
    open class DropDownAdapter(spinnerAdapter: SpinnerAdapter?, theme: Theme?): ListAdapter, SpinnerAdapter {
        private val mAdapter: SpinnerAdapter?
        private val mListAdapter: ListAdapter?
        init {
            mAdapter = spinnerAdapter
            mListAdapter = if (spinnerAdapter is ListAdapter) {
                spinnerAdapter
            } else {
                null
            }
            if (theme != null) {
                if (spinnerAdapter is ThemedSpinnerAdapter) {
                    if (spinnerAdapter.dropDownViewTheme != theme) {
                        spinnerAdapter.dropDownViewTheme = theme
                    }
                }
            }
        }
        override fun getCount(): Int {
            return mAdapter?.count ?: 0
        }
        override fun getItem(p0: Int): Any? {
            return mAdapter?.getItem(p0)
        }
        override fun getItemId(p0: Int): Long {
            return mAdapter?.getItemId(p0) ?: -1L
        }
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
            return getDropDownView(p0, p1, p2)
        }
        override fun getItemViewType(p0: Int): Int {
            return 0
        }
        override fun getViewTypeCount(): Int {
            return 1
        }
        override fun getDropDownView(p0: Int, p1: View?, p2: ViewGroup?): View? {
            return mAdapter?.getDropDownView(p0, p1, p2)
        }
        override fun hasStableIds(): Boolean {
            return mAdapter?.hasStableIds() == true
        }
        override fun registerDataSetObserver(p0: DataSetObserver) {
            mAdapter?.registerDataSetObserver(p0)
        }
        override fun unregisterDataSetObserver(p0: DataSetObserver?) {
            mAdapter?.unregisterDataSetObserver(p0)
        }
        override fun areAllItemsEnabled(): Boolean {
            return mListAdapter?.areAllItemsEnabled() ?: true
        }
        override fun isEnabled(p0: Int): Boolean {
            return mListAdapter?.isEnabled(p0) ?: true
        }
        override fun isEmpty(): Boolean {
            return count == 0
        }
    }
    class DialogPopupAdapter(
        spinnerAdapter: SpinnerAdapter?,
        theme: Theme
    ) : DropDownAdapter(spinnerAdapter, theme)
    class DropDownPopupAdapter(
        spinnerAdapter: SpinnerAdapter?,
        theme: Theme
    ) : DropDownAdapter(spinnerAdapter, theme) {
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
            val view2 = super.getView(p0, p1, p2)
            TaggingDrawableUtil.updateItemBackground(view2, p0, count)
            return view2
        }
    }
}