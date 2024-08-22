package dev.lackluster.hyperx.internal.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue

object AttributeResolver {
    private val TYPED_VALUE: TypedValue = TypedValue()
    private val TYPED_VALUE_THREAD_LOCAL: ThreadLocal<TypedValue> = ThreadLocal()
    fun getTypedValue(context: Context): TypedValue {
        if (context.mainLooper.thread == Thread.currentThread()) {
            return TYPED_VALUE
        }
        val threadLocal = TYPED_VALUE_THREAD_LOCAL
        val typedValue = threadLocal.get()
        if (typedValue != null) {
            return typedValue
        }
        val typedValue2 = TypedValue()
        threadLocal.set(typedValue2)
        return typedValue2
    }
    fun resolve(context: Context, resId: Int): Int {
        val typedValue = getTypedValue(context)
        if (context.theme.resolveAttribute(resId, typedValue, true)) {
            return typedValue.resourceId
        }
        return -1
    }
    fun resolveDrawable(context: Context, resId: Int): Drawable? {
        val typedValue = getTypedValue(context)
        if (!context.theme.resolveAttribute(resId, typedValue, true)) {
            return null
        }
        if (typedValue.resourceId > 0) {
            return context.resources.getDrawable(typedValue.resourceId, context.theme)
        }
        val type = typedValue.type
        if (type < 28 || type > 31) {
            return null
        }
        return ColorDrawable(typedValue.data)
    }
    fun resolveColor(context: Context, resId: Int): Int {
        val innerResolveColor = innerResolveColor(context, resId)
        if (innerResolveColor != null) {
            return innerResolveColor
        }
        return context.getColor(-1)
    }
    fun resolveColor(context: Context, resId: Int, defColor: Int): Int {
        val innerResolveColor = innerResolveColor(context, resId)
        return innerResolveColor ?: defColor
    }
    private fun innerResolveColor(context: Context, resId: Int): Int? {
        val typedValue = getTypedValue(context)
        if (!context.theme.resolveAttribute(resId, typedValue, true)) {
            return null
        }
        if (typedValue.resourceId > 0) {
            return context.getColor(typedValue.resourceId)
        }
        val type = typedValue.type
        if (type < 28 || type > 31) {
            return null
        }
        return typedValue.data
    }
    fun resolveBoolean(context: Context, resId: Int, defBoolean: Boolean): Boolean {
        val typedValue = getTypedValue(context)
        return if (context.theme.resolveAttribute(resId, typedValue, true)) {
            typedValue.type == 18 && typedValue.data != 0
        } else {
            defBoolean
        }
    }
    fun resolveDimension(context: Context, resId: Int): Float {
        return context.resources.getDimension(resolve(context, resId))
    }
    fun resolveDimensionPixelSize(context: Context, resId: Int): Int {
        return context.resources.getDimensionPixelSize(resolve(context, resId))
    }
    fun resolveInt(context: Context, resId: Int, defInt: Int): Int {
        val typedValue = getTypedValue(context)
        if (!context.theme.resolveAttribute(resId, typedValue, true)) {
            return defInt
        }
        val type = typedValue.type
        if (type < 16 || type > 31) {
            return defInt
        }
        return typedValue.data
    }
    fun resolveFloat(context: Context, resId: Int, defFloat: Float): Float {
        val typedValue = getTypedValue(context)
        return if (context.theme.resolveAttribute(resId, typedValue, true) && typedValue.type == 4) {
            typedValue.data.toFloat()
        } else {
            defFloat
        }
    }
    fun resolveTypedValue(context: Context, resId: Int): TypedValue? {
        val typedValue = TypedValue()
        if (context.theme.resolveAttribute(resId, typedValue, true)) {
            return typedValue
        }
        return null
    }
}