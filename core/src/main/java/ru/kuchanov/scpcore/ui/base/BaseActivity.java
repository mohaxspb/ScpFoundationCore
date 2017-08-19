package ru.kuchanov.scpcore.ui.base;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.vending.billing.IInAppBillingService;
import com.appodeal.ads.Appodeal;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.yandex.metrica.YandexMetrica;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable;
import ru.kuchanov.scpcore.manager.MyNotificationManager;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.AdMobHelper;
import ru.kuchanov.scpcore.monetization.util.InAppHelper;
import ru.kuchanov.scpcore.monetization.util.MyAdListener;
import ru.kuchanov.scpcore.monetization.util.MyAppodealInterstitialCallbacks;
import ru.kuchanov.scpcore.monetization.util.MyRewardedVideoCallbacks;
import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp;
import ru.kuchanov.scpcore.mvp.base.MonetizationActions;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.ui.activity.ArticleActivity;
import ru.kuchanov.scpcore.ui.activity.GalleryActivity;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.activity.TagSearchActivity;
import ru.kuchanov.scpcore.ui.adapter.SocialLoginAdapter;
import ru.kuchanov.scpcore.ui.dialog.FreeAdsDisablingDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.NewVersionDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.SetttingsBottomSheetDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.SubscriptionsFragmentDialog;
import ru.kuchanov.scpcore.ui.dialog.TextSizeDialogFragment;
import ru.kuchanov.scpcore.ui.holder.SocialLoginHolder;
import ru.kuchanov.scpcore.ui.util.DialogUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ru.kuchanov.scpcore.ui.activity.MainActivity.EXTRA_SHOW_DISABLE_ADS;

/**
 * Created by mohax on 31.12.2016.
 * <p>
 * for scp_ru
 */
public abstract class BaseActivity<V extends BaseActivityMvp.View, P extends BaseActivityMvp.Presenter<V>>
        extends MvpActivity<V, P>
        implements BaseActivityMvp.View,
        SharedPreferences.OnSharedPreferenceChangeListener, GoogleApiClient.OnConnectionFailedListener {

    public static final String EXTRA_ARTICLES_URLS_LIST = "EXTRA_ARTICLES_URLS_LIST";
    public static final String EXTRA_POSITION = "EXTRA_POSITION";
    public static final String EXTRA_TAGS = "EXTRA_TAGS";

    //google login
    private static final int RC_SIGN_IN = 5555;
    protected GoogleApiClient mGoogleApiClient;
    //facebook
    private CallbackManager mCallbackManager = CallbackManager.Factory.create();
    ///////////

    @BindView(R2.id.root)
    protected View mRoot;
    @BindView(R2.id.content)
    protected View mContent;
    @Nullable
    @BindView(R2.id.toolBar)
    protected Toolbar mToolbar;
    @Nullable
    @BindView(R2.id.banner)
    protected AdView mAdView;

    @Inject
    protected P mPresenter;

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;
    @Inject
    protected MyNotificationManager mMyNotificationManager;
    @Inject
    protected ConstantValues mConstantValues;
    @Inject
    protected DialogUtils mDialogUtils;
    @Inject
    protected ru.kuchanov.scp.downloads.DialogUtils<Article> mDownloadAllChooser;
    @Inject
    protected InAppHelper mInAppHelper;
    //inapps and ads
    private IInAppBillingService mService;

    private InterstitialAd mInterstitialAd;
    private MaterialDialog mProgressDialog;

    @NonNull
    @Override
    public P createPresenter() {
        return mPresenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate");
        callInjections();
        if (mMyPreferenceManager.isNightMode()) {
            setTheme(R.style.SCP_Theme_Dark);
        } else {
            setTheme(R.style.SCP_Theme_Light);
        }
        super.onCreate(savedInstanceState);

        //remote config
        initAndUpdateRemoteConfig();

        setContentView(getLayoutResId());
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        //google login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_application_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //facebook login
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d("onSuccess: %s", loginResult);
                mPresenter.startFirebaseLogin(Constants.Firebase.SocialProvider.FACEBOOK, loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Timber.e("onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Timber.e(error);
            }
        });

        mPresenter.onCreate();

        //setAlarm for notification
        mMyNotificationManager.checkAlarm();

        //initAds subs service
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        //ads
        initAds();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void showLoginProvidersPopup() {
        MaterialDialog dialog;
        List<Constants.Firebase.SocialProvider> providers = new ArrayList<>(Arrays.asList(Constants.Firebase.SocialProvider.values()));
        if (!getResources().getBoolean(R.bool.social_login_vk_enabled)) {
            providers.remove(Constants.Firebase.SocialProvider.VK);
        }
        SocialLoginAdapter adapter = new SocialLoginAdapter();
        dialog = new MaterialDialog.Builder(this)
                .title(R.string.dialog_social_login_title)
                .items(providers)
                .adapter(adapter, new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
                .positiveText(android.R.string.cancel)
                .build();
        adapter.setItemClickListener(data -> {
            startLogin(data.getSocialProvider());
            dialog.dismiss();
        });
        adapter.setData(SocialLoginHolder.SocialLoginModel.getModels(providers));
        dialog.getRecyclerView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        dialog.show();
    }

    @Override
    public void startLogin(Constants.Firebase.SocialProvider provider) {
        switch (provider) {
            case VK:
                VKSdk.login(this, VKScope.EMAIL, VKScope.GROUPS);
                break;
            case GOOGLE:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case FACEBOOK:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                break;
            default:
                throw new RuntimeException("unexpected provider");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unsubscribe from firebase;
        mPresenter.onActivityStopped();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //unsubscribe from firebase;
        mPresenter.onActivityStarted();
    }

    @Override
    public void initAds() {
        //init frameworks
        MobileAds.initialize(getApplicationContext(), getString(R.string.ads_app_id));

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id_interstitial));
        mInterstitialAd.setAdListener(new MyAdListener());

        //appodeal
        Appodeal.disableLocationPermissionCheck();
        if (BuildConfig.FLAVOR.equals("dev")) {
            Appodeal.setTesting(true);
//            Appodeal.setLogLevel(Log.LogLevel.debug);
        }
        Appodeal.disableNetwork(this, "facebook");
        Appodeal.initialize(this, getString(R.string.appodeal_app_key), Appodeal.REWARDED_VIDEO | Appodeal.INTERSTITIAL);

        //user settings
//        UserSettings userSettings = Appodeal.getUserSettings(this);
        //we should get this data from each network while login and store it in i.e. prefs to set it here.
        //also we should update it periodically

        Appodeal.muteVideosIfCallsMuted(true);
        Appodeal.setRewardedVideoCallbacks(new MyRewardedVideoCallbacks() {
            @Override
            public void onRewardedVideoFinished(int i, String s) {
                super.onRewardedVideoFinished(i, s);
                mMyPreferenceManager.applyAwardFromAds();
                long numOfMillis = FirebaseRemoteConfig.getInstance()
                        .getLong(Constants.Firebase.RemoteConfigKeys.REWARDED_VIDEO_COOLDOWN_IN_MILLIS);
                long hours = numOfMillis / 1000 / 60 / 60;
                showMessage(getString(R.string.ads_reward_gained, hours));

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.EventType.REWARD_GAINED);
                FirebaseAnalytics.getInstance(BaseActivity.this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                @DataSyncActions.ScoreAction
                String action = DataSyncActions.ScoreAction.REWARDED_VIDEO;
                mPresenter.updateUserScoreForScoreAction(action);
            }
        });
        Appodeal.setInterstitialCallbacks(new MyAppodealInterstitialCallbacks() {
            @Override
            public void onInterstitialClosed() {
                super.onInterstitialClosed();
                @DataSyncActions.ScoreAction
                String action = DataSyncActions.ScoreAction.INTERSTITIAL_SHOWN;
                mPresenter.updateUserScoreForScoreAction(action);
            }
        });

        if (!isAdsLoaded() && mMyPreferenceManager.isTimeToLoadAds()) {
            requestNewInterstitial();
        }

        setUpBanner();
    }

    private void setUpBanner() {
        if (mMyPreferenceManager.isHasSubscription()
                || mMyPreferenceManager.isHasNoAdsSubscription()
                || !isBannerEnabled()
                || !mMyPreferenceManager.isTimeToShowBannerAds()) {
            if (mAdView != null) {
                mAdView.setEnabled(false);
                mAdView.setVisibility(View.GONE);
            }
        } else {
            if (mAdView != null) {
                mAdView.setEnabled(true);
                mAdView.setVisibility(View.VISIBLE);
                if (!mAdView.isLoading()) {
                    mAdView.loadAd(AdMobHelper.buildAdRequest(this));
                }
            }
        }
    }

    @Override
    public void startRewardedVideoFlow() {
        if (mMyPreferenceManager.isRewardedDescriptionShown()) {
            showRewardedVideo();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.ads_reward_description_title)
                    .content(R.string.ads_reward_description_content)
                    .positiveText(R.string.ads_reward_ok)
                    .onPositive((dialog, which) -> {
                        mMyPreferenceManager.setRewardedDescriptionIsNotShown(true);
                        startRewardedVideoFlow();
                    })
                    .show();
        }
    }

    @Override
    public void showRewardedVideo() {
        if (Appodeal.isLoaded(Appodeal.NON_SKIPPABLE_VIDEO)) {
            //analitics
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.EventType.REWARD_REQUESTED);
            FirebaseAnalytics.getInstance(BaseActivity.this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Appodeal.show(this, Appodeal.NON_SKIPPABLE_VIDEO);
        } else {
            showMessage(R.string.reward_not_loaded_yet);
        }
    }

    @Override
    public boolean isTimeToShowAds() {
        boolean hasSubscription = mMyPreferenceManager.isHasNoAdsSubscription() || mMyPreferenceManager.isHasSubscription();
        return !hasSubscription && mMyPreferenceManager.isTimeToShowAds();
    }

    @Override
    public boolean isAdsLoaded() {
        return mInterstitialAd.isLoaded();
    }

    /**
     * ads adsListener with showing SnackBar after ads closing and calles {@link MonetizationActions#showInterstitial(MyAdListener, boolean)}
     */
    @Override
    public void showInterstitial() {
        MyAdListener adListener = new MyAdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                showSnackBarWithAction(Constants.Firebase.CallToActionReason.REMOVE_ADS);

                @DataSyncActions.ScoreAction
                String action = DataSyncActions.ScoreAction.INTERSTITIAL_SHOWN;
                mPresenter.updateUserScoreForScoreAction(action);
            }
        };
        showInterstitial(adListener, true);
    }

    /**
     * checks if it's time to show rewarded instead of simple interstitial
     * and it's ready and shows rewarded video or interstitial
     */
    @Override
    public void showInterstitial(MyAdListener adListener, boolean showVideoIfNeedAndCan) {
        if (mMyPreferenceManager.isTimeToShowVideoInsteadOfInterstitial() && Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
            //TODO we should redirect user to desired activity...
            Appodeal.show(this, Appodeal.INTERSTITIAL);
        } else {
            //add score in activity, that will be shown from close callback of listener
            mInterstitialAd.setAdListener(adListener);
            mInterstitialAd.show();
        }
    }

    @Override
    public void showSnackBarWithAction(Constants.Firebase.CallToActionReason reason) {
        Timber.d("showSnackBarWithAction: %s", reason);
        Snackbar snackbar;
        switch (reason) {
            case REMOVE_ADS:
                snackbar = Snackbar.make(mRoot, R.string.remove_ads, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.yes_bliad, v -> {
                    snackbar.dismiss();
                    BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
                    subsDF.show(getSupportFragmentManager(), subsDF.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.SNACK_BAR);
                    FirebaseAnalytics.getInstance(BaseActivity.this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                });
                break;
            case ENABLE_FONTS:
                snackbar = Snackbar.make(mRoot, R.string.only_premium, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.activate, action -> {
                    BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
                    subsDF.show(getSupportFragmentManager(), subsDF.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.FONT);
                    FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                });
                break;
            case ENABLE_AUTO_SYNC:
                snackbar = Snackbar.make(mRoot, R.string.auto_sync_disabled, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.turn_on, v -> {
                    snackbar.dismiss();
                    BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
                    subsDF.show(getSupportFragmentManager(), subsDF.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.AUTO_SYNC_SNACKBAR);
                    FirebaseAnalytics.getInstance(BaseActivity.this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                });
                break;
            case SYNC_NEED_AUTH:
                snackbar = Snackbar.make(mRoot, R.string.sync_need_auth, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.authorize, v -> {
                    snackbar.dismiss();
                    showLoginProvidersPopup();
                });
                break;
            default:
                throw new IllegalArgumentException("unexpected callToActionReason");
        }
        snackbar.setActionTextColor(ContextCompat.getColor(BaseActivity.this, R.color.material_green_500));
        snackbar.show();
    }

    @Override
    public void requestNewInterstitial() {
        Timber.d("requestNewInterstitial loading/loaded: %s/%s", mInterstitialAd.isLoading(), mInterstitialAd.isLoaded());
        if (mInterstitialAd.isLoading() || mInterstitialAd.isLoaded()) {
            Timber.d("loading already in progress or already done");
        } else {
            mInterstitialAd.loadAd(AdMobHelper.buildAdRequest(this));
        }
    }

    public IInAppBillingService getIInAppBillingService() {
        return mService;
    }

    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("onServiceDisconnected");
            mService = null;
            InAppBillingServiceConnectionObservable.getInstance().getServiceStatusObservable().onNext(false);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Timber.d("onServiceConnected");
            mService = IInAppBillingService.Stub.asInterface(service);
            InAppBillingServiceConnectionObservable.getInstance().getServiceStatusObservable().onNext(true);
            //update invalidated subs list every some hours
            if (mMyPreferenceManager.isTimeToValidateSubscriptions()) {
                updateOwnedMarketItems();
            }

            //offer free trial every week for non subscribed users
            //check here as we need to have connected service
            if (!mMyPreferenceManager.isHasAnySubscription() && mMyPreferenceManager.isTimeToPeriodicalOfferFreeTrial()) {
                showOfferFreeTrialSubscriptionPopup();
                mMyPreferenceManager.setLastTimePeriodicalFreeTrialOffered(System.currentTimeMillis());
            }
        }
    };

    @Override
    public void updateOwnedMarketItems() {
        Timber.d("updateOwnedMarketItems forceSubsValidation: %s");
        mInAppHelper
                .getValidatedOwnedSubsObservable(mService)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        validatedItems -> {
                            Timber.d("market validatedItems: %s", validatedItems);

                            mMyPreferenceManager.setLastTimeSubscriptionsValidated(System.currentTimeMillis());

                            @InAppHelper.SubscriptionType
                            int type = mInAppHelper.getSubscriptionTypeFromItemsList(validatedItems);
                            Timber.d("subscription type: %s", type);
                            switch (type) {
                                case InAppHelper.SubscriptionType.NONE:
                                    mMyPreferenceManager.setHasNoAdsSubscription(false);
                                    mMyPreferenceManager.setHasSubscription(false);
                                    break;
                                case InAppHelper.SubscriptionType.NO_ADS: {
                                    mMyPreferenceManager.setHasNoAdsSubscription(true);
                                    mMyPreferenceManager.setHasSubscription(false);
                                    //remove banner
                                    if (mAdView != null) {
                                        mAdView.setEnabled(false);
                                        mAdView.setVisibility(View.GONE);
                                    }
                                    break;
                                }
                                case InAppHelper.SubscriptionType.FULL_VERSION: {
                                    mMyPreferenceManager.setHasSubscription(true);
                                    mMyPreferenceManager.setHasNoAdsSubscription(true);
                                    //remove banner
                                    AdView banner = ButterKnife.findById(this, R.id.banner);
                                    if (banner != null) {
                                        banner.setEnabled(false);
                                        banner.setVisibility(View.GONE);
                                    }
                                    break;
                                }
                                default:
                                    throw new IllegalArgumentException("unexpected type: " + type);
                            }
                        },
                        e -> Timber.e(e, "error while getting owned items")
                );
        //also check if user joined app vk group
        mPresenter.checkIfUserJoinedAppVkGroup();
    }

    /**
     * @return id of activity layout
     */
    protected abstract int getLayoutResId();

    /**
     * inject DI here
     */
    protected abstract void callInjections();

    /**
     * Override it to add menu or return 0 if you don't want it
     */
    protected abstract int getMenuResId();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getMenuResId() != 0) {
            getMenuInflater().inflate(getMenuResId(), menu);
        }
        return true;
    }

    /**
     * workaround from http://stackoverflow.com/a/30337653/3212712 to show menu icons
     */
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Timber.e(e, "onMenuOpened...unable to set icons for overflow menu");
                }
            }

            boolean nightModeIsOn = mMyPreferenceManager.isNightMode();
            MenuItem themeMenuItem = menu.findItem(R.id.night_mode_item);
            if (themeMenuItem != null) {
                if (nightModeIsOn) {
                    themeMenuItem.setIcon(R.drawable.ic_brightness_low_white_24dp);
                    themeMenuItem.setTitle(R.string.day_mode);
                } else {
                    themeMenuItem.setIcon(R.drawable.ic_brightness_3_white_24dp);
                    themeMenuItem.setTitle(R.string.night_mode);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void showError(Throwable throwable) {
        Snackbar.make(mRoot, throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(String message) {
        Timber.d("showMessage: %s", message);
        Snackbar.make(mRoot, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(@StringRes int message) {
        showMessage(getString(message));
    }

    @Override
    public void showMessageLong(String message) {
        Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessageLong(@StringRes int message) {
        showMessageLong(getString(message));
    }

    @Override
    public void showProgressDialog(String title) {
        mProgressDialog = new MaterialDialog.Builder(this)
                .progress(true, 0)
                .content(title)
                .cancelable(false)
                .show();
    }

    @Override
    public void showProgressDialog(@StringRes int title) {
        showProgressDialog(getString(title));
    }

    @Override
    public void dismissProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.dismiss();
    }

    @Override
    public void showNeedLoginPopup() {
        Timber.d("showNeedLoginPopup");
        new MaterialDialog.Builder(this)
                .title(R.string.need_login)
                .content(R.string.need_login_content)
                .positiveText(R.string.authorize)
                .onPositive((dialog, which) -> showLoginProvidersPopup())
                .negativeText(android.R.string.cancel)
                .onNegative((dialog, which) -> dialog.dismiss())
                .build()
                .show();
    }

    @Override
    public void showOfferLoginPopup(MaterialDialog.SingleButtonCallback cancelCallback) {
        Timber.d("showOfferLoginPopup");
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_offer_login_to_gain_score_title)
                .content(R.string.dialog_offer_login_to_gain_score_content)
                .positiveText(R.string.authorize)
                .onPositive((dialog, which) -> showLoginProvidersPopup())
                .negativeText(android.R.string.cancel)
                .onNegative(cancelCallback)
                .build()
                .show();
    }

    @Override
    public void showOfferSubscriptionPopup() {
        Timber.d("showOfferSubscriptionPopup");
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_offer_subscription_title)
                .content(R.string.dialog_offer_subscription_content)
                .positiveText(R.string.yes_bliad)
                .onPositive((dialog, which) -> {
                    BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
                    subsDF.show(getSupportFragmentManager(), subsDF.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.AFTER_LEVEL_UP);
                    FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                })
                .negativeText(android.R.string.cancel)
                .onNegative((dialog, which) -> dialog.dismiss())
                .build()
                .show();
    }

    @Override
    public void showOfferFreeTrialSubscriptionPopup() {
        Timber.d("showOfferFreeTrialSubscriptionPopup");

        showProgressDialog(R.string.wait);
        mInAppHelper.getSubsListToBuyObservable(mService, mInAppHelper.getFreeTrailSubsSkus())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        subscriptions -> {
                            dismissProgressDialog();
                            mDialogUtils.showFreeTrialSubscriptionOfferDialog(this, subscriptions.get(0).freeTrialPeriodInDays());
                        },
                        e -> {
                            Timber.e(e);
                            dismissProgressDialog();
                            showError(e);
                        }
                );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.settings) {
            Timber.d("settings pressed");
            BottomSheetDialogFragment settingsDF = SetttingsBottomSheetDialogFragment.newInstance();
            settingsDF.show(getSupportFragmentManager(), settingsDF.getTag());
            return true;
        } else if (i == R.id.subscribe) {
            Timber.d("subscribe pressed");
            BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
            subsDF.show(getSupportFragmentManager(), subsDF.getTag());

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.MENU);
            FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            return true;
        } else if (i == R.id.night_mode_item) {
            mMyPreferenceManager.setIsNightMode(!mMyPreferenceManager.isNightMode());
            return true;
        } else if (i == R.id.text_size) {
            BottomSheetDialogFragment fragmentDialogTextAppearance = TextSizeDialogFragment.newInstance(TextSizeDialogFragment.TextSizeType.ALL);
            fragmentDialogTextAppearance.show(getSupportFragmentManager(), TextSizeDialogFragment.TAG);
            return true;
        } else if (i == R.id.info) {
            DialogFragment dialogFragment = NewVersionDialogFragment.newInstance(getString(R.string.app_info));
            dialogFragment.show(getFragmentManager(), NewVersionDialogFragment.TAG);
            return true;
        } else if (i == R.id.menuItemDownloadAll) {
            mDownloadAllChooser.showDownloadDialog(this);
            return true;
        } else if (i == R.id.faq) {
            mDialogUtils.showFaqDialog(this);
            return true;
        } else if (i == R.id.appLangVersions) {
            mDialogUtils.showAllAppLangVariantsDialog(this);
            return true;
        } else {
            Timber.wtf("unexpected id: %s", item.getItemId());
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!BuildConfig.FLAVOR.equals("dev")) {
            YandexMetrica.onResumeActivity(this);
        }

        if (!isAdsLoaded() && mMyPreferenceManager.isTimeToLoadAds()) {
            requestNewInterstitial();
        }

        setUpBanner();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        //TODO activate it when add Appodeal banner
        Appodeal.onResume(this, Appodeal.BANNER);
    }

    @Override
    public void onPause() {
        if (!BuildConfig.FLAVOR.equals("dev")) {
            YandexMetrica.onPauseActivity(this);
        }
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //ignore facebook analytics log spam
        if (key.startsWith("com.facebook")) {
            return;
        }
        Timber.d("onSharedPreferenceChanged with key: %s", key);
        switch (key) {
            case MyPreferenceManager.Keys.NIGHT_MODE:
                recreate();
                break;
            case MyPreferenceManager.Keys.TIME_FOR_WHICH_BANNERS_DISABLED:
                //check if there is banner in layout
                setUpBanner();
                break;
            //TODO think if we should react on subscriptions events
            default:
                break;
        }
    }

    /**
     * we need this for calligraphy
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void showFreeAdsDisablePopup() {
        android.support.v4.app.DialogFragment dialogFragment = FreeAdsDisablingDialogFragment.newInstance();
        dialogFragment.show(getSupportFragmentManager(), FreeAdsDisablingDialogFragment.TAG);
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> vkCallback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken vkAccessToken) {
                //Пользователь успешно авторизовался
                Timber.d("Auth successful: %s", vkAccessToken.email);
                if (vkAccessToken.email != null) {
                    //here can be case, when we login via Google or Facebook, but try to join group to receive reward
                    //in this case we have firebase user already, so no need to login to firebase
                    //FIXME TODO add support of connect vk acc to firebase acc as social provider
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Timber.e("Firebase user exists, do nothing as we do not implement connect VK acc to Firebase as social provider");
                    } else {
                        mPresenter.startFirebaseLogin(Constants.Firebase.SocialProvider.VK, VKAccessToken.currentToken().accessToken);
                    }
                } else {
                    Toast.makeText(BaseActivity.this, R.string.error_login_no_email, Toast.LENGTH_SHORT).show();
                    mPresenter.logoutUser();
                }
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Timber.e(error.errorMessage);
                Toast.makeText(BaseActivity.this, error.errorMessage, Toast.LENGTH_SHORT).show();
            }
        };
        if (VKSdk.onActivityResult(requestCode, resultCode, data, vkCallback)) {
            Timber.d("Vk receives and handled onActivityResult");
            super.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Timber.d("Auth successful: %s", result);
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct == null) {
                    Timber.wtf("GoogleSignInAccount is NULL!");
                    showMessage("GoogleSignInAccount is NULL!");
                    return;
                }
                String email = acct.getEmail();
                if (!TextUtils.isEmpty(email)) {
                    mPresenter.startFirebaseLogin(Constants.Firebase.SocialProvider.GOOGLE, acct.getIdToken());
                } else {
                    Toast.makeText(BaseActivity.this, R.string.error_login_no_email, Toast.LENGTH_SHORT).show();
                    mPresenter.logoutUser();
                }
            } else {
                // Signed out, show unauthenticated UI.
                mPresenter.logoutUser();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void updateUser(User user) {
        //nothing to do here
    }

    private void initAndUpdateRemoteConfig() {
        //remote config
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.FLAVOR.equals("dev"))
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        // Set default Remote Config values. In general you should have in app defaults for all
        // values that you may configure using Remote Config later on. The idea is that you
        // use the in app defaults and when you need to adjust those defaults, you set an updated
        // value in the App Manager console. Then the next time you application fetches from the
        // server, the updated value will be used. You can set defaults via an xml file like done
        // here or you can set defaults inline by using one of the other setDefaults methods.S
        // [START set_default_values]
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        // cacheExpirationSeconds is set to cacheExpiration here, indicating that any previously
        // fetched and cached config would be considered expired because it would have been fetched
        // more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
        // throttling is in progress. The default expiration duration is 43200 (12 hours).
        long cacheExpiration = 20000; //default 43200
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 60 * 5;//for 5 min
        }
        //comment this if you want to use local data
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Timber.d("Fetch Succeeded");
                // Once the config is successfully fetched it must be activated before newly fetched
                // values are returned.
                mFirebaseRemoteConfig.activateFetched();
            } else {
                Timber.d("Fetch Failed");
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.e("onConnectionFailed: %s", connectionResult);
    }

    public void startArticleActivity(List<String> urls, int position) {
        Timber.d("startActivity: urls.size() %s, position: %s", urls.size(), position);

        Intent intent = new Intent(this, getArticleActivityClass());
        intent.putExtra(EXTRA_ARTICLES_URLS_LIST, new ArrayList<>(urls));
        intent.putExtra(EXTRA_POSITION, position);

        if (isTimeToShowAds()) {
            if (isAdsLoaded()) {
                showInterstitial(new MyAdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        intent.putExtra(EXTRA_SHOW_DISABLE_ADS, true);
                        startActivity(intent);
                    }
                }, true);
                return;
            } else {
                Timber.d("Ads not loaded yet");
            }
        } else {
            Timber.d("it's not time to showInterstitial ads");
        }
        startActivity(intent);
    }

    public void startArticleActivity(String url) {
        Timber.d("startActivity: %s", url);
        startArticleActivity(Collections.singletonList(url), 0);
    }

    public void startMaterialsActivity() {
        Timber.d("startActivity");

        Intent intent = new Intent(BaseActivity.this, getMaterialsActivityClass());

        if (isTimeToShowAds()) {
            if (isAdsLoaded()) {
                showInterstitial(new MyAdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();

                        intent.putExtra(EXTRA_SHOW_DISABLE_ADS, true);
                        startActivity(intent);
                    }
                }, true);
                return;
            } else {
                Timber.d("Ads not loaded yet");
            }
        } else {
            Timber.d("it's not time to showInterstitial ads");
        }
        startActivity(intent);
    }

    public void startGalleryActivity() {
        Timber.d("startActivity");

        Intent intent = new Intent(this, getGalleryActivityClass());

        if (isTimeToShowAds()) {
            if (isAdsLoaded()) {
                showInterstitial(new MyAdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        intent.putExtra(EXTRA_SHOW_DISABLE_ADS, true);
                        startActivity(intent);
                    }
                }, true);
                return;
            } else {
                Timber.d("Ads not loaded yet");
            }
        } else {
            Timber.d("it's not time to showInterstitial ads");
        }
        startActivity(intent);
    }

    public void startTagsSearchActivity(List<ArticleTag> tagList) {
        Timber.d("startActivity");

        Intent intent = new Intent(BaseActivity.this, getTagsSearchActivityClass());
        intent.putExtra(EXTRA_TAGS, new ArrayList<>(ArticleTag.getStringsFromTags(tagList)));

        if (isTimeToShowAds()) {
            if (isAdsLoaded()) {
                showInterstitial(new MyAdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        intent.putExtra(EXTRA_SHOW_DISABLE_ADS, true);
                        startActivity(intent);
                    }
                }, true);
                return;
            } else {
                Timber.d("Ads not loaded yet");
            }
        } else {
            Timber.d("it's not time to showInterstitial ads");
        }
        startActivity(intent);
    }

    public void startTagsSearchActivity() {
        Timber.d("startActivity");

        Intent intent = new Intent(this, getTagsSearchActivityClass());

        if (isTimeToShowAds()) {
            if (isAdsLoaded()) {
                showInterstitial(new MyAdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        intent.putExtra(EXTRA_SHOW_DISABLE_ADS, true);
                        startActivity(intent);
                    }
                }, true);
                return;
            } else {
                Timber.d("Ads not loaded yet");
            }
        } else {
            Timber.d("it's not time to showInterstitial ads");
        }
        startActivity(intent);
    }

    protected Class getTagsSearchActivityClass() {
        return TagSearchActivity.class;
    }

    protected Class getGalleryActivityClass() {
        return GalleryActivity.class;
    }

    protected Class getMaterialsActivityClass() {
        return MaterialsActivity.class;
    }

    protected Class getArticleActivityClass() {
        return ArticleActivity.class;
    }
}