package cn.fkj233.ui.activity.data

data class SwitchData(
    val key: String,
    val defValue: Boolean = false,
    val dataBindingRecv: DataBinding.Binding.Recv? = null,
    val dataBindingSend: DataBinding.Binding.Send? = null,
    val onCheckedChangeListener: ((Boolean) -> Unit)? = null
)