package ru.kuchanov.scpcore.ui.util;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
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
import ru.kuchanov.scpcore.ui.base.BaseActivity;
import ru.kuchanov.scpcore.util.AttributeGetter;
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

    //TODO think how to restore image dialog Maybe use fragment dialog?..
    public void showImageDialog(Context context, String imgUrl) {
        Timber.d("showImageDialog");
        Dialog nagDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        nagDialog.setCancelable(true);

        ImageView imageView;
        if(imgUrl.endsWith("gif")){
            nagDialog.setContentView(R.layout.dialog_preview_gif);
            imageView = nagDialog.findViewById(R.id.image_view_touch);
        } else {
            nagDialog.setContentView(R.layout.dialog_preview_image);
            imageView = nagDialog.findViewById(R.id.image_view_touch);
            ((PhotoView)imageView).setMaximumScale(5f);
        }

        ProgressBar progressBar = nagDialog.findViewById(R.id.progressCenter);
        progressBar.setVisibility(View.VISIBLE);

        Glide.with(imageView.getContext())
                .load(imgUrl)
                .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);

        nagDialog.show();
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