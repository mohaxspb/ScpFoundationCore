package ru.kuchanov.scpcore.ui.holder;

import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.R2;

/**
 * Created by mohax on 23.03.2017.
 * <p>
 * for scp_ru
 */
public class HeaderViewHolderUnlogined {
    @BindView(R2.id.login)
    public View mLogin;
    @BindView(R2.id.loginInfo)
    public View mLoginInfo;

    public HeaderViewHolderUnlogined(View view) {
        ButterKnife.bind(this, view);
    }
}