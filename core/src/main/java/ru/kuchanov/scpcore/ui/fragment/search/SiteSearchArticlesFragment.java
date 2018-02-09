package ru.kuchanov.scpcore.ui.fragment.search;

import android.graphics.Color;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.mvp.contract.search.SiteSearchArticlesMvp;
import ru.kuchanov.scpcore.ui.fragment.articleslists.BaseArticlesListFragment;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class SiteSearchArticlesFragment
        extends BaseArticlesListFragment<SiteSearchArticlesMvp.View, SiteSearchArticlesMvp.Presenter>
        implements SiteSearchArticlesMvp.View {

    public static final String TAG = SiteSearchArticlesFragment.class.getSimpleName();

    public static SiteSearchArticlesFragment newInstance() {
        return new SiteSearchArticlesFragment();
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_search;
    }

    @Override
    protected boolean isHasOptionsMenu() {
        return true;
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected boolean shouldUpdateThisListOnLaunch() {
        return false;
    }

    @Override
    protected void initAdapter() {
        super.initAdapter();
        mAdapter.setShouldShowPreview(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(!isAdded()){
            return;
        }
        SearchView searchView = new SearchView(getActivity());
        searchView.setQueryHint(Html.fromHtml("<font color = #ffffff>" + getResources().getString(R.string.search_hint) + "</font>"));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mPresenter.setQuery(query);
                mPresenter.getDataFromApi(Constants.Api.ZERO_OFFSET);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        changeSearchViewTextColor(searchView);
        MenuItem search = menu.findItem(R.id.menuItemSearch);
        search.setActionView(searchView);
    }

    /**
     * (Грязный хак исправления цвета текста)
     */
    private void changeSearchViewTextColor(View view) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.WHITE);
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i));
                }
            }
        }
    }
}