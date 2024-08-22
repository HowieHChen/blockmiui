package dev.lackluster.hyperx.widget.dialoganim

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

object DimAnimator {
    fun show(view: View?) {
        val ofFloat = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 0.3f)
        ofFloat.interpolator = DecelerateInterpolator(1.5f)
        ofFloat.duration = 300L
        ofFloat.start()
    }

    fun dismiss(view: View?) {
        val ofFloat = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 0.0f)
        ofFloat.interpolator = DecelerateInterpolator(1.5f)
        ofFloat.duration = 250L
        ofFloat.start()
    }
}