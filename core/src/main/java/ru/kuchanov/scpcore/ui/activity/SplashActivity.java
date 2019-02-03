package ru.kuchanov.scpcore.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

import static ru.kuchanov.scpcore.ui.activity.MainActivity.EXTRA_SHOW_ABOUT;

/**
 * Created by Ivan Semkin on 4/23/2017.
 * <p>
 * for ScpFoundationRu
 */
public class SplashActivity extends AppCompatActivity {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaseApplication.getAppComponent().inject(this);

        startNextActivity();
    }

    private void startNextActivity() {
        final Intent intent;
        if (!mMyPreferenceManager.isPersonalDataAccepted()) {
            intent = new Intent(this, MainActivity.class).putExtra(EXTRA_SHOW_ABOUT, true);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finishAffinity();
    }
}