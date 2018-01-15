package ru.kuchanov.scpcore.controller.adapter.delegate

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_text.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.TextViewModel

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
class TextDelegate : AbsListItemAdapterDelegate<TextViewModel, MyListItem, TextDelegate.AppViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is TextViewModel

    override fun onCreateViewHolder(parent: ViewGroup) =
            AppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_text, parent, false))

    override fun onBindViewHolder(item: TextViewModel, viewHolder: AppViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView.textView){
            text = context.getString(item.text)
            setTextColor(ContextCompat.getColor(context, item.textColor))
            setBackgroundResource(item.bgColor)
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}