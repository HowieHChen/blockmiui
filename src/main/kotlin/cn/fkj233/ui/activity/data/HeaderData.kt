package cn.fkj233.ui.activity.data

data class HeaderData(
    val value: String? = null,
    val showArrow: Boolean = true,
    val largeIcon: Boolean = false,
    val corneRadius: Int = 0,
    val dataBindingRecv: DataBinding.Binding.Recv? = null
)