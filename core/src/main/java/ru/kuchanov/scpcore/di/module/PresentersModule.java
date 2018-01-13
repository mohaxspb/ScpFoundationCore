package ru.kuchanov.scpcore.di.module;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.GalleryScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.MainMvp;
import ru.kuchanov.scpcore.mvp.contract.article.ArticleMvp;
import ru.kuchanov.scpcore.mvp.contract.article.ArticleScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.articleslists.FavoriteArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects1Articles;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects2Articles;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects3Articles;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects4Articles;
import ru.kuchanov.scpcore.mvp.contract.articleslists.ObjectsRuArticles;
import ru.kuchanov.scpcore.mvp.contract.articleslists.OfflineArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.articleslists.RatedArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.articleslists.ReadArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.articleslists.RecentArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsArchiveMvp;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsExperimentsMvp;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsIncidentsMvp;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsInterviewsMvp;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsJokesMvp;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsOtherMvp;
import ru.kuchanov.scpcore.mvp.contract.materials.MaterialsScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsScreenContract;
import ru.kuchanov.scpcore.mvp.contract.search.SiteSearchArticlesMvp;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsSearchMvp;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsSearchResultsArticlesMvp;
import ru.kuchanov.scpcore.mvp.presenter.MainPresenter;
import ru.kuchanov.scpcore.mvp.presenter.article.ArticlePresenter;
import ru.kuchanov.scpcore.mvp.presenter.article.ArticleScreenPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.FavoriteArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.MostRatedArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.MostRecentArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.Objects1ArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.Objects2ArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.Objects3ArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.Objects4ArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.ObjectsRuArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.OfflineArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.ReadArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.gallery.GalleryScreenPresenter;
import ru.kuchanov.scpcore.mvp.presenter.materials.MaterialsArchivePresenter;
import ru.kuchanov.scpcore.mvp.presenter.materials.MaterialsExperimentsPresenter;
import ru.kuchanov.scpcore.mvp.presenter.materials.MaterialsIncidentsPresenter;
import ru.kuchanov.scpcore.mvp.presenter.materials.MaterialsInterviewPresenter;
import ru.kuchanov.scpcore.mvp.presenter.materials.MaterialsJokesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.materials.MaterialsOtherPresenter;
import ru.kuchanov.scpcore.mvp.presenter.materials.MaterialsScreenPresenter;
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsScreenPresenter;
import ru.kuchanov.scpcore.mvp.presenter.search.SiteSearchArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.tags.TagSearchResultsArticlesPresenter;
import ru.kuchanov.scpcore.mvp.presenter.tags.TagsSearchFragmentPresenter;
import ru.kuchanov.scpcore.mvp.presenter.tags.TagsSearchScreenPresenter;

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
    OfflineArticlesMvp.Presenter providesOfflineArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new OfflineArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    @NonNull
    ReadArticlesMvp.Presenter providesReadArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new ReadArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
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
    SubscriptionsScreenContract.Presenter providesSubscriptionsScreenPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new SubscriptionsScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
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
//    @Singleton
    @NonNull
    TagsSearchResultsArticlesMvp.Presenter providesTagsSearchResultsPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new TagSearchResultsArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient) ;
    }
}