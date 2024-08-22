package dev.lackluster.hyperx.os

import android.content.Context
import dev.lackluster.hyperx.core.util.EnvStateManager.getScreenShortEdge


object DeviceHelper {
    fun detectType(context: Context?): Int {
        if (Build.IS_FOLDABLE) {
            return 3
        }
        return if (Build.IS_TABLET) 2 else 1
    }
    fun isWideScreen(context: Context): Boolean {
        return getScreenShortEdge(context).toFloat() > context.resources.displayMetrics.density * 600.0f
    }
}