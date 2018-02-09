package ru.kuchanov.scpcore.ui.util;

import android.content.Context;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Locale;

import ru.kuchanov.scp.downloads.ApiClientModel;
import ru.kuchanov.scp.downloads.DbProviderFactoryModel;
import ru.kuchanov.scp.downloads.MyPreferenceManagerModel;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.model.remoteconfig.AppLangVersionsJson;
import ru.kuchanov.scpcore.db.model.Article;
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

    private MyPreferenceManagerModel mPreferenceManager;
    private DbProviderFactoryModel mDbProviderFactory;
    private ApiClientModel<Article> mApiClient;

    private MaterialDialog mProgressDialog;

//    public DialogUtils(
//            MyPreferenceManager preferenceManager,
//            DbProviderFactory dbProviderFactory,
//            ApiClient apiClient
//    ) {
//        mPreferenceManager = preferenceManager;
//        mDbProviderFactory = dbProviderFactory;
//        mApiClient = apiClient;
//    }

    public DialogUtils(
            MyPreferenceManagerModel preferenceManager,
            DbProviderFactoryModel dbProviderFactory,
            ApiClientModel<Article> apiClient
    ) {
        mPreferenceManager = preferenceManager;
        mDbProviderFactory = dbProviderFactory;
        mApiClient = apiClient;
    }

    public void showFaqDialog(Context context) {
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

    public void showAppLangVariantsDialog(Context context, AppLangVersionsJson.AppLangVersion version) {
        String langName = new Locale(version.code).getDisplayLanguage();
        new MaterialDialog.Builder(context)
                .content(context.getString(R.string.offer_app_lang_version_content, langName, langName))
                .title(version.title)
                .positiveText(R.string.open_play_market)
                .onPositive((dialog1, which) -> IntentUtils.tryOpenPlayMarket(context, version.appPackage))
                .build()
                .show();
    }

    public void showAllAppLangVariantsDialog(Context context) {
        List<AppLangVersionsJson.AppLangVersion> appLangVersions = new GsonBuilder().create()
                .fromJson(FirebaseRemoteConfig.getInstance()
                                .getString(Constants.Firebase.RemoteConfigKeys.APP_LANG_VERSIONS),
                        AppLangVersionsJson.class).langs;
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

    public void showFreeTrialSubscriptionOfferDialog(BaseActivity baseActivity, int freeTrialDaysCount) {
        new MaterialDialog.Builder(baseActivity)
                .title(R.string.dialog_offer_free_trial_subscription_title)
                .content(baseActivity.getString(R.string.dialog_offer_free_trial_subscription_content, freeTrialDaysCount, freeTrialDaysCount))
                .positiveText(R.string.yes_bliad)
                .onPositive((dialog, which) -> {
                    try {
                        InAppHelper.startSubsBuy(baseActivity, baseActivity.getIInAppBillingService(), InAppHelper.InappType.SUBS, baseActivity.getString(R.string.subs_free_trial).split(",")[0]);
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

    public void showProgressDialog(Context context, String title) {
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

    public void showProgressDialog(Context context, @StringRes int title) {
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