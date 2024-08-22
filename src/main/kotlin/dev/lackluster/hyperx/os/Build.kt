package dev.lackluster.hyperx.os

import dev.lackluster.hyperx.core.util.SystemProperties

object Build {
    val IS_DEBUGGABLE: Boolean = isDebuggable()
    val IS_INTERNATIONAL_BUILD: Boolean = isInternational()
    val IS_TABLET: Boolean = isTablet()
    val IS_FOLDABLE: Boolean = isFoldable()

    private fun isDebuggable(): Boolean {
        return SystemProperties.getInt("ro.debuggable", 0) == 1
    }

    private fun isInternational(): Boolean {
        return SystemProperties.get("ro.product.mod_device", "").contains("_global")
    }

    private fun isTablet(): Boolean {
        return SystemProperties.get("ro.build.characteristics").contains("tablet")
    }

    private fun isFoldable(): Boolean {
        return SystemProperties.getInt("persist.sys.muiltdisplay_type", 1) == 2
    }
}