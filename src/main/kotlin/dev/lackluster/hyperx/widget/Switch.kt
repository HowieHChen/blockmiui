package dev.lackluster.hyperx.widget

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Switch
import cn.fkj233.ui.R
import java.lang.reflect.Field

class Switch(context: Context) : Switch(context) {
    init {
        setBackgroundResource(R.drawable.hyper_switch_bg)
        setThumbResource(R.drawable.hyper_switch_thumb)
        setTrackResource(R.drawable.hyper_switch_track)
        showText = false
        val trackWidth = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_width)
        val trackHeight = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_height)
        val trackHorizontalPadding = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_horizontal_padding)
        val trackVerticalPadding = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_vertical_padding)
        width = trackWidth + trackHorizontalPadding * 2
        height = trackHeight + trackVerticalPadding * 2
        setPadding(trackHorizontalPadding, trackVerticalPadding, trackHorizontalPadding, trackVerticalPadding)
    }

    @SuppressLint("DiscouragedPrivateApi", "DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mSwitchWidth: Field = Switch::class.java.getDeclaredField("mSwitchWidth")
        mSwitchWidth.isAccessible = true
        val trackWidth = resources.getDimensionPixelSize(R.dimen.hyperx_switch_track_width)
        mSwitchWidth.setInt(this, trackWidth)
    }
}