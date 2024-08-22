package cn.fkj233.ui.activity.view

import android.content.Context
import android.view.View
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.SwitchData
import cn.fkj233.ui.activity.data.DescData
import dev.lackluster.hyperx.preference.SwitchPreference

class SwitchP(
    private val descData: DescData,
    private val switchData: SwitchData,
    val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {
    // lateinit var switch: Switch
    override fun isHyperXView(): Boolean {
        return true
    }

    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return SwitchPreference(context).also {
            it.setIcon(descData.icon)
            it.setTitle(descData.title ?: descData.titleId?.let { it1 -> context.getText(it1) })
            it.setSummary(descData.summary ?: descData.summaryId?.let { it1 -> context.getText(it1) })
            if (!MIUIActivity.safeSP.containsKey(switchData.key)) {
                MIUIActivity.safeSP.putAny(switchData.key, switchData.defValue)
            }
            it.setChecked(MIUIActivity.safeSP.getBoolean(switchData.key, switchData.defValue))
            it.setOnCheckedChangeListener { _, p1 ->
                MIUIActivity.safeSP.putAny(switchData.key, p1)
                switchData.dataBindingSend?.let { send ->
                    send.send(p1)
                }
                callBacks?.let { it1 -> it1() }
                switchData.onCheckedChangeListener?.let { it(p1) }
            }
            switchData.dataBindingRecv?.setView(it.getSwitch())
            dataBindingRecv?.setView(it)
        }
    }
}