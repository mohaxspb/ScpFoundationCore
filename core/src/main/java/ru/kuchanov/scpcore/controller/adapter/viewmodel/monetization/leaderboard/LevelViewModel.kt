package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard

import ru.kuchanov.scpcore.api.model.remoteconfig.LevelsJson
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem

/**
 * Created by mohax on 03.02.2018.
 *
 * for ScpCore
 */
data class LevelViewModel(
        val level: LevelsJson.Level,
        val scoreToNextLevel: Int,
        val nextLevelScore: Int,
        val isMaxLevel: Boolean
) : MyListItem