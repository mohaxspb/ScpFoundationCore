package ru.kuchanov.scpcore.controller.adapter.delegate.monetization.subscriptions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_cur_subs.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.subscriptions.CurSubsViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
class CurSubsDelegate(
    val clickListener: (String) -> Unit,
    private val refreshClickListener: () -> Unit
) : AbsListItemAdapterDelegate<CurSubsViewModel, MyListItem, CurSubsDelegate.AppViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) =
            item is CurSubsViewModel

    override fun onCreateViewHolder(parent: ViewGroup) =
            AppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_cur_subs, parent, false))

    override fun onBindViewHolder(item: CurSubsViewModel, viewHolder: AppViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            titleTextView.text = context.getString(item.title)
            descriptionTextView.text = if (item.description != 0) context.getString(item.description) else null
            iconImageView.setImageResource(item.icon)
            cardView.setOnClickListener { clickListener(item.id) }
            refreshImageView.setOnClickListener { refreshClickListener() }
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}