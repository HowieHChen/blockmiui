package cn.fkj233.ui.activity.data

import android.graphics.drawable.Drawable
import android.view.View

data class FilterSortViewData(
    val entries: Array<TabViewData>,
    val defFiltered: Int = -1,
    val dataBindingSend: DataBinding.Binding.Send? = null
) {
    data class TabViewData(
        val text: String? = null,
        val textId: Int? = null,
        val onClickListener: View.OnClickListener? = null,
        val indicator: Drawable? = null,
        val indicatorVisibility: Int = View.GONE
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilterSortViewData

        return entries.contentEquals(other.entries)
    }

    override fun hashCode(): Int {
        return entries.contentHashCode()
    }
}