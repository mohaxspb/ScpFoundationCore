package ru.kuchanov.scpcore.db.model

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class LeaderboardUser(
        @PrimaryKey
        @Index
        var id: Long = 0,
        var fullName: String? = "",
        var avatar: String? = "",
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
        val FIELD_ID = "id"
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
        @JvmField
        val READ_ARTICLES_COUNT_NONE = -1
    }

    override fun toString(): String {
        return "LeaderboardUser(id='$id', fullName=$fullName, avatar=$avatar, score=$score, numOfReadArticles=$numOfReadArticles, levelNum=$levelNum, scoreToNextLevel=$scoreToNextLevel, curLevelScore=$curLevelScore)"
    }
}