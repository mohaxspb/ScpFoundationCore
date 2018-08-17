package ru.kuchanov.scpcore.ui.fragment.articleslists;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.mvp.contract.articleslists.Objects5Articles;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class Objects5ArticlesFragment
        extends BaseListArticlesWithSearchFragment<Objects5Articles.View, Objects5Articles.Presenter>
        implements Objects5Articles.View {

    public static final String TAG = Objects5ArticlesFragment.class.getSimpleName();

    public static Objects5ArticlesFragment newInstance() {
        return new Objects5ArticlesFragment();
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