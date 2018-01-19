package ru.kuchanov.scpcore.controller.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_cur_subs_empty.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.CurSubsEmptyViewModel
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
class CurSubsEmptyDelegate(val clickListener: (String) -> Unit) : AbsListItemAdapterDelegate<CurSubsEmptyViewModel, MyListItem, CurSubsEmptyDelegate.AppViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is CurSubsEmptyViewModel

    override fun onCreateViewHolder(parent: ViewGroup) =
            AppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_cur_subs_empty, parent, false))

    override fun onBindViewHolder(item: CurSubsEmptyViewModel, viewHolder: AppViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            cardView.setOnClickListener { clickListener(item.id) }
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}