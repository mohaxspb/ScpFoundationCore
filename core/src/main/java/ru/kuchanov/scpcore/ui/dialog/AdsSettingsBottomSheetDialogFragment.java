package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
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
import ru.kuchanov.scpcore.monetization.model.Subscription;
import ru.kuchanov.scpcore.monetization.util.InAppHelper;
import ru.kuchanov.scpcore.ui.adapter.SettingsSpinnerAdapter;
import ru.kuchanov.scpcore.ui.adapter.SettingsSpinnerCardDesignAdapter;
import ru.kuchanov.scpcore.ui.base.BaseBottomSheetDialogFragment;
import ru.kuchanov.scpcore.util.AttributeGetter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

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
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        adsInListsBannerSwitch.setChecked(mMyPreferenceManager.isBannerInArticlesListsEnabled());
        adsInListsNativeSwitch.setChecked(!mMyPreferenceManager.isBannerInArticlesListsEnabled());
        adsInArticleBannerSwitch.setChecked(mMyPreferenceManager.isBannerInArticleEnabled());
        adsInArticleNativeSwitch.setChecked(!mMyPreferenceManager.isBannerInArticleEnabled());

        //todo set text for remove ads price
        List<String> skus = new ArrayList<>();
        String noAdsSku;
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            noAdsSku = mInAppHelper.getNewNoAdsSubsSkus().get(0);
        } else {
            noAdsSku = mInAppHelper.getNewSubsSkus().get(0);
        }
        skus.add(noAdsSku);
        mInAppHelper.getSubsListToBuyObservable(getBaseActivity().getIInAppBillingService(), skus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(subscriptions -> subscriptions.isEmpty() ? Observable.error(new IllegalArgumentException("empty subs list")) : Observable.just(subscriptions))
                .subscribe(
                        subscriptions -> removeAdsForMonth.setText(getString(R.string.remove_ads_for_month, subscriptions.get(0).price)),
                        Timber::e
                );
    }

    @OnCheckedChanged(value = {R2.id.adsInListsBannerSwitch, R2.id.adsInListsNativeSwitch})
    void onBannerInArticlesListsEnabledCheckChangeListener(boolean checked) {
        Timber.d("setBannerInArticlesListsEnabled: %s", checked);
        mMyPreferenceManager.setBannerInArticlesListsEnabled(checked);
    }

    @OnCheckedChanged(value = {R2.id.adsInArticleBannerSwitch, R2.id.adsInArticleNativeSwitch})
    void onBannerInArticleEnabledCheckChangeListener(boolean checked) {
        Timber.d("setBannerInArticleEnabled: %s", checked);
        mMyPreferenceManager.setBannerInArticleEnabled(checked);
    }

    @OnClick(R2.id.removeAdsForMonth)
    void onRemoveAdsForMonthClick() {
        Timber.d("onRemoveAdsForMonthClick");
        String noAdsSku;
        if (FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.NO_ADS_SUBS_ENABLED)) {
            noAdsSku = mInAppHelper.getNewNoAdsSubsSkus().get(0);
        } else {
            noAdsSku = mInAppHelper.getNewSubsSkus().get(0);
        }
        try {
            InAppHelper.startSubsBuy(this, getBaseActivity().getIInAppBillingService(), InAppHelper.InappType.SUBS, noAdsSku);
        } catch (Exception e) {
            Timber.e(e);
            Snackbar.make(mRoot, e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Firebase.Analitics.EventParam.PLACE, Constants.Firebase.Analitics.StartScreen.REMOVE_ADS_SETTINGS);
        FirebaseAnalytics.getInstance(getActivity()).logEvent(Constants.Firebase.Analitics.EventName.SUBSCRIPTIONS_SHOWN, bundle);
    }

    @OnClick(R2.id.removeAdsForFree)
    void onRemoveAdsForFreeClick() {
        Timber.d("onRemoveAdsForFreeClick");
        dismiss();
        DialogFragment subsDF = FreeAdsDisablingDialogFragment.newInstance();
        subsDF.show(getActivity().getSupportFragmentManager(), subsDF.getTag());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = d.findViewById(android.support.design.R.id.design_bottom_sheet);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!isAdded()) {
            return;
        }
        switch (key) {
            //TODO think if we should react on ads settings here
            default:
                //do nothing
                break;
        }
    }
}