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

public class NewVersionDialogFragment extends DialogFragment {

    public static final String TAG = NewVersionDialogFragment.class.getSimpleName();

    public static final String EXTRA_TITLE = NewVersionDialogFragment.class.getSimpleName();

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    public static DialogFragment newInstance(String title) {
        DialogFragment fragment = new NewVersionDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        fragment.setArguments(args);
        return fragment;
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
        final MaterialDialog dialogTextSize;
        String newVersionFeatures = "";
        try {
            Timber.d("SystemUtils.getPackageInfo().versionCode: %s", SystemUtils.getPackageInfo().versionCode);
            newVersionFeatures = StorageUtils.readFromAssets(getActivity(), "releaseNotes/newVersionFeatures" + SystemUtils.getPackageInfo().versionCode + ".txt");
        } catch (Exception e) {
            Timber.e(e, "error while read newVersionFeatures from file");
        }

        String title = getArguments().getString(EXTRA_TITLE, getString(R.string.app_name));

        MaterialDialog.Builder dialogTextSizeBuilder = new MaterialDialog.Builder(getActivity());
        dialogTextSizeBuilder
                .content(Html.fromHtml(newVersionFeatures, null, new MyHtmlTagHandler()))
                .title(title)
                .positiveText(R.string.yes_sir);

        dialogTextSize = dialogTextSizeBuilder.build();

        return dialogTextSize;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mMyPreferenceManager.setCurAppVersion(SystemUtils.getPackageInfo().versionCode);
    }
}