package ru.kuchanov.scpcore.mvp.contract;

import ru.kuchanov.scpcore.mvp.base.BaseArticlesListMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface SiteSearchArticlesMvp {
    interface View extends BaseArticlesListMvp.View {
    }

    interface Presenter extends BaseArticlesListMvp.Presenter<View> {
        void setQuery(String query);
    }
}