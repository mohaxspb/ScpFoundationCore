package ru.kuchanov.scpcore.ui.holder.adsfreeactions;

import com.bumptech.glide.Glide;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.monetization.model.PlayMarketApplication;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;
import ru.kuchanov.scpcore.ui.holder.BaseHolder;

public class OurApplicationHolder extends BaseHolder<BaseModel, BaseAdapterClickListener<BaseModel>> {

    @BindView(R2.id.content)
    TextView content;

    @BindView(R2.id.image)
    ImageView image;

    @BindView(R2.id.title)
    TextView title;

    public OurApplicationHolder(final View itemView) {
        super(itemView);
    }

    @Override
    public void bind(final BaseModel data) {
        super.bind(data);

        final PlayMarketApplication application = (PlayMarketApplication) data;

        title.setText(application.name);

        content.setText(application.description);

        Glide.with(image.getContext())
                .load(application.imageUrl)
                .fitCenter()
                .dontAnimate()
                .into(image);

        itemView.setOnClickListener(view -> mAdapterClickListener.onItemClick(data));
    }
}