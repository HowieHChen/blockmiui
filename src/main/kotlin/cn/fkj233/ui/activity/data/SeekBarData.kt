package cn.fkj233.ui.activity.data

import android.widget.TextView

data class SeekBarData(
    val key: String,
    val min: Int,
    val max: Int,
    val defProgress: Int = 0,
    val showValue: Boolean = false,
    val dataBindingRecv: DataBinding.Binding.Recv? = null,
    val dataBindingSend: DataBinding.Binding.Send? = null,
    val callBacks: ((Int, TextView) -> Unit)? = null
)