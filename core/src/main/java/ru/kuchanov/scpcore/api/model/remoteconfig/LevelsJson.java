package ru.kuchanov.scpcore.api.model.remoteconfig;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;

import ru.kuchanov.scpcore.Constants;
import timber.log.Timber;

/**
 * Created by mohax on 29.04.2017.
 * <p>
 * for scp_ru
 */
public class LevelsJson {

    public static final int MAX_LEVEL_ID = 5;

    public List<Level> levels;

    public static class Level {

        public int id;
        public String title;
        public int score;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Level level = (Level) o;

            return id == level.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return "Level{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", score=" + score +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LevelsJson{" +
                "levels=" + levels +
                '}';
    }

    public static Level getLevelForScore(int score) {
        Level userLevel = null;
        String levelsJsonString = FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.LEVELS_JSON);
        LevelsJson levelsJson = new GsonBuilder().create().fromJson(levelsJsonString, LevelsJson.class);
        if (levelsJson != null) {
//            for (int i = 0; i < levelsJson.levels.size(); i++) {
//                LevelsJson.Level level = levelsJson.levels.get(i);
//                if (score >= level.score) {
//                    userLevel = level;
//                    break;
//                } else if (i == levelsJson.levels.size() - 1) {
//                    //so max level reached
//                    userLevel = level;
//                }
//            }
            Collections.reverse(levelsJson.levels);
            for (int i = 0; i < levelsJson.levels.size(); i++) {
                LevelsJson.Level level = levelsJson.levels.get(i);
                if (score >= level.score) {
                    userLevel = level;
                    break;
                } else if (i == levelsJson.levels.size() - 1) {
                    //so max level reached
                    userLevel = level;
                }
            }
        } else {
            Timber.e("levelsJson is null");
        }
        return userLevel;
    }
}