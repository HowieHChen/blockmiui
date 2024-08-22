package dev.lackluster.hyperx.core.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Point
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

object HyperXUIUtils {
    private val mTmpValue = TypedValue()
    fun dp2px(float: Float, float2: Float): Int {
        return ((float * float2) + 0.5f).toInt()
    }
    fun px2dp(float: Float, float2: Float): Int {
        return ((float2 / float) + 0.5f).toInt()
    }
    fun dp2px(context: Context, f: Float): Int {
        return dp2px(context.resources.configuration.densityDpi / 160.0f, f)
    }

    fun getDefDimen(context: Context, resId: Int): Int {
        val typedValue = TypedValue()
        context.resources.getValue(resId, typedValue, true)
        return TypedValue.complexToFloat(typedValue.data).toInt()
    }
    fun isFullScreenGestureMode(context: Context): Boolean {
        return getNaviBarIntercationMode(context) == 2
    }
    @SuppressLint("DiscouragedApi")
    fun getNaviBarIntercationMode(context: Context): Int {
        val identifier = context.resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
        if (identifier > 0) {
            return context.resources.getInteger(identifier)
        }
        return 0
    }
    fun isSupportGestureLine(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, "use_gesture_version_three", 0) != 0
    }
    fun isEnableGestureLine(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, "hide_gesture_line", 0) == 0
    }
    fun isShowNavigationHandle(context: Context): Boolean {
        return isEnableGestureLine(context) && !isFullScreenGestureMode(context) && isSupportGestureLine(context)
    }
    fun isInMultiWindowMode(context: Context): Boolean {
        var tContext = context
        while (tContext is ContextWrapper) {
            if (tContext is Activity) {
                return checkMultiWindow(tContext)
            }
            tContext = tContext.baseContext
        }
        return false
    }
    fun checkMultiWindow(activity: Activity): Boolean {
        return activity.isInMultiWindowMode
    }
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun getStatusBarHeight(context: Context): Int {
        val identifier = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (identifier > 0) {
            return context.resources.getDimensionPixelSize(identifier)
        }
        return 0
    }
    fun isFreeformMode(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val physicalSize = getPhysicalSize(context)
        return context.resources.configuration.toString().contains("mWindowingMode=freeform") &&
                ((point.x.toFloat() + 0.0f) / physicalSize.x.toFloat() <= 0.9f || (point.y.toFloat() + 0.0f) / physicalSize.y.toFloat() <= 0.9f)
    }
    private fun getPhysicalSize(context: Context): Point {
        val point = Point()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val defaultDisplay = windowManager.defaultDisplay
        try {
            val fDisplayInfo = defaultDisplay::class.java.getDeclaredField("mDisplayInfo")
            fDisplayInfo.isAccessible = true
            val obj = fDisplayInfo.get(defaultDisplay)
            val fLogicalWidth = obj::class.java.getField("logicalWidth")
            fLogicalWidth.isAccessible = true
            point.x = fLogicalWidth.getInt(obj)
            val fLogicalHeight = obj::class.java.getField("logicalHeight")
            fLogicalHeight.isAccessible = true
            point.y = fLogicalHeight.getInt(obj)
        } catch (_: Throwable) {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            point.x = displayMetrics.widthPixels
            point.y = displayMetrics.heightPixels
        }
        return point
    }
}