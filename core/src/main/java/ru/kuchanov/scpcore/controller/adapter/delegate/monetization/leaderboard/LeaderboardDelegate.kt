package ru.kuchanov.scpcore.controller.adapter.delegate.monetization.leaderboard

import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_leaderboard_user.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel

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

            Glide.with(context)
                    .load(user.avatar)
                    .asBitmap()
                    .centerCrop()
                    .into(object : BitmapImageViewTarget(avatarImageView) {
                        override fun setResource(resource: Bitmap) {
                            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, resource)
                            circularBitmapDrawable.isCircular = true
                            avatarImageView.setImageDrawable(circularBitmapDrawable)
                        }
                    })

            nameTextView.text = user.fullName
            readArticlesCountTextView.text = context.getString(R.string.leaderboard_articles_read, user.numOfReadArticles)

            //todo level info
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}