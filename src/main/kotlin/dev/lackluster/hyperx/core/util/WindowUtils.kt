package dev.lackluster.hyperx.core.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.view.Display
import android.view.WindowManager

object WindowUtils {
    fun getWindowManager(context: Context): WindowManager {
        return context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    fun getDisplay(context: Context): Display? {
        return try {
            context.display
        } catch (_: Throwable) {
            getWindowManager(context).defaultDisplay
        }
    }
    fun getWindowSize(context: Context, point: Point) {
        getWindowSize(getWindowManager(context), context, point)
    }
    fun getWindowSize(windowManager: WindowManager, context: Context, point: Point) {
        val bounds = windowManager.currentWindowMetrics.bounds
        point.x = bounds.width()
        point.y = bounds.height()
    }
    fun getScreenSize(context: Context, point: Point) {
        getScreenSize(getWindowManager(context), context, point)
    }
    fun getScreenSize(windowManager: WindowManager, context: Context, point: Point) {
        val bounds = windowManager.maximumWindowMetrics.bounds
        point.x = bounds.width()
        point.y = bounds.height()
    }
    fun getWindowSize(context: Context): Point {
        val point = Point()
        getWindowSize(context, point)
        return point
    }
    fun getWindowHeight(context: Context): Int {
        return getWindowSize(context).y
    }
    fun isFreeformMode(configuration: Configuration, point: Point, point2: Point): Boolean {
        return configuration.toString().contains("mWindowingMode=freeform") &&
                (((point2.x.toFloat()) + 0.0f) / (point.x.toFloat()) <= 0.9f || ((point2.y.toFloat()) + 0.0f) / (point.y.toFloat()) <= 0.9f)
    }
}