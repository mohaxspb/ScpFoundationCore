package ru.kuchanov.scpcore.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.monetization.model.AppInstallHeader;
import ru.kuchanov.scpcore.monetization.model.AppInviteModel;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.monetization.model.DisableAdsForAuth;
import ru.kuchanov.scpcore.monetization.model.PlayMarketApplication;
import ru.kuchanov.scpcore.monetization.model.RewardedVideo;
import ru.kuchanov.scpcore.monetization.model.VkGroupToJoin;
import ru.kuchanov.scpcore.ui.holder.AppInstallHeaderHolder;
import ru.kuchanov.scpcore.ui.holder.AppInviteHolder;
import ru.kuchanov.scpcore.ui.holder.BaseHolder;
import ru.kuchanov.scpcore.ui.holder.OurApplicationHolder;
import ru.kuchanov.scpcore.ui.holder.RewardedVideoHolder;
import ru.kuchanov.scpcore.ui.holder.SignInHolder;
import ru.kuchanov.scpcore.ui.holder.VkGroupToJoinHolder;
import timber.log.Timber;

public class FreeAdsDisableRecyclerAdapter extends BaseRecyclerAdapter<
        BaseModel,
        BaseAdapterClickListener<BaseModel>,
        BaseHolder<BaseModel, BaseAdapterClickListener<BaseModel>>
        > {

    private static final int TYPE_INVITE = 0;
    private static final int TYPE_APP_TO_INSTALL = 1;
    private static final int TYPE_APP_TO_INSTALL_HEADER = 2;
    private static final int TYPE_REWARDED_VIDEO = 3;
    private static final int TYPE_VK_GROUP_TO_JOIN_HEADER = 4;
    private static final int TYPE_VK_GROUP_TO_JOIN = 5;
    private static final int TYPE_SIGN_IN = 6;

    @Override
    public void setData(List<BaseModel> data) {
        super.setData(data);
//        Timber.d("data: %s", data);
    }

    @Override
    public BaseHolder<BaseModel, BaseAdapterClickListener<BaseModel>> onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case TYPE_INVITE:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_title, parent, false);
                return new AppInviteHolder(itemView, mAdapterClickListener);
            case TYPE_APP_TO_INSTALL:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_title_content_image, parent, false);
                return new OurApplicationHolder(itemView);
            case TYPE_REWARDED_VIDEO:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_title, parent, false);
                return new RewardedVideoHolder(itemView, mAdapterClickListener);
            case TYPE_APP_TO_INSTALL_HEADER:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_title_non_clickable, parent, false);
                return new AppInstallHeaderHolder(itemView);
            case TYPE_VK_GROUP_TO_JOIN_HEADER:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_title_non_clickable, parent, false);
                return new AppInstallHeaderHolder(itemView);
            case TYPE_VK_GROUP_TO_JOIN:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_title_content_image, parent, false);
                return new VkGroupToJoinHolder(itemView);
            case TYPE_SIGN_IN:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_title, parent, false);
                return new SignInHolder(itemView, mAdapterClickListener);
            default:
                throw new RuntimeException(String.format("unexpected type: %s", viewType));
        }
    }

    @Override
    public int getItemViewType(int position) {
        BaseModel baseModel = mData.get(position);
        if (baseModel instanceof AppInviteModel) {
            return TYPE_INVITE;
        } else if (baseModel instanceof PlayMarketApplication) {
            return TYPE_APP_TO_INSTALL;
        } else if (baseModel instanceof RewardedVideo) {
            return TYPE_REWARDED_VIDEO;
        } else if (baseModel instanceof AppInstallHeader) {
            return TYPE_APP_TO_INSTALL_HEADER;
        } else if (baseModel instanceof VkGroupToJoin) {
            return TYPE_VK_GROUP_TO_JOIN;
        } else if (baseModel instanceof DisableAdsForAuth) {
            return TYPE_SIGN_IN;
        } else {
            throw new RuntimeException(String.format("unexpected type for position: %s", position));
        }
    }
}