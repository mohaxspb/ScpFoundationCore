package ru.kuchanov.scpcore.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.InAppBillingServiceConnectionObservable;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.Item;
import ru.kuchanov.scpcore.monetization.model.Subscription;
import ru.kuchanov.scpcore.monetization.util.InappHelper;
import ru.kuchanov.scpcore.ui.adapter.SubscriptionsRecyclerAdapter;
import ru.kuchanov.scpcore.ui.base.BaseBottomSheetDialogFragment;
import ru.kuchanov.scpcore.util.SecureUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SubscriptionsFragmentDialog
        extends BaseBottomSheetDialogFragment
        implements SubscriptionsRecyclerAdapter.SubscriptionClickListener {

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

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    private IInAppBillingService mInAppBillingService;

    private boolean isDataLoaded;

    public static SubscriptionsFragmentDialog newInstance() {
        return new SubscriptionsFragmentDialog();
    }

    @Override
    protected void callInjection() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_bottom_sheet_subscriptions;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

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
    void onRemoveAdsOneDayClicked() {
        Timber.d("onRemoveAdsFree");
        //TODO move to base activities method
        DialogFragment dialogFragment = FreeAdsDisablingDialogFragment.newInstance();
        dialogFragment.show(getChildFragmentManager(), FreeAdsDisablingDialogFragment.TAG);
        dismiss();
    }

    @OnClick(R2.id.refresh)
    void onRefreshClicked() {
        Timber.d("onRefreshClicked");
        getMarketData();
    }

    private void getMarketData() {
        if (!isAdded()) {
            return;
        }
        mInAppBillingService = getBaseActivity().getIInAppBillingService();

        refresh.setVisibility(View.GONE);
        progressCenter.setVisibility(View.VISIBLE);

        InappHelper.getOwnedSubsObserveble(mInAppBillingService)
                .flatMap(ownedItems -> InappHelper.getSubsListToBuyObserveble(mInAppBillingService)
                        .flatMap(toBuy -> Observable.just(new Pair<>(ownedItems, toBuy))))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ownedItemsAndSubscriptions -> {
                            if (!isAdded()) {
                                return;
                            }
                            Timber.d("items: %s", ownedItemsAndSubscriptions.first);
                            Timber.d("subs: %s", ownedItemsAndSubscriptions.second);

                            if (ownedItemsAndSubscriptions.first.contains(new Item(null, null, getString(R.string.inapp_skus).split(",")[0], null))) {
                                Timber.d("has level inapp, so consume it");
                                InappHelper.consumeInapp(getString(R.string.inapp_skus).split(",")[0], mInAppBillingService)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                result -> {
                                                    Timber.d("consumed result: %s", result);
                                                },
                                                Timber::e
                                        );
                            }

                            isDataLoaded = true;
                            refresh.setVisibility(View.GONE);
                            progressCenter.setVisibility(View.GONE);
                            infoContainer.setVisibility(View.VISIBLE);
//                            if (ownedItemsAndSubscriptions.first.isEmpty()) {
//                                currentSubscriptionValue.setText(getString(R.string.no_subscriptions));
//                            } else {
//                                currentSubscriptionValue.setText(ownedItemsAndSubscriptions.first.get(0).sku);
//                            }
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setHasFixedSize(true);
                            SubscriptionsRecyclerAdapter adapter = new SubscriptionsRecyclerAdapter();
                            adapter.setData(ownedItemsAndSubscriptions.second);
                            adapter.setArticleClickListener(SubscriptionsFragmentDialog.this);
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
            InappHelper.startSubsBuy(this, mInAppBillingService, InappHelper.InappType.SUBS, article.productId);
        } catch (Exception e) {
            Timber.e(e);
            Snackbar.make(mRoot, e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // Do something with your dialog like setContentView() or whatever
        return dialog;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("called in fragment");
        if (requestCode == REQUEST_CODE_SUBSCRIPTION) {
//            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
//            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == Activity.RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    Timber.d("You have bought the %s", sku);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, sku);
//                    bundle.putFloat(FirebaseAnalytics.Param.VALUE, .5f);
                    bundle.putFloat(FirebaseAnalytics.Param.PRICE, .5f);
                    FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);

                    if (SecureUtils.checkCrack(getActivity())) {
                        Bundle args = new Bundle();
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        args.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CRACK_" + sku + ((firebaseUser != null) ? firebaseUser.getUid() : ""));
                        FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, args);
                    }
                } catch (JSONException e) {
                    Timber.e(e, "Failed to parse purchase data.");
                }
                //remove ads item from menu via updating ownedItems list
                getBaseActivity().updateOwnedMarketItems();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}