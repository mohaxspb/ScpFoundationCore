package ru.kuchanov.scpcore.ui.util;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.downloads.DownloadEntry;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.service.DownloadAllServiceDefault;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;
import ru.kuchanov.scpcore.ui.activity.SubscriptionsActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by mohax on 01.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DialogUtilsDefault extends ru.kuchanov.scpcore.downloads.DialogUtils {

    public DialogUtilsDefault(
            final MyPreferenceManager preferenceManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues,
            final Class clazz
    ) {
        super(preferenceManager, dbProviderFactory, apiClient, constantValues, clazz);
    }

    @Override
    public List<DownloadEntry> getDownloadTypesEntries(final Context context) {
        final List<DownloadEntry> downloadEntries = new ArrayList<>();

        downloadEntries.add(new DownloadEntry(R.string.type_1, context.getString(R.string.type_1), Constants.Urls.OBJECTS_1, Article.FIELD_IS_IN_OBJECTS_1));
        downloadEntries.add(new DownloadEntry(R.string.type_2, context.getString(R.string.type_2), Constants.Urls.OBJECTS_2, Article.FIELD_IS_IN_OBJECTS_2));
        downloadEntries.add(new DownloadEntry(R.string.type_3, context.getString(R.string.type_3), Constants.Urls.OBJECTS_3, Article.FIELD_IS_IN_OBJECTS_3));
        downloadEntries.add(new DownloadEntry(R.string.type_4, context.getString(R.string.type_4), Constants.Urls.OBJECTS_4, Article.FIELD_IS_IN_OBJECTS_4));
        downloadEntries.add(new DownloadEntry(R.string.type_ru, context.getString(R.string.type_ru), Constants.Urls.OBJECTS_RU, Article.FIELD_IS_IN_OBJECTS_RU));

        downloadEntries.add(new DownloadEntry(R.string.type_experiments, context.getString(R.string.type_experiments), Constants.Urls.EXPERIMENTS, Article.FIELD_IS_IN_EXPERIMETS));
        downloadEntries.add(new DownloadEntry(R.string.type_incidents, context.getString(R.string.type_incidents), Constants.Urls.INCEDENTS, Article.FIELD_IS_IN_INCIDENTS));
        downloadEntries.add(new DownloadEntry(R.string.type_interviews, context.getString(R.string.type_interviews), Constants.Urls.INTERVIEWS, Article.FIELD_IS_IN_INTERVIEWS));
        downloadEntries.add(new DownloadEntry(R.string.type_jokes, context.getString(R.string.type_jokes), Constants.Urls.JOKES, Article.FIELD_IS_IN_JOKES));
        downloadEntries.add(new DownloadEntry(R.string.type_archive, context.getString(R.string.type_archive), Constants.Urls.ARCHIVE, Article.FIELD_IS_IN_ARCHIVE));
        downloadEntries.add(new DownloadEntry(R.string.type_other, context.getString(R.string.type_other), Constants.Urls.OTHERS, Article.FIELD_IS_IN_OTHER));

        downloadEntries.add(new DownloadEntry(R.string.type_all, context.getString(R.string.type_all), Constants.Urls.NEW_ARTICLES, Article.FIELD_IS_IN_RECENT));
        return downloadEntries;
    }

    @Override
    protected boolean isServiceRunning() {
        return DownloadAllServiceDefault.isRunning();
    }

    @Override
    protected void onIncreaseLimitClick(final Context context) {
        final Bundle bundle = new Bundle();
        bundle.putString(Constants.Firebase.Analitics.EventParam.PLACE, Constants.Firebase.Analitics.StartScreen.DOWNLOAD_DIALOG);
        FirebaseAnalytics.getInstance(context).logEvent(Constants.Firebase.Analitics.EventName.SUBSCRIPTIONS_SHOWN, bundle);

        SubscriptionsActivity.start(context);
    }

    @Override
    protected void logDownloadAttempt(final DownloadEntry type) {
        Timber.d("logDownloadAttempt: %s", type);

        final Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type.name);
        FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(Constants.Firebase.Analitics.EventName.MASS_DOWNLOAD, bundle);
    }

    @Override
    public void showFreeTrialOfferDialog(final Context context) {
        final Bundle bundle = new Bundle();
        bundle.putString(Constants.Firebase.Analitics.EventParam.PLACE,
                Constants.Firebase.Analitics.EventValue.DOWNLOAD_RANGE);
        FirebaseAnalytics.getInstance(context)
                .logEvent(Constants.Firebase.Analitics.EventName.FREE_TRIAL_OFFER_SHOWN, bundle);

        final BaseActivity baseActivity = (BaseActivity) context;
        final DialogUtils dialogUtils = new DialogUtils(mPreferenceManager, mDbProviderFactory, mApiClient, mConstantValues);
        dialogUtils.showProgressDialog(context, R.string.wait);
        final InAppHelper mInAppHelper = new InAppHelper(mPreferenceManager, mDbProviderFactory, mApiClient);
        mInAppHelper.getSubsListToBuyObservable(baseActivity.getIInAppBillingService(), InAppHelper.getFreeTrailSubsSkus())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        subscriptions -> {
                            dialogUtils.dismissProgressDialog();
                            dialogUtils.showFreeTrialSubscriptionOfferDialog(baseActivity, subscriptions);
                        },
                        e -> {
                            Timber.e(e);
                            dialogUtils.dismissProgressDialog();
                            baseActivity.showError(e);
                        }
                );
    }
}