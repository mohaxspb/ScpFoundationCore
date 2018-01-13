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
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.di.module.PresentersModule;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

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
             MyPreferenceManager myPreferencesManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient,
             ConstantValues constantValues
    ) {
        return new ObjectsFrArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    
    ObjectsJpArticles.Presenter providesObjectsJpArticlesPresenter(
             MyPreferenceManager myPreferencesManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient,
             ConstantValues constantValues
    ) {
        return new ObjectsJpArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    
    ObjectsEsArticles.Presenter providesObjectsEsArticlesPresenter(
             MyPreferenceManager myPreferencesManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient,
             ConstantValues constantValues
    ) {
        return new ObjectsEsArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    
    ObjectsPlArticles.Presenter providesObjectsPlArticlesPresenter(
             MyPreferenceManager myPreferencesManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient,
             ConstantValues constantValues
    ) {
        return new ObjectsPlArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    
    ObjectsDeArticles.Presenter providesObjectsDeArticlesPresenter(
             MyPreferenceManager myPreferencesManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient,
             ConstantValues constantValues
    ) {
        return new ObjectsDeArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }
}