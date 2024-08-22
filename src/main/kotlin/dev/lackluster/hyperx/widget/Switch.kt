package dev.lackluster.hyperx.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Switch
import cn.fkj233.ui.R
import java.lang.reflect.Field

class Switch : Switch {
    constructor(context: Context) : this(context, null, 0, R.style.Widget_Switch)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0, R.style.Widget_Switch)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.Widget_Switch)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes) {
        val trackWidth = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_width)
        val trackHeight = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_height)
        val trackHorizontalPadding = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_horizontal_padding)
        val trackVerticalPadding = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_vertical_padding)
        width = trackWidth + trackHorizontalPadding * 2
        height = trackHeight + trackVerticalPadding * 2
    }

    @SuppressLint("DiscouragedPrivateApi", "DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mSwitchWidth: Field = Switch::class.java.getDeclaredField("mSwitchWidth")
        mSwitchWidth.isAccessible = true
        val trackWidth = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_width)
        mSwitchWidth.setInt(this, trackWidth)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        if (!super.onTouchEvent(ev)) {
            when(ev.action) {
                MotionEvent.ACTION_UP -> {
                    val touchX: Float = ev.x
                    val touchY: Float = ev.y
                    val maxX = width.toFloat()
                    val maxY = height.toFloat()
                    if (touchX in 0.0f..maxX && touchY in 0.0f..maxY) {
                        performClick()
                    }
                }
            }
        }
        return true
    }
}