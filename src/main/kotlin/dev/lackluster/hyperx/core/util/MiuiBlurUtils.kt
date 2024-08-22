package dev.lackluster.hyperx.core.util

import android.content.Context
import android.provider.Settings
import android.view.View
import java.lang.reflect.Method


object MiuiBlurUtils {
    private var ENABLE_MIUI_BLUR: Boolean? = null
    var METHOD_ADD_BG_BLEND_COLOR: Method? = null
    var METHOD_CLEAR_BG_BLEND_COLOR: Method? = null
    var METHOD_SET_BG_BLUR_MODE: Method? = null
    var METHOD_SET_BG_BLUR_RADIUS: Method? = null
    var METHOD_SET_VIEW_BLUR_MODE: Method? = null
    private var SUPPORT_MIUI_BLUR: Boolean? = null
    private const val isForceEnable = true

    init {
        SUPPORT_MIUI_BLUR = if (!isForceEnable) {
            false
        } else {
            SystemProperties.get("persist.sys.background_blur_supported", "false").toBooleanStrict()
        }
    }

    fun isEnable(): Boolean {
        return SUPPORT_MIUI_BLUR == true
    }
    @Synchronized
    fun isEffectEnable(context: Context?): Boolean {
        synchronized(MiuiBlurUtils::class.java) {
            if (SUPPORT_MIUI_BLUR != true) {
                return false
            }
            if (context == null) {
                return false
            }
            if (ENABLE_MIUI_BLUR == null) {
                ENABLE_MIUI_BLUR = try {
                    Settings.Secure.getInt(context.contentResolver, "background_blur_enable") == 1
                } catch (unused: Settings.SettingNotFoundException) {
                    true
                }
            }
            return ENABLE_MIUI_BLUR == true
        }
    }

    @Synchronized
    fun clearEffectEnable() {
        synchronized(MiuiBlurUtils::class.java) {
            ENABLE_MIUI_BLUR = null
        }
    }

    fun setBackgroundBlur(view: View, i: Int, z: Boolean): Boolean {
        return if (SUPPORT_MIUI_BLUR != true || !isEffectEnable(view.context)) {
            false
        } else try {
            if (METHOD_SET_BG_BLUR_MODE == null) {
                METHOD_SET_BG_BLUR_MODE = View::class.java.getMethod("setMiBackgroundBlurMode", Integer.TYPE)
            }
            if (METHOD_SET_BG_BLUR_RADIUS == null) {
                METHOD_SET_BG_BLUR_RADIUS = View::class.java.getMethod("setMiBackgroundBlurRadius", Integer.TYPE)
            }
            METHOD_SET_BG_BLUR_MODE!!.invoke(view, 1)
            METHOD_SET_BG_BLUR_RADIUS!!.invoke(view, i)
            setViewBlurMode(view, if (z) 2 else 1)
        } catch (unused: Exception) {
            METHOD_SET_BG_BLUR_MODE = null
            METHOD_SET_BG_BLUR_RADIUS = null
            false
        }
    }

    fun setBackgroundBlurMode(view: View?, i: Int): Boolean {
        return if (SUPPORT_MIUI_BLUR != true) {
            false
        } else try {
            if (METHOD_SET_BG_BLUR_MODE == null) {
                METHOD_SET_BG_BLUR_MODE = View::class.java.getMethod("setMiBackgroundBlurMode", Integer.TYPE)
            }
            METHOD_SET_BG_BLUR_MODE!!.invoke(view, i)
            true
        } catch (unused: Exception) {
            METHOD_SET_BG_BLUR_MODE = null
            false
        }
    }

    fun clearBackgroundBlur(view: View?): Boolean {
        return if (setBackgroundBlurMode(view, 0)) {
            setViewBlurMode(view, 0)
        } else false
    }

    fun setViewBlurMode(view: View?, i: Int): Boolean {
        return if (SUPPORT_MIUI_BLUR != true) {
            false
        } else try {
            if (METHOD_SET_VIEW_BLUR_MODE == null) {
                METHOD_SET_VIEW_BLUR_MODE = View::class.java.getMethod("setMiViewBlurMode", Integer.TYPE)
            }
            METHOD_SET_VIEW_BLUR_MODE!!.invoke(view, i)
            true
        } catch (unused: Exception) {
            METHOD_SET_VIEW_BLUR_MODE = null
            false
        }
    }

    fun addBackgroundBlenderColor(view: View, i: Int, i2: Int): Boolean {
        return if (SUPPORT_MIUI_BLUR != true || !isEffectEnable(view.context)) {
            false
        } else try {
            if (METHOD_ADD_BG_BLEND_COLOR == null) {
                val cls: Class<*> = Integer.TYPE
                METHOD_ADD_BG_BLEND_COLOR = View::class.java.getMethod("addMiBackgroundBlendColor", cls, cls)
            }
            METHOD_ADD_BG_BLEND_COLOR!!.invoke(view, i, i2)
            true
        } catch (unused: Exception) {
            METHOD_ADD_BG_BLEND_COLOR = null
            false
        }
    }

    fun clearBackgroundBlenderColor(view: View): Boolean {
        return if (SUPPORT_MIUI_BLUR != true) {
            false
        } else try {
            if (METHOD_CLEAR_BG_BLEND_COLOR == null) {
                METHOD_CLEAR_BG_BLEND_COLOR = View::class.java.getMethod("clearMiBackgroundBlendColor", *arrayOfNulls(0))
            }
            METHOD_CLEAR_BG_BLEND_COLOR!!.invoke(view, *arrayOfNulls(0))
            true
        } catch (unused: Exception) {
            METHOD_CLEAR_BG_BLEND_COLOR = null
            false
        }
    }
}