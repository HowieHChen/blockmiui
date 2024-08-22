package dev.lackluster.hyperx.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class UnPressableLinearLayout : LinearLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    override fun dispatchSetPressed(pressed: Boolean) {
    }
}