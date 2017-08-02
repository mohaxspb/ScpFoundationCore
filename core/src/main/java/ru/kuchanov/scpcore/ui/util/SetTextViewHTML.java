package ru.kuchanov.scpcore.ui.util;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.util.AttributeGetter;
import timber.log.Timber;

public class SetTextViewHTML {

    private ConstantValues mConstantValues;

    public SetTextViewHTML(ConstantValues constantValues) {
        mConstantValues = constantValues;
    }

    public void setText(TextView textView, String html, TextItemsClickListener textItemsClickListener) {
        URLImageParser imgGetter = new URLImageParser(textView);
        MyHtmlTagHandler myHtmlTagHandler = new MyHtmlTagHandler();
        CharSequence sequence = Html.fromHtml(html, imgGetter, myHtmlTagHandler);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, textItemsClickListener);
        }
        ImageSpan[] imgs = strBuilder.getSpans(0, sequence.length(), ImageSpan.class);
        for (ImageSpan span : imgs) {
            makeImgsClickable(strBuilder, span, textItemsClickListener);
        }
        replaceQuoteSpans(textView.getContext(), strBuilder);
        textView.setText(strBuilder);
    }

    private void makeLinkClickable(
            SpannableStringBuilder strBuilder,
            URLSpan span,
            TextItemsClickListener textItemsClickListener
    ) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Timber.d("Link clicked: %s", span.getURL());

                String link = span.getURL();
                if (link.contains("javascript")) {
                    if (textItemsClickListener != null) {
                        textItemsClickListener.onUnsupportedLinkPressed(link);
                    }
                    return;
                }
                if (TextUtils.isDigitsOnly(link)) {
                    if (textItemsClickListener != null) {
                        textItemsClickListener.onSnoskaClicked(link);
                    }
                    return;
                }
                if (link.startsWith("scp://")) {
                    if (textItemsClickListener != null) {
                        textItemsClickListener.onSnoskaClicked(link.replace("scp://", ""));
                    }
                    return;
                }
                if (link.startsWith("bibitem-")) {
                    if (textItemsClickListener != null) {
                        textItemsClickListener.onBibliographyClicked(link);
                    }
                    return;
                }
                if (link.startsWith("#")) {
                    if (textItemsClickListener != null) {
                        textItemsClickListener.onTocClicked(link);
                    }
                    return;
                }
                if (!link.startsWith("http") && !link.startsWith(Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                    link = mConstantValues.getBaseApiUrl() + link;
                }

                if (link.endsWith(".mp3")) {
                    if (textItemsClickListener != null) {
                        textItemsClickListener.onMusicClicked(link);
                    }
                    return;
                }

                if (link.startsWith(Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                    if (textItemsClickListener != null) {
                        String url = link.split(Constants.Api.NOT_TRANSLATED_ARTICLE_URL_DELIMITER)[1];
                        textItemsClickListener.onNotTranslatedArticleClick(url);
                    }
                    return;
                }

                if (!link.startsWith(mConstantValues.getBaseApiUrl())
                        || link.startsWith(mConstantValues.getBaseApiUrl() + "/forum")) {
                    if (textItemsClickListener != null) {
                        textItemsClickListener.onExternalDomenUrlClicked(link);
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

    private static void makeImgsClickable(
            SpannableStringBuilder strBuilder,
            ImageSpan span,
            TextItemsClickListener textItemsClickListener
    ) {
        final String imageSrc = span.getSource();
        final int start = strBuilder.getSpanStart(span);
        final int end = strBuilder.getSpanEnd(span);

        ClickableSpan click_span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Timber.d("makeImgsClickable Click: %s", imageSrc);
                if (textItemsClickListener != null) {
                    textItemsClickListener.onImageClicked(imageSrc);
                }
            }
        };
        ClickableSpan[] click_spans = strBuilder.getSpans(start, end, ClickableSpan.class);

        if (click_spans.length != 0) {
            for (ClickableSpan c_span : click_spans) {
                strBuilder.removeSpan(c_span);
            }
        }
        strBuilder.setSpan(click_span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * quotes
     *
     * @see <a href="http://stackoverflow.com/a/29114976/3212712">en-SO</a>
     */
    private static void replaceQuoteSpans(Context context, Spannable spannable) {
        int colorBackground = AttributeGetter.getColor(context, R.attr.quoteBackgroundColor);
        int colorStripe = AttributeGetter.getColor(context, R.attr.colorAccent);

        QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);

        for (QuoteSpan quoteSpan : quoteSpans) {
            int start = spannable.getSpanStart(quoteSpan);
            int end = spannable.getSpanEnd(quoteSpan);
            int flags = spannable.getSpanFlags(quoteSpan);
            spannable.removeSpan(quoteSpan);
            spannable.setSpan(
                    new CustomQuoteSpan(
                            colorBackground,
                            colorStripe,
                            5,
                            10),
                    start,
                    end,
                    flags);
        }
    }

    public interface TextItemsClickListener {

        void onLinkClicked(String link);

        void onSnoskaClicked(String link);

        void onBibliographyClicked(String link);

        void onTocClicked(String link);

        void onImageClicked(String link);

        void onUnsupportedLinkPressed(String link);

        void onMusicClicked(String link);

        void onExternalDomenUrlClicked(String link);

        void onTagClicked(ArticleTag tag);

        void onNotTranslatedArticleClick(String link);
    }
}