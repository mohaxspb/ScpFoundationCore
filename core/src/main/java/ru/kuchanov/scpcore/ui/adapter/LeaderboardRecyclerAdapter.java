package ru.kuchanov.scpcore.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser;
import ru.kuchanov.scpcore.ui.holder.LeaderboardHolder;
import timber.log.Timber;

/**
 * Created by mohax on 25.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class LeaderboardRecyclerAdapter extends RecyclerView.Adapter<LeaderboardHolder> {

    protected List<FirebaseObjectUser> mData;

    private LeaderboardClickListener mAdapterClickListener;

    public LeaderboardRecyclerAdapter() {
        Timber.d("FreeAdsDisableRecyclerAdapter constructor");
    }

    @Override
    public LeaderboardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardHolder(itemView, mAdapterClickListener);
    }

    public void setItemClickListener(LeaderboardClickListener postClickListener) {
        this.mAdapterClickListener = postClickListener;
    }

    public void setData(List<FirebaseObjectUser> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public List<FirebaseObjectUser> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public void onBindViewHolder(LeaderboardHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    public interface LeaderboardClickListener {
        void onUserClicked(FirebaseObjectUser data);
    }
}