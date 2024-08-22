package cn.fkj233.ui.activity.data

import dev.lackluster.hyperx.preference.EditTextPreference

data class EditTextData(
    val key: String,
    val valueType: ValueType,
    val defValue: Any? = null,
    val hintText: String? = null,
    val showValue: ValuePosition = ValuePosition.VALUE_VIEW,
    val showArrow: Boolean = true,
    val dialogData: DialogData,
    var convertor: EditTextPreference.ValueConvertor? = null,
    val isValueValid: ((value: Any) -> Boolean)? = null,
    val onValueChangeListener: ((value: Any) -> Unit)? = null,
    val dataBindingRecv: DataBinding.Binding.Recv? = null,
    val dataBindingSend: DataBinding.Binding.Send? = null,
) {
    companion object {
        val BOOLEAN_CONVERTOR = object : EditTextPreference.ValueConvertor {
            override fun toString(value: Any): String {
                return (value as Boolean).toString()
            }
            override fun fromString(string: String): Any? {
                return string.toBooleanStrictOrNull()
            }
        }
        val INT_CONVERTOR = object : EditTextPreference.ValueConvertor {
            override fun toString(value: Any): String {
                return (value as Int).toString()
            }
            override fun fromString(string: String): Any? {
                return string.toIntOrNull()
            }
        }
        val FLOAT_CONVERTOR = object : EditTextPreference.ValueConvertor {
            override fun toString(value: Any): String {
                return (value as Float).toString()
            }
            override fun fromString(string: String): Any? {
                return string.toFloatOrNull()
            }
        }
        val LONG_CONVERTOR = object : EditTextPreference.ValueConvertor {
            override fun toString(value: Any): String {
                return (value as Long).toString()
            }
            override fun fromString(string: String): Any? {
                return string.toLongOrNull()
            }
        }
        val STRING_CONVERTOR = object : EditTextPreference.ValueConvertor {
            override fun toString(value: Any): String {
                return (value as String)
            }
            override fun fromString(string: String): Any {
                return string
            }
        }
    }
    init {
        if (convertor == null) {
            convertor = when (valueType) {
                ValueType.BOOLEAN -> BOOLEAN_CONVERTOR
                ValueType.INT -> INT_CONVERTOR
                ValueType.FLOAT -> FLOAT_CONVERTOR
                ValueType.LONG -> LONG_CONVERTOR
                else -> STRING_CONVERTOR
            }
        }
    }
    enum class ValueType {
        BOOLEAN,
        INT,
        FLOAT,
        LONG,
        STRING,
    }
    enum class ValuePosition {
        HIDDEN,
        VALUE_VIEW,
        SUMMARY_VIEW,
    }
}