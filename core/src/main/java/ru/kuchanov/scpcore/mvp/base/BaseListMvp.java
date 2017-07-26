package ru.kuchanov.scpcore.mvp.base;

import java.util.List;

import ru.kuchanov.scpcore.db.model.Article;

/**
 * Created by mohax on 25.12.2016.
 * <p>
 * for pacanskiypublic
 */
public interface BaseListMvp {

    interface View extends BaseMvp.View {

        void showSwipeProgress(boolean show);

        void showCenterProgress(boolean show);

        void showBottomProgress(boolean show);

        void enableSwipeRefresh(boolean enable);

        void updateData(List<Article> data);

        void resetOnScrollListener();
    }

    interface Presenter<V extends View> extends BaseMvp.Presenter<V> {

        List<Article> getData();

        void getDataFromDb();

        void getDataFromApi(int offset);
    }
}