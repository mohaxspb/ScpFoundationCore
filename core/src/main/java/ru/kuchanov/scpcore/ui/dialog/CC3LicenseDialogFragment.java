package ru.kuchanov.scpcore.ui.dialog;

import com.afollestad.materialdialogs.MaterialDialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.activity.MainActivity;

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
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        //icons from https://github.com/hjnilsson/country-flags/tree/master/svg

        final MaterialDialog.Builder dialogTextSizeBuilder = new MaterialDialog.Builder(getActivity());
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

        final MaterialDialog dialog = dialogTextSizeBuilder.build();

        if (dialog.getCustomView() != null) {
            final View en = dialog.getCustomView().findViewById(R.id.en);
            final View ru = dialog.getCustomView().findViewById(R.id.ru);
            final View pl = dialog.getCustomView().findViewById(R.id.pl);
            final View de = dialog.getCustomView().findViewById(R.id.de);
            final View fr = dialog.getCustomView().findViewById(R.id.fr);
            final View es = dialog.getCustomView().findViewById(R.id.es);
            final View it = dialog.getCustomView().findViewById(R.id.it);
            final View pt = dialog.getCustomView().findViewById(R.id.pt);
            final View ch = dialog.getCustomView().findViewById(R.id.ch);

            final TextView content = dialog.getCustomView().findViewById(R.id.content);

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
                case "fr":
                    setContentText(content, getString(R.string.license_fr, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "es":
                    setContentText(content, getString(R.string.license_es, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "it":
                    setContentText(content, getString(R.string.license_it, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "pt":
                    setContentText(content, getString(R.string.license_pt, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                case "ch":
                    setContentText(content, getString(R.string.license_ch, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl()));
                    break;
                default:
                    throw new IllegalArgumentException("unexpected lang: " + mConstantValues.getAppLang());
            }

            en.setOnClickListener(view -> setContentText(content, getString(R.string.license_en, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            pl.setOnClickListener(view -> setContentText(content, getString(R.string.license_pl, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            ru.setOnClickListener(view -> setContentText(content, getString(R.string.license_ru, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            de.setOnClickListener(view -> setContentText(content, getString(R.string.license_de, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            fr.setOnClickListener(view -> setContentText(content, getString(R.string.license_fr, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            es.setOnClickListener(view -> setContentText(content, getString(R.string.license_es, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            it.setOnClickListener(view -> setContentText(content, getString(R.string.license_it, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            pt.setOnClickListener(view -> setContentText(content, getString(R.string.license_pt, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
            ch.setOnClickListener(view -> setContentText(content, getString(R.string.license_ch, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl())));
        }
        return dialog;
    }

    private void setContentText(final TextView content, final String text) {
        content.setText(Html.fromHtml(text));
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);

        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showAppLangOrVersionFeaturesDialog();
        }
    }
}