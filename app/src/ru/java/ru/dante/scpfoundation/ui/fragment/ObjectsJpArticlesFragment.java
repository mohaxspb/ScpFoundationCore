package ru.dante.scpfoundation.ui.fragment;

import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.di.AppComponentImpl;
import ru.dante.scpfoundation.mvp.contract.ObjectsJpArticles;
import ru.kuchanov.scpcore.ui.fragment.articleslists.BaseListArticlesWithSearchFragment;

/**
 * Created by mohax on 27.10.2017.
 * <p>
 * for ScpCore
 */
public class ObjectsJpArticlesFragment
        extends BaseListArticlesWithSearchFragment<ObjectsJpArticles.View, ObjectsJpArticles.Presenter>
        implements ObjectsJpArticles.View {

    public static final String TAG = ObjectsJpArticlesFragment.class.getSimpleName();

    public static ObjectsJpArticlesFragment newInstance() {
        return new ObjectsJpArticlesFragment();
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