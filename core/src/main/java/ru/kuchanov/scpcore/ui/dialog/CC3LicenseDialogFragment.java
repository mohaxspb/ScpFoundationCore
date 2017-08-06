package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import butterknife.ButterKnife;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import timber.log.Timber;

public class CC3LicenseDialogFragment extends DialogFragment {

    public static final String TAG = CC3LicenseDialogFragment.class.getSimpleName();

    @Inject
    MyPreferenceManager mMyPreferenceManager;
    @Inject
    ApiClient mApiClient;
    @Inject
    ConstantValues mConstantValues;

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
                .cancelable(false)
                .positiveText(R.string.i_accept)
                .negativeText(android.R.string.no)
                .onPositive((dialog1, which) -> {
                    mMyPreferenceManager.setPersonalDataAccepted(true);
                    dismiss();
                })
                .onNegative((dialog1, which) -> {
                    mMyPreferenceManager.setPersonalDataAccepted(false);
                    getActivity().finish();
                });

        dialog = dialogTextSizeBuilder.build();

        if (dialog.getCustomView() != null) {
            View en = ButterKnife.findById(dialog.getCustomView(), R.id.en);
            View ru = ButterKnife.findById(dialog.getCustomView(), R.id.ru);
            View pl = ButterKnife.findById(dialog.getCustomView(), R.id.pl);

            TextView coontent = ButterKnife.findById(dialog.getCustomView(), R.id.content);

            //todo get lang from constantValues
            switch (mApiClient.getAppLang()) {
                case "ru":
                    coontent.setText(getString(R.string.license_ru, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "en":
                    coontent.setText(getString(R.string.license_en, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "pl":
                    coontent.setText(getString(R.string.license_pl, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                default:
                    throw new IllegalArgumentException("unexpected lang: " + mApiClient.getAppLang());
            }

            en.setOnClickListener(view -> coontent.setText(getString(R.string.license_en, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            pl.setOnClickListener(view -> coontent.setText(getString(R.string.license_pl, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            ru.setOnClickListener(view -> coontent.setText(getString(R.string.license_ru, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
        }
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showAppLangOrVersionFeaturesDialog();
        }
    }
}