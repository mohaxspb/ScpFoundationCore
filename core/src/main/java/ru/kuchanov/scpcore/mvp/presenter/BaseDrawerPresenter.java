package ru.kuchanov.scpcore.mvp.presenter;

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
        getView().showProgressDialog(R.string.dialog_random_page_message);
        mApiClient.getRandomUrl()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        url -> {
                            getView().dismissProgressDialog();
                            getView().onReceiveRandomUrl(url);
                        },
                        e -> {
                            getView().dismissProgressDialog();
                            getView().showError(e);
                        }
                );
    }

    @Override
    public void onNavigationItemClicked(int id) {
        Timber.d("onNavigationItemClicked: %s", id);
        //nothing to do
    }
}