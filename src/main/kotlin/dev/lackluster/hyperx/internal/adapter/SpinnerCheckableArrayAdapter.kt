package dev.lackluster.hyperx.internal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.RadioButton
import cn.fkj233.ui.R

class SpinnerCheckableArrayAdapter: ArrayAdapter<Any> {
    companion object val TAG_VIEW = R.id.tag_spinner_dropdown_view
    private val mCheckProvider: CheckedStateProvider?
    private val mContentAdapter: ArrayAdapter<Any>
    private val mInflater: LayoutInflater
    constructor(context: Context, resource: Int, arrayAdapter: ArrayAdapter<Any>, checkedStateProvider: CheckedStateProvider?):
            super(context, resource, android.R.id.text1) {
        mInflater = LayoutInflater.from(context)
        mContentAdapter = arrayAdapter
        mCheckProvider = checkedStateProvider
    }
    constructor(context: Context, arrayAdapter: ArrayAdapter<Any>, checkedStateProvider: CheckedStateProvider?):
            this(context, R.layout.hyperx_simple_spinner_layout_integrated, arrayAdapter, checkedStateProvider)

    override fun hasStableIds(): Boolean {
        return mContentAdapter.hasStableIds()
    }

    override fun getItemId(position: Int): Long {
        return mContentAdapter.getItemId(position)
    }

    override fun getItem(position: Int): Any? {
        return mContentAdapter.getItem(position)
    }

    override fun getCount(): Int {
        return mContentAdapter.count
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var flag = false
        var view = convertView
        if (view?.getTag(TAG_VIEW) == null) {
            view = mInflater.inflate(R.layout.hyperx_spinner_dropdown_checkable_item, parent, false)
            val viewHolder = ViewHolder(
                container = view.findViewById(R.id.spinner_dropdown_container),
                radioButton = view.findViewById(android.R.id.checkbox)
            )
            view.setTag(TAG_VIEW, viewHolder)
        }
        val viewHolder = view!!.getTag(TAG_VIEW)
        if (viewHolder != null && viewHolder is ViewHolder) {
            val dropDownView = mContentAdapter.getDropDownView(position, viewHolder.container.getChildAt(0), parent)
            viewHolder.container.removeAllViews()
            viewHolder.container.addView(dropDownView)
            val checkedStateProvider = mCheckProvider
            if (checkedStateProvider != null && checkedStateProvider.isChecked(position)) {
                flag = true
            }
            viewHolder.radioButton.isChecked = flag
            view.isActivated = flag
        }
        return view
    }
    interface CheckedStateProvider {
        fun isChecked(index: Int): Boolean
    }

    data class ViewHolder(
        val container: FrameLayout,
        val radioButton: RadioButton
    )
}