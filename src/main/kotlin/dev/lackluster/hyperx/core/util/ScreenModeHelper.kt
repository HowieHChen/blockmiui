package dev.lackluster.hyperx.core.util

import android.content.Context
import android.graphics.Point
import dev.lackluster.hyperx.core.util.screenutils.FreeFormModeHelper
import dev.lackluster.hyperx.core.util.screenutils.SplitScreenModeHelper


object ScreenModeHelper {
    fun isInFreeFormMode(i: Int): Boolean {
        return i and 8192 != 0
    }
    fun isInSplitScreenMode(i: Int): Boolean {
        return i and 4096 != 0
    }
    fun detectWindowMode(context: Context, windowBaseInfo: WindowBaseInfo, point: Point) {
        FreeFormModeHelper.detectFreeFormInfo(windowBaseInfo, context, point)
        if (isInFreeFormMode(windowBaseInfo.windowMode)) {
            return
        }
        SplitScreenModeHelper.detectSplitScreenInfo(windowBaseInfo, point)
    }
}