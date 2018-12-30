package ru.kuchanov.scpcore.ui.fragment.articleslists;

import android.view.Menu;
import android.view.MenuInflater;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.mvp.contract.articleslists.ReadArticlesMvp;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class ReadArticlesFragment
        extends BaseListArticlesWithSearchFragment<ReadArticlesMvp.View, ReadArticlesMvp.Presenter>
        implements ReadArticlesMvp.View {

    public static final String TAG = ReadArticlesFragment.class.getSimpleName();

    public static ReadArticlesFragment newInstance() {
        return new ReadArticlesFragment();
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected boolean isHasOptionsMenu() {
        return true;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_offline;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //remove as we already have one here
        menu.findItem(R.id.menuItemDownloadAll).setVisible(false);
    }

    @Override
    public void resetOnScrollListener() {
        //we do not have paging
    }

    @Override
    protected boolean isSwipeRefreshEnabled() {
        //have api for it, we do not need to update list
        return false;
    }
}