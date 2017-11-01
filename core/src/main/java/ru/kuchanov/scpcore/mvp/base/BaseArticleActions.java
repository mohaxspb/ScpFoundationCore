package ru.kuchanov.scpcore.mvp.base;

import ru.kuchanov.scpcore.db.model.Article;

/**
 * Created by mohax on 09.01.2017.
 * <p>
 * for scp_ru
 */
interface BaseArticleActions {

    void toggleFavoriteState(Article article);

    void toggleReadState(Article article);

    /**
     *  we need article as arg, as we should determine if we
     *  should start download or should delete text
     */
    void toggleOfflineState(Article article);

    void toggleOfflineState(String url);
}