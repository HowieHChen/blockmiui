package dev.lackluster.hyperx.view.animation

import android.view.animation.Interpolator

class CubicEaseInOutInterpolator : Interpolator {
    override fun getInterpolation(p0: Float): Float {
        val f2: Float = p0 * 2.0f
        if (f2 < 1.0f) {
            return 0.5f * f2 * f2 * f2
        }
        val f3 = f2 - 2.0f
        return (f3 * f3 * f3 + 2.0f) * 0.5f
    }
}