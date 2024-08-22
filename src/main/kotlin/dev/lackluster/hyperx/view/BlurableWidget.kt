package dev.lackluster.hyperx.view

interface BlurableWidget {
    fun applyBlur(enabled: Boolean)
    fun setEnableBlur(enabled: Boolean)
    fun setSupportBlur(support: Boolean)
}