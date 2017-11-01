package ru.dante.scpfoundation.ui.fragment;

import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.di.AppComponentImpl;
import ru.dante.scpfoundation.mvp.contract.ObjectsEsArticles;
import ru.dante.scpfoundation.mvp.contract.ObjectsPlArticles;
import ru.kuchanov.scpcore.ui.fragment.BaseListArticlesWithSearchFragment;

/**
 * Created by mohax on 27.10.2017.
 * <p>
 * for ScpCore
 */
public class ObjectsPlArticlesFragment
        extends BaseListArticlesWithSearchFragment<ObjectsPlArticles.View, ObjectsPlArticles.Presenter>
        implements ObjectsPlArticles.View {

    public static final String TAG = ObjectsPlArticlesFragment.class.getSimpleName();

    public static ObjectsPlArticlesFragment newInstance() {
        return new ObjectsPlArticlesFragment();
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