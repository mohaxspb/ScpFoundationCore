package ru.kuchanov.scpcore.ui.fragment.articleslists;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.mvp.base.BaseArticlesListMvp;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter;
import ru.kuchanov.scpcore.ui.dialog.AdsSettingsBottomSheetDialogFragment;
import ru.kuchanov.scpcore.ui.fragment.BaseListFragment;
import ru.kuchanov.scpcore.ui.util.EndlessRecyclerViewScrollListener;
import timber.log.Timber;

/**
 * Created by mohax on 03.01.2017.
 */
public abstract class BaseArticlesListFragment<V extends BaseArticlesListMvp.View, P extends BaseArticlesListMvp.Presenter<V>>
        extends BaseListFragment<V, P> {

    private static final String EXTRA_SORT_TYPE = "EXTRA_SORT_TYPE";

    protected ArticlesListAdapter mAdapter;
    private ArticlesListAdapter.SortType mSortType = ArticlesListAdapter.SortType.NONE;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSortType = (ArticlesListAdapter.SortType) savedInstanceState.getSerializable(EXTRA_SORT_TYPE);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_SORT_TYPE, mSortType);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ArticlesListAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new ArticlesListAdapter();
        }
        return mAdapter;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_list;
    }

    @Override
    protected void initViews() {
        super.initViews();
        Timber.d("initViews");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mRecyclerView.setDrawingCacheEnabled(true);
//        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
//        mRecyclerView.setItemViewCacheSize(40);

        initAdapter();

        mRecyclerView.setAdapter(getAdapter());

        if (mPresenter.getData() != null) {
            getAdapter().setData(mPresenter.getData());
        } else {
            mPresenter.getDataFromDb();
            //TODO add settings to update list on launch
            if (shouldUpdateThisListOnLaunch()) {
                mPresenter.getDataFromApi(Constants.Api.ZERO_OFFSET);
            }
        }

        resetOnScrollListener();

        initSwipeRefresh();
    }

    @Override
    protected boolean isHasOptionsMenu() {
        return true;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_articles_list;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.menuItemSort) {
            final List<ArticlesListAdapter.SortType> sortTypes = new ArrayList<>(Arrays.asList(ArticlesListAdapter.SortType.values()));
            if (!getResources().getBoolean(R.bool.filter_by_type_enabled)) {
                sortTypes.remove(ArticlesListAdapter.SortType.EUCLID);
                sortTypes.remove(ArticlesListAdapter.SortType.KETER);
                sortTypes.remove(ArticlesListAdapter.SortType.NEUTRAL_OR_NOT_ADDED);
                sortTypes.remove(ArticlesListAdapter.SortType.SAFE);
                sortTypes.remove(ArticlesListAdapter.SortType.THAUMIEL);
            }

            final int selectedIndex = sortTypes.indexOf(getAdapter().getSortType());

            new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_sort_title)
                    .items(sortTypes)
                    .alwaysCallSingleChoiceCallback()
                    .itemsCallbackSingleChoice(selectedIndex, (dialog, itemView, which, text) -> {
                        Timber.d("sortBy: %s", text);
                        mSortType = sortTypes.get(which);
                        getAdapter().sortByType(mSortType);
                        getAdapter().notifyDataSetChanged();
                        dialog.dismiss();
                        getActivity().invalidateOptionsMenu();
                        return true;
                    })
                    .positiveText(R.string.close)
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .build()
                    .show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem item = menu.findItem(R.id.menuItemSort);
        if (item != null && getActivity() != null) {
            if (mSortType != ArticlesListAdapter.SortType.NONE) {
                item.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.material_green_500), PorterDuff.Mode.SRC_ATOP);
            } else {
                item.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), android.R.color.transparent), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    /**
     * override it if you do not want to update this list on first launch
     *
     * @return true by default
     */
    protected boolean shouldUpdateThisListOnLaunch() {
        return true;
    }

    @Override
    protected boolean isSwipeRefreshEnabled() {
        return true;
    }

    private void initSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            if (isSwipeRefreshEnabled()) {
                mSwipeRefreshLayout.setColorSchemeResources(R.color.zbs_color_red);
                mSwipeRefreshLayout.setOnRefreshListener(() -> mPresenter.getDataFromApi(Constants.Api.ZERO_OFFSET));
            } else {
                mSwipeRefreshLayout.setEnabled(false);
            }
        }
    }

    /**
     * override it to add something
     */
    protected void initAdapter() {
        getAdapter().setArticleClickListener(new ArticlesListAdapter.ArticleClickListener() {
            @Override
            public void onArticleClick(final Article article) {
                Timber.d("onArticleClick: %s", article.title);
                if (!isAdded()) {
                    return;
                }
                getBaseActivity().startArticleActivity(Article.getListOfUrls(getAdapter().getDisplayedData()), getAdapter().getDisplayedData().indexOf(article));
            }

            @Override
            public void toggleReadenState(final Article article) {
                Timber.d("toggleReadenState: %s", article.title);
                mPresenter.toggleReadState(article);
            }

            @Override
            public void toggleFavoriteState(final Article article) {
                Timber.d("toggleFavoriteState: %s", article.title);
                mPresenter.toggleFavoriteState(article);
            }

            @Override
            public void onOfflineClick(final Article article) {
                Timber.d("onOfflineClick: %s", article.title);
                mPresenter.toggleOfflineState(article);
            }

            @Override
            public void onTagClick(final ArticleTag tag) {
                Timber.d("onTagClick: %s", tag);
                getBaseActivity().startTagsSearchActivity(new ArrayList<>(Collections.singletonList(tag)));
            }

            @Override
            public void onRewardedVideoClick() {
                if (!isAdded()) {
                    return;
                }
                getBaseActivity().startRewardedVideoFlow();
            }

            @Override
            public void onAdsSettingsClick() {
                if (!isAdded()) {
                    return;
                }
                final BottomSheetDialogFragment dialogFragment = AdsSettingsBottomSheetDialogFragment.newInstance();
                dialogFragment.show(getActivity().getSupportFragmentManager(), dialogFragment.getTag());
            }
        });
        getAdapter().setHasStableIds(true);
        getAdapter().setShouldShowPopupOnFavoriteClick(isShouldShowPopupOnFavoriteClick());
        getAdapter().sortByType(mSortType);
        getAdapter().notifyDataSetChanged();
    }

    protected boolean isShouldShowPopupOnFavoriteClick() {
        return false;
    }

    @Override
    public void updateData(final List<Article> data) {
//        Timber.d("updateData size: %s", data == null ? "data is null" : data.size());
        if (!isAdded()) {
            return;
        }
        getAdapter().setData(data);
        resetOnScrollListener();
    }

    /**
     * override it to change or disable endless scrolling behavior
     */
    @Override
    public void resetOnScrollListener() {
        mRecyclerView.clearOnScrollListeners();
        if (mAdapter.getItemCount() < Constants.Api.NUM_OF_ARTICLES_ON_SEARCH_PAGE) {
            //so there is to less arts to be able to load from bottom
            //this can be if we receive few search results
            //si we just no need to set scrollListener
            return;
        }
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Timber.d("onLoadMode with page: %s, and offset: %s", page, view.getAdapter().getItemCount());
                showBottomProgress(true);
                mPresenter.getDataFromApi(getAdapter().getItemCount());
            }
        });

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        mRecyclerView.addOnScrollListener(mVerticalRecyclerViewFastScroller.getOnScrollListener());
    }

    @Override
    protected void onTextSizeUiChanged() {
        if (!isAdded()) {
            return;
        }
        getAdapter().notifyDataSetChanged();
    }
}