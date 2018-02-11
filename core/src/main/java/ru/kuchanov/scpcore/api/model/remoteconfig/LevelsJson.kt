package ru.kuchanov.scpcore.api.model.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import ru.kuchanov.scpcore.Constants
import java.util.*

/**
 * Created by mohax on 29.04.2017.
 *
 *
 * for scp_ru
 */
class LevelsJson {

    var levels: List<Level>? = null

    /**
     * returns NO_SCORE_TO_MAX_LEVEL if level is already MAX_LEVEL_ID
     */
    fun scoreToNextLevel(userScore: Int, curLevel: Level): Int {
        if (curLevel.id == MAX_LEVEL_ID) {
            return NO_SCORE_TO_MAX_LEVEL
        }
        val nextLevel = levels!![curLevel.id + 1]

        val nextLevelScore = nextLevel.score

        val max = nextLevelScore - curLevel.score
        val value = userScore - curLevel.score

        return max - value
    }

    fun getLevelMaxScore(curLevel: Level): Int {
        if (curLevel.id == MAX_LEVEL_ID) {
            return NO_SCORE_TO_MAX_LEVEL
        }
        val nextLevel = levels!![curLevel.id + 1]

        val nextLevelScore = nextLevel.score

        return nextLevelScore - curLevel.score
    }

    fun getLevelForScore(score: Int): Level? {
        var userLevel: Level? = null
            Collections.reverse(levels!!)
            for (i in levels!!.indices) {
                val level = levelsJson.levels!![i]
                if (score >= level.score) {
                    userLevel = level
                    break
                } else if (i == levels!!.size - 1) {
                    //so max level reached
                    userLevel = level
                }
            }
        return userLevel
    }

    class Level {

        var id: Int = 0
        var title: String? = null
        var score: Int = 0

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false

            val level = o as Level?

            return id == level!!.id
        }

        override fun hashCode(): Int {
            return id
        }

        override fun toString(): String {
            return "Level{" +
                    "id=" + id +
                    ", title='" + title + '\''.toString() +
                    ", score=" + score +
                    '}'.toString()
        }
    }

    override fun toString(): String {
        return "LevelsJson{" +
                "levels=" + levels +
                '}'.toString()
    }

    companion object {

        val MAX_LEVEL_ID = 5
        val NO_SCORE_TO_MAX_LEVEL = -1

        val levelsJson: LevelsJson
            get() {
                val levelsJsonString = FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.LEVELS_JSON)
                return Gson().fromJson(levelsJsonString, LevelsJson::class.java)
            }
    }
}