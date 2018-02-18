package ru.kuchanov.scpcore.ui.dialog;

import com.afollestad.materialdialogs.MaterialDialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;

import javax.inject.Inject;

import ru.kuchanov.rate.PreRate;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.util.MyHtmlTagHandler;
import ru.kuchanov.scpcore.util.IntentUtils;
import ru.kuchanov.scpcore.util.StorageUtils;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

public class NewVersionDialogFragment extends DialogFragment {

    public static final String TAG = NewVersionDialogFragment.class.getSimpleName();

    public static final String EXTRA_TITLE = NewVersionDialogFragment.class.getSimpleName();

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @Inject
    ConstantValues mConstantValues;

    public static DialogFragment newInstance(final String title) {
        final DialogFragment fragment = new NewVersionDialogFragment();
        final Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String newVersionFeatures = "";
        try {
            final long versionCode = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
            newVersionFeatures = StorageUtils.readFromAssets(getActivity(), "releaseNotes/newVersionFeatures" + versionCode + ".txt");
        } catch (final Exception e) {
            Timber.e(e, "error while read newVersionFeatures from file");
        }

        newVersionFeatures = getString(R.string.version, SystemUtils.getPackageInfo().versionName) +
                             "<br/><br/>" +
                             newVersionFeatures +
                             "<br/><br/>" +
                             "<a href=\"rateApp://rateApp\">" +
                             getString(R.string.rate_app_title, getString(getActivity().getApplicationInfo().labelRes)) +
                             "</a>" +
                             "<br/><br/>" +
                             getString(R.string.contacts_info) +
                             "<br/><br/>" +
                             getString(R.string.license_en, mConstantValues.getBaseApiUrl(), mConstantValues.getBaseApiUrl());

        ////////
        final CharSequence sequence = Html.fromHtml(newVersionFeatures, null, new MyHtmlTagHandler());
        final SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        final URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (final URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, new TextItemsClickListener() {

                @Override
                public void onLinkClicked(final String link) {
                    IntentUtils.openUrl(link);
                }

                @Override
                public void onRateAppClick() {
                    PreRate.init(getActivity(), getString(R.string.feedback_email), getString(R.string.feedback_title)).showRateDialog();
                }
            });
        }
//        textView.setText(strBuilder);
        ////////
        final MaterialDialog.Builder dialogTextSizeBuilder = new MaterialDialog.Builder(getActivity())
                .content(strBuilder)
                .title(getArguments().getString(EXTRA_TITLE, getString(R.string.app_name)))
                .positiveText(R.string.yes_sir);
//        final MaterialDialog.Builder dialogTextSizeBuilder = new MaterialDialog.Builder(getActivity())
//                .content(Html.fromHtml(newVersionFeatures, null, new MyHtmlTagHandler()))
//                .title(getArguments().getString(EXTRA_TITLE, getString(R.string.app_name)))
//                .positiveText(R.string.yes_sir);

        final MaterialDialog dialog = dialogTextSizeBuilder.build();

        if (dialog.getContentView() != null) {
            dialog.getContentView().setLinksClickable(true);
            dialog.getContentView().setAutoLinkMask(Linkify.WEB_URLS);
        }
        return dialog;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        mMyPreferenceManager.setCurAppVersion(SystemUtils.getPackageInfo().versionCode);
    }

    private void makeLinkClickable(
            final Spannable strBuilder,
            final URLSpan span,
            final TextItemsClickListener textItemsClickListener
    ) {
        final int start = strBuilder.getSpanStart(span);
        final int end = strBuilder.getSpanEnd(span);
        final int flags = strBuilder.getSpanFlags(span);
        final ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void updateDrawState(final TextPaint ds) {
                super.updateDrawState(ds);
                if (span.getURL().startsWith(Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                    ds.setColor(ContextCompat.getColor(BaseApplication.getAppInstance(), R.color.material_red_500));
                }
            }

            @Override
            public void onClick(final View view) {
                Timber.d("Link clicked: %s", span.getURL());

                final String link = span.getURL();
                if (link.startsWith("rateApp://")) {
                    if (textItemsClickListener != null) {
                        textItemsClickListener.onRateAppClick();
                    }
                    return;
                }

                if (textItemsClickListener != null) {
                    textItemsClickListener.onLinkClicked(link);
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public interface TextItemsClickListener {

        void onLinkClicked(final String link);

        void onRateAppClick();
    }
}