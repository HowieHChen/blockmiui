package dev.lackluster.hyperx.core.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.view.WindowManager
import dev.lackluster.hyperx.core.util.WindowUtils.getScreenSize
import dev.lackluster.hyperx.core.util.WindowUtils.getWindowManager
import dev.lackluster.hyperx.core.util.WindowUtils.getWindowSize
import dev.lackluster.hyperx.responsive.ResponsiveStateHelper
import dev.lackluster.hyperx.view.DisplayConfig
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.Volatile
import kotlin.math.min


object EnvStateManager {
    var sOriginConfig: DisplayConfig? = null
    val sScreenSize = Point(-1, -1)
    val sWindowInfoMap: ConcurrentHashMap<Int, WindowBaseInfo> = ConcurrentHashMap<Int, WindowBaseInfo>()
    val mNaviModeLock = Any()
    val mStatusBarHeightLock = Any()
    val mNavigationBarHeightLock = Any()

    @Volatile
    var mIsFullScreenGestureMode: Boolean? = null

    @Volatile
    var mStatusBarHeight = -1

    @Volatile
    var mStatusBarHeightDp = -1

    @Volatile
    var mNavigationBarHeight = -1

    @Volatile
    var mNavigationBarHeightDp = -1

    fun init(activity: Activity) {
        sOriginConfig = DisplayConfig(activity.resources.configuration)
    }
    fun init(application: Application) {
        sOriginConfig = DisplayConfig(application.resources.configuration)
    }
    fun updateOriginConfig(displayConfig: DisplayConfig?) {
        sOriginConfig = displayConfig
    }
    fun removeInfoOfContext(context: Context) {
        sWindowInfoMap.remove(context.resources.hashCode())
    }
    fun updateScreenSize(windowManager: WindowManager, context: Context) {
        val point = sScreenSize
        synchronized(point) {
            getScreenSize(windowManager, context, point)
        }
    }
    fun getScreenSize(context: Context): Point {
        val point = sScreenSize
        if (isSizeDirty(point)) {
            updateScreenSize(getWindowManager(context), context)
        }
        return point
    }
    fun getScreenShortEdge(context: Context): Int {
        val screenSize = getScreenSize(context)
        return min(screenSize.x, screenSize.y)
    }
    fun markEnvStateDirty(context: Context?) {
        val point = sScreenSize
        synchronized(point) {
            markSizeDirty(point)
        }
        synchronized(mNaviModeLock) {
            mIsFullScreenGestureMode = null
        }
        synchronized(mNavigationBarHeightLock) {
            mNavigationBarHeight = -1
            mNavigationBarHeightDp = -1
        }
        synchronized(mStatusBarHeightLock) {
            mStatusBarHeight = -1
            mStatusBarHeightDp = -1
        }
    }
    fun markSizeDirty(point: Point) {
        if (point.x == -1 && point.y == -1) {
            return
        }
        point.x = -1
        point.y = -1
    }
    @Synchronized
    fun markWindowInfoDirty(context: Context) {
        synchronized(EnvStateManager::class.java) {
            markWindowInfoDirty(getInnerWindowInfo(context))
        }
    }
    fun markWindowInfoDirty(windowBaseInfo: WindowBaseInfo) {
        windowBaseInfo.modeDirty = true
        windowBaseInfo.sizeDirty = true
    }
    fun getWindowSize(context: Context): Point {
        val innerWindowInfo: WindowBaseInfo = getInnerWindowInfo(context)
        if (innerWindowInfo.sizeDirty) {
            updateWindowSize(context, innerWindowInfo)
        }
        return innerWindowInfo.windowSize
    }
    private fun getInnerWindowInfo(context: Context): WindowBaseInfo {
        val hashCode = context.resources.hashCode()
        val concurrentHashMap: ConcurrentHashMap<Int, WindowBaseInfo> = sWindowInfoMap
        val windowBaseInfo: WindowBaseInfo? = concurrentHashMap[hashCode]
        if (windowBaseInfo != null) {
            return windowBaseInfo
        }
        val windowBaseInfo2 = WindowBaseInfo()
        concurrentHashMap[hashCode] = windowBaseInfo2
        return windowBaseInfo2
    }

    fun getWindowInfo(context: Context): WindowBaseInfo {
        return getWindowInfo(context, null, false)
    }

    fun getWindowInfo(context: Context, configuration: Configuration?): WindowBaseInfo {
        return getWindowInfo(context, configuration, false)
    }

    fun getWindowInfo(context: Context, configuration: Configuration?, z: Boolean): WindowBaseInfo {
        val innerWindowInfo: WindowBaseInfo = getInnerWindowInfo(context)
        updateWindowInfo(context, innerWindowInfo, configuration, z)
        return innerWindowInfo
    }

    fun updateWindowInfo(context: Context, windowBaseInfo: WindowBaseInfo?, configuration: Configuration?, z: Boolean) {
        if (windowBaseInfo == null) {
            return
        }
        if (windowBaseInfo.sizeDirty || z) {
            if (configuration != null) {
                updateWindowSizeByConfig(configuration, windowBaseInfo)
            } else {
                updateWindowSize(context, windowBaseInfo)
            }
        }
        if (windowBaseInfo.modeDirty || z) {
            updateWindowMode(context, windowBaseInfo)
        }
    }
    fun updateWindowSize(context: Context, windowBaseInfo: WindowBaseInfo) {
        getWindowSize(context, windowBaseInfo.windowSize)
        val f = context.resources.configuration.densityDpi / 160.0f
        windowBaseInfo.windowSizeDp.set(
            HyperXUIUtils.px2dp(f, windowBaseInfo.windowSize.x.toFloat()),
            HyperXUIUtils.px2dp(f, windowBaseInfo.windowSize.y.toFloat())
        )
        val point: Point = windowBaseInfo.windowSizeDp
        windowBaseInfo.windowType =
            ResponsiveStateHelper.detectResponsiveWindowType(point.x, point.y)
        windowBaseInfo.sizeDirty = false
    }
    fun updateWindowSizeByConfig(configuration: Configuration, windowBaseInfo: WindowBaseInfo) {
        ensureOriginConfigExist(configuration)
        val i: Int = configuration.densityDpi
        val f: Float = sOriginConfig!!.densityDpi * 1.0f / i
        val f2 = i / 160.0f * f
        windowBaseInfo.windowSize.set(
            HyperXUIUtils.dp2px(f2, configuration.screenWidthDp.toFloat()),
            HyperXUIUtils.dp2px(f2, configuration.screenHeightDp.toFloat())
        )
        windowBaseInfo.windowSizeDp.set(
            (configuration.screenWidthDp * f).toInt(),
            (configuration.screenHeightDp * f).toInt()
        )
        val point: Point = windowBaseInfo.windowSizeDp
        windowBaseInfo.windowType =
            ResponsiveStateHelper.detectResponsiveWindowType(point.x, point.y)
        windowBaseInfo.sizeDirty = false
    }
    fun updateWindowMode(context: Context, windowBaseInfo: WindowBaseInfo) {
        if (windowBaseInfo.sizeDirty) {
            updateWindowSize(context, windowBaseInfo)
        }
        ScreenModeHelper.detectWindowMode(context, windowBaseInfo, getScreenSize(context))
        windowBaseInfo.modeDirty = false
    }
    private fun isSizeDirty(point: Point): Boolean {
        return point.x == -1 && point.y == -1
    }
    fun isFreeFormMode(context: Context): Boolean {
        return ScreenModeHelper.isInFreeFormMode(getInnerWindowInfo(context).windowMode)
    }
    fun isFullScreenGestureMode(context: Context): Boolean {
        if (mIsFullScreenGestureMode == null) {
            synchronized(mNaviModeLock) {
                if (mIsFullScreenGestureMode == null) {
                    mIsFullScreenGestureMode =
                        java.lang.Boolean.valueOf(HyperXUIUtils.isFullScreenGestureMode(context))
                }
            }
        }
        return mIsFullScreenGestureMode!!
    }
    fun getStatusBarHeight(context: Context, z: Boolean): Int {
        if (mStatusBarHeight == -1) {
            synchronized(mStatusBarHeightLock) {
                if (mStatusBarHeight == -1) {
                    mStatusBarHeight = HyperXUIUtils.getStatusBarHeight(context)
                    mStatusBarHeightDp = (mStatusBarHeight / (context.resources.configuration.densityDpi / 160.0f)).toInt()
                }
            }
        }
        return if (z) mStatusBarHeightDp else mStatusBarHeight
    }
    fun getSmallestScreenWidthDp(context: Context): Int {
        val configuration = context.resources.configuration
        ensureOriginConfigExist(configuration)
        return (configuration.smallestScreenWidthDp * (sOriginConfig!!.densityDpi * 1.0f / configuration.densityDpi)).toInt()
    }
    private fun ensureOriginConfigExist(configuration: Configuration) {
        if (sOriginConfig == null) {
            sOriginConfig = DisplayConfig(configuration)
        }
    }
}