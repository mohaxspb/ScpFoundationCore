package ru.kuchanov.scpcore.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.R2;

/**
 * Created by mohax on 23.03.2017.
 * <p>
 * for scp_ru
 */
public class HeaderViewHolderLogined {

    @BindView(R2.id.level)
    public TextView level;
    @BindView(R2.id.levelUp)
    public ImageView levelUp;
    @BindView(R2.id.name)
    public TextView name;
    @BindView(R2.id.avatar)
    public ImageView avatar;
    @BindView(R2.id.circleView)
    public CircleProgressView circleProgress;
    @BindView(R2.id.levelNum)
    public TextView levelNum;
    @BindView(R2.id.logout)
    public View logout;
    @BindView(R2.id.inapp)
    public View inapp;

    public HeaderViewHolderLogined(View view) {
        ButterKnife.bind(this, view);
    }
}