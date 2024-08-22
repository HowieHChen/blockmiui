package dev.lackluster.hyperx.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import cn.fkj233.ui.R
import dev.lackluster.hyperx.widget.SeekBar

class SeekBarPreference : BasePreference {
    private val mSeekBar: SeekBar
    private val mSeekBarValue: TextView
    private var mOnSeekBarChangeListener: OnSeekBarChangeListener? = null
    private val mTrainsOnSeekBarChangeListener: OnSeekBarChangeListener?
    private var mValueLabelAdapter: ((Int) -> String)? = null
    private var mShowSeekBarValue: Boolean = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.seekBarPreferenceStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.HyperX_Preference_SeekBarPreference)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attributeSet, defStyleAttr, defStyleRes) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes)
        val layout = obtainStyledAttributes.getResourceId(R.styleable.SeekBarPreference_android_layout, R.layout.hyperx_preference_seekbar_layout)
        val icon = obtainStyledAttributes.getDrawable(R.styleable.SeekBarPreference_android_icon)
        val title = obtainStyledAttributes.getString(R.styleable.SeekBarPreference_android_title)
        val singleLineTitle = obtainStyledAttributes.getBoolean(R.styleable.SeekBarPreference_singleLineTitle, true)
        val showSeekBarValue = obtainStyledAttributes.getBoolean(R.styleable.SeekBarPreference_showSeekBarValue, false)
        obtainStyledAttributes.recycle()
        initResource(this, layout, 0)
        mSeekBar = mRootView.findViewById(R.id.seekbar)
        mSeekBarValue = mRootView.findViewById(R.id.seekbar_value)
        setIcon(icon)
        setTitle(title)
        setSingleLineTitle(singleLineTitle)
        setShowSeekBarValue(showSeekBarValue)
        mTrainsOnSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: android.widget.SeekBar?, p1: Int, p2: Boolean) {
                mSeekBarValue.text = mValueLabelAdapter?.invoke(p1) ?: p1.toString()
                mOnSeekBarChangeListener?.onProgressChanged(p0, p1, p2)
            }

            override fun onStartTrackingTouch(p0: android.widget.SeekBar?) {
                mOnSeekBarChangeListener?.onStartTrackingTouch(p0)
            }

            override fun onStopTrackingTouch(p0: android.widget.SeekBar?) {
                mOnSeekBarChangeListener?.onStopTrackingTouch(p0)
            }
        }
        setOnSeekBarChangeListener(mTrainsOnSeekBarChangeListener)
    }

    fun getSeekBar(): SeekBar {
        return mSeekBar
    }

    fun getValueLabel(): TextView {
        return mSeekBarValue
    }

    fun setProgress(progress: Int) {
        mSeekBar.progress = progress
    }

    fun setValueLabelAdapter(adapter: ((Int) -> String)?) {
        mValueLabelAdapter = adapter
    }

    fun setShowSeekBarValue(show: Boolean) {
        mShowSeekBarValue = show
        if (show) {
            mSeekBarValue.visibility = VISIBLE
        } else {
            mSeekBarValue.visibility = GONE
        }
    }

    fun initSeekBar(min: Int, max: Int, defProgress: Int) {
        mSeekBar.min = min
        mSeekBar.max = max
        mSeekBar.progress = defProgress
    }

    fun setOnSeekBarChangeListener(onSeekBarChangeListener: OnSeekBarChangeListener) {
        if (mTrainsOnSeekBarChangeListener == onSeekBarChangeListener) {
            mSeekBar.setOnSeekBarChangeListener(mTrainsOnSeekBarChangeListener)
        } else {
            mOnSeekBarChangeListener = onSeekBarChangeListener
        }
    }
}