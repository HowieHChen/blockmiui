package cn.fkj233.ui.activity.data

data class DropDownData(
    val key: String,
    val defValue: Int = 0,
    val mode: Int = -1,
    val entries: Array<SpinnerItemData>,
    val dataBindingRecv: DataBinding.Binding.Recv? = null,
    val dataBindingSend: DataBinding.Binding.Send? = null,
    val onItemSelectedListener: ((String, Int) -> Unit)? = null
) {
    data class SpinnerItemData(
        val entry: String,
        val value: Int,
        val iconRes: Int? = null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DropDownData

        if (key != other.key) return false
        if (defValue != other.defValue) return false
        if (!entries.contentEquals(other.entries)) return false
        if (dataBindingRecv != other.dataBindingRecv) return false
        if (dataBindingSend != other.dataBindingSend) return false
        if (onItemSelectedListener != other.onItemSelectedListener) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + defValue.hashCode()
        result = 31 * result + entries.contentHashCode()
        result = 31 * result + (dataBindingRecv?.hashCode() ?: 0)
        result = 31 * result + (dataBindingSend?.hashCode() ?: 0)
        result = 31 * result + (onItemSelectedListener?.hashCode() ?: 0)
        return result
    }
}