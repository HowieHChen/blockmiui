package dev.lackluster.hyperx.internal.widget

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.FrameLayout

class DialogRootView: FrameLayout {
    private var mCallback: ConfigurationChangedCallback? = null
    private var mNotifyConfigChanged: Boolean

    constructor(context: Context): super(context) {
        mNotifyConfigChanged = false
    }
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet) {
        mNotifyConfigChanged = false
    }
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        mNotifyConfigChanged = false
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mNotifyConfigChanged = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mNotifyConfigChanged) {
            mNotifyConfigChanged = false
            val configuration = resources.configuration
            val widthDp = configuration.screenWidthDp
            val heightDp = configuration.screenHeightDp
            val configurationChangedCallback = mCallback
            configurationChangedCallback?.onConfigurationChanged(resources.configuration, left, top, right, bottom)
            post {
                val config = resources.configuration
                if ((config.screenWidthDp == widthDp && config.screenHeightDp == heightDp) || mCallback == null) {
                    return@post
                }
                mCallback?.onConfigurationChanged(resources.configuration, left, top, right, bottom)
            }
        }
    }

    fun setConfigurationChangedCallback(callback: ConfigurationChangedCallback) {
        mCallback = callback
    }

    interface ConfigurationChangedCallback {
        fun onConfigurationChanged(configuration: Configuration, left: Int, top: Int, right: Int, bottom: Int)
    }
}