package ru.kuchanov.scpcore.ui.util;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.GsonBuilder;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jetbrains.annotations.NotNull;

import android.content.Context;
import android.support.annotation.StringRes;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.api.model.remoteconfig.AppLangVersionsJson;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.Subscription;
import ru.kuchanov.scpcore.monetization.util.InAppHelper;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;
import ru.kuchanov.scpcore.util.IntentUtils;
import timber.log.Timber;

/**
 * Created by mohax on 29.05.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DialogUtils {

    private final MyPreferenceManager mPreferenceManager;

    private final DbProviderFactory mDbProviderFactory;

    private final ApiClient mApiClient;

    private MaterialDialog mProgressDialog;

    public DialogUtils(
            final MyPreferenceManager preferenceManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient
    ) {
        super();
        mPreferenceManager = preferenceManager;
        mDbProviderFactory = dbProviderFactory;
        mApiClient = apiClient;
    }

    public void showFaqDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.faq)
                .positiveText(R.string.close)
                .items(R.array.fag_items)
                .alwaysCallSingleChoiceCallback()
                .itemsCallback((dialog, itemView, position, text) -> {
                    Timber.d("itemsCallback: %s", text);
                    new MaterialDialog.Builder(context)
                            .title(text)
                            .content(context.getResources().getStringArray(R.array.fag_items_content)[position])
                            .positiveText(R.string.close)
                            .build()
                            .show();
                })
                .build()
                .show();
    }

    public void showAppLangVariantsDialog(final Context context, final AppLangVersionsJson.AppLangVersion version) {
        final String langName = new Locale(version.code).getDisplayLanguage();
        new MaterialDialog.Builder(context)
                .content(context.getString(R.string.offer_app_lang_version_content, langName, langName))
                .title(version.title)
                .positiveText(R.string.open_play_market)
                .onPositive((dialog1, which) -> IntentUtils.tryOpenPlayMarket(context, version.appPackage))
                .build()
                .show();
    }

    public void showAllAppLangVariantsDialog(final Context context) {
        final List<AppLangVersionsJson.AppLangVersion> appLangVersions = new GsonBuilder().create()
                .fromJson(
                        FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.APP_LANG_VERSIONS),
                        AppLangVersionsJson.class
                ).langs;
        new MaterialDialog.Builder(context)
                .title(R.string.menuAppLangVersions)
                .positiveText(R.string.close)
                .items(appLangVersions)
                .alwaysCallSingleChoiceCallback()
                .itemsCallback((dialog, itemView, position, text) ->
                        IntentUtils.tryOpenPlayMarket(context, appLangVersions.get(position).appPackage))
                .build()
                .show();
    }

    public void showFreeTrialSubscriptionOfferDialog(final BaseActivity baseActivity, @NotNull final List<Subscription> subscriptions) {
        final boolean trialForYearEnabled = FirebaseRemoteConfig.getInstance()
                .getBoolean(Constants.Firebase.RemoteConfigKeys.OFFER_TRIAL_FOR_YEAR);
        Collections.sort(subscriptions, Subscription.COMPARATOR_MONTH);
        final Subscription subscription = subscriptions.get(trialForYearEnabled ? subscriptions.size() - 1 : 0);
        final String sku = subscription.productId;
        final int freeTrialDays = subscription.freeTrialPeriodInDays();
        new MaterialDialog.Builder(baseActivity)
                .title(R.string.dialog_offer_free_trial_subscription_title)
                .content(baseActivity.getString(R.string.dialog_offer_free_trial_subscription_content, freeTrialDays, freeTrialDays))
                .positiveText(R.string.yes_bliad)
                .onPositive((dialog, which) -> {
                    try {
                        InAppHelper.startSubsBuy(
                                baseActivity,
                                baseActivity.getIInAppBillingService(),
                                InAppHelper.InappType.SUBS,
                                sku
                        );
                    } catch (Exception e) {
                        Timber.e(e);
                        baseActivity.showError(e);
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative((dialog, which) -> dialog.dismiss())
                .build()
                .show();
    }

    public void showProgressDialog(final Context context, final String title) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        mProgressDialog = new MaterialDialog.Builder(context)
                .progress(true, 0)
                .content(title)
                .cancelable(false)
                .build();
        mProgressDialog.show();
    }

    public void showProgressDialog(final Context context, @StringRes final int title) {
        showProgressDialog(context, context.getString(title));
    }

    public void dismissProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }
}