package dev.lackluster.hyperx.responsive

object ResponsiveStateHelper {
    fun detectResponsiveWindowType(i: Int, i2: Int): Int {
        if (i <= 670) {
            return 1
        }
        if (i >= 960) {
            return 3
        }
        return if (i2 > 550) 2 else 1
    }
}