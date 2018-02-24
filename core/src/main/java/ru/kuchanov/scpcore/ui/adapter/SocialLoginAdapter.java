package ru.kuchanov.scpcore.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.ui.holder.SocialLoginHolder;


/**
 * Created by mohax on 20.05.2017.
 * <p>
 * for ScpFoundationRu
 */
public class SocialLoginAdapter extends BaseRecyclerAdapter<
        SocialLoginHolder.SocialLoginModel,
        BaseAdapterClickListener<SocialLoginHolder.SocialLoginModel>,
        SocialLoginHolder
        > {

    @Override
    public SocialLoginHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_social_login, parent, false);
        return new SocialLoginHolder(view);
    }
}