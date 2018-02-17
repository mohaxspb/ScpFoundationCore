package ru.kuchanov.scpcore.ui.holder;

import org.jetbrains.annotations.NotNull;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;

public abstract class BaseHolder<D extends BaseModel, A extends BaseAdapterClickListener<D>>
        extends RecyclerView.ViewHolder {

    @NotNull
    protected D mData;

    @Nullable
    protected A mAdapterClickListener;

    public BaseHolder(final View itemView, final A adapterClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mAdapterClickListener = adapterClickListener;
    }

    public BaseHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(final D data) {
        mData = data;
    }

    public void bind(final D data, final A adapterClickListener) {
        mData = data;
        mAdapterClickListener = adapterClickListener;
    }

    public void setAdapterClickListener(final A adapterClickListener) {
        mAdapterClickListener = adapterClickListener;
    }

    public A getAdapterClickListener() {
        return mAdapterClickListener;
    }
}