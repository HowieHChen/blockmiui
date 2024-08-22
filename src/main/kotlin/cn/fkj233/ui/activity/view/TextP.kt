package cn.fkj233.ui.activity.view

import android.content.Context
import android.view.View
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.DescData
import cn.fkj233.ui.activity.data.TextData
import dev.lackluster.hyperx.preference.TextPreference

class TextP(
    private val descData: DescData,
    private val textData: TextData,
    val onClickListener: ((View) -> Unit)? = null,
    val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {
    // lateinit var value: TextView
    override fun isHyperXView(): Boolean {
        return true
    }

    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return TextPreference(context).also {
            it.setIcon(descData.icon)
            it.setTitle(descData.title ?: descData.titleId?.let { it1 -> context.getText(it1) })
            it.setSummary(descData.summary ?: descData.summaryId?.let { it1 -> context.getText(it1) })
            it.setValue(textData.value ?: textData.valueAdapter?.let { it() })
            it.setShowRightArrow(textData.showArrow)
            // value = it.mValue
            onClickListener?.let { unit ->
                it.setOnClickListener { view ->
                    unit(view)
                    callBacks?.let { it1 -> it1() }
                }
            }
            textData.dataBindingRecv?.setView(it.getValueLabel())
            dataBindingRecv?.setView(it)
        }
    }
}