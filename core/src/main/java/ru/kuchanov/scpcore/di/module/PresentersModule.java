package ru.kuchanov.scpcore.di.module;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.ArticleMvp;
import ru.kuchanov.scpcore.mvp.contract.ArticleScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.FavoriteArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.GalleryScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.MainMvp;
import ru.kuchanov.scpcore.mvp.contract.MaterialsArchiveMvp;
import ru.kuchanov.scpcore.mvp.contract.MaterialsExperimentsMvp;
import ru.kuchanov.scpcore.mvp.contract.MaterialsIncidentsMvp;
import ru.kuchanov.scpcore.mvp.contract.MaterialsInterviewsMvp;
import ru.kuchanov.scpcore.mvp.contract.MaterialsJokesMvp;
import ru.kuchanov.scpcore.mvp.contract.MaterialsOtherMvp;
import ru.kuchanov.scpcore.mvp.contract.MaterialsScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.Objects1Articles;
import ru.kuchanov.scpcore.mvp.contract.Objects2Articles;
import ru.kuchanov.scpcore.mvp.contract.Objects3Articles;
import ru.kuchanov.scpcore.mvp.contract.Objects4Articles;
import ru.kuchanov.scpcore.mvp.contract.ObjectsRuArticles;
import ru.kuchanov.scpcore.mvp.contract.OfflineArticles;
import ru.kuchanov.scpcore.mvp.contract.RatedArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.RecentArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.SiteSearchArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.TagsScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.TagsSearchMvp;
import ru.kuchanov.scpcore.mvp.contract.TagsSearchResultsArticlesMvp;
import ru.kuchanov.scpcore.mvp.presenter.ArticlePresenter;
import ru.kuchanov.scpcore.mvp.presenter.ArticleScreenPresenter;
import ru.kuchanov.scpcore.mvp.presenter.FavoriteArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.GalleryScreenPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MainPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MaterialsArchivePresenter;
import ru.kuchanov.scpcore.mvp.presenter.MaterialsExperimentsPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MaterialsIncidentsPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MaterialsInterviewPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MaterialsJokesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MaterialsOtherPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MaterialsScreenPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MostRatedArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.MostRecentArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.Objects1ArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.Objects2ArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.Objects3ArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.Objects4ArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.ObjectsRuArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.OfflineArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.SiteSearchArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.TagSearchResultsArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.TagsSearchFragmentPresenter;
import ru.kuchanov.scpcore.mvp.presenter.TagsSearchScreenPresenter;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
@Module
public class PresentersModule {

    @Provides
    @Singleton
    @NonNull
    MainMvp.Presenter providesMainPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new MainPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @NonNull
    ArticleScreenMvp.Presenter providesArticleScreenPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new ArticleScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    MaterialsScreenMvp.Presenter providesMaterialsScreenPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new MaterialsScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    TagsScreenMvp.Presenter providesTagsSearchScreenPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new TagsSearchScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    GalleryScreenMvp.Presenter providesGalleryScreenPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new GalleryScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
//    @Singleton
    @NonNull
    ArticleMvp.Presenter providesArticlePresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new ArticlePresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    RecentArticlesMvp.Presenter providesRecentArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new MostRecentArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    RatedArticlesMvp.Presenter providesRatedArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new MostRatedArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    SiteSearchArticlesMvp.Presenter providesSiteSearchArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new SiteSearchArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    FavoriteArticlesMvp.Presenter providesFavoriteArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new FavoriteArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    OfflineArticles.Presenter providesOfflineArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new OfflineArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    Objects1Articles.Presenter providesObjects1ArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new Objects1ArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    Objects2Articles.Presenter providesObjects2ArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new Objects2ArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    Objects3Articles.Presenter providesObjects3ArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new Objects3ArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    Objects4Articles.Presenter providesObjects4ArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new Objects4ArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    ObjectsRuArticles.Presenter providesObjectsRuArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new ObjectsRuArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    MaterialsExperimentsMvp.Presenter providesMaterialsExperimentsPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new MaterialsExperimentsPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    MaterialsInterviewsMvp.Presenter providesMaterialsInterviewsPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new MaterialsInterviewPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    MaterialsIncidentsMvp.Presenter providesMaterialsIncidentsPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new MaterialsIncidentsPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    MaterialsOtherMvp.Presenter providesMaterialsOtherPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new MaterialsOtherPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    MaterialsArchiveMvp.Presenter providesMaterialsArchivePresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
            ) {
        return new MaterialsArchivePresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    MaterialsJokesMvp.Presenter providesMaterialsJokesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new MaterialsJokesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    @NonNull
    TagsSearchMvp.Presenter providesTagsSearchFragmentPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new TagsSearchFragmentPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    TagsSearchResultsArticlesMvp.Presenter providesTagsSearchResultsPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new TagSearchResultsArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient) ;
    }
}