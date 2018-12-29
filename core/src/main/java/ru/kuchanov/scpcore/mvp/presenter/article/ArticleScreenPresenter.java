package ru.kuchanov.scpcore.mvp.presenter.article;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.article.ArticleScreenMvp;
import ru.kuchanov.scpcore.mvp.presenter.BaseDrawerPresenter;

public class ArticleScreenPresenter
        extends BaseDrawerPresenter<ArticleScreenMvp.View>
        implements ArticleScreenMvp.Presenter {

    public ArticleScreenPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    protected boolean getUserInConstructor() {
        return false;
    }
}