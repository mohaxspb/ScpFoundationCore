package ru.kuchanov.scpcore.controller.adapter.delegate.monetization.leaderboard

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_read_history.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.ReadHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*


class ReadHistoryDelegate(
        private val clickListener: (String) -> Unit,
        private val clickListenerDelete: (Long) -> Unit
) : AbsListItemAdapterDelegate<
        ReadHistoryViewModel,
        MyListItem,
        ReadHistoryDelegate.ViewHolder
        >() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) =
            item is ReadHistoryViewModel

    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(
                    LayoutInflater
                            .from(parent.context)
                            .inflate(
                                    R.layout.list_item_read_history,
                                    parent,
                                    false
                            )
            )

    override fun onBindViewHolder(item: ReadHistoryViewModel, viewHolder: ViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            titleTextView.text = item.readHistoryTransaction.title
                    ?: item.readHistoryTransaction.url
            dateTextView.text = simpleDateFormat.format(item.readHistoryTransaction.created)
            deleteImageView.setOnClickListener {
                clickListenerDelete.invoke(item.readHistoryTransaction.id)
            }
            setOnClickListener { clickListener.invoke(item.readHistoryTransaction.url) }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {

        @SuppressLint("ConstantLocale")
        val simpleDateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
    }
}