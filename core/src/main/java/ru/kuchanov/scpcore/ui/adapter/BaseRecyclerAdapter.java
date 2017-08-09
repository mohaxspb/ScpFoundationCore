package ru.kuchanov.scpcore.ui.adapter;

import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.holder.BaseHolder;

public abstract class BaseRecyclerAdapter<D extends BaseModel, A extends BaseAdapterClickListener<D>, H extends BaseHolder<D, A>>
        extends RecyclerView.Adapter<H> {

    public BaseRecyclerAdapter(List<D> data, A adapterClickListener) {
        mData = data;
        mAdapterClickListener = adapterClickListener;
    }

    public BaseRecyclerAdapter() {
    }

    protected List<D> mData;

    protected A mAdapterClickListener;

    public void setItemClickListener(A postClickListener) {
        this.mAdapterClickListener = postClickListener;
    }

    public void setData(List<D> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public List<D> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public void onBindViewHolder(H holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public void onViewAttachedToWindow(H holder) {
        super.onViewAttachedToWindow(holder);
        holder.setAdapterClickListener(mAdapterClickListener);
        //here you can reset inner adapters
    }

    @Override
    public void onViewDetachedFromWindow(H holder) {
        super.onViewDetachedFromWindow(holder);
        holder.setAdapterClickListener(null);
        //here you can clear resources in inner adapters
    }
}