package cn.fkj233.ui.activity.data

import android.content.DialogInterface

data class DialogButtonData(
    val text: String? = null,
    val textId: Int? = null,
    val onClickListener: ((dialog: DialogInterface, which: Int) -> Unit)? = null
)