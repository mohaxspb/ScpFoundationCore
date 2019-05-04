package ru.dante.scpfoundation.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.dante.scpfoundation.mvp.contract.ObjectsDeArticles;
import ru.dante.scpfoundation.mvp.contract.ObjectsEsArticles;
import ru.dante.scpfoundation.mvp.contract.ObjectsFrArticles;
import ru.dante.scpfoundation.mvp.contract.ObjectsJpArticles;
import ru.dante.scpfoundation.mvp.contract.ObjectsPlArticles;
import ru.dante.scpfoundation.mvp.presenter.ObjectsDeArticlesPresenter;
import ru.dante.scpfoundation.mvp.presenter.ObjectsEsArticlesPresenter;
import ru.dante.scpfoundation.mvp.presenter.ObjectsFrArticlesPresenter;
import ru.dante.scpfoundation.mvp.presenter.ObjectsJpArticlesPresenter;
import ru.dante.scpfoundation.mvp.presenter.ObjectsPlArticlesPresenter;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.di.module.PresentersModule;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;

/**
 * Created by mohax on 13.07.2017.
 * <p>
 * for ScpFoundationRu
 */
@Module(includes = PresentersModule.class)
public class PresentersModuleImpl extends PresentersModule {

    @Provides
    @Singleton
    ObjectsFrArticles.Presenter providesObjectsFrArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues,
            final InAppHelper inAppHelper
    ) {
        return new ObjectsFrArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues, inAppHelper);
    }

    @Provides
    @Singleton
    ObjectsJpArticles.Presenter providesObjectsJpArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues,
            final InAppHelper inAppHelper
    ) {
        return new ObjectsJpArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues, inAppHelper);
    }

    @Provides
    @Singleton
    ObjectsEsArticles.Presenter providesObjectsEsArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues,
            final InAppHelper inAppHelper
    ) {
        return new ObjectsEsArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues, inAppHelper);
    }

    @Provides
    @Singleton
    ObjectsPlArticles.Presenter providesObjectsPlArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues,
            final InAppHelper inAppHelper
    ) {
        return new ObjectsPlArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues, inAppHelper);
    }

    @Provides
    @Singleton
    ObjectsDeArticles.Presenter providesObjectsDeArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues,
            final InAppHelper inAppHelper
    ) {
        return new ObjectsDeArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues, inAppHelper);
    }
}