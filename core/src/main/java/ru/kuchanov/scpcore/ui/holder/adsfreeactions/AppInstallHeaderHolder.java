package ru.kuchanov.scpcore.ui.holder.adsfreeactions;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;
import ru.kuchanov.scpcore.ui.holder.BaseHolder;

public class AppInstallHeaderHolder extends BaseHolder<BaseModel, BaseAdapterClickListener<BaseModel>> {

    @BindView(R2.id.title)
    TextView title;

    public AppInstallHeaderHolder(final View itemView) {
        super(itemView);
    }

    @Override
    public void bind(final BaseModel data) {
        super.bind(data);

        title.setText(data.title);
    }
}