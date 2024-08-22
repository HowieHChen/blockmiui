package cn.fkj233.ui.activity.view

import android.content.Context
import android.view.View
import cn.fkj233.ui.activity.data.CategoryData
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.DescData
import dev.lackluster.hyperx.preference.CategoryTitle

class CategoryTitleP(
    private val descData: DescData?,
    private val categoryData: CategoryData,
    val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {
    override fun isHyperXView(): Boolean {
        return true
    }

    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return CategoryTitle(context).also {
            it.setTitle(descData?.title ?: descData?.titleId?.let { it1 -> context.getText(it1) })
            it.setStyle(categoryData.hideTitle, categoryData.hideLine)
            dataBindingRecv?.setView(it)
        }
    }
}