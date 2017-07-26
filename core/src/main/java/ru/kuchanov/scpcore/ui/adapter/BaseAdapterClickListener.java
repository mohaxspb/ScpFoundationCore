package ru.kuchanov.scpcore.ui.adapter;

import ru.kuchanov.scpcore.monetization.model.BaseModel;

/**
 * Created by y.kuchanov on 11.01.17.
 * <p>
 * for TappAwards
 */
public interface BaseAdapterClickListener<D extends BaseModel> {

    void onItemClick(D data);
}