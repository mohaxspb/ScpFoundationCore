package ru.kuchanov.scpcore.api.model.response

import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser
import java.io.Serializable

/**
 * Created by mohax on 05.05.2017.
 *
 * for scp-ru
 */
data class LeaderBoardResponse(
    var lastUpdated: Long,
    var timeZone: String,
    var users: MutableList<FirebaseObjectUser>
) : Serializable