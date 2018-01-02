package ru.kuchanov.scpcore.ui.activity;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Locale;

import ru.kuchanov.rate.PreRate;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.model.remoteconfig.AppLangVersionsJson;
import ru.kuchanov.scpcore.mvp.contract.MainMvp;
import ru.kuchanov.scpcore.ui.base.BaseDrawerActivity;
import ru.kuchanov.scpcore.ui.dialog.CC3LicenseDialogFragment;
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
import ru.kuchanov.scpcore.ui.fragment.ReadArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.RecentArticlesFragment;
import ru.kuchanov.scpcore.ui.fragment.SiteSearchArticlesFragment;
import ru.kuchanov.scpcore.util.IntentUtils;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.APP_LANG_VERSIONS;
import static ru.kuchanov.scpcore.ui.activity.LicenceActivity.EXTRA_SHOW_ABOUT;

public class MainActivity
        extends BaseDrawerActivity<MainMvp.View, MainMvp.Presenter>
        implements MainMvp.View {

    public static final String EXTRA_LINK = "EXTRA_LINK";
    public static final String EXTRA_SHOW_DISABLE_ADS = "EXTRA_SHOW_DISABLE_ADS";

    public static void startActivity(Context context, String link) {
        Timber.d("startActivity: %s", link);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_LINK, link);
        context.startActivity(intent);
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
            for (String pressedLink : mConstantValues.getAllLinksArray()) {
                if (pressedLink.equals(link)) {
                    setDrawerItemFromLink(link);
                    return;
                }
            }
            startArticleActivity(link);
        }
    }

    private void setDrawerItemFromLink(String link) {
        Timber.d("setDrawerItemFromLink: %s", link);
        if (link.equals(mConstantValues.getAbout())) {
            mCurrentSelectedDrawerItemId = (R.id.about);
        } else if (link.equals(mConstantValues.getNews())) {
            mCurrentSelectedDrawerItemId = (R.id.news);
        } else if (link.equals(mConstantValues.getBaseApiUrl())
                || link.equals(mConstantValues.getBaseApiUrl() + "/")
                || link.equals(mConstantValues.getMostRated())) {
            mCurrentSelectedDrawerItemId = R.id.mostRatedArticles;
        } else if (link.equals(mConstantValues.getNewArticles())) {
            mCurrentSelectedDrawerItemId = R.id.mostRecentArticles;
        } else if (link.equals(mConstantValues.getObjects1())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_I);
        } else if (link.equals(mConstantValues.getObjects2())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_II);
        } else if (link.equals(mConstantValues.getObjects3())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_III);
        } else if (link.equals(mConstantValues.getObjects4())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_IV);
        } else if (link.equals(mConstantValues.getObjectsRu())) {
            mCurrentSelectedDrawerItemId = (R.id.objects_RU);
        } else if (link.equals(mConstantValues.getStories())) {
            mCurrentSelectedDrawerItemId = (R.id.stories);
        } else if (link.equals(Constants.Urls.FAVORITES)) {
            mCurrentSelectedDrawerItemId = (R.id.favorite);
        } else if (link.equals(Constants.Urls.OFFLINE)) {
            mCurrentSelectedDrawerItemId = (R.id.offline);
        } else if (link.equals(Constants.Urls.READ)) {
            mCurrentSelectedDrawerItemId = (R.id.read);
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

        if (!mMyPreferenceManager.isPersonalDataAccepted()) {
            DialogFragment dialogFragment = CC3LicenseDialogFragment.newInstance();
            dialogFragment.show(getFragmentManager(), CC3LicenseDialogFragment.TAG);
        } else {
            Timber.d("mMyPreferenceManager.getCurAppVersion(): %s", mMyPreferenceManager.getCurAppVersion());
            Timber.d("SystemUtils.getPackageInfo().versionCode: %s", SystemUtils.getPackageInfo().versionCode);
            if (mMyPreferenceManager.getCurAppVersion() != SystemUtils.getPackageInfo().versionCode) {
                DialogFragment dialogFragment = NewVersionDialogFragment.newInstance(getString(R.string.new_version_features));
                dialogFragment.show(getFragmentManager(), NewVersionDialogFragment.TAG);
            }
        }
    }

    public void showAppLangOrVersionFeaturesDialog() {
        //first time check for device locale and offer proper lang app version if it is
        //else - show new version features
        if (mMyPreferenceManager.getCurAppVersion() == 0) {
            String deviceLang = Locale.getDefault().getLanguage();
            String json = FirebaseRemoteConfig.getInstance().getString(APP_LANG_VERSIONS);
            if (TextUtils.isEmpty(json)) {
                return;
            }
            try {
                AppLangVersionsJson appLangVersions = new GsonBuilder().create().fromJson(json, AppLangVersionsJson.class);
                for (AppLangVersionsJson.AppLangVersion version : appLangVersions.langs) {
                    String appToOfferLang = new Locale(version.code).getLanguage();
                    if (deviceLang.equals(appToOfferLang)) {
                        if (mConstantValues.getAppLang().equals(appToOfferLang)) {
                            //proper lang version already installed, do nothing
                            Timber.d("It is the iterated version, do nothing");
                        } else {
                            //check if app is not installed yet
                            if (IntentUtils.isPackageInstalled(this, version.appPackage)) {
                                Timber.d("correct lang version already installed");
                            } else {
                                //offer app install
                                mDialogUtils.showAppLangVariantsDialog(this, version);
                                break;
                            }
                        }
                    }
                }
                //set app version to show release notes on next update
                mMyPreferenceManager.setCurAppVersion(SystemUtils.getPackageInfo().versionCode);
            } catch (Exception e) {
                Timber.e(e);
            }

        } else if (mMyPreferenceManager.getCurAppVersion() != SystemUtils.getPackageInfo().versionCode) {
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
        if (id == R.id.invite) {
            IntentUtils.firebaseInvite(this);
            return false;
        } else if (id == R.id.about) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ArticleFragment.newInstance(mConstantValues.getAbout()),
                    ArticleFragment.TAG + "#" + mConstantValues.getAbout());
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
        } else if (id == R.id.read) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ReadArticlesFragment.newInstance(), ReadArticlesFragment.TAG);
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
        } else if (id == R.id.objects_RU) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ObjectsRuArticlesFragment.newInstance(), ObjectsRuArticlesFragment.TAG);
            return true;
        } else if (id == R.id.news) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ArticleFragment.newInstance(mConstantValues.getNews()),
                    ArticleFragment.TAG + "#" + mConstantValues.getNews());
            return true;
        } else if (id == R.id.objects_IV) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(Objects4ArticlesFragment.newInstance(), Objects4ArticlesFragment.TAG);
            return true;
        } else if (id == R.id.stories) {
            mCurrentSelectedDrawerItemId = id;
            showFragment(ArticleFragment.newInstance(mConstantValues.getStories()),
                    ArticleFragment.TAG + "#" + mConstantValues.getStories());
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
        //maybe we can move to separate interface and inject correct impl for lang each flavor
        //but it works now without any problems... So..
        String title;
        if (id == R.id.about) {
            title = getString(R.string.drawer_item_1);
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
        } else if (id == R.id.favorite) {
            title = getString(R.string.drawer_item_12);
        } else if (id == R.id.offline) {
            title = getString(R.string.drawer_item_13);
        } else if (id == R.id.siteSearch) {
            title = getString(R.string.drawer_item_15);
        } else if (id == R.id.objects_RU) {
            title = getString(R.string.drawer_item_9);
        } else if (id == R.id.news) {
            title = getString(R.string.drawer_item_2);
        } else if (id == R.id.objects_IV) {
            title = getString(R.string.drawer_item_objects4);
        } else if (id == R.id.stories) {
            title = getString(R.string.drawer_item_11);
        } else {
            Timber.e("unexpected item ID");
            title = null;
        }
        if (title != null && mToolbar != null) {
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

    @Override
    public boolean isBannerEnabled() {
//        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
//        return !config.getBoolean(MAIN_BANNER_DISABLED) && !config.getBoolean(NATIVE_ADS_LISTS_ENABLED);
        return mMyPreferenceManager.isBannerInArticlesListsEnabled();
    }
}