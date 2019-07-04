package ru.kuchanov.scpcore.mvp.contract;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ru.kuchanov.scpcore.db.model.Article;

/**
 * Created by mohax on 24.03.2017.
 */
public interface DataSyncActions {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ScoreAction.FAVORITE, ScoreAction.READ, ScoreAction.INTERSTITIAL_SHOWN,
            ScoreAction.VK_GROUP, ScoreAction.OUR_APP, ScoreAction.REWARDED_VIDEO,
            ScoreAction.AUTH, ScoreAction.VK_APP_SHARE, ScoreAction.NONE
    })
    @interface ScoreAction {

        String FAVORITE = "FAVORITE";
        String READ = "READ";
        String INTERSTITIAL_SHOWN = "INTERSTITIAL_SHOWN";
        String VK_GROUP = "VK_GROUP";
        String OUR_APP = "OUR_APP";
        String REWARDED_VIDEO = "REWARDED_VIDEO";
        String AUTH = "AUTH";
        String VK_APP_SHARE = "VK_APP_SHARE";
        String NONE = "NONE";
    }

    void updateArticleInFirebase(Article article, boolean showResultMessage);

    void syncData(boolean showResultMessage);

    void updateUserScoreForVkGroup(String id);

    void updateUserScoreForScoreAction(@ScoreAction String action);

    void checkIfUserJoinedAppVkGroup();
}
