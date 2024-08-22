package dev.lackluster.hyperx.app

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.content.res.TypedArray
import android.util.AttributeSet
import cn.fkj233.ui.R
import dev.lackluster.hyperx.smooth.SmoothContainerDrawable2
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


class AdaptRoundButtonDrawable : SmoothContainerDrawable2() {
    private var mCapsuleRaidus = 0f
    private var mRadius = 0f

    @Throws(IOException::class, XmlPullParserException::class)
    override fun inflate(
        r: Resources,
        parser: XmlPullParser,
        attrs: AttributeSet,
        theme: Theme?
    ) {
        init(r, attrs, theme)
        super.inflate(r, parser, attrs, theme)
        setCornerRadius(mRadius)
    }

    private fun init(resources: Resources, attributeSet: AttributeSet, theme: Theme?) {
        val obtainAttributes: TypedArray = obtainAttributes(
            resources,
            theme,
            attributeSet,
            R.styleable.AdaptRoundButtonDrawable
        )
        mRadius =
            obtainAttributes.getDimension(R.styleable.AdaptRoundButtonDrawable_buttonRadius, 0.0f)
        mCapsuleRaidus = obtainAttributes.getDimension(
            R.styleable.AdaptRoundButtonDrawable_buttonCapsuleRadius,
            0.0f
        )
        obtainAttributes.recycle()
    }
}