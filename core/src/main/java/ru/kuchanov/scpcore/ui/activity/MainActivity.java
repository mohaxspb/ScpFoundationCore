package ru.kuchanov.scpcore.ui.activity;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;

import butterknife.BindView;
import ru.kuchanov.rate.PreRate;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.mvp.contract.MainMvp;
import ru.kuchanov.scpcore.ui.base.BaseDrawerActivity;
import ru.kuchanov.scpcore.ui.dialog.NewVersionDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.TextSizeDialogFragment;
import ru.kuchanov.scpcore.ui.fragment.ArticleFragment;
import ru.kuchanov.scpcore.ui.fragment.FavoriteArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects1ArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects2ArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects3ArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects4ArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.ObjectsRuArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.OfflineArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.RatedArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.RecentArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.SiteSearchArticlesFragment;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.MAIN_BANNER_DISABLED;
import static ru.kuchanov.scpcore.ui.activity.LicenceActivity.EXTRA_SHOW_ABOUT;

public class MainActivity
        extends BaseDrawerActivity<MainMvp.View, MainMvp.Presenter>
        implements MainMvp.View {

    public static final String EXTRA_LINK = "EXTRA_LINK";
    public static final String EXTRA_SHOW_DISABLE_ADS = "EXTRA_SHOW_DISABLE_ADS";

    @BindView(R2.id.banner)
    AdView mAdView;

    public static void startActivity(Context context, String link) {
        Timber.d("startActivity: %s", link);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_LINK, link);
        context.startActivity(intent);
    }

    @Override
    public void initAds() {
        super.initAds();

        if (!isAdsLoaded()) {
            requestNewInterstitial();
        }

        AdRequest.Builder adRequest = new AdRequest.Builder();

        if (BuildConfig.DEBUG) {
            @SuppressLint("HardwareIds")
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId;
            deviceId = SystemUtils.MD5(androidId);
            if (deviceId != null) {
                deviceId = deviceId.toUpperCase();
                adRequest.addTestDevice(deviceId);
            }
            adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        }
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        if (mMyPreferenceManager.isHasSubscription()
                || mMyPreferenceManager.isHasNoAdsSubscription()
                || remoteConfig.getBoolean(MAIN_BANNER_DISABLED)) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadAd(adRequest.build());
        }
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Timber.d("onNewIntent");
        super.onNewIntent(intent);

        setIntent(intent);

        setDrawerFromIntent(intent);

        mNavigationView.setCheckedItem(mCurrentSelectedDrawerItemId);
        onNavigationItemClicked(mCurrentSelectedDrawerItemId);
        setToolbarTitleByDrawerItemId(mCurrentSelectedDrawerItemId);
    }

    private void setDrawerFromIntent(Intent intent) {
        if (intent.hasExtra(EXTRA_LINK)) {
            setDrawerItemFromLink(intent.getStringExtra(EXTRA_LINK));
        } else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            String link = intent.getDataString();
            for (String pressedLink : mConstantValues.getUrlsValues().getAllLinksArray()) {
                if (link.equals(pressedLink)) {
                    setDrawerItemFromLink(link);
                    return;
                }
            }
            startArticleActivity(link);
        }
    }

    private void setDrawerItemFromLink(String link) {
        Timber.d("setDrawerItemFromLink: %s", link);
        if (link.equals(mConstantValues.getUrlsValues().getAbout())) {
            mCurrentSelectedDrawerItemId = (R.id.about);
        } else if (link.equals(mConstantValues.getUrlsValues().getNews())) {
            mCurrentSelectedDrawerItemId = (R.id.news);
        } else if (link.equals(mConstantValues.getUrlsValues().getMain())
                || link.equals(mConstantValues.getUrlsValues().getMostRated())) {
            mCurrentSelectedDrawerItemId = R.id.mostRatedArticles;
        } else if (link.equals(mConstantValues.getUrlsValues().getNewArticles())) {
            mCurrentSelectedDrawerItemId = R.id.mostRecentArticles;
        } else if (link.equals(mConstantValues.getUrlsValues().getObjects1())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_I);
        } else if (link.equals(mConstantValues.getUrlsValues().getObjects2())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_II);
        } else if (link.equals(mConstantValues.getUrlsValues().getObjects3())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_III);
        } else if (link.equals(mConstantValues.getUrlsValues().getObjects4())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_IV);
        } else if (link.equals(mConstantValues.getUrlsValues().getObjectsRu())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_RU);
        } else if (link.equals(mConstantValues.getUrlsValues().getStories())) {
            mCurrentSelectedDrawerItemId = (R.id.stories);
        } else if (link.equals(Constants.Urls.FAVORITES)) {
            mCurrentSelectedDrawerItemId = (R.id.favorite);
        } else if (link.equals(Constants.Urls.OFFLINE)) {
            mCurrentSelectedDrawerItemId = (R.id.offline);
        } else if (link.equals(Constants.Urls.SEARCH)) {
            mCurrentSelectedDrawerItemId = (R.id.siteSearch);
        } else {
            mCurrentSelectedDrawerItemId = SELECTED_DRAWER_ITEM_NONE;
        }
        getIntent().removeExtra(EXTRA_LINK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if ("ru.kuchanov.scpcore.open.NEW".equals(intent.getAction())) {
            mCurrentSelectedDrawerItemId = (R.id.mostRecentArticles);
        } else if ("ru.kuchanov.scpcore.open.FAVORITES".equals(intent.getAction())) {
            mCurrentSelectedDrawerItemId = (R.id.favorite);
        } else if ("ru.kuchanov.scpcore.open.RANDOM".equals(intent.getAction())) {
            mCurrentSelectedDrawerItemId = (R.id.random_page);
        }

        setDrawerFromIntent(intent);

        if (getSupportFragmentManager().findFragmentById(mContent.getId()) == null) {
            onNavigationItemClicked(mCurrentSelectedDrawerItemId);
        }
        mNavigationView.setCheckedItem(mCurrentSelectedDrawerItemId);
        setToolbarTitleByDrawerItemId(mCurrentSelectedDrawerItemId);

        if (mMyPreferenceManager.getCurAppVersion() != BuildConfig.VERSION_CODE) {
            DialogFragment dialogFragment = NewVersionDialogFragment.newInstance(getString(R.string.new_version_features));
            dialogFragment.show(getFragmentManager(), NewVersionDialogFragment.TAG);
        }
    }

    @Override
    protected int getDefaultNavItemId() {
        return getIntent().hasExtra(EXTRA_SHOW_ABOUT) ? R.id.about : R.id.mostRatedArticles;
    }

    @Override
    protected boolean isDrawerIndicatorEnabled() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_main;
    }

    @Override
    public boolean onNavigationItemClicked(int id) {
        Timber.d("onNavigationItemClicked with id: %s", id);
        setToolbarTitleByDrawerItemId(id);
        if (id == R.id.about) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ArticleFragment.newInstance(mConstantValues.getUrlsValues().getAbout()),
                    ArticleFragment.TAG + "#" + mConstantValues.getUrlsValues().getAbout());
            return true;
        } else if (id == R.id.news) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ArticleFragment.newInstance(mConstantValues.getUrlsValues().getNews()),
                    ArticleFragment.TAG + "#" + mConstantValues.getUrlsValues().getNews());
            return true;
        } else if (id == R.id.mostRatedArticles) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(RatedArticlesFragment.newInstance(), RatedArticlesFragment.TAG);
            return true;
        } else if (id == R.id.mostRecentArticles) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(RecentArticlesFragment.newInstance(), RecentArticlesFragment.TAG);
            return true;
        } else if (id == R.id.random_page) {
            mPresenter.getRandomArticleUrl();
            return false;
        } else if (id == R.id.objects_I) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(Objects1ArticlesFragment.newInstance(), Objects1ArticlesFragment.TAG);
            return true;
        } else if (id == R.id.objects_II) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(Objects2ArticlesFragment.newInstance(), Objects2ArticlesFragment.TAG);
            return true;
        } else if (id == R.id.objects_III) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(Objects3ArticlesFragment.newInstance(), Objects3ArticlesFragment.TAG);
            return true;
        } else if (id == R.id.objects_IV) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(Objects4ArticlesFragment.newInstance(), Objects4ArticlesFragment.TAG);
            return true;
        } else if (id == R.id.files) {
            startMaterialsActivity();
            return false;
        } else if (id == R.id.favorite) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(FavoriteArticlesFragment.newInstance(), FavoriteArticlesFragment.TAG);
            return true;
        } else if (id == R.id.offline) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(OfflineArticlesFragment.newInstance(), OfflineArticlesFragment.TAG);
            return true;
        } else if (id == R.id.gallery) {
            startGalleryActivity();
            return false;
        } else if (id == R.id.siteSearch) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(SiteSearchArticlesFragment.newInstance(), SiteSearchArticlesFragment.TAG);
            return true;
        } else if (id == R.id.tagsSearch) {
            startTagsSearchActivity();
            return true;
        } else if (id == R2.id.objects_RU) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ObjectsRuArticlesFragment.newInstance(), ObjectsRuArticlesFragment.TAG);
            return true;
        } else if (id == R2.id.stories) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ArticleFragment.newInstance(mConstantValues.getUrlsValues().getStories()),
                    ArticleFragment.TAG + "#" + mConstantValues.getUrlsValues().getStories());
            return true;
        } else {
            Timber.e("unexpected item ID");
            return true;
        }
    }

    private void showFragment(Fragment fragmentToShow, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            transaction.add(mContent.getId(), fragmentToShow, tag).commit();
        } else {
            transaction.show(fragment).commit();
        }
    }

    /**
     * adds all found fragments to transaction via hide method
     */
    private void hideFragments(FragmentTransaction transaction) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null || fragments.isEmpty()) {
            return;
        }
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isAdded()) {
                transaction.hide(fragment);
            } else {
                Timber.e("fragment != null && fragment.isAdded() FALSE while switch fragments");
//                showError(new IllegalStateException("fragment != null && fragment.isAdded() FALSE while switch fragments"));
            }
        }
    }

    @Override
    public void setToolbarTitleByDrawerItemId(int id) {
        Timber.d("setToolbarTitleByDrawerItemId with id: %s", id);
        //TODO move to separate interface and inject correct impl for lang each flavor
        //or check so https://stackoverflow.com/a/40085939/3212712
        String title;
        if (id == R.id.about) {
            title = getString(R.string.drawer_item_1);
        } else if (id == R.id.news) {
            title = getString(R.string.drawer_item_2);
        } else if (id == R.id.mostRatedArticles) {
            title = getString(R.string.drawer_item_3);
        } else if (id == R.id.mostRecentArticles) {
            title = getString(R.string.drawer_item_4);
        } else if (id == R.id.objects_I) {
            title = getString(R.string.drawer_item_6);
        } else if (id == R.id.objects_II) {
            title = getString(R.string.drawer_item_7);
        } else if (id == R.id.objects_III) {
            title = getString(R.string.drawer_item_8);
        } else if (id == R.id.objects_IV) {
            title = getString(R.string.drawer_item_objects4);
        } else if (id == R.id.favorite) {
            title = getString(R.string.drawer_item_12);
        } else if (id == R.id.offline) {
            title = getString(R.string.drawer_item_13);
        } else if (id == R.id.siteSearch) {
            title = getString(R.string.drawer_item_15);
        } else if (BuildConfig.FLAVOR.contains("ru") && id == R2.id.objects_RU) {
            title = getString(R.string.drawer_item_9);
        } else if (BuildConfig.FLAVOR.contains("ru") && id == R2.id.stories) {
            title = getString(R.string.drawer_item_11);
        } else {
            Timber.e("unexpected item ID");
            title = null;
        }
        assert mToolbar != null;
        if (title != null) {
            mToolbar.setTitle(title);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreRate.init(this, getString(R.string.feedback_email), getString(R.string.feedback_title)).showIfNeed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreRate.clearDialogIfOpen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.text_size) {
            BottomSheetDialogFragment fragmentDialogTextAppearance =
                    TextSizeDialogFragment.newInstance(TextSizeDialogFragment.TextSizeType.UI);
            fragmentDialogTextAppearance.show(getSupportFragmentManager(), TextSizeDialogFragment.TAG);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}