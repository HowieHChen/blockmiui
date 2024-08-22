package dev.lackluster.hyperx.widget.dialoganim

import android.view.View
import dev.lackluster.hyperx.app.AlertDialog
import dev.lackluster.hyperx.widget.DialogAnimHelper


interface IDialogAnim {
    fun cancelAnimator()
    fun executeDismissAnim(view: View?, view2: View?, onDismiss: DialogAnimHelper.OnDismiss?)
    fun executeShowAnim(view: View?, view2: View?, isLandscape: Boolean, onDialogShowAnimListener: AlertDialog.OnDialogShowAnimListener?)
}