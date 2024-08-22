package dev.lackluster.hyperx.app

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.util.AttributeSet
import cn.fkj233.ui.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


class ButtonForegroundDrawable : AlphaBlendingDrawable() {
    @Throws(IOException::class, XmlPullParserException::class)
    override fun inflate(
        r: Resources,
        parser: XmlPullParser,
        attrs: AttributeSet,
        theme: Theme?
    ) {
        super.inflate(r, parser, attrs, theme)
        init(r, attrs, theme)
    }

    private fun init(resources: Resources, attributeSet: AttributeSet?, theme: Theme?) {
        val obtainAttributes: TypedArray =
            theme?.obtainStyledAttributes(attributeSet, R.styleable.AdaptRoundButtonDrawable, 0, 0)
                ?: resources.obtainAttributes(attributeSet, R.styleable.AdaptRoundButtonDrawable)
        val dimensionPixelSize = obtainAttributes.getDimensionPixelSize(
            R.styleable.AdaptRoundButtonDrawable_buttonRadius,
            0
        )
        val dimensionPixelSize2 = obtainAttributes.getDimensionPixelSize(
            R.styleable.AdaptRoundButtonDrawable_buttonCapsuleRadius,
            0
        )
        obtainAttributes.recycle()
        setRadius(dimensionPixelSize)
    }
}