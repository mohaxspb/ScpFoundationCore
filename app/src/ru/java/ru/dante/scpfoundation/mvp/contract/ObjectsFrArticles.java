package ru.dante.scpfoundation.mvp.contract;

import ru.kuchanov.scpcore.mvp.base.BaseArticlesListMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface ObjectsFrArticles {
    interface View extends BaseArticlesListMvp.View {
    }

    interface Presenter extends BaseArticlesListMvp.Presenter<View> {
    }
}