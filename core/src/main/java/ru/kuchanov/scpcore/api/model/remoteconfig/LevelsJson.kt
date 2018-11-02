package ru.kuchanov.scpcore.api.model.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import ru.kuchanov.scpcore.Constants

/**
 * Created by mohax on 29.04.2017.
 *
 *
 * for scp_ru
 */
data class LevelsJson(val levels: List<Level>) {

    /**
     * returns NO_SCORE_TO_MAX_LEVEL if level is already MAX_LEVEL_ID
     */
    fun scoreToNextLevel(userScore: Int, curLevel: Level): Int {
        if (curLevel.id == MAX_LEVEL_ID) {
            return NO_SCORE_TO_MAX_LEVEL
        }
        val nextLevel = levels[curLevel.id + 1]

        val nextLevelScore = nextLevel.score

        val max = nextLevelScore - curLevel.score
        val value = userScore - curLevel.score

        return max - value
    }

    fun getLevelMaxScore(curLevel: Level): Int {
        if (curLevel.id == MAX_LEVEL_ID) {
            return NO_SCORE_TO_MAX_LEVEL
        }
        val nextLevel = levels[curLevel.id + 1]

        val nextLevelScore = nextLevel.score

        return nextLevelScore - curLevel.score
    }

    fun getLevelForScore(score: Int): Level? = levels.findLast { score > it.score }

    data class Level(val id: Int,
        val title: String,
        val score: Int
    )

    companion object {

        @Suppress("MayBeConstant")
        @JvmField
        val MAX_LEVEL_ID = 5
        const val NO_SCORE_TO_MAX_LEVEL = -1

        private const val LEVELS_JSON_DEFAULT = """{"levels":[{"id":0,"title":"Level 0 (For Official Use Only)","score":0},{"id":1,"title":"Level 1 (Confidential)","score":1000},{"id":2,"title":"Level 2 (Restricted)","score":2000},{"id":3,"title":"Level 3 (Secret)","score":3000},{"id":4,"title":"Level 4 (Top Secret)","score":6000},{"id":5,"title":"Level 5 (Thaumiel)","score":10000}]}"""

        @JvmStatic
        val levelsJson: LevelsJson
            get() {
                val levelsJsonString = FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.LEVELS_JSON) ?: LEVELS_JSON_DEFAULT
                return Gson().fromJson(levelsJsonString, LevelsJson::class.java)
            }
    }
}