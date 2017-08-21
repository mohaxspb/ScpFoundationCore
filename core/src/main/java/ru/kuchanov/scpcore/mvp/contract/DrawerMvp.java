package ru.kuchanov.scpcore.mvp.contract;

import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse;
import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface DrawerMvp {
    interface View extends BaseActivityMvp.View {
        /**
         * @return true if need to show selected
         */
        boolean onNavigationItemClicked(int id);

        void onReceiveRandomUrl(String url);

        void showLeaderboard(LeaderBoardResponse leaderBoardResponse);
    }

    interface Presenter<V extends View> extends BaseActivityMvp.Presenter<V> {
        void onNavigationItemClicked(int id);

        void getRandomArticleUrl();

        void onAvatarClicked();
    }
}