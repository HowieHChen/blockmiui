package dev.lackluster.hyperx.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import cn.fkj233.ui.R


class ProgressBar : android.widget.ProgressBar {
    private var mIndeterminateDrawableOriginal: Drawable? = null

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, R.attr.progressBarStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        postConstruct(context, attributeSet, defStyleAttr)
    }

    fun postConstruct(context: Context, attributeSet: AttributeSet?, i: Int) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ProgressBar, i, R.style.Widget_ProgressBar_Horizontal)
        val drawable = mIndeterminateDrawableOriginal
        if (drawable != null && drawable.javaClass.name == "android.graphics.drawable.AnimatedRotateDrawable") {
            try {
                val cls: Class<*> = drawable.javaClass
                cls.getMethod("setFramesCount", java.lang.Integer::class.java).invoke(
                    drawable,
                    obtainStyledAttributes.getInt(R.styleable.ProgressBar_indeterminateFramesCount, 48)
                )
                cls.getMethod("setFramesDuration", java.lang.Integer::class.java).invoke(
                    drawable,
                    obtainStyledAttributes.getInt(R.styleable.ProgressBar_indeterminateFramesDuration, 25)
                )
            } catch (_: Exception) {
            }
        }
        obtainStyledAttributes.recycle()
    }

    override fun setIndeterminateDrawable(drawable: Drawable?) {
        super.setIndeterminateDrawable(drawable)
        if (mIndeterminateDrawableOriginal != drawable) {
            mIndeterminateDrawableOriginal = drawable
        }
    }
}