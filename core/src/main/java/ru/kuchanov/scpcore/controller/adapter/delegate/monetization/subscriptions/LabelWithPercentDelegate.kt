package ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_label_with_percent.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.LabelWithPercentViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.ui.util.MyHtmlTagHandler

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
class LabelWithPercentDelegate : AbsListItemAdapterDelegate<LabelWithPercentViewModel, MyListItem, LabelWithPercentDelegate.AppViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is LabelWithPercentViewModel

    override fun onCreateViewHolder(parent: ViewGroup) =
            AppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_label_with_percent, parent, false))

    override fun onBindViewHolder(item: LabelWithPercentViewModel, viewHolder: AppViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            titleTextView.text = context.getString(item.text)
            priceTextView.text = Html.fromHtml("<s>${item.price}</s>", null,  MyHtmlTagHandler())
            percentTextView.text = if (!item.percent.isEmpty()) context.getString(R.string.subs_percent, item.percent) else ""
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}