package ru.kuchanov.scpcore.monetization.util.playmarket;

import com.google.gson.GsonBuilder;

import com.android.vending.billing.IInAppBillingService;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.util.Pair;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.api.model.response.PurchaseValidateResponse;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.Item;
import ru.kuchanov.scpcore.monetization.model.Subscription;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static ru.kuchanov.scpcore.ui.activity.BaseDrawerActivity.REQUEST_CODE_INAPP;
import static ru.kuchanov.scpcore.ui.fragment.monetization.SubscriptionsFragment.REQUEST_CODE_SUBSCRIPTION;

/**
 * Created by mohax on 02.02.2017.
 * <p>
 * for scp_ru
 */
public class InAppHelper {

    private final static int API_VERSION_3 = 3;

    public static final int RESULT_OK = 0;// - success

    public static final int RESULT_USER_CANCELED = 1;// - user pressed back or canceled a dialog

    public static final int RESULT_BILLING_UNAVAILABLE = 3;// - this billing API version is not supported for the type requested

    public static final int RESULT_ITEM_UNAVAILABLE = 4;// - requested SKU is not available for purchase

    public static final int RESULT_DEVELOPER_ERROR = 5;// - invalid arguments provided to the API

    public static final int RESULT_ERROR = 6;// - Fatal error during the API action

    public static final int RESULT_ITEM_ALREADY_OWNED = 7;// - Failure to purchase since item is already owned

    public static final int RESULT_ITEM_NOT_OWNED = 8;// - Failure to consume since item is not owned


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            InappType.IN_APP,
            InappType.SUBS
    })
    public @interface InappType {

        String IN_APP = "inapp";
        String SUBS = "subs";
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SubscriptionType.NO_ADS,
            SubscriptionType.FULL_VERSION,
            SubscriptionType.NONE
    })
    public @interface SubscriptionType {

        int NONE = -1;
        int NO_ADS = 0;
        int FULL_VERSION = 1;
    }

    private final ApiClient mApiClient;

    private final MyPreferenceManager mMyPreferenceManager;

    private final DbProviderFactory mDbProviderFactory;

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

    @SubscriptionType
    public static int getSubscriptionTypeFromItemsList(@NonNull final Iterable<Item> ownedItems) {
        final Context context = BaseApplication.getAppInstance();
        //add old old donate subs, new ones and one with free trial period
        final Collection<String> fullVersionSkus = new ArrayList<>(Arrays.asList(context.getString(R.string.old_skus).split(",")));
        Collections.addAll(fullVersionSkus, context.getString(R.string.ver_2_skus).split(","));
        Collections.addAll(fullVersionSkus, context.getString(R.string.ver3_skus).split(","));
        Collections.addAll(fullVersionSkus, context.getString(R.string.subs_free_trial).split(","));
        Collections.addAll(fullVersionSkus, context.getString(R.string.ver3_subs_free_trial).split(","));
        Collections.addAll(fullVersionSkus, context.getString(R.string.ver4_skus).split(","));
        Collections.addAll(fullVersionSkus, context.getString(R.string.ver4_subs_free_trial).split(","));

        final Collection<String> noAdsSkus = new ArrayList<>();
        noAdsSkus.add(context.getString(R.string.subs_no_ads_old));
        noAdsSkus.add(context.getString(R.string.subs_no_ads_ver_2));
        noAdsSkus.add(context.getString(R.string.ver3_subs_no_ads));
        noAdsSkus.add(context.getString(R.string.ver4_subs_no_ads));

        final List<String> ownedSkus = getSkuListFromItemsList(ownedItems);
        noAdsSkus.retainAll(ownedSkus);
        fullVersionSkus.retainAll(ownedSkus);

        @SubscriptionType final int type = fullVersionSkus.isEmpty()
                                           ? noAdsSkus.isEmpty()
                                             ? SubscriptionType.NONE
                                             : SubscriptionType.NO_ADS
                                           : SubscriptionType.FULL_VERSION;

        return type;
    }

    private static List<String> getSkuListFromItemsList(@NonNull final Iterable<Item> ownedItems) {
        final List<String> skus = new ArrayList<>();
        for (final Item item : ownedItems) {
            skus.add(item.sku);
        }
        return skus;
    }

    private Observable<List<Item>> getValidatedOwnedSubsObservable(final IInAppBillingService mInAppBillingService) {
        return Observable.<List<Item>>unsafeCreate(subscriber -> {
            try {
                Bundle ownedItemsBundle = mInAppBillingService.getPurchases(API_VERSION_3, BaseApplication.getAppInstance().getPackageName(), "subs", null);

                Timber.d("ownedItems bundle: %s", ownedItemsBundle);
                if (ownedItemsBundle.getInt("RESPONSE_CODE") == RESULT_OK) {
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
                        subscriber.onNext(ownedItemsList);
                        subscriber.onCompleted();
                    }
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle.getInt(\"RESPONSE_CODE\") is not 0"));
                }
            } catch (RemoteException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        })
                .flatMap(items -> {
                    final List<Item> validatedItems = new ArrayList<>();
                    for (final Item item : items) {
                        Timber.d("validate item: %s", item.sku);
                        final PurchaseValidateResponse purchaseValidateResponse = mApiClient.validateSubscription(
                                BaseApplication.getAppInstance().getPackageName(),
                                item.sku,
                                item.purchaseData.purchaseToken
                        ).toBlocking().value();
                        switch (purchaseValidateResponse.getStatus()) {
                            case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_VALID:
                                Timber.d("Item successfully validated: %s", item.sku);
                                validatedItems.add(item);
                                break;
                            case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_INVALID:
//                                    return Observable.error(new IllegalStateException("Purchase state is INVALID"));
                                Timber.e("Invalid subs: %s", item.sku);
                                break;
                            case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_GOOGLE_SERVER_ERROR:
                                //if there is error we should cancel subs validating
                                return Observable.error(new IllegalStateException("Purchase state cant be validated, as Google Servers sends error"));
                            default:
                                return Observable.error(new IllegalArgumentException("Unexpected validation status: " + purchaseValidateResponse.getStatus()));
                        }
                    }
                    return Observable.just(validatedItems);
                });
    }

    public Observable<List<Item>> getOwnedInAppsObservable(final IInAppBillingService mInAppBillingService) {
        return Observable.unsafeCreate(subscriber -> {
            try {
                final Bundle ownedItemsBundle = mInAppBillingService.getPurchases(API_VERSION_3, BaseApplication.getAppInstance().getPackageName(), "inapp", null);

                Timber.d("ownedItems bundle: %s", ownedItemsBundle);
                if (ownedItemsBundle.getInt("RESPONSE_CODE") == RESULT_OK) {
                    final List<String> ownedSkus = ownedItemsBundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    final List<String> purchaseDataList = ownedItemsBundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    final List<String> signatureList = ownedItemsBundle.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    final String continuationToken = ownedItemsBundle.getString("INAPP_CONTINUATION_TOKEN");

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
                        subscriber.onNext(ownedItemsList);
                        subscriber.onCompleted();
                    }
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle.getInt(\"RESPONSE_CODE\") is not 0"));
                }
            } catch (final RemoteException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        });
    }

    public Observable<List<Subscription>> getSubsListToBuyObservable(
            final IInAppBillingService mInAppBillingService,
            final List<String> skus
    ) {
        return Observable.unsafeCreate(subscriber -> {
            try {
                //get all subs detailed info
                final Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", (ArrayList<String>) skus);
                final Bundle skuDetails = mInAppBillingService.getSkuDetails(API_VERSION_3, BaseApplication.getAppInstance().getPackageName(), "subs", querySkus);
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

                    subscriber.onNext(allSubscriptions);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle.getInt(\"RESPONSE_CODE\") is: " + responseCodeCode));
                }
            } catch (final RemoteException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        });
    }

    public Observable<List<Subscription>> getInAppsListToBuyObservable(final IInAppBillingService mInAppBillingService) {
        return Observable.unsafeCreate(subscriber -> {
            try {
                //get all subs detailed info
                final List<String> skuList = new ArrayList<>();
                //get it from build config
//                Collections.addAll(skuList, BaseApplication.getAppInstance().getString(R.string.inapp_skus).split(","));
                Collections.addAll(skuList, BaseApplication.getAppInstance().getString(R.string.ver3_inapp_skus).split(","));
                Timber.d("skuList: %s", skuList);

                final Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", (ArrayList<String>) skuList);
                final Bundle skuDetails = mInAppBillingService.getSkuDetails(API_VERSION_3, BaseApplication.getAppInstance().getPackageName(), "inapp", querySkus);
                Timber.d("skuDetails: %s", skuDetails);
                if (skuDetails.getInt("RESPONSE_CODE") == RESULT_OK) {
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

                    subscriber.onNext(allSubscriptions);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle.getInt(\"RESPONSE_CODE\") is not 0"));
                }
            } catch (final RemoteException e) {
                subscriber.onError(e);
            }
        });
    }

    public Single<Integer> consumeInApp(
            final String sku,
            final String token,
            final IInAppBillingService mInAppBillingService
    ) {
        final String packageName = BaseApplication.getAppInstance().getPackageName();

        return mApiClient.validateProduct(packageName, sku, token)
                .flatMapObservable(purchaseValidateResponse -> {
                    @PurchaseValidateResponse.PurchaseValidationStatus final int status = purchaseValidateResponse.getStatus();
                    Timber.d("PurchaseValidationStatus: %s", status);
                    switch (status) {
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_VALID:
                            try {
                                final int response = mInAppBillingService.consumePurchase(API_VERSION_3, packageName, token);
                                return Observable.just(response);
                            } catch (final RemoteException e) {
                                return Observable.error(e);
                            }
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_INVALID:
                            return Observable.error(new IllegalStateException("Purchase state is INVALID"));
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_GOOGLE_SERVER_ERROR:
                            return Observable.error(new IllegalStateException("Purchase state cant be validated, as Google Servers sends error"));
                        default:
                            return Observable.error(new IllegalArgumentException("Unexpected validation status: " + status));
                    }
                })
                .toSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Item>> validateSubsObservable(final IInAppBillingService service) {
        return getValidatedOwnedSubsObservable(service)
                .flatMap(validatedItems -> {
                    Timber.d("market validatedItems: %s", validatedItems);

                    mMyPreferenceManager.setLastTimeSubscriptionsValidated(System.currentTimeMillis());

                    @InAppHelper.SubscriptionType final int type = InAppHelper.getSubscriptionTypeFromItemsList(validatedItems);
                    Timber.d("subscription type: %s", type);
                    switch (type) {
                        case InAppHelper.SubscriptionType.NONE:
                            mMyPreferenceManager.setHasNoAdsSubscription(false);
                            mMyPreferenceManager.setHasSubscription(false);
                            break;
                        case InAppHelper.SubscriptionType.NO_ADS: {
                            mMyPreferenceManager.setHasNoAdsSubscription(true);
                            mMyPreferenceManager.setHasSubscription(false);
                            break;
                        }
                        case InAppHelper.SubscriptionType.FULL_VERSION: {
                            mMyPreferenceManager.setHasSubscription(true);
                            mMyPreferenceManager.setHasNoAdsSubscription(true);
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("unexpected type: " + type);
                    }

                    return Observable.just(validatedItems);
                });
    }

    public Single<IntentSender> startPurchase(
            final IInAppBillingService mInAppBillingService,
            @InappType final String type,
            final String sku
    ) {
        return Observable.fromCallable(() -> mInAppBillingService.getBuyIntent(
                API_VERSION_3,
                BaseApplication.getAppInstance().getPackageName(),
                sku,
                type,
                String.valueOf(System.currentTimeMillis())
        ))
                .map(bundle -> new Pair<>(bundle, bundle.getInt("RESPONSE_CODE")))
                .flatMap(bundleResponseCodePair -> {
                    if (bundleResponseCodePair.second == RESULT_OK) {
                        final PendingIntent pendingIntent = bundleResponseCodePair.first.getParcelable("BUY_INTENT");
                        return Single.just(pendingIntent.getIntentSender()).toObservable();
                    } else if (bundleResponseCodePair.second == RESULT_ITEM_ALREADY_OWNED) {
                        return getOwnedInAppsObservable(mInAppBillingService)
                                .flatMapSingle(itemsOwned -> consumeInApp(
                                        itemsOwned.get(0).sku,
                                        itemsOwned.get(0).purchaseData.purchaseToken,
                                        mInAppBillingService
                                ))
                                .observeOn(AndroidSchedulers.mainThread())
                                .flatMapSingle(integer -> mApiClient
                                        .incrementScoreInFirebaseObservable(Constants.LEVEL_UP_SCORE_TO_ADD)
                                        .observeOn(Schedulers.io())
                                        .flatMap(newTotalScore -> mApiClient
                                                .addRewardedInapp(sku)
                                                .flatMap(aVoid -> mDbProviderFactory.getDbProvider().updateUserScore(newTotalScore))
                                        )
                                        .doOnError(throwable -> mMyPreferenceManager.addUnsyncedScore(Constants.LEVEL_UP_SCORE_TO_ADD))
                                        .toSingle()
                                )
                                .observeOn(AndroidSchedulers.mainThread())
                                .flatMapSingle(integer -> startPurchase(
                                        mInAppBillingService,
                                        type,
                                        sku
                                ));
                    } else {
                        return Observable.error(new IllegalStateException(
                                "RESPONSE_CODE is not OK: " + bundleResponseCodePair.second
                        ));
                    }
                })
                .toSingle();
    }

    public void startPurchase(
            final IntentSender intentSender,
            final BaseActivity activity,
            final int requestCode
    ) {
        try {
            activity.startIntentSenderForResult(
                    intentSender,
                    requestCode,
                    new Intent(),
                    0,
                    0,
                    0,
                    null
            );
        } catch (final IntentSender.SendIntentException e) {
            Timber.wtf(e);
            activity.showError(e);
        }
    }

    public static void startPurchase(
            final BaseActivity activity,
            final IInAppBillingService mInAppBillingService,
            @InappType final String type,
            final String sku
    ) throws RemoteException, IntentSender.SendIntentException {
        final Bundle buyIntentBundle = mInAppBillingService.getBuyIntent(
                API_VERSION_3,
                BaseApplication.getAppInstance().getPackageName(),
                sku,
                type,
                String.valueOf(System.currentTimeMillis())
        );
        final int responseCode = buyIntentBundle.getInt("RESPONSE_CODE");
        if (responseCode == RESULT_OK) {
            final PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            if (pendingIntent != null) {
                final int requestCode = type.equals(InappType.IN_APP) ? REQUEST_CODE_INAPP : REQUEST_CODE_SUBSCRIPTION;
                activity.startIntentSenderForResult(
                        pendingIntent.getIntentSender(),
                        requestCode,
                        new Intent(),
                        0,
                        0,
                        0,
                        null
                );
            } else {
                activity.showError(new NullPointerException("pendingIntent is NULL!!!"));
                Timber.wtf("pendingIntent is NULL!!!");
            }
        } else if (responseCode == RESULT_ITEM_ALREADY_OWNED) {
            //todo check if RESPONSE_CODE is 7 (owned) and consume inapp

        } else {
            activity.showError(new IllegalStateException("RESPONSE_CODE is not OK: " + responseCode));
            Timber.wtf("RESPONSE_CODE is not OK: %s", responseCode);
        }
    }

    public static List<String> getNewSubsSkus() {
        return new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.ver4_skus).split(",")));
    }

    public static List<String> getFreeTrailSubsSkus() {
        return new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.ver4_subs_free_trial).split(",")));
    }

    public static List<String> getNewNoAdsSubsSkus() {
        return new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.ver4_subs_no_ads).split(",")));
    }

    public static List<String> getNewInAppsSkus() {
        return new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.ver4_inapp_skus).split(",")));
    }
}