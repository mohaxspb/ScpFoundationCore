package ru.kuchanov.scpcore.ui.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import javax.inject.Inject;

import butterknife.BindView;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.base.BaseListMvp;
import ru.kuchanov.scpcore.util.DimensionUtils;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseListFragment<V extends BaseListMvp.View, P extends BaseListMvp.Presenter<V>>
        extends BaseFragment<V, P>
        implements BaseListMvp.View, SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R2.id.root)
    protected View root;
    @Nullable
    @BindView(R2.id.progressCenter)
    protected ProgressBar mProgressBarCenter;
    @Nullable
    @BindView(R2.id.swipeRefresh)
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R2.id.recyclerView)
    protected RecyclerView mRecyclerView;
    @BindView(R2.id.fastScroller)
    protected VerticalRecyclerViewFastScroller mVerticalRecyclerViewFastScroller;

    protected DividerItemDecoration mDividerItemDecoration;

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    protected abstract <A extends RecyclerView.Adapter> A getAdapter();

    protected abstract boolean isSwipeRefreshEnabled();

    @Override
    protected void initViews() {
        mDividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        if (mMyPreferenceManager.isDesignListNewEnabled()) {
            mRecyclerView.removeItemDecoration(mDividerItemDecoration);
        } else {
            mRecyclerView.addItemDecoration(mDividerItemDecoration);
        }
        // Connect the recycler to the scroller (to let the scroller scroll the list)
        mVerticalRecyclerViewFastScroller.setRecyclerView(mRecyclerView);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        mRecyclerView.addOnScrollListener(mVerticalRecyclerViewFastScroller.getOnScrollListener());
    }

    @Override
    public void showSwipeProgress(boolean show) {
        if (!isAdded() || mSwipeRefreshLayout == null) {
            return;
        }
        if (!mSwipeRefreshLayout.isRefreshing() && !show) {
            return;
        }
        mSwipeRefreshLayout.setProgressViewEndTarget(false, DimensionUtils.getActionBarHeight(getActivity()));
        mSwipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void showCenterProgress(boolean show) {
        if (!isAdded() || mProgressBarCenter == null) {
            return;
        }
        if (show) {
            if (getAdapter() != null && getAdapter().getItemCount() != 0) {
                mProgressBarCenter.setVisibility(View.GONE);
                showSwipeProgress(true);
            } else {
                mProgressBarCenter.setVisibility(View.VISIBLE);
            }
        } else {
            mProgressBarCenter.setVisibility(View.GONE);
        }
    }

    @Override
    public void showBottomProgress(boolean show) {
        if (!isAdded() || mSwipeRefreshLayout == null) {
            return;
        }

        if (show) {
            int screenHeight = DimensionUtils.getScreenHeight();
            mSwipeRefreshLayout.setProgressViewEndTarget(false, screenHeight - DimensionUtils.getActionBarHeight(getActivity()) * 2);
        }

        mSwipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void enableSwipeRefresh(boolean enable) {
        if (!isAdded() || mSwipeRefreshLayout == null || !isSwipeRefreshEnabled()) {
            return;
        }
        mSwipeRefreshLayout.setEnabled(enable);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case MyPreferenceManager.Keys.TEXT_SCALE_UI:
                onTextSizeUiChanged();
                break;
            case MyPreferenceManager.Keys.DESIGN_FONT_PATH:
                getAdapter().notifyDataSetChanged();
                break;
            case MyPreferenceManager.Keys.DESIGN_LIST_TYPE:
                if (!isAdded()) {
                    return;
                }
                if (mMyPreferenceManager.isDesignListNewEnabled()) {
                    mRecyclerView.removeItemDecoration(mDividerItemDecoration);
                } else {
                    mRecyclerView.addItemDecoration(mDividerItemDecoration);
                }
                mRecyclerView.setAdapter(null);
                mRecyclerView.setAdapter(getAdapter());
                break;
            default:
                //do nothing
                break;
        }
    }

    protected abstract void onTextSizeUiChanged();
}