package ru.dante.scpfoundation.ui.fragment;

import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.di.AppComponentImpl;
import ru.dante.scpfoundation.mvp.contract.ObjectsFrArticles;
import ru.kuchanov.scpcore.ui.fragment.articleslists.BaseListArticlesWithSearchFragment;

/**
 * Created by mohax on 27.10.2017.
 * <p>
 * for ScpCore
 */
public class ObjectsFrArticlesFragment
        extends BaseListArticlesWithSearchFragment<ObjectsFrArticles.View, ObjectsFrArticles.Presenter>
        implements ObjectsFrArticles.View {

    public static final String TAG = ObjectsFrArticlesFragment.class.getSimpleName();

    public static ObjectsFrArticlesFragment newInstance() {
        return new ObjectsFrArticlesFragment();
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