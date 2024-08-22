package dev.lackluster.hyperx.internal.util

object LiteUtils {
    private var mIsCommonLiteStrategy: Boolean? = null

    fun isCommonLiteStrategy(): Boolean {
        if (mIsCommonLiteStrategy == null) {
            mIsCommonLiteStrategy = false
        }
        return false
    }
}