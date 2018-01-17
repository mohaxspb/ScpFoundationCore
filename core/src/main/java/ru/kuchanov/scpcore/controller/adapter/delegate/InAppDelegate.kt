package ru.kuchanov.scpcore.controller.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_in_app.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.InAppViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
class InAppDelegate(val clickListener: (String) -> Unit) : AbsListItemAdapterDelegate<InAppViewModel, MyListItem, InAppDelegate.AppViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is InAppViewModel

    override fun onCreateViewHolder(parent: ViewGroup) =
            AppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_in_app, parent, false))

    override fun onBindViewHolder(item: InAppViewModel, viewHolder: AppViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            titleTextView.text = context.getString(item.title)
            descriptionTextView.text = if (item.description != 0) context.getString(item.description) else null
            priceTextView.text = item.price
            iconImageView.setImageResource(item.icon)
            cardView.setOnClickListener { clickListener(item.id) }
            root.setBackgroundResource(item.background)
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}