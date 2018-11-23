package ru.kuchanov.scpcore.controller.adapter.delegate.monetization.freeadsdisable

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_free_ads_app.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.freeadsdisable.VkGroupToJoinViewModel

/**
 * Created by mohax on 27.01.2018.
 *
 * for ScpCore
 */
class VkGroupToJoinDelegate(
        val clickListener: (String) -> Unit
) : AbsListItemAdapterDelegate<VkGroupToJoinViewModel, MyListItem, VkGroupToJoinDelegate.ViewHolder>() {

    override fun isForViewType(
            item: MyListItem,
            items: MutableList<MyListItem>,
            position: Int
    ) = item is VkGroupToJoinViewModel

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    R.layout.list_item_free_ads_simple,
                    parent,
                    false)
    )

    override fun onBindViewHolder(
            item: VkGroupToJoinViewModel,
            viewHolder: ViewHolder,
            payloads: MutableList<Any>
    ) {
        with(viewHolder.itemView) {
            setBackgroundResource(item.backgroundColor)
            titleTextView.text = item.title
            titleTextView.setTextColor(ContextCompat.getColor(context, item.textColor))
            descriptionTextView.text = item.subtitle
            descriptionTextView.setTextColor(ContextCompat.getColor(context, item.textColor))
            Glide.with(context)
                    .load(item.iconUrl)
                    .fitCenter()
                    .dontAnimate()
                    .into(iconImageView)
            cardView.setOnClickListener { clickListener(item.id) }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}