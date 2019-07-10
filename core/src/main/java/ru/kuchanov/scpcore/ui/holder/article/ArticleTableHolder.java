package ru.kuchanov.scpcore.ui.holder.article;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.model.ArticleTextPartViewModel;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.AttributeGetter;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleTableHolder extends RecyclerView.ViewHolder {

    private final SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    @BindView(R2.id.webView)
    WebView webView;

    @Inject
    ConstantValues mConstantValues;

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    public ArticleTableHolder(final View itemView, final SetTextViewHTML.TextItemsClickListener clickListener) {
        super(itemView);

        BaseApplication.getAppComponent().inject(this);

        ButterKnife.bind(this, itemView);

        mTextItemsClickListener = clickListener;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void bind(final ArticleTextPartViewModel viewModel) {
        final Context context = itemView.getContext();

        if (viewModel.isInSpoiler) {
            final int defaultMargin = context.getResources().getDimensionPixelSize(R.dimen.defaultMargin);
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(defaultMargin, 0, defaultMargin, 0);
            itemView.setBackgroundColor(AttributeGetter.getColor(context, R.attr.windowBackgroundDark));
        } else {
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(0, 0, 0, 0);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        final String backgroundColor = String.format("#%06X", (0xFFFFFF & AttributeGetter.getColor(context, android.R.attr.windowBackground)));
        final String textColor = String.format("#%06X", (0xFFFFFF & AttributeGetter.getColor(context, android.R.attr.textColor)));

        String fullHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta charset=\"utf-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, user-scalable=yes\" />" +
                "        <style>" +
                "table.wiki-content-table{border-collapse:collapse;border-spacing:0;margin:.5em auto}" +
                "table.wiki-content-table td{border:1px solid " + textColor + ";color: " + textColor + ";padding:.3em .7em;background-color:" + backgroundColor + "}" +
                "table.wiki-content-table th{border:1px solid " + textColor + ";color: " + textColor + ";padding:.3em .7em;background-color:" + backgroundColor + "}" +
                "</style>\n" +
                "    </head>\n" +
                "    <body>";
        fullHtml += (String) viewModel.data;
        fullHtml += "</body>\n" +
                "</html>";

        final WebSettings settings = webView.getSettings();
        settings.setMinimumFontSize(18);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        settings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, final String url) {
                super.onPageFinished(view, url);
//                Timber.d("onPageFinished: %s", url);

                final int indexOfHashTag = url.lastIndexOf("#");
                if (indexOfHashTag != -1) {
                    String link = url.substring(indexOfHashTag);
//                    Timber.d("link: %s", link);

                    if (checkUrl(link)) {
//                        Timber.d("Link clicked: %s", link);
                    }
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String link) {
//                Timber.d("Link clicked: %s", link);
                return checkUrl(link);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
//                Timber.d("Link clicked: %s", request.getUrl().toString());
                final String link = request.getUrl().toString();

                return checkUrl(link);
            }

            private boolean checkUrl(String link) {
                if (link.contains("javascript")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onUnsupportedLinkPressed(link);
                    }
                    return true;
                }
                if (TextUtils.isDigitsOnly(link)) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onSnoskaClicked(link);
                    }
                    return true;
                }
                if (link.startsWith("scp://")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onSnoskaClicked(link.replace("scp://", ""));
                    }
                    return true;
                }
                if (link.startsWith("bibitem-")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onBibliographyClicked(link);
                    }
                    return true;
                }
                if (link.startsWith("#")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onTocClicked(link);
                    }
                    return true;
                }
                if (!link.startsWith("http")) {
                    link = mConstantValues.getBaseApiUrl() + link;
                }

                if (link.endsWith(".mp3")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onMusicClicked(link);
                    }
                    return true;
                }

                if (link.endsWith(".jpg") || link.endsWith(".jpeg") || link.endsWith(".png") || link.endsWith(".gif")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onImageClicked(link, null);
                    }
                    return true;
                }

                if (link.startsWith(Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                    if (mTextItemsClickListener != null) {
                        final String url = link.split(Constants.Api.NOT_TRANSLATED_ARTICLE_URL_DELIMITER)[1];
                        mTextItemsClickListener.onNotTranslatedArticleClick(url);
                    }
                    return true;
                }

                if (!link.startsWith(mConstantValues.getBaseApiUrl())) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onExternalDomenUrlClicked(link);
                    }
                    return true;
                }

                if (mTextItemsClickListener != null) {
                    mTextItemsClickListener.onLinkClicked(link);
                    return true;
                }

                return false;
            }
        });

        webView.loadUrl("about:blank");
        webView.loadData(fullHtml, "text/html; charset=UTF-8", null);
    }
}