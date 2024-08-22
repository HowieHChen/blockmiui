package dev.lackluster.hyperx.internal.graphics.drawable

import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.StateListDrawable
import java.util.Arrays

class TaggingDrawable : DrawableWrapper {
    private var mRawState: IntArray
    private var mTaggingState: IntArray

    constructor(drawable: Drawable) : this(drawable, drawable.state)
    constructor(drawable: Drawable, state: IntArray) : super(drawable) {
        mTaggingState = intArrayOf()
        mRawState = intArrayOf()
        setTaggingState(state)
    }

    companion object {
        fun containsTagState(stateListDrawable: StateListDrawable, state: IntArray): Boolean {
            val stateCount = stateListDrawable.stateCount
            for (i in 0 until stateCount) {
                for (stateSet in stateListDrawable.getStateSet(i)) {
                    if (Arrays.binarySearch(state, stateSet) >= 0) {
                        return true
                    }
                }
            }
            return false
        }
    }

    fun setTaggingState(state: IntArray): Boolean {
        if (state contentEquals mTaggingState) {
            return false
        }
        mTaggingState = state
        return super.setState(mergeTaggingState(state, mRawState))
    }

    override fun setState(state: IntArray): Boolean {
        if (state contentEquals mRawState) {
            return false
        }
        mRawState = state
        return super.setState(mergeTaggingState(mTaggingState, state))
    }
    private fun mergeTaggingState(base: IntArray, addition: IntArray): IntArray {
        val newArray = IntArray(base.size + addition.size)
        System.arraycopy(base, 0, newArray, 0, base.size)
        System.arraycopy(addition, 0, newArray, base.size, addition.size)
        return newArray
    }
}