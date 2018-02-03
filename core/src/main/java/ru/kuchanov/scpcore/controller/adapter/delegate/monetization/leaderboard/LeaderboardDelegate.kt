package ru.kuchanov.scpcore.controller.adapter.delegate.monetization.leaderboard

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_leaderboard_user.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel
import ru.kuchanov.scpcore.db.model.User

/**
 * Created by mohax on 15.01.2018.
 *
 * for ScpCore
 */
class LeaderboardDelegate : AbsListItemAdapterDelegate<LeaderboardUserViewModel, MyListItem, LeaderboardDelegate.ViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is LeaderboardUserViewModel

    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_leaderboard_user, parent, false))

    override fun onBindViewHolder(item: LeaderboardUserViewModel, viewHolder: ViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            val user = item.user
            chartPlaceTextView.text = item.position.toString()
            //todo
//            titleTextView.text = context.getString(item.title)
//            descriptionTextView.text = if (item.description != 0) context.getString(item.description) else null
//            iconImageView.setImageResource(item.icon)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}