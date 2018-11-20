package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyNotificationManager;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.ui.activity.SubscriptionsActivity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public class AdsSettingsBottomSheetDialogFragment
        extends BaseBottomSheetDialogFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @Inject
    MyNotificationManager mMyNotificationManager;

    @Inject
    InAppHelper mInAppHelper;

    //ads
    @BindView(R2.id.adsInListsBannerSwitch)
    SwitchCompat adsInListsBannerSwitch;

    @BindView(R2.id.adsInListsNativeSwitch)
    SwitchCompat adsInListsNativeSwitch;

    @BindView(R2.id.adsInArticleBannerSwitch)
    SwitchCompat adsInArticleBannerSwitch;

    @BindView(R2.id.adsInArticleNativeSwitch)
    SwitchCompat adsInArticleNativeSwitch;

    @BindView(R2.id.adsRemovedTextView)
    View adsRemovedTextView;

    @BindView(R2.id.adsRemoveActionsView)
    View adsRemoveActionsView;

    @BindView(R2.id.removeAdsForMonth)
    TextView removeAdsForMonth;

    @BindView(R2.id.removeAdsForFree)
    TextView removeAdsForFree;

    public static BottomSheetDialogFragment newInstance() {
        return new AdsSettingsBottomSheetDialogFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.ads_settings;
    }

    @Override
    protected void callInjection() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public void setupDialog(final Dialog dialog, final int style) {
        super.setupDialog(dialog, style);

        adsInListsBannerSwitch.setChecked(mMyPreferenceManager.isBannerInArticlesListsEnabled());
        adsInListsNativeSwitch.setChecked(!mMyPreferenceManager.isBannerInArticlesListsEnabled());
        adsInArticleBannerSwitch.setChecked(mMyPreferenceManager.isBannerInArticleEnabled());
        adsInArticleNativeSwitch.setChecked(!mMyPreferenceManager.isBannerInArticleEnabled());

        //set text for remove ads price
        final String noAdsSku;
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            noAdsSku = InAppHelper.getNewNoAdsSubsSkus().get(0);
        } else {
            noAdsSku = InAppHelper.getNewSubsSkus().get(0);
        }
        final List<String> skus = new ArrayList<>();
        skus.add(noAdsSku);
        mInAppHelper.getSubsListToBuyObservable(getBaseActivity().getIInAppBillingService(), skus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(subscriptions -> subscriptions.isEmpty() ? Observable.error(new IllegalArgumentException("empty subs list")) : Observable.just(subscriptions))
                .subscribe(
                        subscriptions -> removeAdsForMonth.setText(Html.fromHtml(getString(R.string.remove_ads_for_month, "<br/><b><font color = #4CAF50>" + subscriptions.get(0).price + "</font></b>"))),
                        Timber::e
                );

        removeAdsForFree.setText(Html.fromHtml(getString(R.string.remove_ads_for_free_for_hours)));

        setRemoveAdsViewVisibility();
    }

    private void setRemoveAdsViewVisibility() {
        final boolean hasAnySubscription = mMyPreferenceManager.isHasAnySubscription();

        adsRemovedTextView.setVisibility(hasAnySubscription ? View.VISIBLE : View.INVISIBLE);
        adsRemoveActionsView.setVisibility(hasAnySubscription ? View.INVISIBLE : View.VISIBLE);
    }

    @OnCheckedChanged(R2.id.adsInListsBannerSwitch)
    void onBannerInArticlesListsEnabledCheckChangeListener(final boolean checked) {
        Timber.d("setBannerInArticlesListsEnabled: %s", checked);
        mMyPreferenceManager.setBannerInArticlesListsEnabled(checked);
        adsInListsNativeSwitch.setChecked(!checked);
    }

    @OnCheckedChanged(R2.id.adsInListsNativeSwitch)
    void onNativeInArticlesListsEnabledCheckChangeListener(final boolean checked) {
        Timber.d("setNativeInArticlesListsEnabled: %s", checked);
        mMyPreferenceManager.setBannerInArticlesListsEnabled(!checked);
        adsInListsBannerSwitch.setChecked(!checked);
    }

    @OnCheckedChanged(R2.id.adsInArticleBannerSwitch)
    void onBannerInArticleEnabledCheckChangeListener(final boolean checked) {
        Timber.d("setBannerInArticleEnabled: %s", checked);
        mMyPreferenceManager.setBannerInArticleEnabled(checked);
        adsInArticleNativeSwitch.setChecked(!checked);
    }

    @OnCheckedChanged(R2.id.adsInArticleNativeSwitch)
    void onNativeInArticleEnabledCheckChangeListener(final boolean checked) {
        Timber.d("setNativeInArticleEnabled: %s", checked);
        mMyPreferenceManager.setBannerInArticleEnabled(!checked);
        adsInArticleBannerSwitch.setChecked(!checked);
    }

    @OnClick(R2.id.removeAdsForMonth)
    void onRemoveAdsForMonthClick() {
        Timber.d("onRemoveAdsForMonthClick");
        final String noAdsSku;
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            noAdsSku = InAppHelper.getNewNoAdsSubsSkus().get(0);
        } else {
            noAdsSku = InAppHelper.getNewSubsSkus().get(0);
        }

        getBaseActivity().createPresenter().onPurchaseClick(noAdsSku, getBaseActivity(), true);

        final Bundle bundle = new Bundle();
        bundle.putString(Constants.Firebase.Analitics.EventParam.PLACE, Constants.Firebase.Analitics.StartScreen.REMOVE_ADS_SETTINGS);
        FirebaseAnalytics.getInstance(getBaseActivity()).logEvent(Constants.Firebase.Analitics.EventName.SUBSCRIPTIONS_SHOWN, bundle);
    }

    @OnClick(R2.id.removeAdsForFree)
    void onRemoveAdsForFreeClick() {
        Timber.d("onRemoveAdsForFreeClick");
        dismiss();
        SubscriptionsActivity.start(getBaseActivity(), SubscriptionsActivity.TYPE_DISABLE_ADS_FOR_FREE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            final BottomSheetDialog d = (BottomSheetDialog) dialog1;

            final FrameLayout bottomSheet = d.findViewById(android.support.design.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // Do something with your dialog like setContentView() or whatever
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (!isAdded()) {
            return;
        }
        switch (key) {
            case MyPreferenceManager.Keys.HAS_NO_ADS_SUBSCRIPTION:
            case MyPreferenceManager.Keys.HAS_SUBSCRIPTION:
                setRemoveAdsViewVisibility();
                break;
            default:
                //do nothing
                break;
        }
    }
}