package ru.kuchanov.scpcore.api.model.response

import java.io.Serializable
import java.util.*

/**
 * Created by mohax on 05.05.2017.
 *
 * for scp-ru
 */
data class LeaderboardUsersUpdateDates(
    val id: Long,
    val langId: String,
    var updated: Date
) : Serializable