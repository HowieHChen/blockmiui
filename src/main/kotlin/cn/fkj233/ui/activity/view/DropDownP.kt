package cn.fkj233.ui.activity.view

import android.content.Context
import android.view.View
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.DescData
import cn.fkj233.ui.activity.data.DropDownData
import dev.lackluster.hyperx.preference.DropDownPreference

class DropDownP(
    private val descData: DescData,
    private val dropDownData: DropDownData,
    val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {
    override fun isHyperXView(): Boolean {
        return true
    }
    override fun getType(): BaseView = this
    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return DropDownPreference(context, dropDownData.mode).also { it ->
            it.setIcon(descData.icon)
            val title = descData.title ?: descData.titleId?.let { it1 -> context.getText(it1) }
            it.setTitle(title)
            it.setSummary(descData.summary ?: descData.summaryId?.let { it1 -> context.getText(it1) })
            if (dropDownData.mode == 0) {
                it.setPrompt(title)
            }
            if (!MIUIActivity.safeSP.containsKey(dropDownData.key)) {
                MIUIActivity.safeSP.putAny(dropDownData.key, dropDownData.defValue)
            }
            val entriesData = dropDownData.entries
            val entriesCount = entriesData.size
            val entries = Array<CharSequence?>(entriesCount) { index ->
                entriesData[index].entry
            }
            val entryValues = Array<CharSequence?>(entriesCount) { index ->
                entriesData[index].value.toString()
            }
            val entryIcons = IntArray(entriesCount) { index ->
                entriesData[index].iconRes ?: -1
            }
            it.setEntries(entries)
            it.setEntryValues(entryValues)
            it.setEntryIcons(entryIcons)
            it.setValueIndex(MIUIActivity.safeSP.getInt(dropDownData.key, dropDownData.defValue))
            it.setOnItemSelectedListener { entry, value ->
                MIUIActivity.safeSP.putAny(dropDownData.key, value)
                dropDownData.dataBindingSend?.let { send ->
                    send.send(value)
                }
                callBacks?.let { it1 -> it1() }
                dropDownData.onItemSelectedListener?.let { it1 -> it1(entry, value) }
            }
            dropDownData.dataBindingRecv?.setView(it.getSpinner())
            dataBindingRecv?.setView(it)
        }
    }
}