package dev.lackluster.hyperx.view

import android.content.res.Configuration

class DisplayConfig(configuration: Configuration) {
    var defaultBitmapDensity = 0
    var density = 0f
    var densityDpi = 0
    var fontScale = 0f
    var scaledDensity = 0f
    init {
        val i = configuration.densityDpi
        defaultBitmapDensity = i
        densityDpi = i
        val f = i * 0.00625f
        density = f
        val f2 = configuration.fontScale
        fontScale = f2
        scaledDensity = f * (if (f2 == 0.0f) 1.0f else f2)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is DisplayConfig) {
            return false
        }
        return density.compareTo(other.density) == 0 && scaledDensity.compareTo(other.scaledDensity) == 0 && fontScale.compareTo(other.fontScale) == 0 && densityDpi == other.densityDpi && defaultBitmapDensity == other.defaultBitmapDensity
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "{ densityDpi:$densityDpi, density:$density, scaledDensity:$scaledDensity, fontScale: $fontScale, defaultBitmapDensity:$defaultBitmapDensity}"
    }
}