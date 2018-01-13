package ru.kuchanov.scpcore.ui.fragment.articleslists;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects1Articles;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class Objects1ArticlesFragment
        extends BaseListArticlesWithSearchFragment<Objects1Articles.View, Objects1Articles.Presenter>
        implements Objects1Articles.View {

    public static final String TAG = Objects1ArticlesFragment.class.getSimpleName();

    public static Objects1ArticlesFragment newInstance() {
        return new Objects1ArticlesFragment();
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public void resetOnScrollListener() {
        //we do not have paging
    }

    @Override
    protected boolean shouldUpdateThisListOnLaunch() {
        return false;
    }
}