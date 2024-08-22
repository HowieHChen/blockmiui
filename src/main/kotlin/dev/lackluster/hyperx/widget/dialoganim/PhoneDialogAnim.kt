package dev.lackluster.hyperx.widget.dialoganim

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Insets
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsets
import android.view.animation.DecelerateInterpolator
import dev.lackluster.hyperx.animation.property.ViewProperty
import dev.lackluster.hyperx.app.AlertDialog
import dev.lackluster.hyperx.core.util.HyperXUIUtils
import dev.lackluster.hyperx.core.util.WindowUtils
import dev.lackluster.hyperx.widget.DialogAnimHelper
import java.lang.ref.WeakReference

class PhoneDialogAnim: IDialogAnim {
    private var mImeHeight: Int = 0
    companion object {
        private var sValueAnimatorWeakRef: WeakReference<ValueAnimator>? = null
        fun doExecuteShowAnim(view: View, from: Int, to: Int, z: Boolean, weakRefShowListener: WeakRefShowListener, weakRefUpdateListener: WeakRefUpdateListener) {
            val ofInt = ValueAnimator.ofInt(from, to)
            ofInt.setDuration(350L)
            ofInt.interpolator = DecelerateInterpolator(1.5f)
            ofInt.addUpdateListener(weakRefUpdateListener)
            ofInt.addListener(weakRefShowListener)
            ofInt.start()
            sValueAnimatorWeakRef = WeakReference(ofInt)
        }
        fun relayoutView(view: View, translationY: Int, useAnim: Boolean) {
            if (useAnim) {
                view.animate().cancel()
                view.animate().setDuration(100L).translationY(translationY.toFloat()).start()
            } else {
                view.animate().cancel()
                view.translationY = translationY.toFloat()
            }
        }
    }
    override fun cancelAnimator() {
        val valueAnimator = sValueAnimatorWeakRef?.get()
        valueAnimator?.cancel()
    }

    override fun executeDismissAnim(
        view: View?,
        view2: View?,
        onDismiss: DialogAnimHelper.OnDismiss?
    ) {
        if ("hide" == view?.tag) {
            return
        }
        dismissPanel(view, WeakRefDismissListener(view, onDismiss))
        DimAnimator.dismiss(view2)
    }

    private fun dismissPanel(view: View?, weakRefDismissListener: WeakRefDismissListener) {
        if (view == null) return
        val ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat(
            ViewProperty.TRANSLATION_Y,
            view.translationY, view.height + (view.layoutParams as MarginLayoutParams).bottomMargin.toFloat()
        ))
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
        if (view == null || view2 == null || "show" == view.tag) {
            return
        }
        mImeHeight = 0
        val i = (view2.layoutParams as MarginLayoutParams).bottomMargin
        if (view.scaleX != 1.0f) {
            view.scaleX = 1.0f
            view.scaleY = 1.0f
        }
        val onLayoutChangeListener = object : AnimLayoutChangeListener(view, view2) {
            override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
                val insets: Insets?
                super.onLayoutChange(p0, p1, p2, p3, p4, p5, p6, p7, p8)
                val rootWindowInsets = p0?.rootWindowInsets
                if (rootWindowInsets != null) {
                    val isVisible = rootWindowInsets.isVisible(WindowInsets.Type.ime())
                    insets = rootWindowInsets.getInsets(WindowInsets.Type.ime())
                    val insets2 = rootWindowInsets.getInsets(WindowInsets.Type.navigationBars())
                    if (isVisible) {
                        this@PhoneDialogAnim.mImeHeight = insets.bottom - insets2.bottom
                    }
                } else {
                    insets = null
                }
                val context = p0?.context ?: return
                if (isInMultiScreenMode(context) && isInMultiScreenBottom(context)) {
                    updateDimBgMargin(i + (insets?.bottom ?: 0))
                }
            }
        }
        if (view.height > 0) {
            view.addOnLayoutChangeListener(object : OnLayoutChangeListener {
                override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
                    if (p0 == null) return
                    p0.removeOnLayoutChangeListener(this)
                    val height = view.height
                    relayoutView(p0, height, false)
                    doExecuteShowAnim(
                        p0, height, 0, isLandscape,
                        WeakRefShowListener(onDialogShowAnimListener, onLayoutChangeListener, p0, 0),
                        WeakRefUpdateListener(p0, isLandscape),
                    )
                }
            })
            view.visibility = View.INVISIBLE
            view.alpha = 1.0f
        } else {
            view.addOnLayoutChangeListener(object : OnLayoutChangeListener {
                override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
                    if (p0 == null) return
                    p0.removeOnLayoutChangeListener(this)
                    val i9 = p4 - p2
                    relayoutView(p0, i9, false)
                    doExecuteShowAnim(
                        view, i9, 0, isLandscape,
                        WeakRefShowListener(onDialogShowAnimListener, onLayoutChangeListener, p0, 0),
                        WeakRefUpdateListener(p0, isLandscape)
                    )
                }
            })
        }
        DimAnimator.show(view2)
    }

    inner class WeakRefDismissListener(view: View?, onDismiss: DialogAnimHelper.OnDismiss?): Animator.AnimatorListener {
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
        private var mOnLayoutChange: OnLayoutChangeListener?,
        view: View?,
        private val mEndTranslateY: Int
    ): AnimatorListenerAdapter() {
        private val mOnShow: WeakReference<AlertDialog.OnDialogShowAnimListener?>
        private val mView: WeakReference<View?>
        init {
            mOnShow = WeakReference(onDialogShowAnimListener)
            mView = WeakReference(view)
        }
        override fun onAnimationStart(animation: Animator) {
            val view = mView.get()
            if (view != null) {
                view.tag = "show"
                val onLayoutChangeListener = mOnLayoutChange
                if (onLayoutChangeListener != null) {
                    view.addOnLayoutChangeListener(onLayoutChangeListener)
                }
            }
            mOnShow.get()?.onShowAnimStart()
        }
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            val view = mView.get()
            val rootWindowInsets = view?.rootWindowInsets
            if (view != null && rootWindowInsets != null) {
                val isVisible = rootWindowInsets.isVisible(WindowInsets.Type.ime())
                val insets = rootWindowInsets.getInsets(WindowInsets.Type.ime())
                val insets2 = rootWindowInsets.getInsets(WindowInsets.Type.navigationBars())
                if (isVisible) {
                    this@PhoneDialogAnim.mImeHeight = insets.bottom - insets2.bottom
                } else {
                    this@PhoneDialogAnim.mImeHeight = 0
                }
                relayoutView(view, mEndTranslateY - this@PhoneDialogAnim.mImeHeight, true)
            }
            mOnShow.clear()
            mView.clear()
        }
        override fun onAnimationCancel(animation: Animator) {
            super.onAnimationCancel(animation)
            done()
            val view = mView.get()
            if (view != null) {
                relayoutView(view, mEndTranslateY, true)
            }
            mOnShow.clear()
            mView.clear()
        }
        private fun done() {
            val view = mView.get()
            if (view != null) {
                view.tag = null
                val onLayoutChangeListener = mOnLayoutChange
                if (onLayoutChangeListener != null) {
                    view.removeOnLayoutChangeListener(onLayoutChangeListener)
                    mOnLayoutChange = null
                }
            }
            mOnShow.get()?.onShowAnimComplete()
            sValueAnimatorWeakRef?.clear()
            sValueAnimatorWeakRef = null
        }
    }
    inner class WeakRefUpdateListener(
        view: View?,
        val mIsLandscape: Boolean
    ): ValueAnimator.AnimatorUpdateListener {
        private val mView: WeakReference<View?>
        init {
            mView = WeakReference(view)
        }
        override fun onAnimationUpdate(p0: ValueAnimator) {
            val view = mView.get() ?: return
            if ("hide" == view.tag) {
                p0.cancel()
                return
            }
            val rootWindowInsets = view.rootWindowInsets
            if (rootWindowInsets != null) {
                val isVisible = rootWindowInsets.isVisible(WindowInsets.Type.ime())
                val insets = rootWindowInsets.getInsets(WindowInsets.Type.ime())
                val insets2 = rootWindowInsets.getInsets(WindowInsets.Type.navigationBars())
                if (isVisible) {
                    this@PhoneDialogAnim.mImeHeight = insets.bottom - insets2.bottom
                } else {
                    this@PhoneDialogAnim.mImeHeight = 0
                }
            }
            relayoutView(view, (p0.animatedValue as Int) - this@PhoneDialogAnim.mImeHeight, false)
        }
    }
    open inner class AnimLayoutChangeListener(view: View?, view2: View?): OnLayoutChangeListener {
        private val wkDecorView: WeakReference<View?>
        private val wkDimBgView: WeakReference<View?>
        private val windowVisibleFrame: Rect = Rect()
        private val screenSize: Point = Point()
        init {
            wkDecorView = WeakReference(view?.rootView)
            wkDimBgView = WeakReference(view2)
        }
        override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
            wkDecorView.get()?.getWindowVisibleDisplayFrame(windowVisibleFrame)
        }
        fun updateDimBgMargin(margin: Int) {
            val view = wkDimBgView.get()
            if (view != null) {
                val marginLayoutParams = view.layoutParams as MarginLayoutParams
                if (marginLayoutParams.bottomMargin != margin) {
                    marginLayoutParams.bottomMargin = margin
                    view.layoutParams = marginLayoutParams
                }
            }
        }
        fun isInMultiScreenMode(context: Context): Boolean {
            return HyperXUIUtils.isInMultiWindowMode(context) && !HyperXUIUtils.isFreeformMode(context)
        }
        @SuppressWarnings("Deprecation")
        fun isInMultiScreenBottom(context: Context): Boolean {
            WindowUtils.getDisplay(context)?.getRealSize(screenSize)
            val rect = windowVisibleFrame
            if (rect.left != 0) {
                return false
            }
            val point = screenSize
            if (rect.right == point.x) {
                return rect.top >= (point.y.toFloat() * 0.2f).toInt()
            }
            return false
        }
    }
}