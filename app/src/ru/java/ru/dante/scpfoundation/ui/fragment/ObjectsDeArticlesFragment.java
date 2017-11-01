package ru.dante.scpfoundation.ui.fragment;

import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.di.AppComponentImpl;
import ru.dante.scpfoundation.mvp.contract.ObjectsDeArticles;
import ru.dante.scpfoundation.mvp.contract.ObjectsPlArticles;
import ru.kuchanov.scpcore.ui.fragment.BaseListArticlesWithSearchFragment;

/**
 * Created by mohax on 27.10.2017.
 * <p>
 * for ScpCore
 */
public class ObjectsDeArticlesFragment
        extends BaseListArticlesWithSearchFragment<ObjectsDeArticles.View, ObjectsDeArticles.Presenter>
        implements ObjectsDeArticles.View {

    public static final String TAG = ObjectsDeArticlesFragment.class.getSimpleName();

    public static ObjectsDeArticlesFragment newInstance() {
        return new ObjectsDeArticlesFragment();
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