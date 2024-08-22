package cn.fkj233.ui.activity.data

data class DialogData(
    val descData: DescData,
    val positiveButton: DialogButtonData? = null,
    val negativeButton: DialogButtonData? = null,
    val cancelable: Boolean = true,
    val checkBoxChecked: Boolean = false,
    val checkBoxMsg: String? = null,
    val checkBoxMsgId: Int? = null,
)