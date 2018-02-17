package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.model.firebase.FirebaseObjectUser;
import ru.kuchanov.scpcore.api.model.remoteconfig.LevelsJson;

/**
 * Created by mohax on 06.05.2017.
 * <p>
 * for scp_ru
 */
public class LeaderboardHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.name)
    TextView name;
    @BindView(R2.id.level)
    TextView level;
    @BindView(R2.id.avatar)
    ImageView image;
    @BindView(R2.id.place)
    TextView place;
    @BindView(R2.id.score)
    TextView score;

    public LeaderboardHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(FirebaseObjectUser data) {
        Context context = itemView.getContext();

        place.setText(String.valueOf(getAdapterPosition() + 1));

        name.setText(data.fullName);

        score.setText(String.valueOf(data.score));

        LevelsJson.Level userLevel = LevelsJson.getLevelsJson().getLevelForScore(data.score);
        level.setText(context.getString(R.string.level, userLevel.id));

        Glide.with(image.getContext())
                .load(data.avatar)
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(image) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        image.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }
}