package ru.kuchanov.scpcore.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.SubscriptionsScreenContract;
import ru.kuchanov.scpcore.ui.base.BaseActivity;
import timber.log.Timber;

/**
 * Created by mohax on 17.09.2017.
 * <p>
 * for ScpCore
 */
public class SubscriptionsActivity
        extends BaseActivity<SubscriptionsScreenContract.View, SubscriptionsScreenContract.Presenter>
        implements SubscriptionsScreenContract.View {

    public static void start(Context context) {
        context.startActivity(new Intent(context, SubscriptionsActivity.class));
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_subscribtions;
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected int getMenuResId() {
        return 0;
    }

    @Override
    public boolean isBannerEnabled() {
        return false;
    }
}
