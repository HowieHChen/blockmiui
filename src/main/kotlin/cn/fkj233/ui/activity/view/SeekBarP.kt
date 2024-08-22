package cn.fkj233.ui.activity.view

import android.content.Context
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.DescData
import cn.fkj233.ui.activity.data.SeekBarData
import dev.lackluster.hyperx.preference.SeekBarPreference

class SeekBarP(
    private val descData: DescData,
    private val seekBarData: SeekBarData,
    val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {
    override fun isHyperXView(): Boolean {
        return true
    }

    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return SeekBarPreference(context).also {
            it.setIcon(null)
            it.setTitle(descData.title ?: descData.titleId?.let { it1 -> context.getText(it1) })
            it.setShowSeekBarValue(seekBarData.showValue)
            it.initSeekBar(seekBarData.min, seekBarData.max, seekBarData.defProgress)
            if (!MIUIActivity.safeSP.containsKey(seekBarData.key)) {
                MIUIActivity.safeSP.putAny(seekBarData.key, seekBarData.defProgress)
            }
            it.setProgress( MIUIActivity.safeSP.getInt(seekBarData.key, seekBarData.defProgress))
            it.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    MIUIActivity.safeSP.putAny(seekBarData.key, p1)
                    callBacks?.let { it() }
                    seekBarData.dataBindingSend?.send(p1)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
            seekBarData.dataBindingRecv?.setView(it.getSeekBar())
            dataBindingRecv?.setView(it)
        }
    }
}