package ru.kuchanov.scpcore.db.model

import io.realm.RealmObject

class LeaderboardUser(
    val uid: String,
    val fullName: String,
    val avatar: String,
    val score: Int,
    val numOfReadArticles: Int,
    val levelNum: Int,
    val scoreToNextLevel: Int,
    val curLevelScore: Int
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