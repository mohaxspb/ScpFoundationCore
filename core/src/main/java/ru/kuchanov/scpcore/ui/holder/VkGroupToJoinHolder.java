package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.monetization.model.VkGroupToJoin;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;

/**
 * Created by mohax on 25.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class VkGroupToJoinHolder extends BaseHolder<BaseModel, BaseAdapterClickListener<BaseModel>> {

    @BindView(R2.id.content)
    TextView content;
    @BindView(R2.id.image)
    ImageView image;
    @BindView(R2.id.title)
    TextView title;

    public VkGroupToJoinHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(BaseModel data) {
        super.bind(data);

        VkGroupToJoin application = (VkGroupToJoin) data;

        Context context = itemView.getContext();

        title.setText(application.name);

        content.setText(application.description);

        Glide.with(context)
                .load(application.imageUrl)
                .fitCenter()
                .dontAnimate()
                .into(image);

        itemView.setOnClickListener(view -> mAdapterClickListener.onItemClick(data));
    }
}