package dev.lackluster.hyperx.smooth

import android.graphics.drawable.Drawable
import android.view.View

class SmoothCornerHelper {
    companion object {
        val FORCE_USE_SMOOTH: Boolean? = null
        val IS_SUPPORT_SMOOTH_CORNER: Boolean = true
        private var sEnableAppSmoothCorner: Boolean? = null

        fun isEnableAppSmoothCorner(): Boolean {
            if (sEnableAppSmoothCorner == null) {

                try {
                    val field = View::class.java.getDeclaredField("sAppSmoothCornerEnabled")
                    field.isAccessible = true
                    val bool = field.get(null) as Boolean?
                    if (bool == null) {
                        sEnableAppSmoothCorner = false
                    }
                } catch (_: Throwable) {
                    sEnableAppSmoothCorner = false
                }
            }
            return sEnableAppSmoothCorner!!
        }

        fun setViewSmoothCornerEnable(view: View, enable: Boolean) {
            if (IS_SUPPORT_SMOOTH_CORNER && !isEnableAppSmoothCorner()) {
                try {
                    View::class.java.getDeclaredMethod("setSmoothCornerEnabled", Boolean::class.java).invoke(view, enable)
                } catch (_: Throwable) {
                }
            }
        }

        fun setDrawableSmoothCornerEnable(drawable: Drawable, enable: Boolean) {
            if (IS_SUPPORT_SMOOTH_CORNER && !isEnableAppSmoothCorner()) {
                try {
                    Drawable::class.java.getDeclaredMethod("setSmoothCornerEnabled", Boolean::class.java).invoke(drawable, enable)
                } catch (_: Throwable) {
                }
            }
        }
    }
}