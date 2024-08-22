package dev.lackluster.hyperx.core.util.screenutils

import android.content.Context
import android.graphics.Point
import dev.lackluster.hyperx.core.util.WindowBaseInfo
import dev.lackluster.hyperx.core.util.WindowUtils.isFreeformMode


object FreeFormModeHelper {
    fun detectFreeFormInfo(
        windowBaseInfo: WindowBaseInfo,
        context: Context,
        point: Point
    ): WindowBaseInfo {
        return acquireFreeFormWindowRatioInternal(windowBaseInfo, context, point)
    }
    private fun acquireFreeFormWindowRatioInternal(
        windowBaseInfo: WindowBaseInfo,
        context: Context,
        point: Point
    ): WindowBaseInfo {
        if (!isFreeformMode(context.resources.configuration, point, windowBaseInfo.windowSize)) {
            windowBaseInfo.windowMode = windowBaseInfo.windowMode and -8193
            return windowBaseInfo
        }
        val i = windowBaseInfo.windowSize.x
        return freeFormModeRatioToCodeInternal(
            windowBaseInfo,
            if (i != 0) windowBaseInfo.windowSize.y * 1.0f / i else 0.0f
        )
    }
    private fun freeFormModeRatioToCodeInternal(
        windowBaseInfo: WindowBaseInfo,
        f: Float
    ): WindowBaseInfo {
        if (f <= 0.0f) {
            windowBaseInfo.windowMode = 0
        } else if (f >= 0.74f && f < 0.76f) {
            windowBaseInfo.windowMode = 8195
        } else if (f >= 1.32f && f < 1.34f) {
            windowBaseInfo.windowMode = 8194
        } else if (f >= 1.76f && f < 1.79f) {
            windowBaseInfo.windowMode = 8193
        } else {
            windowBaseInfo.windowMode = 8196
        }
        return windowBaseInfo
    }
}