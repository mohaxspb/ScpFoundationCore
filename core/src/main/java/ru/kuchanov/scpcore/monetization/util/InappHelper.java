package ru.kuchanov.scpcore.monetization.util;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
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
import rx.Observable;
import timber.log.Timber;

import static ru.kuchanov.scpcore.ui.dialog.SubscriptionsFragmentDialog.REQUEST_CODE_SUBSCRIPTION;

/**
 * Created by mohax on 02.02.2017.
 * <p>
 * for scp_ru
 */
public class InappHelper {

    private final static int API_VERSION = 3;

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

    private ApiClient mApiClient;
    private MyPreferenceManager mMyPreferenceManager;
    private DbProviderFactory mDbProviderFactory;

    public InappHelper(MyPreferenceManager preferenceManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        mMyPreferenceManager = preferenceManager;
        mDbProviderFactory = dbProviderFactory;
        mApiClient = apiClient;
    }

    @SubscriptionType
    public int getSubscriptionTypeFromItemsList(@NonNull List<Item> ownedItems) {
        @SubscriptionType
        int type;

        List<String> fullVersionSkus = new ArrayList<>(Arrays.asList(BaseApplication.getAppInstance().getString(R.string.old_skus).split(",")));
        Collections.addAll(fullVersionSkus, BaseApplication.getAppInstance().getString(R.string.ver_2_skus).split(","));

        List<String> noAdsSkus = new ArrayList<>();
        noAdsSkus.add(BaseApplication.getAppInstance().getString(R.string.subs_no_ads_old));
        noAdsSkus.add(BaseApplication.getAppInstance().getString(R.string.subs_no_ads_ver_2));

        List<String> ownedSkus = getSkuListFromItemsList(ownedItems);
        noAdsSkus.retainAll(ownedSkus);
        fullVersionSkus.retainAll(ownedSkus);

        type = !fullVersionSkus.isEmpty()
                ? SubscriptionType.FULL_VERSION : !noAdsSkus.isEmpty() ? SubscriptionType.NO_ADS : SubscriptionType.NONE;

        return type;
    }

    private List<String> getSkuListFromItemsList(@NonNull List<Item> ownedItems) {
        List<String> skus = new ArrayList<>();
        for (Item item : ownedItems) {
            skus.add(item.sku);
        }
        return skus;
    }

    public Observable<List<Item>> getOwnedSubsObserveble(
            IInAppBillingService mInAppBillingService,
            boolean forceInvalidation
    ) {
        return Observable.<List<Item>>unsafeCreate(subscriber -> {
            try {
                Bundle ownedItemsBundle = mInAppBillingService.getPurchases(API_VERSION, BaseApplication.getAppInstance().getPackageName(), "subs", null);

                Timber.d("ownedItems bundle: %s", ownedItemsBundle);
                if (ownedItemsBundle.getInt("RESPONSE_CODE") == 0) {
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
                .map(items -> {
                    if (forceInvalidation) {
                        List<Item> validatedItems = new ArrayList<>();
                        String packageName = BaseApplication.getAppInstance().getPackageName();
                        for (Item item : items) {
                            Timber.d("validate item: %s", item.sku);
                            try {
                                PurchaseValidateResponse purchaseValidateResponse = null;
                                purchaseValidateResponse = mApiClient.validatePurchaseSync(false, packageName, item.sku, item.purchaseData.purchaseToken);

                                @PurchaseValidateResponse.PurchaseValidationStatus
                                int status = purchaseValidateResponse.getStatus();
                                Timber.d("PurchaseValidationStatus: %s", status);
                                switch (status) {
                                    case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_VALID:
                                        Timber.d("Item successfully validated: %s", item.sku);
                                        validatedItems.add(item);
                                        break;
                                    case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_INVALID:
                                        Timber.e("Item validation INVALID: %s", item.sku);
                                        break;
                                    case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_GOOGLE_SERVER_ERROR:
                                        Timber.e("Item validation failed on google side: %s", item.sku);
                                        break;
                                    default:
                                        Timber.e("Unexpected validation status: %s", status);
                                        break;
                                }
                            } catch (IOException e) {
                                Timber.e(e, "failed validation request to vps server");
                            }
                        }
                        return validatedItems;
                    } else {
                        return items;
                    }
                });
        //todo add server check here every 6 hours
    }

    public Observable<List<Item>> getOwnedInappsObserveble(IInAppBillingService mInAppBillingService) {
        return Observable.unsafeCreate(subscriber -> {
            try {
                Bundle ownedItemsBundle = mInAppBillingService.getPurchases(API_VERSION, BaseApplication.getAppInstance().getPackageName(), "inapp", null);

                Timber.d("ownedItems bundle: %s", ownedItemsBundle);
                if (ownedItemsBundle.getInt("RESPONSE_CODE") == 0) {
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
                        Timber.d("ownedItemsList: %s", ownedItemsList);
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
        });
    }

    public Observable<List<Subscription>> getSubsListToBuyObserveble(IInAppBillingService mInAppBillingService) {
        return Observable.unsafeCreate(subscriber -> {
            try {
                //get all subs detailed info
                List<Subscription> allSubscriptions = new ArrayList<>();
                List<String> skuList = new ArrayList<>();
                //get it from build config
                Collections.addAll(skuList, BaseApplication.getAppInstance().getString(R.string.ver_2_skus).split(","));
                if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
                    skuList.add(BaseApplication.getAppInstance().getString(R.string.subs_no_ads_ver_2));
                }
                Timber.d("skuList: %s", skuList);

                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", (ArrayList<String>) skuList);
                Bundle skuDetails = mInAppBillingService.getSkuDetails(API_VERSION, BaseApplication.getAppInstance().getPackageName(), "subs", querySkus);
                Timber.d("skuDetails: %s", skuDetails);
                if (skuDetails.getInt("RESPONSE_CODE") == 0) {
                    List<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                    if (responseList == null) {
                        subscriber.onError(new IllegalStateException("responseList is null while get subs details"));
                        return;
                    }

                    for (String thisResponse : responseList) {
//                            Timber.d(thisResponse);
                        JSONObject object = new JSONObject(thisResponse);
                        String sku = object.getString("productId");
                        String price = object.getString("price");
                        String title = object.getString("title");
                        allSubscriptions.add(new Subscription(sku, price, title));
                    }
                    Collections.sort(allSubscriptions, Subscription.COMPARATOR_PRICE);

                    subscriber.onNext(allSubscriptions);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle.getInt(\"RESPONSE_CODE\") is not 0"));
                }
            } catch (RemoteException | JSONException e) {
                Timber.e(e);
                subscriber.onError(e);
            }
        });
    }

    public Observable<List<Subscription>> getInappsListToBuyObserveble(IInAppBillingService mInAppBillingService) {
        return Observable.unsafeCreate(subscriber -> {
            try {
                //get all subs detailed info
                List<Subscription> allSubscriptions = new ArrayList<>();
                List<String> skuList = new ArrayList<>();
                //get it from build config
//                Collections.addAll(skuList, BuildConfig.INAPP_SKUS);
                Collections.addAll(skuList, BaseApplication.getAppInstance().getString(R.string.inapp_skus).split(","));
                Timber.d("skuList: %s", skuList);

                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", (ArrayList<String>) skuList);
                Bundle skuDetails = mInAppBillingService.getSkuDetails(API_VERSION, BaseApplication.getAppInstance().getPackageName(), "inapp", querySkus);
                Timber.d("skuDetails: %s", skuDetails);
                if (skuDetails.getInt("RESPONSE_CODE") == 0) {
                    List<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                    if (responseList == null) {
                        subscriber.onError(new IllegalStateException("responseList is null while get subs details"));
                        return;
                    }

                    for (String thisResponse : responseList) {
                        Timber.d(thisResponse);
                        JSONObject object = new JSONObject(thisResponse);
                        String sku = object.getString("productId");
                        String price = object.getString("price");
                        String title = object.getString("title");
                        allSubscriptions.add(new Subscription(sku, price, title));
                    }
                    Collections.sort(allSubscriptions, Subscription.COMPARATOR_PRICE);

                    subscriber.onNext(allSubscriptions);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("ownedItemsBundle.getInt(\"RESPONSE_CODE\") is not 0"));
                }
            } catch (RemoteException | JSONException e) {
                subscriber.onError(e);
            }
        });
    }

//    public static Observable<Integer> consumeInapp(
//            String token,
//            IInAppBillingService mInAppBillingService
//    ) {
//        return Observable.unsafeCreate(subscriber -> {
//            try {
//                int response = mInAppBillingService.consumePurchase(API_VERSION, BaseApplication.getAppInstance().getPackageName(), token);
//                subscriber.onNext(response);
//                subscriber.onCompleted();
//            } catch (RemoteException e) {
//                subscriber.onError(e);
//            }
//        });
//    }

    public Observable<Integer> consumeInapp(
            String sku,
            String token,
            IInAppBillingService mInAppBillingService
    ) {
        String packageName = BaseApplication.getAppInstance().getPackageName();

        return mApiClient.validatePurchase(false, packageName, sku, token)
                .flatMap(purchaseValidateResponse -> {
                    @PurchaseValidateResponse.PurchaseValidationStatus
                    int status = purchaseValidateResponse.getStatus();
                    Timber.d("PurchaseValidationStatus: %s", status);
                    switch (status) {
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_VALID:
                            try {
                                int response = mInAppBillingService.consumePurchase(API_VERSION, packageName, token);
                                return Observable.just(response);
                            } catch (RemoteException e) {
                                return Observable.error(e);
                            }
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_INVALID:
                            return Observable.error(new IllegalStateException("Purchase state is INVALID"));
                        case PurchaseValidateResponse.PurchaseValidationStatus.STATUS_GOOGLE_SERVER_ERROR:
                            return Observable.error(new IllegalStateException("Purchase state cant be validated, as Google Servers sends error"));
                        default:
                            return Observable.error(new IllegalArgumentException("Unexpected validation status: " + status));
                    }
                });
    }

    public static void startSubsBuy(
            Fragment fragment,
            IInAppBillingService mInAppBillingService,
            @InappType String type,
            String sku
    ) throws RemoteException, IntentSender.SendIntentException {
        Bundle buyIntentBundle = mInAppBillingService.getBuyIntent(
                API_VERSION,
                BaseApplication.getAppInstance().getPackageName(),
                sku,
                type,
                String.valueOf(System.currentTimeMillis())
        );
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        if (pendingIntent != null) {
            fragment.startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_SUBSCRIPTION, new Intent(), 0, 0, 0, null);
        }
    }

    public static void startSubsBuy(
            FragmentActivity activity,
            IInAppBillingService mInAppBillingService,
            @InappType String type,
            String sku
    ) throws RemoteException, IntentSender.SendIntentException {
        Bundle buyIntentBundle = mInAppBillingService.getBuyIntent(
                API_VERSION,
                BaseApplication.getAppInstance().getPackageName(),
                sku,
                type,
                String.valueOf(System.currentTimeMillis())
        );
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        if (pendingIntent != null) {
            activity.startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_SUBSCRIPTION, new Intent(), 0, 0, 0, null);
        }
    }
}