package ru.kuchanov.scpcore.ui.holder;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;

/**
 * Created by mohax on 25.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class AppInstallHeaderHolder extends BaseHolder<BaseModel, BaseAdapterClickListener<BaseModel>> {

    @BindView(R2.id.title)
    TextView title;

    public AppInstallHeaderHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(BaseModel data) {
        super.bind(data);

        title.setText(data.title);
    }
}