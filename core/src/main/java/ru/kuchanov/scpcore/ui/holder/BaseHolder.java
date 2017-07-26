package ru.kuchanov.scpcore.ui.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;

/**
 * Created by y.kuchanov on 11.01.17.
 * <p>
 * for TappAwards
 */
public abstract class BaseHolder<D extends BaseModel, A extends BaseAdapterClickListener<D>> extends RecyclerView.ViewHolder {

    protected D mData;

    protected A mAdapterClickListener;

    public BaseHolder(View itemView, A adapterClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mAdapterClickListener = adapterClickListener;
    }

    public BaseHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(D data) {
        mData = data;
    }

    public void bind(D data, A adapterClickListener) {
        mData = data;
        mAdapterClickListener = adapterClickListener;
    }

    public void setAdapterClickListener(A adapterClickListener) {
        mAdapterClickListener = adapterClickListener;
    }

    public A getAdapterClickListener() {
        return mAdapterClickListener;
    }
}