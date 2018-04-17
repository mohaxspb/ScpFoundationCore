package ru.kuchanov.scpcore.downloads;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProvider;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by mohax on 29.05.2017.
 * <p>
 * for ScpFoundationRu
 */
public abstract class DialogUtils {

    protected MyPreferenceManager mPreferenceManager;

    protected DbProviderFactory mDbProviderFactory;

    protected ApiClient mApiClient;

    protected ConstantValues mConstantValues;

    private final Class clazz;

    public DialogUtils(
            final MyPreferenceManager preferenceManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues,
            final Class clazz
    ) {
        super();
        mPreferenceManager = preferenceManager;
        mDbProviderFactory = dbProviderFactory;
        mApiClient = apiClient;
        mConstantValues = constantValues;
        this.clazz = clazz;
    }

    public void showDownloadDialog(final Context context) {
        final List<DownloadEntry> entries = getDownloadTypesEntries(context);

        final MaterialDialog materialDialog = new MaterialDialog.Builder(context)
                .title(R.string.download_all_title)
                .items(entries)
                .itemsCallbackSingleChoice(-1, (dialog, itemView, which, text) -> {
                    Timber.d("which: %s, text: %s", which, text);
                    if (!isServiceRunning()) {
                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    }
                    return true;
                })
                .alwaysCallSingleChoiceCallback()
                .positiveText(R.string.download)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .onNegative((dialog, which) -> dialog.dismiss())
                .onPositive((dialog, which) -> {
                    Timber.d("dialog.getSelectedIndex(): %s", dialog.getSelectedIndex());

                    DownloadEntry type = entries.get(dialog.getSelectedIndex());

                    logDownloadAttempt(type);

                    Observable<Integer> numOfArticlesObservable;
                    Observable<List<Article>> articlesObservable;
                    if (type.resId == R.string.type_all) {
                        //simply start download all with popup for limit users,
                        //in which tell, that we can't now how many arts he can load
                        numOfArticlesObservable = Observable.just(Integer.MIN_VALUE);
                    } else if (type.resId == R.string.type_archive) {
                        articlesObservable = mApiClient.getMaterialsArchiveArticles();
                        numOfArticlesObservable = articlesObservable.map(List::size);
                    } else if (type.resId == R.string.type_jokes) {
                        articlesObservable = mApiClient.getMaterialsJokesArticles();
                        numOfArticlesObservable = articlesObservable.map(List::size);
                    } else if (type.resId == R.string.type_1
                               || type.resId == R.string.type_2
                               || type.resId == R.string.type_3
                               || type.resId == R.string.type_4
                               || type.resId == R.string.type_ru
                               || type.resId == R.string.type_fr
                               || type.resId == R.string.type_jp
                               || type.resId == R.string.type_es
                               || type.resId == R.string.type_pl
                               || type.resId == R.string.type_de) {
                        articlesObservable = mApiClient.getObjectsArticles(type.url);
                        numOfArticlesObservable = articlesObservable.map(List::size);
                    } else {
                        articlesObservable = mApiClient.getMaterialsArticles(type.url);
                        numOfArticlesObservable = articlesObservable.map(List::size);
                    }
                    loadArticlesAndCountThem(context, numOfArticlesObservable, type);
                    dialog.dismiss();
                })
                .neutralText(R.string.stop_download)
                .onNeutral((dialog, which) -> {
                    Timber.d("onNeutral clicked");
                    DownloadAllService.stopDownload(context, clazz);
                    dialog.dismiss();
                })
                .build();

        materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);

        if (DownloadAllService.isRunning()) {
            materialDialog.getActionButton(DialogAction.NEUTRAL).setEnabled(true);
        } else {
            materialDialog.getActionButton(DialogAction.NEUTRAL).setEnabled(false);
        }

        materialDialog.getRecyclerView().setOverScrollMode(View.OVER_SCROLL_NEVER);

        materialDialog.show();
    }

    private void loadArticlesAndCountThem(
            final Context context,
            final Observable<Integer> countObservable,
            final DownloadEntry type
    ) {
        final MaterialDialog progress = new MaterialDialog.Builder(context)
                .progress(true, 0)
                .content(R.string.download_art_list)
                .cancelable(false)
                .build();

        progress.show();

        countObservable
                .flatMap(numOfArts -> {
                    int limit = mPreferenceManager.getFreeOfflineLimit();
                    int numOfScorePerArt = mPreferenceManager.getScorePerArt();

                    DbProvider dbProvider = mDbProviderFactory.getDbProvider();
                    limit += mDbProviderFactory.getDbProvider().getScore() / numOfScorePerArt;
                    dbProvider.close();
                    return Observable.just(new Pair<>(numOfArts, limit));
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        numOfArtsAndLimit -> {
                            Timber.d("numOfArtsAndLimit: %s/%s", numOfArtsAndLimit.first, numOfArtsAndLimit.second);
                            progress.dismiss();
//                            Timber.d("mPreferenceManager.isHasSubscription(): %s",
//                                    mPreferenceManager.isHasSubscription());
//                            Timber.d("remConf.getBoolean(RemoteConfigKeys.DOWNLOAD_ALL_ENABLED_FOR_FREE): %s",
//                                    mPreferenceManager.isDownloadAllEnabledForFree());
                            final boolean ignoreLimit = mPreferenceManager.isHasSubscription()
                                                        || mPreferenceManager.isDownloadAllEnabledForFree();

                            if (type.resId == R.string.type_all) {
                                if (!ignoreLimit) {
                                    //simply start download all with popup for limit users,
                                    //in which tell, that we can't now how many arts he can load
                                    new MaterialDialog.Builder(context)
                                            .title(R.string.download_all)
                                            .content(context.getString(R.string.download_all_with_limit, numOfArtsAndLimit.second))
                                            .positiveText(R.string.download)
                                            .onPositive((dialog, which) ->
                                                    DownloadAllService.startDownloadWithType(
                                                            context,
                                                            type,
                                                            0,
                                                            numOfArtsAndLimit.second,
                                                            clazz
                                                    )
                                            )
                                            //TODO add increase/remove limit button
                                            .negativeText(android.R.string.cancel)
                                            .build()
                                            .show();
                                } else {
                                    DownloadAllService.startDownloadWithType(
                                            context,
                                            type,
                                            DownloadAllService.RANGE_NONE,
                                            DownloadAllService.RANGE_NONE,
                                            clazz
                                    );
                                }
                            } else {
                                if (numOfArtsAndLimit.first == 1) {
                                    DownloadAllService.startDownloadWithType(
                                            context,
                                            type,
                                            0,
                                            1,
                                            clazz
                                    );
                                } else {
                                    showRangeDialog(context, type, numOfArtsAndLimit.first, numOfArtsAndLimit.second, ignoreLimit);
                                }
                            }
                        },
                        e -> {
                            Timber.e(e);
                            progress.dismiss();
                        }
                );
    }

    public void showRangeDialog(
            final Context context,
            final DownloadEntry type,
            final int numOfArticles,
            final int limit,
            final boolean ignoreLimit
    ) {
        Timber.d(
                "showRangeDialog type/numOfArticles/limit/ignoreLimit: %s/%s/%s/%s",
                type,
                numOfArticles,
                limit,
                ignoreLimit
        );

        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_download_range, false)
                .title(R.string.downlad_art_list_range)
                .cancelable(false)
                .negativeText(android.R.string.cancel)
                .onNegative((dialog1, which) -> dialog1.dismiss())
                .positiveText(R.string.download)
                .build();

        final View view = dialog.getCustomView();
        if (view == null) {
            return;
        }
        final CrystalRangeSeekbar seekbar = view.findViewById(R.id.rangeSeekbar);
        seekbar.setMaxValue(numOfArticles).apply();
        seekbar.setMinStartValue(0).apply();

        if (!ignoreLimit && limit < numOfArticles) {
            seekbar.setMaxStartValue(limit).apply();
        } else {
            seekbar.setMaxStartValue(numOfArticles).apply();
        }

        final TextView min = view.findViewById(R.id.min);
        final TextView max = view.findViewById(R.id.max);
        final TextView userLimit = view.findViewById(R.id.userLimit);
        final TextView articlesSelected = view.findViewById(R.id.articlesSelected);
        final TextView increaseLimit = view.findViewById(R.id.increaseLimit);
        final ImageView info = view.findViewById(R.id.info);

        final boolean isNightMode = mPreferenceManager.isNightMode();
        final int tint = isNightMode ? Color.WHITE : ContextCompat.getColor(context, R.color.zbs_color_red);
        info.setColorFilter(tint);

        final int scorePerArt = mPreferenceManager.getScorePerArt();
        final int freeOfflineLimit = mPreferenceManager.getFreeOfflineLimit();

        final String limitDescriptionText = context.getString(R.string.limit_description_disabled_free_downloads, freeOfflineLimit, scorePerArt);
        info.setOnClickListener(view1 -> new MaterialDialog.Builder(context)
                .title(R.string.info)
                .content(limitDescriptionText)
                .positiveText(android.R.string.ok)
                .show());

        increaseLimit.setVisibility(ignoreLimit ? View.INVISIBLE : View.VISIBLE);
        increaseLimit.setOnClickListener(v -> onIncreaseLimitClick(context));

        userLimit.setText(context.getString(
                R.string.user_limit,
                ignoreLimit ? context.getString(R.string.no_limit) : String.valueOf(limit)
        ));

        seekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            min.setText(String.valueOf(minValue));
            max.setText(String.valueOf(maxValue));

            articlesSelected.setText(context.getString(R.string.selected, maxValue.intValue() - minValue.intValue()));

            dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(v -> {
                final int range = maxValue.intValue() - minValue.intValue();
                if (!ignoreLimit && range > limit) {
                    showFreeTrialOfferDialog(context);
                } else {
                    DownloadAllService.startDownloadWithType(
                            context,
                            type,
                            minValue.intValue(),
                            maxValue.intValue(),
                            clazz
                    );
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    /**
     * realize it in core and extend core realization in app via override only getdownloadtypesEntries
     */
    public abstract void showFreeTrialOfferDialog(Context baseActivity);

    public abstract List<DownloadEntry> getDownloadTypesEntries(Context context);

    protected abstract boolean isServiceRunning();

    /**
     * show dialog with subscriptions
     */
    protected abstract void onIncreaseLimitClick(Context context);

    protected abstract void logDownloadAttempt(DownloadEntry type);
}