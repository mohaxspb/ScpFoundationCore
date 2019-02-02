package ru.kuchanov.scpcore.controller.adapter.viewmodel.monetization.leaderboard

import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem
import ru.kuchanov.scpcore.db.model.ReadHistoryTransaction

/**
 * Created by mohax on 02.02.2018.
 *
 * for ScpCore
 */
data class ReadHistoryViewModel(
        val readHistoryTransaction: ReadHistoryTransaction
) : MyListItem