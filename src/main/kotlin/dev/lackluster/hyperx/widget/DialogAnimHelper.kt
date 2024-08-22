package dev.lackluster.hyperx.widget

import android.view.View
import dev.lackluster.hyperx.app.AlertDialog
import dev.lackluster.hyperx.os.Build
import dev.lackluster.hyperx.widget.dialoganim.IDialogAnim
import dev.lackluster.hyperx.widget.dialoganim.PadDialogAnim
import dev.lackluster.hyperx.widget.dialoganim.PhoneDialogAnim


object DialogAnimHelper {
    private var sDialogAnim: IDialogAnim? = null

    fun executeShowAnim(
        view: View?,
        view2: View?,
        z: Boolean,
        onDialogShowAnimListener: AlertDialog.OnDialogShowAnimListener?
    ) {
        if (sDialogAnim == null) {
            sDialogAnim = if (Build.IS_TABLET) {
                PadDialogAnim()
            } else {
                PhoneDialogAnim()
            }
        }
        sDialogAnim!!.executeShowAnim(view, view2, z, onDialogShowAnimListener)
    }

    fun cancelAnimator() {
        val iDialogAnim = sDialogAnim
        iDialogAnim?.cancelAnimator()
    }

    fun executeDismissAnim(view: View?, view2: View?, onDismiss: OnDismiss?) {
        if (sDialogAnim == null) {
            sDialogAnim = if (Build.IS_TABLET) {
                PadDialogAnim()
            } else {
                PhoneDialogAnim()
            }
        }
        sDialogAnim!!.executeDismissAnim(view, view2, onDismiss)
        sDialogAnim = null
    }

    interface OnDismiss {
        fun end()
    }
}