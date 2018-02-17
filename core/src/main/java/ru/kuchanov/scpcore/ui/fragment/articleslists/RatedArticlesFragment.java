package ru.kuchanov.scpcore.ui.fragment.articleslists;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.mvp.contract.articleslists.RatedArticlesMvp;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class RatedArticlesFragment
        extends BaseArticlesListFragment<RatedArticlesMvp.View, RatedArticlesMvp.Presenter>
        implements RatedArticlesMvp.View {

    public static final String TAG = RatedArticlesFragment.class.getSimpleName();

    public static RatedArticlesFragment newInstance() {
        return new RatedArticlesFragment();
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected boolean isSwipeRefreshEnabled() {
        return true;
    }

    @Override
    protected boolean isHasOptionsMenu() {
        return false;
    }
}