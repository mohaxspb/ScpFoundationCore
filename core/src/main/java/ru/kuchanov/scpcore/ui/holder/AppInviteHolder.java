package ru.kuchanov.scpcore.ui.holder;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;
import ru.kuchanov.scpcore.util.DimensionUtils;

/**
 * Created by mohax on 25.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class AppInviteHolder extends BaseHolder<BaseModel, BaseAdapterClickListener<BaseModel>> {

    @BindView(R2.id.title)
    TextView title;

    public AppInviteHolder(View itemView, BaseAdapterClickListener<BaseModel> adapterClickListener) {
        super(itemView, adapterClickListener);
    }

    @Override
    public void bind(BaseModel data) {
        super.bind(data);

        title.setText(data.title);

        title.setCompoundDrawablePadding(DimensionUtils.getDefaultMargin());
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person, 0, 0, 0);

        itemView.setOnClickListener(view -> mAdapterClickListener.onItemClick(data));
    }
}