package ru.kuchanov.scpcore.ui.util;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by mohax on 31.03.2017.
 * <p>
 * for scp_ru
 */
public abstract class ReachBottomRecyclerScrollListener extends RecyclerView.OnScrollListener {

    private boolean alreadyReachedBottom;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int totalItemCount = layoutManager.getItemCount();
        int lastCompletelyVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (!alreadyReachedBottom && lastCompletelyVisibleItemPosition == totalItemCount - 1) {
            alreadyReachedBottom = true;
            onBottomReached();
        }
    }

    public abstract void onBottomReached();
}