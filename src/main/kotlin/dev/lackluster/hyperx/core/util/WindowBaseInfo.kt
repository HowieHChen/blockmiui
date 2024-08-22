package dev.lackluster.hyperx.core.util

import android.graphics.Point

class WindowBaseInfo {
    var windowMode = 0
    var windowType = 0
    var sizeDirty = true
    var modeDirty = true
    var windowSize = Point()
    var windowSizeDp = Point()

    fun isDirty(): Boolean {
        return sizeDirty || modeDirty
    }
}