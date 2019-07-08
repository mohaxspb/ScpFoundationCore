package ru.kuchanov.scpcore.monetization.util.playmarket;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kotlin.NotImplementedError;
import kotlin.Pair;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.api.model.response.PurchaseValidateResponse;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.Item;
import ru.kuchanov.scpcore.monetization.model.Subscription;
import ru.kuchanov.scpcore.monetization.util.InappPurchaseUtil;
import ru.kuchanov.scpcore.monetization.util.IntentSenderWrapper;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static ru.kuchanov.scpcore.ui.activity.BaseDrawerActivity.REQUEST_CODE_INAPP;
import static ru.kuchanov.scpcore.ui.fragment.monetization.SubscriptionsFragment.REQUEST_CODE_SUBSCRIPTION;

/**
 * Created by mohax on 02.02.2017.
 */
public class InAppHelper implements InappPurchaseUtil {

    private final static int API_VERSION_3 = 3;

    private final static int API_VERSION_6 = 6;

    public static final int RESULT_OK = 0;// - success

    public static final int RESULT_USER_CANCELED = 1;// - user pressed back or canceled a dialog

    public static final int RESULT_BILLING_UNAVAILABLE = 3;// - this billing API version is not supported for the type requested

    public static final int RESULT_ITEM_UNAVAILABLE = 4;// - requested SKU is not available for purchase

    public static final int RESULT_DEVELOPER_ERROR = 5;// - invalid arguments provided to the API

    public static final int RESULT_ERROR = 6;// - Fatal error during the API action

    public static final int RESULT_ITEM_ALREADY_OWNED = 7;// - Failure to purchase since item is already owned

    public static final int RESULT_ITEM_NOT_OWNED = 8;// - Failure to consume since item is not owned

    private final ApiClient mApiClient;

    private final MyPreferenceManager mMyPreferenceManager;

    private final DbProviderFactory mDbProviderFactory;

    @Nullable
    private IInAppBillingService mInAppBillingService;

    @Nullable
    private BaseActivity<?, ?> baseActivity;

    public InAppHelper(
            final MyPreferenceManager preferenceManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient
    ) {
        super();
        mMyPreferenceManager = preferenceManager;
        mDbProviderFactory = dbProviderFactory;
        mApiClient = apiClient;
    }

    //fixme check it
    @Override
    public void onActivate(@NotNull final BaseActivity<?, ?> activity) {
        //todo
        baseActivity = activity;

        //initAds subs service
        final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        baseActivity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        //nothing to do?..
    }

    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Timber.d("onServiceDisconnected");
            mInAppBillingService = null;
            InAppBillingServiceConnectionObservable.getInstance().getServiceStatusObservable().onNext(false);
        }

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            Timber.d("onServiceConnected");
            mInAppBillingService = IInAppBillingService.Stub.asInterface(service);
            InAppBillingServiceConnectionObservable.getInstance().getServiceStatusObservable().onNext(true);
            //update invalidated subs list every some hours
            if (mMyPreferenceManager.isTimeToValidateSubscriptions()) {
                baseActivity.updateOwnedMarketItems();
            }

            //offer free trial every week for non subscribed users
            //check here as we need to have connected service
            if (!mMyPreferenceManager.isHasAnySubscription()
                    && mMyPreferenceManager.isTimeToPeriodicalOfferFreeTrial()) {
                final Bundle bundle = new Bundle();
                bundle.putString(
                        Constants.Firebase.Analytics.EventParam.PLACE,
                        Constants.Firebase.Analytics.EventValue.PERIODICAL
                );
                FirebaseAnalytics
                        .getInstance(BaseApplication.getAppInstance())
                        .logEvent(Constants.Firebase.Analytics.EventName.FREE_TRIAL_OFFER_SHOWN, bundle);

                baseActivity.showOfferFreeTrialSubscriptionPopup();
                mMyPreferenceManager.setLastTimePeriodicalFreeTrialOffered(System.currentTimeMillis());
            }

            //check here along with onUserChange as there can be situation when data from DB gained,
            //but service not connected yet
            //check if user score is greater than 1000 and offer him/her a free trial if there is no subscription owned
            if (!mMyPreferenceManager.isHasAnySubscription()
                    && baseActivity.getPresenter().getUser() != null
                    && baseActivity.getPresenter().getUser().score >= 1000
                    //do not show it after level up gain, where we add 10000 score
                    && baseActivity.getPresenter().getUser().score < 10000
                    && !mMyPreferenceManager.isFreeTrialOfferedAfterGetting1000Score()) {
                final Bundle bundle = new Bundle();
                bundle.putString(
                        Constants.Firebase.Analytics.EventParam.PLACE,
                        Constants.Firebase.Analytics.EventValue.SCORE_1000_REACHED
                );
                FirebaseAnalytics
                        .getInstance(BaseApplication.getAppInstance())
                        .logEvent(Constants.Firebase.Analytics.EventName.FREE_TRIAL_OFFER_SHOWN, bundle);

                baseActivity.showOfferFreeTrialSubscriptionPopup();
                mMyPreferenceManager.setFreeTrialOfferedAfterGetting1000Score();
            }
        }
    };

    @Override
    public void onActivityDestroy(@NotNull final Activity activity) {
        if (mInAppBillingService != null) {
            activity.unbindService(mServiceConn);
        }
        baseActivity = null;
    }

    private Single<List<Item>> getValidatedOwnedSubsObservable() {
        return Single.<List<Item>>create(subscriber -> {
            try {
                Bundle ownedItemsBundle = mInAppBillingService.getPurchases(
                        API_VERSION_3,
                        BaseApplication.getAppInstance().getPackageName(),
                        "subs",
                        null
                );

                Timber.d("ownedItems bundle: %s", ownedItemsBundle);
                final int playMarketResponseCode = ownedItemsBundle.getInt("RESPONSE_CODE");
                if (playMarketResponseCode == RESULT_OK) {
                    //TODO use gson for parsing
                    List<String> ownedSkus = ownedItemsBundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    List<String> purchaseDataList = ownedItemsBundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    List<String> signatureList = ownedItemsBundle.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    String continuationToken = ownedItemsBundle.getString("INAPP_CONTINUATION_TOKEN");

                    if (ownedSkus == null || purchaseDataList == null || signatureList == null) {
                        subscriber.onError(new IllegalStateException("some of owned items info is null while get owned items"));
                    } else {
                        List<Item> ownedItemsList = new ArrayList<>();
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            String purchaseData = purchaseDataList.get(i);
                            String signature = signatureList.get(i);
                            String sku = ownedSkus.get(i);
                            ownedItemsList.add(new Item(purchaseData, signature, sku, continuationToken));
                        }
                        subscriber.onSuccess(ownedItemsList);
                    }
                } else {
                    subscriber.onError(
                            new IllegalStateException(
                                    "ownedItemsBundle RESPONSE_CODE is not 0. It's " + playMarketResponseCode
                            )
                    );
                }
            } catch (RemoteException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        })
                .flatMap(items -> {
                    final List<Item> validatedItems = new ArrayList<>();
                    for (final Item item : items) {
                        Timber.d("validate item: %s", item.getSku());
                        final PurchaseValidateResponse purchaseValidateResponse = mApiClient.validateSubscription(
                                BaseApplication.getAppInstance().getPackageName(),
                                item.getSku(),
                                item.getPurchaseData().getPurchaseToken()
                        ).toBlocking().value();
                        switch (purchaseValidateResponse.getStatus()) {
                            case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_VALID:
                                Timber.d("Item successfully validated: %s", item.getSku());
                                validatedItems.add(item);
                                break;
                            case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_INVALID:
//                                    return Observable.error(new IllegalStateException("Purchase state is INVALID"));
                                Timber.e("Invalid subs: %s", item.getSku());
                                break;
                            case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_GOOGLE_SERVER_ERROR:
                                //if there is error we should cancel subs validating
                                return Single.error(new IllegalStateException("Purchase state cant be validated, as Google Servers sends error"));
                            default:
                                return Single.error(new IllegalArgumentException("Unexpected validation status: " + purchaseValidateResponse.getStatus()));
                        }
                    }
                    return Single.just(validatedItems);
                });
    }

    @NotNull
    @Override
    public Single<Subscription> getInAppHistory() {
        return Single.create(subscriber -> {
            try {
                final Bundle bundle = mInAppBillingService.getPurchaseHistory(
                        API_VERSION_6,
                        BaseApplication.getAppInstance().getPackageName(),
                        "inapp",
                        null,
                        null
                );

                for (final String key : bundle.keySet()) {
                    Timber.d("ownedItems bundle: %s/%s", key, bundle.get(key));
                }
                if (bundle.getInt("RESPONSE_CODE") == RESULT_OK) {
                    final List<String> ownedSkus = bundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    final List<String> purchaseDataList = bundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    final List<String> signatureList = bundle.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    final String continuationToken = bundle.getString("INAPP_CONTINUATION_TOKEN");

                    if (ownedSkus == null || purchaseDataList == null || signatureList == null) {
                        subscriber.onError(new IllegalStateException("some of owned items info is null while get owned items"));
                    } else {
                        final List<Item> ownedItemsList = new ArrayList<>();
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            final String purchaseData = purchaseDataList.get(i);
                            final String signature = signatureList.get(i);
                            final String sku = ownedSkus.get(i);
                            ownedItemsList.add(new Item(purchaseData, signature, sku, continuationToken));
                        }
                        Timber.d("ownedItemsList: %s", ownedItemsList);
                        if (ownedItemsList.isEmpty()) {
                            subscriber.onError(new IllegalStateException("OwnedItems list is empty! WTF?!"));
                        } else {
                            final Item item = ownedItemsList.get(0);
                            subscriber.onSuccess(
                                    new Subscription(
                                            item.getSku(),
                                            InappPurchaseUtil.InappType.IN_APP,
                                            "N/A", //price,
                                            -1,//price_amount_micros
                                            "N/A", //price_currency_code
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            0,
                                            null,
                                            0,
                                            item.getPurchaseData().getPurchaseToken()
                                    )
                            );
                        }
                    }
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle RESPONSE_CODE is not 0: " + bundle.getInt("RESPONSE_CODE")));
                }
            } catch (final RemoteException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        });
    }

    @NotNull
    @Override
    public Single<List<Subscription>> getSubsListToBuyObservable(@NotNull final List<String> skus) {
        return Single.create(subscriber -> {
            try {
                //get all subs detailed info
                final Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", (ArrayList<String>) skus);
                final Bundle skuDetails = mInAppBillingService.getSkuDetails(
                        API_VERSION_3,
                        BaseApplication.getAppInstance().getPackageName(),
                        "subs",
                        querySkus
                );
                final int responseCodeCode = skuDetails.getInt("RESPONSE_CODE");
                if (responseCodeCode == RESULT_OK) {
                    final List<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                    if (responseList == null) {
                        subscriber.onError(new IllegalStateException("responseList is null while get subs details"));
                        return;
                    }

                    final List<Subscription> allSubscriptions = new ArrayList<>();
                    for (final String thisResponse : responseList) {
                        final Subscription subscription = new GsonBuilder().create().fromJson(thisResponse, Subscription.class);
                        allSubscriptions.add(subscription);
                    }
                    Collections.sort(allSubscriptions, Subscription.COMPARATOR_PRICE);

                    subscriber.onSuccess(allSubscriptions);
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle.getInt(\"RESPONSE_CODE\") is: " + responseCodeCode));
                }
            } catch (final RemoteException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        });
    }

    @NotNull
    @Override
    public Single<List<Subscription>> getInAppsListToBuy() {
        Timber.d("getInAppsListToBuy");
        return Single.create(subscriber -> {
            try {
                //get all subs detailed info
                final ArrayList<String> skuList = new ArrayList<>();
                //get it from build config
//                Collections.addAll(skuList, BaseApplication.getAppInstance().getString(R.string.inapp_skus).split(","));
                Collections.addAll(skuList, BaseApplication.getAppInstance().getString(R.string.ver3_inapp_skus).split(","));
                Timber.d("skuList: %s", skuList);

                final Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                final Bundle skuDetails = mInAppBillingService.getSkuDetails(API_VERSION_3, BaseApplication.getAppInstance().getPackageName(), "inapp", querySkus);
                Timber.d("skuDetails: %s", skuDetails);
                final int responseCode = skuDetails.getInt("RESPONSE_CODE");
                if (responseCode == RESULT_OK) {
                    final List<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                    if (responseList == null) {
                        subscriber.onError(new IllegalStateException("responseList is null while get subs details"));
                        return;
                    }

                    final List<Subscription> allSubscriptions = new ArrayList<>();
                    for (final String thisResponse : responseList) {
                        final Subscription subscription = new GsonBuilder().create().fromJson(thisResponse, Subscription.class);
                        allSubscriptions.add(subscription);
                    }
                    Collections.sort(allSubscriptions, Subscription.COMPARATOR_PRICE);

                    subscriber.onSuccess(allSubscriptions);
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle RESPONSE_CODE is not 0: " + responseCode));
                }
            } catch (final RemoteException e) {
                subscriber.onError(e);
            }
        });
    }

    @NotNull
    @Override
    public Single<Integer> consumeInApp(@NotNull final String sku, @NotNull final String token) {
        final String packageName = BaseApplication.getAppInstance().getPackageName();

        return mApiClient.validateProduct(packageName, sku, token)
                .flatMap(purchaseValidateResponse -> {
                    @PurchaseValidateResponse.PurchaseValidationStatus final int status = purchaseValidateResponse.getStatus();
                    Timber.d("PurchaseValidationStatus: %s", status);
                    switch (status) {
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_VALID:
                            try {
                                final int response = mInAppBillingService.consumePurchase(API_VERSION_3, packageName, token);
                                return Single.just(response);
                            } catch (final RemoteException e) {
                                return Single.error(e);
                            }
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_INVALID:
                            return Single.error(new IllegalStateException("Purchase state is INVALID"));
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_GOOGLE_SERVER_ERROR:
                            return Single.error(new IllegalStateException("Purchase state cant be validated, as Google Servers sends error"));
                        default:
                            return Single.error(new IllegalArgumentException("Unexpected validation status: " + status));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(integer -> mApiClient
                        .incrementScoreInFirebase(Constants.LEVEL_UP_SCORE_TO_ADD)
                        .observeOn(Schedulers.io())
                        .flatMap(newTotalScore -> mApiClient
                                .addRewardedInapp(sku)
                                .flatMap(aVoid -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                        )
                        .doOnError(throwable -> mMyPreferenceManager.addUnsyncedScore(Constants.LEVEL_UP_SCORE_TO_ADD))
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NotNull
    @Override
    public Single<List<Item>> validateSubsObservable() {
        return getValidatedOwnedSubsObservable()
                .flatMap(validatedItems -> {
                    Timber.d("market validatedItems: %s", validatedItems);

                    mMyPreferenceManager.setLastTimeSubscriptionsValidated(System.currentTimeMillis());

                    @SubscriptionType final int type = getSubscriptionTypeFromItemsList(validatedItems);
                    Timber.d("subscription type: %s", type);
                    switch (type) {
                        case SubscriptionType.NONE:
                            mMyPreferenceManager.setHasNoAdsSubscription(false);
                            mMyPreferenceManager.setHasSubscription(false);
                            break;
                        case SubscriptionType.NO_ADS: {
                            mMyPreferenceManager.setHasNoAdsSubscription(true);
                            mMyPreferenceManager.setHasSubscription(false);
                            break;
                        }
                        case SubscriptionType.FULL_VERSION: {
                            mMyPreferenceManager.setHasSubscription(true);
                            mMyPreferenceManager.setHasNoAdsSubscription(true);
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("unexpected type: " + type);
                    }

                    return Single.just(validatedItems);
                });
    }

    @NotNull
    @Override
    public Single<IntentSenderWrapper> intentSenderSingle(
            @NotNull @InappType final String type,
            @NotNull final String sku
    ) {
        return Single.fromCallable(() -> mInAppBillingService.getBuyIntent(
                API_VERSION_3,
                BaseApplication.getAppInstance().getPackageName(),
                sku,
                type,
                String.valueOf(System.currentTimeMillis())
        ))
                .map(bundle -> new Pair<>(bundle, bundle.getInt("RESPONSE_CODE")))
                .flatMap(bundleResponseCodePair -> {
                    Timber.d("bundleResponseCodePair: %s/%s", bundleResponseCodePair.getFirst(), bundleResponseCodePair.getSecond());
                    if (bundleResponseCodePair.getSecond() == RESULT_OK) {
                        final PendingIntent pendingIntent = bundleResponseCodePair.getFirst().getParcelable("BUY_INTENT");
                        return Single
                                .just(
                                        new IntentSenderWrapper(
                                                pendingIntent.getIntentSender(),
                                                type,
                                                sku
                                        )
                                );
                    } else if (bundleResponseCodePair.getSecond() == RESULT_ITEM_ALREADY_OWNED) {
                        return getInAppHistory()
                                .flatMap(itemsOwned -> consumeInApp(
                                        itemsOwned.productId,
                                        itemsOwned.purchaseToken
                                ))
                                .flatMap(integer -> intentSenderSingle(
                                        type,
                                        sku
                                ));
                    } else {
                        return Single.error(
                                new IllegalStateException(
                                        "RESPONSE_CODE is not OK: " + bundleResponseCodePair.getSecond()
                                )
                        );
                    }
                });
    }

    @Override
    public Single<Subscription> startPurchase(
            @NotNull final IntentSenderWrapper intentSender
    ) {
        int requestCode = -1;
        if (intentSender.getType().equals(InappType.IN_APP)) {
            requestCode = REQUEST_CODE_INAPP;
        } else if (intentSender.getType().equals(InappType.SUBS)) {
            requestCode = REQUEST_CODE_SUBSCRIPTION;
        }

        try {
            baseActivity.startIntentSenderForResult(
                    intentSender.getIntentSender(),
                    requestCode,
                    new Intent(),
                    0,
                    0,
                    0,
                    null
            );
        } catch (final IntentSender.SendIntentException e) {
            Timber.wtf(e);
            baseActivity.showError(e);
        }
    }

    @NotNull
    @Override
    public Pair<Integer, Integer> getTitleAndIconForSubsSku(@NotNull final String sku) {
        @StringRes
        int title;
        @DrawableRes
        int icon;
        switch (getMonthFromSkuId(sku)) {
            case 1:
                title = R.string.subs_1_month_title;
                icon = R.drawable.ic_scp_icon_laborant;
                break;
            case 3:
                title = R.string.subs_3_month_title;
                icon = R.drawable.ic_scp_icon_mns;
                break;
            case 6:
                title = R.string.subs_6_month_title;
                icon = R.drawable.ic_scp_icon_ns;
                break;
            case 12:
                title = R.string.subs_12_month_title;
                icon = R.drawable.ic_scp_icon_sns;
                break;
            default:
                throw new IllegalArgumentException("unexpected subs period");
        }

        return new Pair<>(title, icon);
    }

    @Override
    @NotNull
    public List<String> getNewSubsSkus() {
        return new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.ver4_skus).split(",")));
    }

    @Override
    @NotNull
    public List<String> getFreeTrailSubsSkus() {
        return new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.ver4_subs_free_trial).split(",")));
    }

    @Override
    @NotNull
    public List<String> getNewNoAdsSubsSkus() {
        return new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.ver4_subs_no_ads).split(",")));
    }

    @Override
    @NotNull
    public List<String> getNewInAppsSkus() {
        return new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.ver4_inapp_skus).split(",")));
    }
}
