package ru.kuchanov.scpcore.ui.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.model.remoteconfig.AppLangVersionsJson;
import ru.kuchanov.scpcore.monetization.model.Subscription;
import ru.kuchanov.scpcore.mvp.base.BaseActivityMvp;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;
import ru.kuchanov.scpcore.ui.adapter.AppLangVersionsAdapter;
import ru.kuchanov.scpcore.util.IntentUtils;
import timber.log.Timber;

/**
 * Created by mohax on 29.05.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DialogUtils {

    private final ConstantValues mConstantValues;

    private MaterialDialog mProgressDialog;

    public DialogUtils(final ConstantValues constantValues) {
        super();
        mConstantValues = constantValues;
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
                .onPositive((dialog1, which) -> {
                    final String linkToMarket = "https://play.google.com/store/apps/details?id="
                                                + version.appPackage
                                                + "&utm_source=scpReader&utm_medium=appLangsVersions&utm_campaign="
                                                + mConstantValues.getAppLang();
                    IntentUtils.openUrl(linkToMarket);
                })
                .build()
                .show();
    }

    public void showAllAppLangVariantsDialog(final Context context) {
        final List<AppLangVersionsJson.AppLangVersion> appLangVersions = new GsonBuilder().create()
                .fromJson(
                        FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.APP_LANG_VERSIONS),
                        AppLangVersionsJson.class
                ).langs;
        final AppLangVersionsAdapter adapter = new AppLangVersionsAdapter(appLangVersions);
        adapter.setCallbacks(position -> {
//            final String linkToMarket = "https://play.google.com/store/apps/details?id="
//                                        + appLangVersions.get(position).appPackage
//                                        + "&utm_source=scpReader&utm_medium=appLangsVersions&utm_campaign="
//                                        + mConstantValues.getAppLang();

            final String linkToMarket = Constants.Urls.LANDING_PAGE;

            IntentUtils.openUrl(linkToMarket);
        });

        new MaterialDialog.Builder(context)
                .title(R.string.menuAppLangVersions)
                .positiveText(R.string.close)
                .items(appLangVersions)
                .adapter(adapter, new LinearLayoutManager(context))
                .alwaysCallSingleChoiceCallback()
                .build()
                .show();
    }

    public void showFreeTrialSubscriptionOfferDialog(
            final BaseActivity<? extends BaseActivityMvp.View, ? extends BaseActivityMvp.Presenter<? extends BaseActivityMvp.View>> baseActivity,
            @NotNull final List<Subscription> subscriptions
    ) {
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
                .onPositive((dialog, which) -> baseActivity.getPresenter().onPurchaseClick(
                        sku,
                        false
                ))
                .negativeText(android.R.string.cancel)
                .onNegative((dialog, which) -> dialog.dismiss())
                .build()
                .show();
    }

    public void showOfferLoginForLevelUpPopup(final BaseActivity baseActivity) {
        new MaterialDialog.Builder(baseActivity)
                .title(R.string.need_login)
                .content(R.string.need_login_for_level_up_content)
                .positiveText(R.string.authorize)
                .onPositive((dialog, which) -> baseActivity.showLoginProvidersPopup())
                .negativeText(android.R.string.cancel)
                .onNegative((dialog, which) -> dialog.dismiss())
                .build()
                .show();
    }

    public void showSelectableTextWarningDialog(
            final Context context,
            final MaterialDialog.SingleButtonCallback positiveCallback,
            final MaterialDialog.SingleButtonCallback negativeCallback
    ) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_selectable_text_title)
                .content(R.string.dialog_selectable_text_content)
                .positiveText(android.R.string.ok)
                .onPositive(positiveCallback)
                .negativeText(android.R.string.cancel)
                .onNegative(negativeCallback)
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
