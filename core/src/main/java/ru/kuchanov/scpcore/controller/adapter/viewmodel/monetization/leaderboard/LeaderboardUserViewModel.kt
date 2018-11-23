package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard

import android.support.annotation.ColorRes
import ru.kuchanov.scpcore.R
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.db.model.LeaderboardUser

/**
 * Created by mohax on 02.02.2018.
 *
 * for ScpCore
 */
data class LeaderboardUserViewModel(
        val position: Int,
        val user: LeaderboardUser,
        val levelViewModel: LevelViewModel,
        @ColorRes var bgColor: Int = R.color.freeAdsBackgroundColor,
        @ColorRes var medalTint: Int = android.R.color.transparent
) : MyListItem {
    companion object {
        const val POSITION_NONE = -1
    }
}