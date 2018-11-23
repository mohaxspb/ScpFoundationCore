package ru.kuchanov.scpcore.ui.holder.adsfreeactions;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;
import ru.kuchanov.scpcore.ui.holder.BaseHolder;
import ru.kuchanov.scpcore.util.DimensionUtils;

public class SignInHolder extends BaseHolder<BaseModel, BaseAdapterClickListener<BaseModel>> {

    @BindView(R2.id.title)
    TextView title;

    public SignInHolder(final View itemView, final BaseAdapterClickListener<BaseModel> adapterClickListener) {
        super(itemView, adapterClickListener);
    }

    @Override
    public void bind(@NotNull final BaseModel data) {
        super.bind(data);

        title.setText(data.title);
        title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.material_green_500));

        title.setCompoundDrawablePadding(DimensionUtils.getDefaultMargin());
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_login_variant, 0, 0, 0);

        itemView.setOnClickListener(view -> mAdapterClickListener.onItemClick(data));
    }
}