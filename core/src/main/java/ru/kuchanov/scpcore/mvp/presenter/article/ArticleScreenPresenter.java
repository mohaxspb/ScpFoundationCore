package ru.kuchanov.scpcore.mvp.presenter.article;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.presenter.BaseDrawerPresenter;
import ru.kuchanov.scpcore.mvp.contract.article.ArticleScreenMvp;
import ru.kuchanov.scpcore.ui.fragment.article.ArticleFragment;
import timber.log.Timber;

public class ArticleScreenPresenter
        extends BaseDrawerPresenter<ArticleScreenMvp.View>
        implements ArticleScreenMvp.Presenter {

    public ArticleScreenPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    protected boolean getUserInConstructor() {
        return false;
    }

    @Override
    public void toggleFavorite(final String url) {
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