package ru.kuchanov.scpcore.ui.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.ui.model.SpoilerViewModel;
import ru.kuchanov.scpcore.ui.model.TabsViewModel;
import ru.kuchanov.scpcore.util.AttributeGetter;
import timber.log.Timber;

public class SetTextViewHTML {

    private final ConstantValues mConstantValues;

    public SetTextViewHTML(final ConstantValues constantValues) {
        super();
        mConstantValues = constantValues;
    }

    public void setText(final TextView textView, final String html, final TextItemsClickListener textItemsClickListener) {
        final Html.ImageGetter imgGetter = new URLImageParser(textView);
        final Html.TagHandler myHtmlTagHandler = new MyHtmlTagHandler();
        final CharSequence sequence = Html.fromHtml(html, imgGetter, myHtmlTagHandler);
        final SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        final URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (final URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, textItemsClickListener);
        }
        final ImageSpan[] imgs = strBuilder.getSpans(0, sequence.length(), ImageSpan.class);
        for (final ImageSpan span : imgs) {
            makeImgsClickable(strBuilder, span, textItemsClickListener);
        }
        replaceQuoteSpans(textView.getContext(), strBuilder);
        textView.setText(strBuilder);
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
                        final String url = link.split(Constants.Api.NOT_TRANSLATED_ARTICLE_URL_DELIMITER)[1];
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
            final SpannableStringBuilder strBuilder,
            final ImageSpan span,
            final TextItemsClickListener textItemsClickListener
    ) {
        final String imageSrc = span.getSource();
        final int start = strBuilder.getSpanStart(span);
        final int end = strBuilder.getSpanEnd(span);

        final ClickableSpan click_span = new ClickableSpan() {
            @Override
            public void onClick(final View widget) {
                Timber.d("makeImgsClickable Click: %s", imageSrc);
                if (textItemsClickListener != null) {
                    textItemsClickListener.onImageClicked(imageSrc, null);
                }
            }
        };
        final ClickableSpan[] click_spans = strBuilder.getSpans(start, end, ClickableSpan.class);

        if (click_spans.length != 0) {
            for (final ClickableSpan c_span : click_spans) {
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
    private static void replaceQuoteSpans(final Context context, final Spannable spannable) {
        final int colorBackground = AttributeGetter.getColor(context, R.attr.quoteBackgroundColor);
        final int colorStripe = AttributeGetter.getColor(context, R.attr.colorAccent);

        final QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);

        for (final QuoteSpan quoteSpan : quoteSpans) {
//            Timber.d("replaceQuoteSpans quoteSpan: %s", quoteSpan);
            final int start = spannable.getSpanStart(quoteSpan);
            final int end = spannable.getSpanEnd(quoteSpan);
            final int flags = spannable.getSpanFlags(quoteSpan);
            spannable.removeSpan(quoteSpan);
            spannable.setSpan(
                    new CustomQuoteSpan(
                            colorBackground,
                            colorStripe,
                            5,
                            10
                    ), start, end, flags);
        }
    }

    public interface TextItemsClickListener {

        void onLinkClicked(String link);

        void onSnoskaClicked(String link);

        void onBibliographyClicked(String link);

        void onTocClicked(String link);

        void onImageClicked(String link, @Nullable String description);

        void onUnsupportedLinkPressed(String link);

        void onMusicClicked(String link);

        void onExternalDomenUrlClicked(String link);

        void onTagClicked(ArticleTag tag);

        void onNotTranslatedArticleClick(String link);

        void onSpoilerExpand(SpoilerViewModel spoilerViewModel);

        void onSpoilerCollapse(SpoilerViewModel spoilerViewModel);

        void onTabSelected(TabsViewModel tabsViewModel);

        void onAdsSettingsClick();

        void onRewardedVideoClick();
    }
}