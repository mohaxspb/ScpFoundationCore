package ru.kuchanov.scpcore.ui.fragment.monetization;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.Subscription;
import ru.kuchanov.scpcore.monetization.util.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.monetization.SubscriptionsContract;
import ru.kuchanov.scpcore.ui.adapter.SubscriptionsAdapter;
import ru.kuchanov.scpcore.ui.base.BaseBottomSheetDialogFragment;
import ru.kuchanov.scpcore.ui.base.BaseFragment;
import ru.kuchanov.scpcore.ui.base.BaseListFragment;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SubscriptionsFragment
        extends BaseFragment<SubscriptionsContract.View, SubscriptionsContract.View>
        implements SubscriptionsContract.View {

    public static final int REQUEST_CODE_SUBSCRIPTION = 1001;

    @BindView(R2.id.progressCenter)
    ProgressBar progressCenter;
    @BindView(R2.id.refresh)
    View refresh;
    @BindView(R2.id.infoContainer)
    View infoContainer;
    @BindView(R2.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R2.id.info)
    ImageView info;
    @BindView(R2.id.dialogTitle)
    TextView dialogTitle;
    @BindView(R2.id.freeActions)
    TextView freeActions;

    //    @BindView(R2.id.refreshCurrentSubscriptions)
//    ImageView refreshCurrentSubscriptions;
    @BindView(R2.id.currentSubscriptionValue)
    TextView currentSubscriptionValue;

    @Inject
    MyPreferenceManager mMyPreferenceManager;
    @Inject
    InAppHelper mInAppHelper;

    private IInAppBillingService mInAppBillingService;

    private boolean isDataLoaded;

    public static SubscriptionsFragment newInstance() {
        return new SubscriptionsFragment();
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_subscriptions;
    }

    @Override
    protected void initViews() {
        InAppBillingServiceConnectionObservable.getInstance().getServiceStatusObservable()
                .subscribe(connected -> {
                    if (connected && !isDataLoaded) {
                        getMarketData();
                    }
                });

        getMarketData();

        FirebaseRemoteConfig remConf = FirebaseRemoteConfig.getInstance();
        boolean freeDownloadEnabled = remConf.getBoolean(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_ALL_ENABLED_FOR_FREE);

        boolean isNightMode = mMyPreferenceManager.isNightMode();
        int tint = isNightMode ? Color.WHITE : ContextCompat.getColor(getActivity(), R.color.zbs_color_red);
        info.setColorFilter(tint);
        info.setOnClickListener(view -> new MaterialDialog.Builder(getActivity())
                .title(R.string.info)
                .content(freeDownloadEnabled ? R.string.subs_info : R.string.subs_info_disabled_free_downloads)
                .positiveText(android.R.string.ok)
                .show());

        dialogTitle.setText(freeDownloadEnabled
                ? R.string.dialog_title_subscriptions : R.string.dialog_title_subscriptions_disabled_free_downloads);
        freeActions.setText(freeDownloadEnabled
                ? R.string.remove_ads_for_free : R.string.remove_ads_for_free_disabled_free_downloads);
    }

    @OnClick(R2.id.removeAdsOneDay)
    void onRemoveAdsOneDayClick() {
        getBaseActivity().showFreeAdsDisablePopup();
    }

    @OnClick(R2.id.refresh)
    void onRefreshClick() {
        Timber.d("onRefreshClick");
        getMarketData();
    }

    @OnClick(R2.id.refreshCurrentSubscriptions)
    void onRefreshCurrentSubscriptionsClick() {
        Timber.d("onRefreshCurrentSubscriptionsClick");
        getMarketData();
    }

    private void getMarketData() {
        if (!isAdded()) {
            return;
        }
        mInAppBillingService = getBaseActivity().getIInAppBillingService();

        refresh.setVisibility(View.GONE);
        progressCenter.setVisibility(View.VISIBLE);

        List<String> skuList = mInAppHelper.getNewSubsSkus();
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            skuList.addAll(mInAppHelper.getNewNoAdsSubsSkus());
        }

        mInAppHelper.getValidatedOwnedSubsObservable(mInAppBillingService)
                .flatMap(ownedItems -> mInAppHelper.getSubsListToBuyObservable(mInAppBillingService, skuList)
                        .flatMap(toBuy -> Observable.just(new Pair<>(ownedItems, toBuy))))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ownedItemsAndSubscriptions -> {
                            if (!isAdded()) {
                                return;
                            }
//                            Timber.d("itemsOwned: %s", ownedItemsAndSubscriptions.first);
//                            Timber.d("subsToBuy: %s", ownedItemsAndSubscriptions.second);

                            isDataLoaded = true;
                            refresh.setVisibility(View.GONE);
                            progressCenter.setVisibility(View.GONE);
                            infoContainer.setVisibility(View.VISIBLE);

                            //show current subscription
                            @InAppHelper.SubscriptionType
                            int type = mInAppHelper.getSubscriptionTypeFromItemsList(ownedItemsAndSubscriptions.first);
                            switch (type) {
                                case InAppHelper.SubscriptionType.NONE:
                                    currentSubscriptionValue.setText(getString(R.string.no_subscriptions));
                                    break;
                                case InAppHelper.SubscriptionType.NO_ADS:
                                    currentSubscriptionValue.setText(getString(R.string.subscription_no_ads_title));
                                    break;
                                case InAppHelper.SubscriptionType.FULL_VERSION:
                                    currentSubscriptionValue.setText(getString(R.string.subscription_full_version_title));
                                    break;
                                default:
                                    throw new IllegalArgumentException("unexected subs type: " + type);
                            }

                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setHasFixedSize(true);
                            SubscriptionsAdapter adapter = new SubscriptionsAdapter();
                            adapter.setData(ownedItemsAndSubscriptions.second);
                            adapter.setArticleClickListener(SubscriptionsFragment.this);
                            recyclerView.setAdapter(adapter);
                        },
                        e -> {
                            if (!isAdded()) {
                                return;
                            }
                            Timber.e(e, "error getting cur subs");
                            isDataLoaded = false;

                            Snackbar.make(mRoot, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            progressCenter.setVisibility(View.GONE);
                            refresh.setVisibility(View.VISIBLE);
                        }
                );
    }

    @Override
    public void onSubscriptionClicked(Subscription article) {
        try {
            InAppHelper.startSubsBuy(this, mInAppBillingService, InAppHelper.InappType.SUBS, article.productId);
        } catch (Exception e) {
            Timber.e(e);
            Snackbar.make(mRoot, e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("called in fragment");
        if (requestCode == REQUEST_CODE_SUBSCRIPTION) {
            if (data == null) {
                if (isAdded()) {
                    getBaseActivity().showMessageLong("Error while parse result, please try again");
                }
                return;
            }
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
//            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == Activity.RESULT_OK && responseCode == InAppHelper.RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Timber.d("You have bought the %s", sku);

                    //validate subs list
                    getBaseActivity().updateOwnedMarketItems();
                } catch (JSONException e) {
                    Timber.e(e, "Failed to parse purchase data.");
                    getBaseActivity().showError(e);
                }
            } else {
                if (isAdded()) {
                    getBaseActivity().showMessageLong("Error: response code is not \"0\". Please try again");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}