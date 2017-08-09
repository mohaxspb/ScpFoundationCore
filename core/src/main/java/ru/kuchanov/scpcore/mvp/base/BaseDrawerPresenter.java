package ru.kuchanov.scpcore.mvp.base;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public abstract class BaseDrawerPresenter<V extends DrawerMvp.View>
        extends BaseActivityPresenter<V>
        implements DrawerMvp.Presenter<V> {

    public BaseDrawerPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    public void getRandomArticleUrl() {
        Timber.d("getRandomArticle");
        if (!mApiClient.getConstantValues().getAppLang().equals("ru")) {
            getView().showMessage(R.string.random_article_warning);
        }
        getView().showProgressDialog(true);
        mApiClient.getRandomUrl()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        url -> {
                            getView().showProgressDialog(false);
                            getView().onReceiveRandomUrl(url);
                        },
                        e -> {
                            getView().showProgressDialog(false);
                            getView().showError(e);
                        }
                );
    }

    @Override
    public void onNavigationItemClicked(int id) {
        Timber.d("onNavigationItemClicked: %s", id);
        //nothing to do
    }

    @Override
    public void onAvatarClicked() {
        Timber.d("onAvatarClicked");
        getView().showProgressDialog(R.string.progress_leaderboard);
        mApiClient.getLeaderboard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        leaderBoardResponse -> {
                            Timber.d("getLeaderboard onNext: %s", leaderBoardResponse);
                            getView().dismissProgressDialog();
                            getView().showLeaderboard(leaderBoardResponse);
                        },
                        e -> {
                            Timber.e(e);
                            getView().dismissProgressDialog();
                            getView().showError(e);
                        }
                );
    }
}