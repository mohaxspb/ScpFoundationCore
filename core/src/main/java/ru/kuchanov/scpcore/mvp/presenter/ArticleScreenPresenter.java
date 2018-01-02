package ru.kuchanov.scpcore.mvp.presenter;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.base.BaseDrawerPresenter;
import ru.kuchanov.scpcore.mvp.contract.ArticleScreenMvp;
import ru.kuchanov.scpcore.ui.fragment.ArticleFragment;
import timber.log.Timber;

public class ArticleScreenPresenter
        extends BaseDrawerPresenter<ArticleScreenMvp.View>
        implements ArticleScreenMvp.Presenter {

    public ArticleScreenPresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    protected boolean getUserInConstructor() {
        return false;
    }

    @Override
    public void toggleFavorite(String url) {
        Timber.d("toggleFavorite url: %s", url);
        //TODO seems to that we can move it to ArticlePresenter
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            getView().showNeedLoginPopup();
//            return;
//        }
        mDbProviderFactory.getDbProvider().toggleFavorite(url)
                .flatMap(article1 -> mDbProviderFactory.getDbProvider().setArticleSynced(article1, false))
                .subscribe(
                        article -> {
                            Timber.d("fav state now is: %s", article);
                            updateArticleInFirebase(article, true);
                            ((ArticleFragment.ToolbarStateSetter) getView()).setFavoriteState(article.isInFavorite != Article.ORDER_NONE);
                        },
                        Timber::e
                );
    }
}