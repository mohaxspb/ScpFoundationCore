package ru.kuchanov.scpcore.ui.fragment.articleslists;

import android.view.Menu;
import android.view.MenuInflater;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.mvp.contract.articleslists.OfflineArticlesMvp;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class OfflineArticlesFragment
        extends BaseListArticlesWithSearchFragment<OfflineArticlesMvp.View, OfflineArticlesMvp.Presenter>
        implements OfflineArticlesMvp.View {

    public static final String TAG = OfflineArticlesFragment.class.getSimpleName();

    public static OfflineArticlesFragment newInstance() {
        return new OfflineArticlesFragment();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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