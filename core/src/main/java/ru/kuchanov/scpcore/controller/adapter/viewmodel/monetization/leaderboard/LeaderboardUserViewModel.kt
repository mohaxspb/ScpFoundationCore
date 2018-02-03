package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard

import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 02.02.2018.
 *
 * for ScpCore
 */
data class LeaderboardUserViewModel(
        val position: Int,
        val user: FirebaseObjectUser,
        val levelViewModel: LevelViewModel
) : MyListItem