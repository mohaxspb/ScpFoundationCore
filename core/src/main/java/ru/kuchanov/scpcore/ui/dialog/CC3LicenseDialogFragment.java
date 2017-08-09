package ru.kuchanov.scpcore.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import butterknife.ButterKnife;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import timber.log.Timber;

public class CC3LicenseDialogFragment extends DialogFragment {

    public static final String TAG = CC3LicenseDialogFragment.class.getSimpleName();

    @Inject
    MyPreferenceManager mMyPreferenceManager;
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

        //icons from https://github.com/hjnilsson/country-flags/tree/master/svg

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
            View de = ButterKnife.findById(dialog.getCustomView(), R.id.de);

            TextView content = ButterKnife.findById(dialog.getCustomView(), R.id.content);

            //todo get lang from constantValues
            switch (mConstantValues.getAppLang()) {
                case "ru":
                    setContentText(content, getString(R.string.license_ru, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "en":
                    setContentText(content, getString(R.string.license_en, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "pl":
                    setContentText(content, getString(R.string.license_pl, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "de":
                    setContentText(content, getString(R.string.license_de, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                default:
                    throw new IllegalArgumentException("unexpected lang: " + mConstantValues.getAppLang());
            }

            en.setOnClickListener(view -> setContentText(content, getString(R.string.license_en, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            pl.setOnClickListener(view -> setContentText(content, getString(R.string.license_pl, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            ru.setOnClickListener(view -> setContentText(content, getString(R.string.license_ru, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            de.setOnClickListener(view -> setContentText(content, getString(R.string.license_de, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
        }
        return dialog;
    }

    private void setContentText(TextView content, String text){
        content.setText(Html.fromHtml(text));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showAppLangOrVersionFeaturesDialog();
        }
    }
}