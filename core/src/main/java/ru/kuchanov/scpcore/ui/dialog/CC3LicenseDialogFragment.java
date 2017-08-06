package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;

import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.util.MyHtmlTagHandler;
import ru.kuchanov.scpcore.util.StorageUtils;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

public class CC3LicenseDialogFragment extends DialogFragment {

    public static final String TAG = CC3LicenseDialogFragment.class.getSimpleName();

    public static final String EXTRA_TITLE = CC3LicenseDialogFragment.class.getSimpleName();

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    public static DialogFragment newInstance() {
        return new CC3LicenseDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.d("onCreateDialog");
        MaterialDialog dialog;

        //icons from https://raw.githubusercontent.com/hjnilsson/country-flags/master/svg

        MaterialDialog.Builder dialogTextSizeBuilder = new MaterialDialog.Builder(getActivity());
        dialogTextSizeBuilder
                .customView(R.layout.dialog_cc3_license, false)
                .title(R.string.attention)
                .positiveText(R.string.yes_sir);

        dialog = dialogTextSizeBuilder.build();

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mMyPreferenceManager.setCurAppVersion(SystemUtils.getPackageInfo().versionCode);
    }
}