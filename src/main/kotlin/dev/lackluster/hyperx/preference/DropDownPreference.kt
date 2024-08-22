package dev.lackluster.hyperx.preference

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import cn.fkj233.ui.R
import dev.lackluster.hyperx.adapter.SpinnerDoubleLineContentAdapter
import dev.lackluster.hyperx.internal.adapter.SpinnerCheckableArrayAdapter
import dev.lackluster.hyperx.widget.Spinner

class DropDownPreference : BaseWidgetPreference {
    private companion object val EMPTY = arrayOfNulls<CharSequence?>(0)
    private var mAdapter: ArrayAdapter<Any>
    private var mContentAdapter: ArrayAdapter<Any>
    private var mEntries: Array<CharSequence?>? = null
    private var mEntryIcons: Array<Drawable?>? = null
    private var mEntryValues: Array<CharSequence?>? = null
    private val mItemSelectedListener: AdapterView.OnItemSelectedListener
    private var mOnItemSelectedListener: ((String, Int) -> Unit)? = null
    private val mNotifyHandler: Handler
    private val mSpinner: Spinner
    private var mValue: String = ""
    private var mValueSet: Boolean = false
    
    constructor(context: Context) : this(context, null)
    constructor(context: Context, mode: Int) : this(context, null, R.attr.dropdownPreferenceStyle, 0, mode)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.dropdownPreferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : this(context, attributeSet, defStyleAttr, defStyleRes, -1)
    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("Deprecation")
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int, mode: Int): super(context, attributeSet, defStyleAttr, defStyleRes) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.DropDownPreference, defStyleAttr, defStyleRes)
        val layout = obtainStyledAttributes.getResourceId(R.styleable.DropDownPreference_android_layout, R.layout.hyperx_preference_layout)
        val widgetLayout = when (mode) {
            0 -> R.layout.hyperx_preference_widget_spinner_dialog
            1 -> R.layout.hyperx_preference_widget_spinner
            else -> obtainStyledAttributes.getResourceId(R.styleable.DropDownPreference_android_widgetLayout, R.layout.hyperx_preference_widget_spinner)
        }
        val icon = obtainStyledAttributes.getDrawable(R.styleable.DropDownPreference_android_icon)
        val title = obtainStyledAttributes.getString(R.styleable.DropDownPreference_android_title)
        val summary = obtainStyledAttributes.getString(R.styleable.DropDownPreference_android_summary)
        val singleLineTitle = obtainStyledAttributes.getBoolean(R.styleable.DropDownPreference_singleLineTitle, false)
        val showRightArrow = obtainStyledAttributes.getBoolean(R.styleable.DropDownPreference_showRightArrow, false)
        obtainStyledAttributes.recycle()
        initResource(this, layout, widgetLayout)
        mSpinner = mWidgetFrame.findViewById(R.id.spinner)
        mWidgetFrame.minimumWidth = 100
        setIcon(icon)
        setTitle(title)
        setSummary(summary)
        setSingleLineTitle(singleLineTitle)
        setShowRightArrow(showRightArrow)
        mNotifyHandler = Handler()
        mItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val entryValues = mEntryValues ?: return
                if (p2 >= 0 && p2 < entryValues.size) {
                    mNotifyHandler.post {
                        val newValue = entryValues[p2].toString()
                        if (newValue == getValue()) {
                            return@post
                        }
                        setValue(newValue)
                        mOnItemSelectedListener?.invoke(newValue, p2)
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        mContentAdapter = DropDownLayoutAdapter(context, attributeSet, defStyleAttr, defStyleRes)
        mAdapter = createAdapter()
        constructEntries()

        mSpinner.importantForAccessibility = 2
        disableSpinnerClick(mSpinner)
        mSpinner.adapter = mAdapter
        mSpinner.onItemSelectedListener = null
        mSpinner.setSelection(findSpinnerIndexOfValue(getValue()))
        mSpinner.onItemSelectedListener = mItemSelectedListener
        mSpinner.setOnSpinnerDismissListener(object : Spinner.OnSpinnerDismissListener {
            override fun onSpinnerDismiss() {
                isSelected = false
            }
        })
        setOnTouchListener { view, motionEvent ->
            if (motionEvent?.action == MotionEvent.ACTION_UP) {
                val touchX: Float = motionEvent.x
                val touchY: Float = motionEvent.y
                val maxX = width.toFloat()
                val maxY = height.toFloat()
                if (touchX in 0.0f..maxX && touchY in 0.0f..maxY) {
                    val rawX = motionEvent.rawX
                    val rawY = motionEvent.rawY
                    mSpinner.setFenceXFromView(view!!)
                    mSpinner.performClick(rawX, rawY)
                    isSelected = true
                    mTitle.isSelected = false
                    mSummary.isSelected = false
                }
            }
            false
        }
        setOnClickListener {  }
    }
    private fun constructEntries() {
        val dropDownLayoutAdapter = mContentAdapter
        if (dropDownLayoutAdapter is DropDownLayoutAdapter) {
            mEntries = dropDownLayoutAdapter.getEntries()
            mEntryValues = dropDownLayoutAdapter.getEntryValues()
            mEntryIcons = dropDownLayoutAdapter.getEntryIcons()
            return
        }
        val count = dropDownLayoutAdapter.count
        mEntries = Array<CharSequence?>(count) {
            dropDownLayoutAdapter.getItem(it).toString()
        }
        mEntryValues = mEntries
        mEntryIcons = null
    }
    fun createAdapter(): ArrayAdapter<Any> {
        return SpinnerCheckableArrayAdapter(context, mContentAdapter, PreferenceCheckedProvider(mContentAdapter))
    }
    fun setAdapter(arrayAdapter: ArrayAdapter<Any>) {
        mContentAdapter = arrayAdapter
        mAdapter = createAdapter()
        constructEntries()
    }
    fun setValue(str: String) {
        val changed = !TextUtils.equals(mValue, str)
        if (changed || !mValueSet) {
            mValue = str
            mValueSet = true
            if (changed) {
                notifyChanged()
            }
        }
    }
    fun getValue(): String {
        return mValue
    }
    fun setPrompt(charSequence: CharSequence?) {
        mSpinner.prompt = charSequence
    }
    fun getPrompt(): CharSequence? {
        return mSpinner.prompt
    }
    override fun onSaveInstanceState(): Parcelable {
        val onSaveInstanceState = super.onSaveInstanceState()
        val savedState = SavedState(onSaveInstanceState)
        savedState.mValue = getValue()
        return savedState
    }
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        setValue(state.mValue)
    }
    fun notifyChanged() {
        mNotifyHandler.post {
            mAdapter.notifyDataSetChanged()
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun performClick(): Boolean {
        return mSpinner.performClick()
    }
    private fun disableSpinnerClick(spinner: Spinner) {
        spinner.isClickable = false
        spinner.isLongClickable = false
        spinner.isContextClickable = false
    }
    fun setSummaries(charSequenceArr: Array<CharSequence?>?) {
        val dropDownLayoutAdapter = mContentAdapter
        if (dropDownLayoutAdapter is DropDownLayoutAdapter) {
            dropDownLayoutAdapter.setSummaries(charSequenceArr)
            notifyChanged()
        }
    }
    fun setEntryIcons(iArr: IntArray?) {
        val dropDownLayoutAdapter = mContentAdapter
        if (dropDownLayoutAdapter is DropDownLayoutAdapter) {
            dropDownLayoutAdapter.setEntryIcons(iArr)
            mEntryIcons = dropDownLayoutAdapter.getEntryIcons()
        }
        notifyChanged()
    }
    fun setEntries(charSequenceArr: Array<CharSequence?>?) {
        mEntries = charSequenceArr
        val dropDownLayoutAdapter = mContentAdapter
        if (dropDownLayoutAdapter is DropDownLayoutAdapter) {
            dropDownLayoutAdapter.setEntries(charSequenceArr)
        } else {
            dropDownLayoutAdapter.clear()
            dropDownLayoutAdapter.addAll(charSequenceArr)
            mEntryValues = mEntries
        }
        mSpinner.setSelection(findSpinnerIndexOfValue(getValue()))
        notifyChanged()
    }
    fun setEntries(resId: Int) {
        setEntries(context.resources.getTextArray(resId))
    }
    fun getEntries(): Array<CharSequence?>? {
        return mEntries
    }
    fun setEntryValues(charSequenceArr: Array<CharSequence?>?) {
        val dropDownLayoutAdapter = mContentAdapter
        if (dropDownLayoutAdapter is DropDownLayoutAdapter) {
            dropDownLayoutAdapter.setEntryValues(charSequenceArr)
            mAdapter.notifyDataSetChanged()
            mEntryValues = charSequenceArr
        }
    }
    fun setEntryValues(resId: Int) {
        setEntries(context.resources.getTextArray(resId))
    }
    fun getEntryValues(): Array<CharSequence?>? {
        val dropDownLayoutAdapter = mContentAdapter
        if (dropDownLayoutAdapter is DropDownLayoutAdapter) {
            return dropDownLayoutAdapter.getEntryValues()
        }
        return EMPTY
    }
    fun getValueIndex(): Int {
        return findIndexOfValue(mValue)
    }
    fun setValueIndex(index: Int) {
        val entryValues = mEntryValues ?: return
        setValue(entryValues[index].toString())
        mSpinner.setSelection(index)
    }
    fun findIndexOfValue(str: String): Int {
        return findSpinnerIndexOfValue(str)
    }
    private fun findSpinnerIndexOfValue(str: String): Int {
        val entryValues = mEntryValues ?: return -1
        for (index in entryValues.indices) {
            if (TextUtils.equals(entryValues[index], str)) {
                return index
            }
        }
        return -1
    }
    fun setOnItemSelectedListener(listener: ((String, Int) -> Unit)? = null) {
        mOnItemSelectedListener = listener
    }
    fun getSpinner(): Spinner {
        return mSpinner
    }
    class DropDownLayoutAdapter(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ): SpinnerDoubleLineContentAdapter(context, 0) {
        private var mValues: Array<CharSequence?>?
        init {
            val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.DropDownPreference, defStyleAttr, defStyleRes)
            mEntries = obtainStyledAttributes.getTextArray(R.styleable.DropDownPreference_android_entries)
            mValues = obtainStyledAttributes.getTextArray(R.styleable.DropDownPreference_android_entryValues)
            mSummaries = obtainStyledAttributes.getTextArray(R.styleable.DropDownPreference_entrySummaries)
            val entryIcons = obtainStyledAttributes.getResourceId(R.styleable.DropDownPreference_entryIcons, -1)
            obtainStyledAttributes.recycle()
            val iArr: IntArray?
            if (entryIcons > 0) {
                val obtainTypedArray = context.resources.obtainTypedArray(entryIcons)
                iArr = IntArray(obtainTypedArray.length())
                for (index in 0..< obtainTypedArray.length()) {
                    iArr[index] = obtainTypedArray.getResourceId(index, 0)
                }
                obtainTypedArray.recycle()
            } else {
                iArr = null
            }
            setEntryIcons(iArr)
        }
        fun setEntryValues(charSequenceArr: Array<CharSequence?>?) {
            mValues = charSequenceArr
        }
        fun getEntryValues(): Array<CharSequence?>? {
            return mValues
        }
    }

    inner class PreferenceCheckedProvider(
        private val mAdapter: ArrayAdapter<Any>
    ): SpinnerCheckableArrayAdapter.CheckedStateProvider {
        override fun isChecked(index: Int): Boolean {
            val entryValues = this@DropDownPreference.mEntryValues ?: return false
            if (index > entryValues.size || index < 0) {
                return false
            }
            return TextUtils.equals(this@DropDownPreference.getValue(), entryValues[index])
        }
    }

    private class SavedState : BaseSavedState {
        var mValue: String = ""
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
            mValue = parcel?.readString() ?: ""
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(mValue)
        }
    }
}