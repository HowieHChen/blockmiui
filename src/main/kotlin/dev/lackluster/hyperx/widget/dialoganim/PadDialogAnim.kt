package dev.lackluster.hyperx.widget.dialoganim

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.DecelerateInterpolator
import dev.lackluster.hyperx.animation.property.ViewProperty
import dev.lackluster.hyperx.app.AlertDialog
import dev.lackluster.hyperx.widget.DialogAnimHelper
import java.lang.ref.WeakReference
import kotlin.math.max

class PadDialogAnim: IDialogAnim {
    override fun cancelAnimator() {
    }
    override fun executeDismissAnim(
        view: View?,
        view2: View?,
        onDismiss: DialogAnimHelper.OnDismiss?
    ) {
        if ("hide" == view?.tag) {
            return
        }
        dismissPanel(view, WeakRefDismissListener(onDismiss, view))
        DimAnimator.dismiss(view2)
    }

    private fun dismissPanel(view: View?, weakRefDismissListener: WeakRefDismissListener) {
        if (view == null) return
        val scale = getScale(view)
        val ofFloat = PropertyValuesHolder.ofFloat(ViewProperty.ALPHA, 1.0f, 0.0f)
        val ofFloat2 = PropertyValuesHolder.ofFloat(ViewProperty.SCALE_X, 1.0f, scale)
        val ofFloat3 = PropertyValuesHolder.ofFloat(ViewProperty.SCALE_Y, 1.0f, scale)
        val ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(view, ofFloat, ofFloat2, ofFloat3)
        ofPropertyValuesHolder.interpolator = DecelerateInterpolator(1.5f)
        ofPropertyValuesHolder.addListener(weakRefDismissListener)
        ofPropertyValuesHolder.duration = 200L
        ofPropertyValuesHolder.start()
    }

    override fun executeShowAnim(
        view: View?,
        view2: View?,
        isLandscape: Boolean,
        onDialogShowAnimListener: AlertDialog.OnDialogShowAnimListener?
    ) {
        if (view == null || "show" == view.tag) {
            return
        }
        if (view.scaleX != 1.0f) {
            view.scaleX = 1.0f
            view.scaleY = 1.0f
        }
        executeShowAnimAndroidUIThread(view, onDialogShowAnimListener)
        DimAnimator.show(view2)
    }

    private fun executeShowAnimAndroidUIThread(view: View, onDialogShowAnimListener: AlertDialog.OnDialogShowAnimListener?) {
        val scale = getScale(view)
        val ofFloat = PropertyValuesHolder.ofFloat(ViewProperty.ALPHA, 0.0f, 1.0f)
        val ofFloat2 = PropertyValuesHolder.ofFloat(ViewProperty.SCALE_X, scale, 1.0f)
        val ofFloat3 = PropertyValuesHolder.ofFloat(ViewProperty.SCALE_Y, scale, 1.0f)
        val ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(view, ofFloat, ofFloat2, ofFloat3)
        ofPropertyValuesHolder.interpolator = DecelerateInterpolator(1.5f)
        ofPropertyValuesHolder.addListener(WeakRefShowListener(onDialogShowAnimListener, view))
        ofPropertyValuesHolder.duration = 300L
        ofPropertyValuesHolder.start()
    }

    private fun getScale(view: View): Float {
        return max(0.8f, 1.0f - (60.0f / max(view.width, view.height).toFloat()))
    }

    inner class WeakRefDismissListener(onDismiss: DialogAnimHelper.OnDismiss?, view: View?): Animator.AnimatorListener {
        private val mOnDismiss: WeakReference<DialogAnimHelper.OnDismiss?>
        private val mView: WeakReference<View?>
        init {
            mOnDismiss = WeakReference(onDismiss)
            mView = WeakReference(view)
        }
        override fun onAnimationStart(p0: Animator) {
            mView.get()?.tag = "hide"
        }
        override fun onAnimationEnd(p0: Animator) {
            mView.get()?.tag = null
            mOnDismiss.get()?.end()
        }
        override fun onAnimationCancel(p0: Animator) {
            mView.get()?.tag = null
            mOnDismiss.get()?.end()
        }
        override fun onAnimationRepeat(p0: Animator) {
        }
    }

    inner class WeakRefShowListener(
        onDialogShowAnimListener: AlertDialog.OnDialogShowAnimListener?,
        view: View?
    ): AnimatorListenerAdapter() {
        private val mShowDismiss: WeakReference<AlertDialog.OnDialogShowAnimListener?>
        private val mView: WeakReference<View?>
        init {
            mShowDismiss = WeakReference(onDialogShowAnimListener)
            mView = WeakReference(view)
        }
        override fun onAnimationStart(animation: Animator) {
            super.onAnimationStart(animation)
            mView.get()?.tag = "show"
            mShowDismiss.get()?.onShowAnimStart()
        }
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            mView.get()?.tag = null
            mShowDismiss.get()?.onShowAnimComplete()
        }
    }
}