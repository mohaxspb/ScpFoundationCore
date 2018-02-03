package ru.kuchanov.scpcore.controller.adapter.delegate.monetization.leaderboard

import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.Target
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_leaderboard_user.view.*
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard.LeaderboardUserViewModel
import timber.log.Timber
import java.lang.Exception

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
                    .error(R.mipmap.ic_launcher)
                    .into(object : BitmapImageViewTarget(avatarImageView) {
                        override fun setResource(resource: Bitmap) {
                            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, resource)
                            circularBitmapDrawable.isCircular = true
                            avatarImageView.setImageDrawable(circularBitmapDrawable)
                        }
                    })

            nameTextView.text = user.fullName
            readArticlesCountTextView.text = context.getString(R.string.leaderboard_articles_read, user.numOfReadArticles)
            userScoreTextView.text = user.score.toString()

            val levelViewModel = item.levelViewModel
            val level = item.levelViewModel.level
            levelNumTextView.text = level.id.toString()
            levelTextView.text = context.getString(R.string.level_num, level.id)
            if (levelViewModel.isMaxLevel) {
                experienceProgressBar.max = 1
                experienceProgressBar.progress = 1
                maxLevelTextView.visibility = View.VISIBLE
            } else {
                maxLevelTextView.visibility = View.GONE
                experienceProgressBar.max = levelViewModel.nextLevelScore
                experienceProgressBar.progress = user.score - level.score
                expToNextLevelTextView.text = context.getString(R.string.score_num, levelViewModel.scoreToNextLevel)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}