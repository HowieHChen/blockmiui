package dev.lackluster.hyperx.internal.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams


object ViewUtils {
    fun getContentRect(view: View, rect: Rect) {
        rect.left = view.scrollX + view.paddingLeft
        rect.top = view.scrollY + view.paddingTop
        rect.right = view.width - view.paddingRight - rect.left
        rect.bottom = view.height - view.paddingBottom - rect.top
    }
    fun isLayoutRtl(view: View): Boolean {
        return view.layoutDirection == 1
    }
    fun layoutChildView(viewGroup: ViewGroup, view: View, i: Int, i2: Int, i3: Int, i4: Int) {
        var i3 = i3
        val isLayoutRtl = isLayoutRtl(viewGroup)
        val width = viewGroup.width
        val i5 = if (isLayoutRtl) width - i3 else i
        if (isLayoutRtl) {
            i3 = width - i
        }
        view.layout(i5, i2, i3, i4)
    }
    fun isNightMode(context: Context): Boolean {
        return isNightMode(context.resources.configuration)
    }
    fun isNightMode(configuration: Configuration): Boolean {
        return configuration.isNightModeActive
    }
    fun resetPaddingBottom(view: View, i: Int) {
        view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, i)
    }
    fun getMeasuredHeightWithMargin(view: View): Int {
        val layoutParams = view.layoutParams
        val measuredHeight = view.measuredHeight
        if (layoutParams !is MarginLayoutParams) {
            return measuredHeight
        }
        return measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
    }
}