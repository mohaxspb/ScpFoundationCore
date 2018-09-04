package ru.kuchanov.scpcore.ui.holder.login;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;
import ru.kuchanov.scpcore.ui.holder.BaseHolder;

public class SocialLoginHolder
        extends BaseHolder<SocialLoginHolder.SocialLoginModel, BaseAdapterClickListener<SocialLoginHolder.SocialLoginModel>> {

    @BindView(R2.id.image)
    public ImageView imageView;

    @BindView(R2.id.title)
    public TextView titleTextView;

    public SocialLoginHolder(final View itemView, final BaseAdapterClickListener<SocialLoginModel> adapterClickListener) {
        super(itemView, adapterClickListener);
    }

    public SocialLoginHolder(final View itemView) {
        super(itemView);
    }

    @Override
    public void bind(final SocialLoginHolder.SocialLoginModel data) {
        super.bind(data);

        titleTextView.setText(data.getSocialProvider().getTitle());
        imageView.setImageResource(data.getSocialProvider().getIcon());

        itemView.setOnClickListener(view -> mAdapterClickListener.onItemClick(data));
    }

    public static class SocialLoginModel extends BaseModel {

        private final Constants.Firebase.SocialProvider mSocialProvider;

        public SocialLoginModel(final Constants.Firebase.SocialProvider socialProvider) {
            super();
            mSocialProvider = socialProvider;
        }

        public Constants.Firebase.SocialProvider getSocialProvider() {
            return mSocialProvider;
        }

        public static SocialLoginModel getModelForProvider(final Constants.Firebase.SocialProvider socialProvider) {
            return new SocialLoginModel(socialProvider);
        }

        public static List<SocialLoginModel> getModels(final Iterable<Constants.Firebase.SocialProvider> providers) {
            final List<SocialLoginModel> models = new ArrayList<>();
            for (final Constants.Firebase.SocialProvider provider : providers) {
                models.add(getModelForProvider(provider));
            }
            return models;
        }
    }
}