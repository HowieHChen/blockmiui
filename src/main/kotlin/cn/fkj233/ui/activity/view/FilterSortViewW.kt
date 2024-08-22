package cn.fkj233.ui.activity.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import cn.fkj233.ui.R
import cn.fkj233.ui.activity.data.DataBinding
import cn.fkj233.ui.activity.data.FilterSortViewData
import dev.lackluster.hyperx.widget.FilterSortView2

class FilterSortViewW(
    private val filterSortViewData: FilterSortViewData,
    val dataBindingRecv: DataBinding.Binding.Recv? = null
) : BaseView {
    override fun isHyperXView(): Boolean {
        return true
    }
    override fun getType(): BaseView = this

    override fun create(context: Context, callBacks: (() -> Unit)?): View {
        return FilterSortView2(context).also {
            it.layoutParams = MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it1 ->
                it1.marginStart = context.resources.getDimensionPixelSize(R.dimen.hyperx_preference_item_padding_start)
                it1.marginEnd = context.resources.getDimensionPixelSize(R.dimen.hyperx_preference_item_padding_end)
            }
            for ((index, entry) in filterSortViewData.entries.withIndex()) {
                it.addView(
                    FilterSortView2.TabView(context).also { tabView ->
                        val obtainStyledAttributes = context.obtainStyledAttributes(null, R.styleable.FilterSortTabView2, R.attr.filterSortTabView2Style, R.style.Widget_FilterSortTabView2)
                        tabView.getTextView()?.text = entry.text ?: entry.textId?.let { it1 -> context.getText(it1) } ?: obtainStyledAttributes.getString(R.styleable.FilterSortTabView2_android_text)
                        tabView.getIconView()?.apply {
                            this.background = entry.indicator ?: obtainStyledAttributes.getDrawable(R.styleable.FilterSortTabView2_arrowFilterSortTabView)
                            this.visibility = if (entry.indicatorVisibility == View.VISIBLE) View.VISIBLE else View.GONE
                        }
                        tabView.background = obtainStyledAttributes.getDrawable(R.styleable.FilterSortTabView2_filterSortTabViewBackground)
                        tabView.foreground = obtainStyledAttributes.getDrawable(R.styleable.FilterSortTabView2_filterSortTabViewForeground)
                        val dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(
                            R.styleable.FilterSortTabView2_filterSortTabViewHorizontalPadding,
                            R.dimen.hyperx_filter_sort_tab_view2_padding_horizontal
                        )
                        val dimensionPixelSize2 = obtainStyledAttributes.getDimensionPixelSize(
                            R.styleable.FilterSortTabView2_filterSortTabViewVerticalPadding,
                            R.dimen.hyperx_filter_sort_tab_view2_padding_vertical
                        )
                        tabView.findViewById<View>(R.id.container).setPadding(
                            dimensionPixelSize,
                            dimensionPixelSize2,
                            dimensionPixelSize,
                            dimensionPixelSize2
                        )
                        val textAppearanceId = obtainStyledAttributes.getResourceId(R.styleable.FilterSortTabView2_filterSortTabViewTabTextAppearance, 0)
                        tabView.setTextAppearance(textAppearanceId)
                        val activatedTextAppearanceId = obtainStyledAttributes.getResourceId(R.styleable.FilterSortTabView2_filterSortTabViewTabActivatedTextAppearance, 0)
                        tabView.setActivatedTextAppearance(activatedTextAppearanceId)
                        obtainStyledAttributes.recycle()
                        tabView.setOnClickListener { view ->
                            entry.onClickListener?.onClick(view)
                            filterSortViewData.dataBindingSend?.let { send ->
                                send.send(index)
                            }
                            callBacks?.let { it1 -> it1() }
                        }
                    }
                )
            }
            it.setFilteredTab(filterSortViewData.defFiltered.coerceIn(0, filterSortViewData.entries.size - 1))
            dataBindingRecv?.setView(it)
        }
    }
}