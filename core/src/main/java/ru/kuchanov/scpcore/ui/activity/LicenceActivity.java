package ru.kuchanov.scpcore.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.util.StorageUtils;
import timber.log.Timber;

/**
 * Created by mohax on 15.01.2017.
 * <p>
 * for scp_ru
 */
public class LicenceActivity extends AppCompatActivity {

    public static final String EXTRA_SHOW_ABOUT = "EXTRA_SHOW_ABOUT";

    @BindView(R2.id.text)
    TextView text;
    @BindView(R2.id.root)
    View mRoot;

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    @OnClick(R2.id.accept)
    public void onAcceptClick() {
        mMyPreferenceManager.setLicenceAccepted(true);
        startActivity(new Intent(this, getLaunchActivityClass()).putExtra(EXTRA_SHOW_ABOUT, true));
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getAppComponent().inject(this);
        if (mMyPreferenceManager.isLicenceAccepted()) {
            startActivity(new Intent(this, getLaunchActivityClass()));
            finish();
        }
        setContentView(R.layout.activity_license);
        ButterKnife.bind(this);

        try {
            String licence = StorageUtils.readFromAssets(this, "licence.txt");
            text.setText(Html.fromHtml(licence));
        } catch (IOException e) {
            Timber.e(e, "error while read licence from file");
            Snackbar.make(mRoot, R.string.error_read_licence, Snackbar.LENGTH_SHORT).show();
        }
    }

    protected Class getLaunchActivityClass() {
        return MainActivity.class;
    }
}