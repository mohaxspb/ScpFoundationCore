package ru.kuchanov.scpcore.controller.adapter.delegate.monetization

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_divider.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.DividerViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
class DividerDelegate : AbsListItemAdapterDelegate<DividerViewModel, MyListItem, DividerDelegate.AppViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is DividerViewModel

    override fun onCreateViewHolder(parent: ViewGroup) =
            AppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_divider, parent, false))

    override fun onBindViewHolder(item: DividerViewModel, viewHolder: AppViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView.dividerView) {
            setBackgroundResource(item.bgColor)
            layoutParams.height = item.height
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}