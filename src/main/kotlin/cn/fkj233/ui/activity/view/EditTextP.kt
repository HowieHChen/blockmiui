package cn.fkj233.ui.activity.view

import android.content.Context
import android.view.View
import android.widget.Toast
import cn.fkj233.ui.R
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.DescData
import cn.fkj233.ui.activity.data.EditTextData
import dev.lackluster.hyperx.preference.EditTextPreference

class EditTextP(
    private val descData: DescData,
    private val editTextData: EditTextData,
    val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {
    override fun isHyperXView(): Boolean {
        return true
    }
    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return EditTextPreference(context).also {
            val title = descData.title ?: descData.titleId?.let { it1 -> context.getText(it1) }
            val summary = descData.summary ?: descData.summaryId?.let { it1 -> context.getText(it1) }
            it.setIcon(descData.icon)
            it.setTitle(title)
            it.setSummary(summary)
            val defValue = editTextData.defValue ?: when (editTextData.valueType) {
                EditTextData.ValueType.BOOLEAN -> false
                EditTextData.ValueType.INT -> 0
                EditTextData.ValueType.FLOAT -> 0.0f
                EditTextData.ValueType.LONG -> 0L
                EditTextData.ValueType.STRING -> ""
            }
            if (!MIUIActivity.safeSP.containsKey(editTextData.key)) {
                MIUIActivity.safeSP.putAny(editTextData.key, defValue)
            }
            val dialogData = editTextData.dialogData
            val dialogDesc = dialogData.descData
            val positiveButton = dialogData.positiveButton
            val negativeButton = dialogData.negativeButton
            editTextData.convertor?.let { it1 -> it.setValueConvertor(it1) }
            it.setDialogIcon(dialogDesc.icon)
            it.setDialogTitle(dialogDesc.title ?: dialogDesc.titleId?.let { it1 -> context.getText(it1) })
            it.setDialogMessage(dialogDesc.summary ?: dialogDesc.summaryId?.let { it1 -> context.getText(it1) })
            it.setDialogCancelable(dialogData.cancelable)
            it.setNegativeButtonText(negativeButton?.text ?: negativeButton?.textId?.let { it1 -> context.getText(it1) } ?: context.getText(android.R.string.cancel))
            it.setNegativeButtonOnClickListener { dialog, which ->
                val listener = negativeButton?.onClickListener
                if (listener != null) {
                    listener.invoke(dialog, which)
                } else {
                    dialog.dismiss()
                }
            }
            it.setPositiveButtonText(positiveButton?.text ?: positiveButton?.textId?.let { it1 -> context.getText(it1) } ?: context.getText(android.R.string.ok))
            it.setPositiveButtonOnClickListener { dialog, which ->
                val listener = positiveButton?.onClickListener
                if (listener != null) {
                    listener.invoke(dialog, which)
                } else {
                    val newValueText = it.getEditText()?.text?.toString() ?: ""
                    val oldValue = it.getValue()
                    val newValue = when (editTextData.valueType) {
                        EditTextData.ValueType.BOOLEAN -> newValueText.toBooleanStrictOrNull()
                        EditTextData.ValueType.INT -> newValueText.toIntOrNull()
                        EditTextData.ValueType.FLOAT -> newValueText.toFloatOrNull()
                        EditTextData.ValueType.LONG -> newValueText.toLongOrNull()
                        EditTextData.ValueType.STRING -> newValueText
                    }
                    if (newValue != null && editTextData.isValueValid?.invoke(newValue) != false) {
                        if (oldValue == newValue) {
                            return@setPositiveButtonOnClickListener
                        } else {
                            MIUIActivity.safeSP.putAny(editTextData.key, newValue)
                            editTextData.dataBindingSend?.let { send ->
                                send.send(newValue)
                            }
                            editTextData.onValueChangeListener?.let { it1 -> it1(newValue) }
                            callBacks?.let { it1 -> it1() }
                            when (editTextData.showValue) {
                                EditTextData.ValuePosition.VALUE_VIEW -> it.setValue(newValue)
                                EditTextData.ValuePosition.SUMMARY_VIEW -> {
                                    it.setValueInternal(newValue)
                                    it.setSummary(it.getValueConvertor().toString(newValue))
                                }
                                EditTextData.ValuePosition.HIDDEN -> {}
                            }
                        }
                        dialog.dismiss()
                    } else if (editTextData.isValueValid != null){
                        Toast.makeText(context, context.getString(R.string.default_error_message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            it.setShowRightArrow(editTextData.showArrow)
            it.setHintText(editTextData.hintText)
            val value = when (editTextData.valueType) {
                EditTextData.ValueType.BOOLEAN -> MIUIActivity.safeSP.getBoolean(editTextData.key, defValue as Boolean)
                EditTextData.ValueType.INT -> MIUIActivity.safeSP.getInt(editTextData.key, defValue as Int)
                EditTextData.ValueType.FLOAT -> MIUIActivity.safeSP.getFloat(editTextData.key, defValue as Float)
                EditTextData.ValueType.LONG -> MIUIActivity.safeSP.getLong(editTextData.key, defValue as Long)
                EditTextData.ValueType.STRING -> MIUIActivity.safeSP.getString(editTextData.key, defValue as String)
            }
            when (editTextData.showValue) {
                EditTextData.ValuePosition.VALUE_VIEW -> {
                    it.getValueView().visibility = View.VISIBLE
                    it.setValue(value)
                }
                EditTextData.ValuePosition.SUMMARY_VIEW -> {
                    it.getValueView().visibility = View.GONE
                    it.setValueInternal(value)
                    it.setSummary(it.getValueConvertor().toString(value))
                }
                EditTextData.ValuePosition.HIDDEN -> {
                    it.getValueView().visibility = View.GONE
                }
            }
            editTextData.dataBindingRecv?.setView(it.getValueView())
            dataBindingRecv?.setView(it)
        }
    }
}