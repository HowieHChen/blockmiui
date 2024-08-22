package dev.lackluster.hyperx.core.util.screenutils

import android.graphics.Point
import dev.lackluster.hyperx.core.util.WindowBaseInfo

object SplitScreenModeHelper {
    fun detectSplitScreenInfo(windowBaseInfo: WindowBaseInfo, point: Point) {
        var n3 = 0.0f
        val n: Float
        val n2: Int
        if (isScreenLandscape(point)) {
            n = windowBaseInfo.windowSize.x.toFloat()
            n2 = point.x
            n3 = n / (n2 + 0.0f)
        } else {
            val windowSize = windowBaseInfo.windowSize
            n3 = windowSize.x / (point.x + 0.0f)
            if (n3 >= 0.95f) {
                n = windowSize.y.toFloat()
                n2 = point.y
                n3 = n / (n2 + 0.0f)
            }
        }
        if (isInRegion(n3, 0.0f, 0.4f)) {
            windowBaseInfo.windowMode = 4097
        } else if (isInRegion(n3, 0.4f, 0.6f)) {
            windowBaseInfo.windowMode = 4098
        } else if (isInRegion(n3, 0.6f, 0.8f)) {
            windowBaseInfo.windowMode = 4099
        } else {
            windowBaseInfo.windowMode = 0
        }
    }
    private fun isInRegion(n: Float, n2: Float, n3: Float): Boolean {
        return n >= n2 && n < n3
    }
    private fun isScreenLandscape(point: Point): Boolean {
        return point.x > point.y
    }
}