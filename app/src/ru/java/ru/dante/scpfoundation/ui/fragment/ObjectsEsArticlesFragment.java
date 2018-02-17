package ru.dante.scpfoundation.ui.fragment;

import ru.dante.scpfoundation.MyApplicationImpl;
import ru.dante.scpfoundation.di.AppComponentImpl;
import ru.dante.scpfoundation.mvp.contract.ObjectsEsArticles;
import ru.kuchanov.scpcore.ui.fragment.articleslists.BaseListArticlesWithSearchFragment;

/**
 * Created by mohax on 27.10.2017.
 * <p>
 * for ScpCore
 */
public class ObjectsEsArticlesFragment
        extends BaseListArticlesWithSearchFragment<ObjectsEsArticles.View, ObjectsEsArticles.Presenter>
        implements ObjectsEsArticles.View {

    public static final String TAG = ObjectsEsArticlesFragment.class.getSimpleName();

    public static ObjectsEsArticlesFragment newInstance() {
        return new ObjectsEsArticlesFragment();
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