package dev.lackluster.hyperx.view

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView


object DensityChangedHelper {
    fun updateTextSizeDefaultUnit(textView: TextView, f: Float) {
        textView.setTextSize(textView.textSizeUnit, f)
    }

    fun updateTextSizeSpUnit(textView: TextView, f: Float) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, f)
    }

    fun updateViewPadding(view: View, f: Float) {
        view.setPadding(
            (view.paddingLeft * f).toInt(),
            (view.paddingTop * f).toInt(),
            (view.paddingRight * f).toInt(),
            (view.paddingBottom * f).toInt()
        )
    }

    fun updateViewSize(view: View, f: Float) {
        val z: Boolean
        val layoutParams = view.layoutParams
        val i = layoutParams.width
        var z2 = true
        if (i > 0) {
            layoutParams.width = (i * f).toInt()
            z = true
        } else {
            z = false
        }
        val i2 = layoutParams.height
        if (i2 > 0) {
            layoutParams.height = (i2 * f).toInt()
        } else {
            z2 = z
        }
        if (z2) {
            view.layoutParams = layoutParams
        }
    }

    fun updateViewMargin(view: View, f: Float) {
        val layoutParams = view.layoutParams
        if (layoutParams is MarginLayoutParams) {
            layoutParams.leftMargin = (layoutParams.leftMargin * f).toInt()
            layoutParams.topMargin = (layoutParams.topMargin * f).toInt()
            layoutParams.rightMargin = (layoutParams.rightMargin * f).toInt()
            layoutParams.bottomMargin = (layoutParams.bottomMargin * f).toInt()
            view.layoutParams = layoutParams
        }
    }
}