package ru.kuchanov.scpcore.ui.fragment;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.mvp.contract.Objects3Articles;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class Objects3ArticlesFragment
        extends BaseListArticlesWithSearchFragment<Objects3Articles.View, Objects3Articles.Presenter>
        implements Objects3Articles.View {

    public static final String TAG = Objects3ArticlesFragment.class.getSimpleName();

    public static Objects3ArticlesFragment newInstance() {
        return new Objects3ArticlesFragment();
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