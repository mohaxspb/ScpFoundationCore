package ru.kuchanov.scpcore.mvp.base;

import ru.kuchanov.scpcore.db.model.Article;
import rx.Subscriber;

/**
 * Created by mohax on 09.01.2017.
 * <p>
 * for scp_ru
 */
public interface BaseArticlesListMvp {

    interface View extends BaseListMvp.View {
    }

    interface Presenter<V extends View> extends BaseListMvp.Presenter<V>, BaseArticleActions {
        //we need it as we have search fragment in which we do not save gained from api articles to DB
        //and so need to manually update adapter with changed by these actions data
        Subscriber<Article> getToggleFavoriteSubscriber();

        Subscriber<Article> getToggleReadSubscriber();

        Subscriber<String> getDeleteArticlesTextSubscriber();

        Subscriber<Article> getDownloadArticleSubscriber();
    }
}