package dev.lackluster.hyperx.internal.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager


class DisplayHelper(context: Context) {
    private val mDensity:Float
    private val mDensityDpi: Int
    private val mDisplayMetrics: DisplayMetrics
    private val mHeightDps: Int
    private val mHeightPixels: Int
    private val mWidthDps: Int
    private val mWidthPixels: Int

    init {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mDisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(mDisplayMetrics)
        val displayMetrics = mDisplayMetrics
        val i = displayMetrics.widthPixels
        mWidthPixels = i
        val i2 = displayMetrics.heightPixels
        mHeightPixels = i2
        val f = displayMetrics.density
        mDensity = f
        mDensityDpi = displayMetrics.densityDpi
        mWidthDps = (i / f).toInt()
        mHeightDps = (i2 / f).toInt()
    }

    fun getWidthPixels(): Int {
        return mWidthPixels
    }

    fun getHeightPixels(): Int {
        return mHeightPixels
    }

    fun getDensity(): Float {
        return mDensity
    }
}