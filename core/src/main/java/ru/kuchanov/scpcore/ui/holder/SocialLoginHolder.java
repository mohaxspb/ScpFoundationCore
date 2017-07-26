package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.monetization.model.BaseModel;
import ru.kuchanov.scpcore.ui.adapter.BaseAdapterClickListener;

/**
 * Created by mohax on 25.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class SocialLoginHolder
        extends BaseHolder<SocialLoginHolder.SocialLoginModel, BaseAdapterClickListener<SocialLoginHolder.SocialLoginModel>> {

    @BindView(R2.id.image)
    ImageView image;
    @BindView(R2.id.title)
    TextView title;

    public SocialLoginHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(SocialLoginHolder.SocialLoginModel data) {
        super.bind(data);

        Context context = itemView.getContext();

        title.setText(data.getSocialProvider().getTitle());

        Glide.with(context)
                .load(data.getSocialProvider().getIcon())
                .dontAnimate()
                .into(image);

        itemView.setOnClickListener(view -> mAdapterClickListener.onItemClick(data));
    }

    public static class SocialLoginModel extends BaseModel {

        private Constants.Firebase.SocialProvider mSocialProvider;

        public SocialLoginModel(Constants.Firebase.SocialProvider socialProvider) {
            mSocialProvider = socialProvider;
        }

        public Constants.Firebase.SocialProvider getSocialProvider() {
            return mSocialProvider;
        }

        public static SocialLoginModel getModelForProvider(Constants.Firebase.SocialProvider socialProvider) {
            return new SocialLoginModel(socialProvider);
        }

//        public static List<SocialLoginModel> getModels() {
//            List<SocialLoginModel> models = new ArrayList<>();
//            for (Constants.Firebase.SocialProvider provider : Constants.Firebase.SocialProvider.values()) {
//                models.add(getModelForProvider(provider));
//            }
//            return models;
//        }

        public static List<SocialLoginModel> getModels(List<Constants.Firebase.SocialProvider> providers) {
            List<SocialLoginModel> models = new ArrayList<>();
            for (Constants.Firebase.SocialProvider provider : providers) {
                models.add(getModelForProvider(provider));
            }
            return models;
        }
    }
}