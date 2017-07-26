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

    public SocialLoginAdapter(List<SocialLoginHolder.SocialLoginModel> models, BaseAdapterClickListener<SocialLoginHolder.SocialLoginModel> baseAdapterClickListener) {
        super(models, baseAdapterClickListener);
    }

    public SocialLoginAdapter() {
    }

    @Override
    public SocialLoginHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_social_login, parent, false);
        return new SocialLoginHolder(view);
    }
}