package ru.kuchanov.scpcore.di.module;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyNotificationManager;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.InAppHelper;
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
import ru.kuchanov.scpcore.mvp.contract.monetization.FreeAdsDisableActionsContract;
import ru.kuchanov.scpcore.mvp.contract.monetization.LeaderboardContract;
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract;
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
import ru.kuchanov.scpcore.mvp.presenter.monetization.FreeAdsDisableActionsPresenter;
import ru.kuchanov.scpcore.mvp.presenter.monetization.LeaderboardPresenter;
import ru.kuchanov.scpcore.mvp.presenter.monetization.SubscriptionsPresenter;
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
    MainMvp.Presenter providesMainPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new MainPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    ArticleScreenMvp.Presenter providesArticleScreenPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new ArticleScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    MaterialsScreenMvp.Presenter providesMaterialsScreenPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new MaterialsScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    TagsScreenMvp.Presenter providesTagsSearchScreenPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new TagsSearchScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    GalleryScreenMvp.Presenter providesGalleryScreenPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new GalleryScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
//    @Singleton
    ArticleMvp.Presenter providesArticlePresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new ArticlePresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    RecentArticlesMvp.Presenter providesRecentArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new MostRecentArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    RatedArticlesMvp.Presenter providesRatedArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new MostRatedArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    SiteSearchArticlesMvp.Presenter providesSiteSearchArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new SiteSearchArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    FavoriteArticlesMvp.Presenter providesFavoriteArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new FavoriteArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    OfflineArticlesMvp.Presenter providesOfflineArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new OfflineArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    ReadArticlesMvp.Presenter providesReadArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new ReadArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    Objects1Articles.Presenter providesObjects1ArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new Objects1ArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    Objects2Articles.Presenter providesObjects2ArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new Objects2ArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    Objects3Articles.Presenter providesObjects3ArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new Objects3ArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    Objects4Articles.Presenter providesObjects4ArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new Objects4ArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    ObjectsRuArticles.Presenter providesObjectsRuArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new ObjectsRuArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    MaterialsExperimentsMvp.Presenter providesMaterialsExperimentsPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new MaterialsExperimentsPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    MaterialsInterviewsMvp.Presenter providesMaterialsInterviewsPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new MaterialsInterviewPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    MaterialsIncidentsMvp.Presenter providesMaterialsIncidentsPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new MaterialsIncidentsPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    MaterialsOtherMvp.Presenter providesMaterialsOtherPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new MaterialsOtherPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    MaterialsArchiveMvp.Presenter providesMaterialsArchivePresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new MaterialsArchivePresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    MaterialsJokesMvp.Presenter providesMaterialsJokesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues
    ) {
        return new MaterialsJokesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }

    @Provides
    @Singleton
    SubscriptionsScreenContract.Presenter providesSubscriptionsScreenPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new SubscriptionsScreenPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    SubscriptionsContract.Presenter providesSubscriptionsPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            InAppHelper inAppHelper
    ) {
        return new SubscriptionsPresenter(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Provides
    @Singleton
    FreeAdsDisableActionsContract.Presenter providesFreeAdsDisableActionsPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            Gson gson,
            MyNotificationManager mMyNotificationManager
    ) {
        return new FreeAdsDisableActionsPresenter(myPreferencesManager, dbProviderFactory, apiClient, gson, mMyNotificationManager);
    }

    @Provides
    @Singleton
    LeaderboardContract.Presenter providesLeaderboardPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            InAppHelper inAppHelper
    ) {
        return new LeaderboardPresenter(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Provides
    @Singleton
    TagsSearchMvp.Presenter providesTagsSearchFragmentPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new TagsSearchFragmentPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Provides
//    @Singleton
    TagsSearchResultsArticlesMvp.Presenter providesTagsSearchResultsPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        return new TagSearchResultsArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient);
    }
}