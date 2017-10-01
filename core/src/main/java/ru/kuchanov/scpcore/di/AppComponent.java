package ru.kuchanov.scpcore.di;

import javax.inject.Singleton;

import dagger.Component;
import ru.kuchanov.scpcore.di.module.AppModule;
import ru.kuchanov.scpcore.di.module.HelpersModule;
import ru.kuchanov.scpcore.di.module.NetModule;
import ru.kuchanov.scpcore.di.module.NotificationModule;
import ru.kuchanov.scpcore.di.module.PresentersModule;
import ru.kuchanov.scpcore.di.module.StorageModule;
import ru.kuchanov.scpcore.monetization.util.MyAdListener;
import ru.kuchanov.scpcore.monetization.util.MyAppodealInterstitialCallbacks;
import ru.kuchanov.scpcore.receivers.AppInstallReceiver;
import ru.kuchanov.scpcore.receivers.ReceiverBoot;
import ru.kuchanov.scpcore.receivers.ReceiverTimer;
import ru.kuchanov.scpcore.service.DownloadAllServiceDefault;
import ru.kuchanov.scpcore.service.MyFirebaseMessagingService;
import ru.kuchanov.scpcore.ui.activity.ArticleActivity;
import ru.kuchanov.scpcore.ui.activity.GalleryActivity;
import ru.kuchanov.scpcore.ui.activity.LicenceActivity;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.activity.SplashActivity;
import ru.kuchanov.scpcore.ui.activity.TagSearchActivity;
import ru.kuchanov.scpcore.ui.adapter.ArticleAdapter;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter;
import ru.kuchanov.scpcore.ui.adapter.SettingsSpinnerAdapter;
import ru.kuchanov.scpcore.ui.adapter.SettingsSpinnerCardDesignAdapter;
import ru.kuchanov.scpcore.ui.adapter.SubscriptionsAdapter;
import ru.kuchanov.scpcore.ui.dialog.CC3LicenseDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.FreeAdsDisablingDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.NewVersionDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.SettingsBottomSheetDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.SubscriptionsFragmentDialog;
import ru.kuchanov.scpcore.ui.dialog.TextSizeDialogFragment;
import ru.kuchanov.scpcore.ui.fragment.ArticleFragment;
import ru.kuchanov.scpcore.ui.fragment.FavoriteArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsArchiveFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsExperimentsFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsIncidentsFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsInterviewsFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsJokesFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsOtherFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects1ArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects2ArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects3ArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects4ArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.ObjectsRuArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.OfflineArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.RatedArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.RecentArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.SiteSearchArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.TagsSearchFragment;
import ru.kuchanov.scpcore.ui.fragment.TagsSearchResultsArticlesFragment;
import ru.kuchanov.scpcore.ui.holder.ArticleImageHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleSpoilerHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTableHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTabsHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTagsHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTextHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTitleHolder;
import ru.kuchanov.scpcore.ui.holder.HolderMin;
import ru.kuchanov.scpcore.ui.holder.NativeAdsArticleListHolder;

@Singleton
@Component(modules = {
        AppModule.class,
        StorageModule.class,
        PresentersModule.class,
        NetModule.class,
        NotificationModule.class,
        HelpersModule.class
})
public interface AppComponent {

    void inject(LicenceActivity activity);

    void inject(ArticleActivity activity);

    void inject(MaterialsActivity activity);

    void inject(MainActivity activity);

    void inject(GalleryActivity activity);

    void inject(TagSearchActivity activity);

    void inject(SplashActivity activity);

    void inject(ArticleFragment fragment);

    void inject(RecentArticlesFragment fragment);

    void inject(RatedArticlesFragment fragment);

    void inject(FavoriteArticlesFragment fragment);

    void inject(OfflineArticlesFragment fragment);

    void inject(Objects1ArticlesFragment fragment);

    void inject(Objects2ArticlesFragment fragment);

    void inject(Objects3ArticlesFragment fragment);

    void inject(ObjectsRuArticlesFragment fragment);

    void inject(SiteSearchArticlesFragment fragment);

    void inject(MaterialsExperimentsFragment fragment);

    void inject(MaterialsInterviewsFragment fragment);

    void inject(MaterialsIncidentsFragment fragment);

    void inject(MaterialsOtherFragment fragment);

    void inject(MaterialsArchiveFragment fragment);

    void inject(MaterialsJokesFragment fragment);

    void inject(Objects4ArticlesFragment fragment);

    void inject(TagsSearchFragment fragment);

    void inject(TagsSearchResultsArticlesFragment fragment);

    void inject(TextSizeDialogFragment dialogFragment);

    void inject(NewVersionDialogFragment dialogFragment);

    void inject(SubscriptionsFragmentDialog dialogFragment);

    void inject(FreeAdsDisablingDialogFragment dialogFragment);

    void inject(SettingsBottomSheetDialogFragment dialogFragment);

    void inject(CC3LicenseDialogFragment dialogFragment);

    void inject(ArticlesListAdapter adapter);

    void inject(ArticleAdapter adapter);

    void inject(SubscriptionsAdapter adapter);

    void inject(SettingsSpinnerAdapter adapter);

    void inject(SettingsSpinnerCardDesignAdapter adapter);

    void inject(ArticleImageHolder holder);

    void inject(ArticleTagsHolder holder);

    void inject(ArticleTitleHolder holder);

    void inject(ArticleTextHolder holder);

    void inject(ArticleSpoilerHolder holder);

    void inject(HolderMin holder);

    void inject(ArticleTableHolder holder);

    void inject(NativeAdsArticleListHolder holder);

    void inject(ArticleTabsHolder holder);

    void inject(ReceiverTimer receiver);

    void inject(ReceiverBoot receiver);

    void inject(AppInstallReceiver receiver);

    void inject(MyAdListener adListener);

    void inject(MyAppodealInterstitialCallbacks callbacks);

    void inject(DownloadAllServiceDefault service);

    void inject(MyFirebaseMessagingService myFirebaseMessagingService);
}