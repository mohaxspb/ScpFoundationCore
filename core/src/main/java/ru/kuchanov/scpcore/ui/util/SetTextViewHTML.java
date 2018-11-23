package ru.kuchanov.scpcore.ui.util;

import android.content.Context;
import android.support.annotation.NonNull;
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

import org.jetbrains.annotations.NotNull;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.ui.model.SpoilerViewModel;
import ru.kuchanov.scpcore.ui.model.TabsViewModel;
import ru.kuchanov.scpcore.util.AttributeGetter;
import timber.log.Timber;

public class SetTextViewHTML {

    @NotNull
    private final ConstantValues mConstantValues;

    public SetTextViewHTML(@NotNull final ConstantValues constantValues) {
        super();
        mConstantValues = constantValues;
    }

    public void setText(
            @NotNull final TextView textView,
            @NotNull final String html,
            @NotNull final TextItemsClickListener textItemsClickListener
    ) {
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
            public void updateDrawState(@NonNull final TextPaint ds) {
                super.updateDrawState(ds);
                if (span.getURL().startsWith(Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                    ds.setColor(ContextCompat.getColor(BaseApplication.getAppInstance(), R.color.material_red_500));
                }
            }

            @Override
            public void onClick(@NonNull final View view) {
                Timber.d("Link clicked: %s", span.getURL());
                if (textItemsClickListener == null) {
                    Timber.wtf("textItemsClickListener is NULL!!!11");
                    return;
                }

                final String link = span.getURL();
                final LinkType linkType = LinkType.getLinkType(link, mConstantValues);
                final String url = LinkType.getFormattedUrl(link, mConstantValues);

                switch (linkType) {
                    case JAVASCRIPT:
                        textItemsClickListener.onUnsupportedLinkPressed(url);
                        break;
                    case SNOSKA:
                        textItemsClickListener.onSnoskaClicked(url);
                        break;
                    case BIBLIOGRAPHY:
                        textItemsClickListener.onBibliographyClicked(url);
                        break;
                    case TOC:
                        textItemsClickListener.onTocClicked(url);
                        break;
                    case MUSIC:
                        textItemsClickListener.onMusicClicked(url);
                        break;
                    case NOT_TRANSLATED:
                        textItemsClickListener.onNotTranslatedArticleClick(url);
                        break;
                    case EXTERNAL:
                        textItemsClickListener.onExternalDomenUrlClicked(url);
                        break;
                    case INNER:
                        textItemsClickListener.onLinkClicked(url);
                        break;
                    default:
                        throw new IllegalArgumentException("unexpected url type");
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
            public void onClick(@NonNull final View widget) {
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
     * handle <blockquote>
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

    public enum LinkType {

        JAVASCRIPT, SNOSKA, BIBLIOGRAPHY, TOC, MUSIC, NOT_TRANSLATED, EXTERNAL, INNER;

        public static LinkType getLinkType(final String link, final ConstantValues mConstantValues) {
            if (link.contains("javascript")) {
                return JAVASCRIPT;
            }
            if (TextUtils.isDigitsOnly(link) || link.startsWith("scp://")) {
                return SNOSKA;
            }
            if (link.startsWith("bibitem-")) {
                return BIBLIOGRAPHY;
            }
            if (link.startsWith("#")) {
                return TOC;
            }
            if (link.endsWith(".mp3")) {
                return MUSIC;
            }
            if (link.startsWith(Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                return NOT_TRANSLATED;
            }
            if (!link.startsWith(mConstantValues.getBaseApiUrl()) ||
                    link.startsWith(mConstantValues.getBaseApiUrl() + "/forum")) {
                return EXTERNAL;
            }
            return INNER;
        }

        public static String getFormattedUrl(final String url, final ConstantValues constantValues) {
            final LinkType type = getLinkType(url, constantValues);
            switch (type) {
                case JAVASCRIPT:
                case INNER:
                case TOC:
                case MUSIC:
                case EXTERNAL:
                case BIBLIOGRAPHY:
                    if (!url.startsWith("http") && !url.startsWith(Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                        return constantValues.getBaseApiUrl() + url;
                    }
                    return url;
                case SNOSKA:
                    if (url.startsWith("scp://")) {
                        return url.replace("scp://", "");
                    }
                    return url;
                case NOT_TRANSLATED:
                    if (url.startsWith(Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                        return url.split(Constants.Api.NOT_TRANSLATED_ARTICLE_URL_DELIMITER)[1];
                    }
                    return url;
            }
            return url;
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