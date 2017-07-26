package ru.kuchanov.scpcore.ui.fragment;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.mvp.contract.FavoriteArticlesMvp;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class FavoriteArticlesFragment
        extends BaseListArticlesWithSearchFragment<FavoriteArticlesMvp.View, FavoriteArticlesMvp.Presenter>
        implements FavoriteArticlesMvp.View {

    public static final String TAG = FavoriteArticlesFragment.class.getSimpleName();

    public static FavoriteArticlesFragment newInstance() {
        return new FavoriteArticlesFragment();
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected boolean isShouldShowPopupOnFavoriteClick() {
        return true;
    }

    @Override
    public void resetOnScrollListener() {
        //we do not have paging
    }

    @Override
    protected boolean isSwipeRefreshEnabled() {
        //FIXME as we do not have api for it, we do not need to update list
        //// TODO: 14.05.2017  now we have firebase as cloud storage, so we can update favs from it
        return false;
    }
}