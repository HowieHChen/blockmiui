package dev.lackluster.hyperx.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import cn.fkj233.ui.R

open class SpinnerDoubleLineContentAdapter : ArrayAdapter<Any> {
    companion object val TAG_VIEW = R.id.tag_spinner_dropdown_view_double_line
    protected var mEntries: Array<CharSequence?>? = null
    protected var mIcons: Array<Drawable?>? = null
    protected var mSummaries: Array<CharSequence?>? = null
    private val mInflater: LayoutInflater
    protected constructor(context: Context, resource: Int): super(context, resource) {
        mInflater = LayoutInflater.from(context)
    }

    constructor(context: Context, charSequenceArr: Array<CharSequence?>?, charSequenceArr2: Array<CharSequence?>?, iconArr: IntArray?): this(context, 0) {
        mEntries = charSequenceArr
        mSummaries = charSequenceArr2
        setEntryIcons(iconArr)
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun hasStableIds(): Boolean {
        return true
    }
    fun setEntryIcons(iArr: IntArray?) {
        if (iArr == null) {
            setEntryIcons(null as Array<Drawable?>?)
            return
        }
        val drawableArr = Array<Drawable?>(iArr.size) {
            val resId = iArr[it]
            if (resId > 0) {
                context.getDrawable(resId)
            } else {
                null
            }
        }
        setEntryIcons(drawableArr)
    }
    fun setEntryIcons(drawableArr: Array<Drawable?>?) {
        mIcons = drawableArr
    }
    fun getEntryIcons(): Array<Drawable?>? {
        return mIcons
    }
    fun setEntries(charSequenceArr: Array<CharSequence?>?) {
        mEntries = charSequenceArr
    }
    fun getEntries(): Array<CharSequence?>? {
        return mEntries
    }
    fun setSummaries(charSequenceArr: Array<CharSequence?>?) {
        mSummaries = charSequenceArr
    }

    override fun getItem(position: Int): Any? {
        return mEntries?.get(position)
    }

    override fun getCount(): Int {
        return mEntries?.size ?: 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view?.getTag(TAG_VIEW) == null) {
            view = mInflater.inflate(R.layout.hyperx_spiner_dropdown_view_double_line, parent, false)
            val viewHolder = ViewHolder(
                icon = view.findViewById(android.R.id.icon),
                title = view.findViewById(android.R.id.title),
                summary = view.findViewById(android.R.id.summary)
            )
            view.setTag(TAG_VIEW, viewHolder)
        }
        val entry = getEntry(position)
        val summary = getSummary(position)
        val icon = getIcon(position)
        val viewHolder = view!!.getTag(TAG_VIEW)
        if (viewHolder != null && viewHolder is ViewHolder) {
            if (!TextUtils.isEmpty(entry)) {
                viewHolder.title.text = entry
                viewHolder.title.visibility = View.VISIBLE
            } else {
                viewHolder.title.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(summary)) {
                viewHolder.summary.text = summary
                viewHolder.summary.visibility = View.VISIBLE
            } else {
                viewHolder.summary.visibility = View.GONE
            }
            if (icon != null) {
                viewHolder.icon.setImageDrawable(icon)
                viewHolder.icon.visibility = View.VISIBLE
            } else {
                viewHolder.icon.visibility = View.GONE
            }
        }
        return view
    }
    fun getEntry(i: Int): CharSequence? {
        val charSequence = mEntries
        if (charSequence == null || i >= charSequence.size) {
            return null
        }
        return charSequence[i]
    }
    fun getSummary(i: Int): CharSequence? {
        val charSequence = mSummaries
        if (charSequence == null || i >= charSequence.size) {
            return null
        }
        return charSequence[i]
    }
    fun getIcon(i: Int): Drawable? {
        val icons = mIcons
        if (icons == null || i >= icons.size) {
            return null
        }
        return icons[i]
    }
    data class ViewHolder(
        val icon: ImageView,
        val title: TextView,
        val summary: TextView
    )
}