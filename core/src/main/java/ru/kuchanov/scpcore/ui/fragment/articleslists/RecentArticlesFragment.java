package ru.kuchanov.scpcore.ui.fragment.articleslists;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.mvp.contract.articleslists.RecentArticlesMvp;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class RecentArticlesFragment
        extends BaseArticlesListFragment<RecentArticlesMvp.View, RecentArticlesMvp.Presenter>
        implements RecentArticlesMvp.View {

    public static final String TAG = RecentArticlesFragment.class.getSimpleName();

    public static RecentArticlesFragment newInstance() {
        return new RecentArticlesFragment();
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