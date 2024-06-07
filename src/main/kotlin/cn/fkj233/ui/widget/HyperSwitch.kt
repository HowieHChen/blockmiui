package cn.fkj233.ui.widget

import android.content.Context
import android.widget.Switch
import cn.fkj233.ui.R

class HyperSwitch(context: Context) : Switch(context) {
    init {
        setBackgroundResource(R.drawable.hyper_switch_bg)
        setThumbResource(R.drawable.hyper_switch_thumb)
        setTrackResource(R.drawable.hyper_switch_track)
        showText = false
    }
}