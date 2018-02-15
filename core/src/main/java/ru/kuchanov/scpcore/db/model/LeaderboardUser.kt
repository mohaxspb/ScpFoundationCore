package ru.kuchanov.scpcore.db.model

import io.realm.RealmObject

open class LeaderboardUser(
    var uid: String = "",
    var fullName: String = "",
    var avatar: String = "",
    var score: Int = 0,
    var numOfReadArticles: Int = 0,
    var levelNum: Int = 0,
    var scoreToNextLevel: Int = 0,
    var curLevelScore: Int = 0
) : RealmObject() {

    companion object {
        @JvmField
        val FIELD_UID = "uid"
        @JvmField
        val FIELD_FULL_NAME = "fullName"
        @JvmField
        val FIELD_AVATAR = "avatar"
        @JvmField
        val FIELD_SCORE = "score"
        @JvmField
        val FIELD_NUM_OF_READ_ARTICLES = "numOfReadArticles"
        @JvmField
        val FIELD_LEVEL_NUM = "levelNum"
        @JvmField
        val FIELD_SCORE_TO_NEXT_LEVEL = "scoreToNextLevel"
        @JvmField
        val FIELD_CUR_LEVEL_SCORE = "curLevelScore"
    }
}