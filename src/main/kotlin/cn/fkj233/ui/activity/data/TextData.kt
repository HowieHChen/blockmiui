package cn.fkj233.ui.activity.data

data class TextData(
    val value: String? = null,
    val showArrow: Boolean = true,
    val dataBindingRecv: DataBinding.Binding.Recv? = null,
    val valueAdapter: (() -> String?)? = null
)