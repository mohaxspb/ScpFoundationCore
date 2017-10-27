package ru.dante.scpfoundation.ui.fragment;

import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.di.AppComponentImpl;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.mvp.contract.Objects1Articles;
import ru.kuchanov.scpcore.ui.fragment.BaseListArticlesWithSearchFragment;
import ru.kuchanov.scpcore.ui.fragment.Objects1ArticlesFragment;

/**
 * Created by mohax on 27.10.2017.
 * <p>
 * for ScpCore
 */
public class ObjectsFrArticlesFragment
        extends BaseListArticlesWithSearchFragment<Objects1Articles.View, Objects1Articles.Presenter>
        implements Objects1Articles.View {

    public static final String TAG = Objects1ArticlesFragment.class.getSimpleName();

    public static Objects1ArticlesFragment newInstance() {
        return new Objects1ArticlesFragment();
    }

    @Override
    protected void callInjections() {
        ((AppComponentImpl) MyApplicationImpl.getAppComponent()).inject(this);
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