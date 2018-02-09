package ru.kuchanov.scpcore.mvp.contract.article;

import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.mvp.base.BaseMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface ArticleMvp {
    interface View extends BaseMvp.View {
        void showSwipeProgress(boolean show);

        void showCenterProgress(boolean show);

        void enableSwipeRefresh(boolean enable);

        void showData(Article article);
    }

    interface Presenter extends BaseMvp.Presenter<View> {
        /**
         * @param url url is id for Article obj
         */
        void setArticleId(String url);

        Article getData();

        void getDataFromDb();

        void getDataFromApi();

        void setArticleIsReaden(String url);
    }
}