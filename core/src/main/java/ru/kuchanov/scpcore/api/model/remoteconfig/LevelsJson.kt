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

    fun getLevel(levelNum: Int) = levels[levelNum]

    data class Level(val id: Int,
        val title: String,
        val score: Int
    )

    companion object {

        @JvmField
        val MAX_LEVEL_ID = 5
        @JvmField
        val NO_SCORE_TO_MAX_LEVEL = -1


        @JvmStatic
        val levelsJson: LevelsJson
            get() {
                val levelsJsonString = FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.LEVELS_JSON)
                return Gson().fromJson(levelsJsonString, LevelsJson::class.java)
            }
    }
}