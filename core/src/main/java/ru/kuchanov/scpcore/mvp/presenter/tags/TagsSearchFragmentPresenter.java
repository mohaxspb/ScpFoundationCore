package ru.kuchanov.scpcore.mvp.presenter.tags;

import java.util.List;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.base.BasePresenter;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsSearchMvp;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class TagsSearchFragmentPresenter
        extends BasePresenter<TagsSearchMvp.View>
        implements TagsSearchMvp.Presenter {

    private List<ArticleTag> mTags;

    private boolean alreadyRefreshFromApi;

    public TagsSearchFragmentPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    public void getTagsFromApi() {
        Timber.d("getTagsFromApi");

        getView().showSwipeProgress(true);

        mApiClient.getTagsFromSite()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(data -> mDbProviderFactory.getDbProvider().saveArticleTags(data))
                .subscribe(
                        data -> {
                            Timber.d("getTagsFromApi onNext: %s", data.size());
                            alreadyRefreshFromApi = true;
                            getView().showSwipeProgress(false);
                        },
                        e -> {
                            Timber.e(e);
                            alreadyRefreshFromApi = true;
                            getView().showSwipeProgress(false);
                            getView().showError(e);
                        }
                );
    }

    @Override
    public void getTagsFromDb() {
        Timber.d("getTagsFromDb");
        mDbProviderFactory.getDbProvider().getArticleTagsAsync()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tags -> {
                            Timber.d("getTagsFromDb onNext: %s", tags.size());
                            mTags = tags;
                            getView().showAllTags(mTags);
                            if (mTags.isEmpty() || !alreadyRefreshFromApi) {
                                getTagsFromApi();
                            }
                        },
                        e -> {
                            Timber.e(e);
                            getView().showError(e);
                        }
                );
    }

    @Override
    public List<ArticleTag> getTags() {
        return mTags;
    }

    @Override
    public void searchByTags(List<ArticleTag> tags) {
        Timber.d("searchByTags: %s", tags);

        getView().showProgress(true);

        mApiClient.getArticlesByTags(tags)
                .flatMap(articles -> mDbProviderFactory.getDbProvider().saveMultipleArticlesWithoutTextSync(articles))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        articles -> {
                            Timber.d("tagsSearchResponse: %s", articles);

                            getView().showProgress(false);

                            if (articles.isEmpty()) {
                                getView().showMessage(R.string.error_no_search_results);
                            } else {
                                getView().showSearchResults(articles);
                            }
                        },
                        e -> {
                            Timber.e(e);
                            getView().showProgress(false);
                        }
                );
    }
}