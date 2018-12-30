package ru.kuchanov.scpcore.api;

import android.support.annotation.StringDef;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.RealmList;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.ui.model.TabsViewModel;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import timber.log.Timber;

/**
 * Created by mohax on 05.01.2017.
 * <p>
 * for scp_ru
 */
public class ParseHtmlUtils {

    private static final String TAG_IMG = "img";

    private static final String TAG_SPAN = "span";

    private static final String TAG_DIV = "div";

    private static final String TAG_P = "p";

    private static final String TAG_BODY = "body";

    private static final String TAG_TABLE = "table";

    private static final String TAG_LI = "li";

    private static final String ID_PAGE_CONTENT = "page-content";

    private static final String CLASS_TABS = "yui-navset";

    private static final String CLASS_SPOILER = "collapsible-block";

    //misc
    private static final String SITE_TAGS_PATH = "system:page-tags/tag/";

    /**
     * formats HTML img tags to common format
     */
    private static void parseImgsTags(final Element pageContent) {
        parseRimgLimgCimgImages("rimg", pageContent);
        parseRimgLimgCimgImages("limg", pageContent);
        parseRimgLimgCimgImages("cimg", pageContent);
    }

    private static void parseRimgLimgCimgImages(final String className, final Element pageContent) {
        //parse multiple imgs in "rimg" tag
        final Elements rimgs = pageContent.getElementsByClass(className);
//            Timber.d("rimg: %s", rimg);
        if (rimgs != null) {
            for (final Element rimg : rimgs) {
                final Elements imgs = rimg.getElementsByTag(TAG_IMG);
//                Timber.d("imgs: %s", imgs);
//                Timber.d("descriptions: %s", descriptions);
                if (imgs != null && imgs.size() > 1) {
                    final Collection<Element> rimgsToAdd = new ArrayList<>();
                    for (int i = 0; i < imgs.size(); i++) {
                        final Element img = imgs.get(i);

                        Element nextImgSibling = img.nextElementSibling();
//                        Timber.d("nextImgSibling: %s", nextImgSibling);

                        final Collection<Element> descriptions = new Elements();
                        while (nextImgSibling != null && !nextImgSibling.tagName().equals("img")) {
                            descriptions.add(nextImgSibling);
                            nextImgSibling = nextImgSibling.nextElementSibling();
                        }
//                        Timber.d("descriptions: %s", descriptions);

                        final Element newRimg = new Element(TAG_DIV);
                        newRimg.addClass(className);
                        newRimg.appendChild(img);

                        for (final Element element : descriptions) {
                            newRimg.appendChild(element);
                        }

                        rimgsToAdd.add(newRimg);
                    }
                    Element rimgLast = rimg;
                    for (final Element newRimg : rimgsToAdd) {
                        rimgLast.after(newRimg);
                        rimgLast = newRimg;
                    }
                    rimg.remove();
                }
            }
        }
    }

    private static void extractTablesFromDivs(final Element pageContent) {
//        Timber.d("extractTablesFromDivs");
        for (final Element div : pageContent.getElementsByTag(TAG_DIV)) {
            if (div.children().size() == 1
                    && div.child(0).tagName().equals(TAG_TABLE)
                    && !div.hasClass("collapsible-block-content")) {
//                Timber.d("extractTablesFromDivs: %s", div.child(0));
                div.after(div.child(0));
                div.remove();
            }
        }
    }

    //todo remake to intDef to be able to use as viewType in adapter
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            TextType.TEXT, TextType.SPOILER, TextType.IMAGE,
            TextType.TABLE, TextType.TITLE, TextType.TAGS,
            TextType.TABS,
            TextType.NATIVE_ADS_APPODEAL, TextType.NATIVE_ADS_SCP_QUIZ
    })
    public @interface TextType {

        String TEXT = "TEXT";
        String SPOILER = "SPOILER";
        String IMAGE = "IMAGE";
        String TABLE = "TABLE";
        String TITLE = "TITLE";
        String TAGS = "TAGS";
        String TABS = "TABS";
        String NATIVE_ADS_APPODEAL = "NATIVE_ADS_APPODEAL";
        String NATIVE_ADS_SCP_QUIZ = "NATIVE_ADS_SCP_QUIZ";
    }

    public static List<String> getArticlesTextParts(final String html) {
        final Document document = Jsoup.parse(html);
//        Timber.d(document.outerHtml());
        Element contentPage = document.getElementById(ID_PAGE_CONTENT);
        if (contentPage == null) {
            contentPage = document.body();
        }
        final List<String> articlesTextParts = new ArrayList<>();
        for (final Element element : contentPage.children()) {
            articlesTextParts.add(element.outerHtml());
        }
        return articlesTextParts;
    }

    @TextType
    public static List<String> getListOfTextTypes(final Iterable<String> articlesTextParts) {
        @TextType final List<String> listOfTextTypes = new ArrayList<>();
        for (final String textPart : articlesTextParts) {
//            Timber.d("getListOfTextTypes: %s", textPart);
            final Element element = Jsoup.parse(textPart);
            final Element ourElement = element.getElementsByTag(TAG_BODY).first().children().first();
            if (ourElement == null) {
                listOfTextTypes.add(TextType.TEXT);
                continue;
            }
            if (ourElement.tagName().equals(TAG_P)) {
                listOfTextTypes.add(TextType.TEXT);
                continue;
            }
            if (ourElement.className().equals(CLASS_SPOILER)) {
                listOfTextTypes.add(TextType.SPOILER);
                continue;
            }
            if (ourElement.classNames().contains(CLASS_TABS)) {
                listOfTextTypes.add(TextType.TABS);
                continue;
            }
            if (ourElement.tagName().equals(TAG_TABLE)) {
                listOfTextTypes.add(TextType.TABLE);
                continue;
            }
            if (ourElement.className().equals("rimg")
                    || ourElement.className().equals("limg")
                    || ourElement.className().equals("cimg")
                    || ourElement.classNames().contains("scp-image-block")) {
                listOfTextTypes.add(TextType.IMAGE);
                continue;
            }
            listOfTextTypes.add(TextType.TEXT);
        }

        return listOfTextTypes;
    }

    public static List<String> parseSpoilerParts(final String html) {
//        Timber.d("parseSpoilerParts: %s", html);
        final List<String> spoilerParts = new ArrayList<>();
        final Document document = Jsoup.parse(html);
        final Element element = document.getElementsByClass("collapsible-block-folded").first();
        final Element elementA = element.getElementsByTag("a").first();
//        spoilerParts.add(elementA.text().replaceAll("&nbsp;", " "));
        spoilerParts.add(elementA.text().replaceAll("\\p{Z}", " "));
//        Timber.d("spoilerParts: %s", spoilerParts.get(0));

        final Element elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first();

        final Element elementExpanded = elementUnfolded.getElementsByClass("collapsible-block-link").first();
        //replacing non-breaking-spaces
        spoilerParts.add(elementExpanded.text().replaceAll("\\p{Z}", " "));
//        Timber.d("spoilerParts: %s", spoilerParts.get(1));

        final Element elementContent = elementUnfolded.getElementsByClass("collapsible-block-content").first();
        if (elementContent != null) {
            spoilerParts.add(elementContent.html());
        } else {
            spoilerParts.add("ERROR WHILE PARSING SPOILER CONTENT. Please, let developers know about it, if you see this message)");
        }
        return spoilerParts;
    }

    public static TabsViewModel parseTabs(final String html) {
//        Element yuiNavset = document.getElementsByAttributeValueStarting("class", "yui-navset").first();
        final Document document = Jsoup.parse(html);
        final Element yuiNavset = document.getElementsByClass(CLASS_TABS).first();
        if (yuiNavset != null) {
            final Element titles = yuiNavset.getElementsByClass("yui-nav").first();
            final Elements liElements = titles.getElementsByTag(TAG_LI);
            final Element yuiContent = yuiNavset.getElementsByClass("yui-content").first();

            final List<String> tabsTitles = new ArrayList<>();
            for (final Element element : liElements) {
                tabsTitles.add(element.text());
            }

            //TODO add supporting inner articles ??? wtf where it can be found on site?
            final List<TabsViewModel.TabData> tabDataList = new ArrayList<>();
            for (final Element tab : yuiContent.children()) {
                tab.attr("id", ID_PAGE_CONTENT);
                final String tabText = tab.outerHtml();

                final List<String> tabsTextParts = getArticlesTextParts(tabText);
                final List<String> tabsTextPartTypes = getListOfTextTypes(tabsTextParts);

//                Timber.d("TAB!!!");
//                Timber.d("tabsTextParts: %s", tabsTextParts);
//                Timber.d("tabsTextPartTypes: %s", tabsTextPartTypes);
//                Timber.d("TAB!!!");

                tabDataList.add(new TabsViewModel.TabData(tabsTextPartTypes, tabsTextParts));
            }

            return new TabsViewModel(tabsTitles, tabDataList, false);
        } else {
            throw new IllegalArgumentException("error parse tabs");
        }
    }

    public static Article parseArticle(
            @NotNull final String url,
            @NotNull final Document doc,
            @NotNull final Element pageContent,
            @NotNull final ConstantValues constantValues
    ) throws Exception {
        try {
            //some article are in div... I.e. http://scp-wiki-cn.wikidot.com/taboo
            //so check it and extract text
            if (pageContent.children().size() == 1
                    && pageContent.children().first().tagName().equals("div")) {
                final Element theOnlyChildDiv = pageContent.children().first();

                Node child = theOnlyChildDiv.children().first();

                final Collection<Node> children = new ArrayList<>();
                while (child != null) {
                    children.add(child);
                    child = child.nextSibling();
                }

                Node prev = theOnlyChildDiv;
                for (final Node node : children) {
                    prev.after(node);
                    prev = node;
                }

                theOnlyChildDiv.remove();
            }


            //замена ссылок в сносках
            final Elements footnoterefs = pageContent.getElementsByClass("footnoteref");
            for (final Element snoska : footnoterefs) {
                final Element aTag = snoska.getElementsByTag("a").first();
                final StringBuilder digits = new StringBuilder();
                for (final char c : aTag.id().toCharArray()) {
                    if (TextUtils.isDigitsOnly(String.valueOf(c))) {
                        digits.append(String.valueOf(c));
                    }
                }
                aTag.attr("href", "scp://" + digits.toString());
            }
            final Elements footnoterefsFooter = pageContent.getElementsByClass("footnote-footer");
            for (final Element snoska : footnoterefsFooter) {
                final Element aTag = snoska.getElementsByTag("a").first();
                snoska.prependText(aTag.text());
                aTag.remove();
//                    aTag.replaceWith(new Element(Tag.valueOf("pizda"), aTag.text()));
            }

            //замена ссылок в библиографии
            final Elements bibliographi = pageContent.getElementsByClass("bibcite");
            for (final Element snoska : bibliographi) {
                final Element aTag = snoska.getElementsByTag("a").first();
                final String onclickAttr = aTag.attr("onclick");

                final String id = onclickAttr.substring(onclickAttr.indexOf("bibitem-"), onclickAttr.lastIndexOf("'"));
                aTag.attr("href", id);
            }
            //remove rating bar
            int rating = 0;
            final Element rateDiv = pageContent.getElementsByClass("page-rate-widget-box").first();
            if (rateDiv != null) {
                final Element spanWithRating = rateDiv.getElementsByClass("rate-points").first();
                if (spanWithRating != null) {
                    final Element ratingSpan = spanWithRating.getElementsByClass("number").first();
//                    Timber.d("ratingSpan: %s", ratingSpan);
                    if (ratingSpan != null && !TextUtils.isEmpty(ratingSpan.text())) {
                        try {
                            rating = Integer.parseInt(ratingSpan.text().substring(1, ratingSpan.text().length()));
//                            Timber.d("rating: %s", rating);
                        } catch (final Exception e) {
                            Timber.e(e);
                        }
                    }
                }

                final Element span1 = rateDiv.getElementsByClass("rateup").first();
                span1.remove();
                final Element span2 = rateDiv.getElementsByClass("ratedown").first();
                span2.remove();
                final Element span3 = rateDiv.getElementsByClass("cancel").first();
                span3.remove();

                final Elements heritageDiv = rateDiv.parent().getElementsByClass("heritage-emblem");
                if (heritageDiv != null && !heritageDiv.isEmpty()) {
                    heritageDiv.first().remove();
                }
            }
            //remove something more
            final Element svernut = pageContent.getElementById("toc-action-bar");
            if (svernut != null) {
                svernut.remove();
            }
            final Elements script = pageContent.getElementsByTag("script");
            for (final Element element : script) {
                element.remove();
            }
            //remove audio link from DE version
            final Elements audio = pageContent.getElementsByClass("audio-img-block");
            if (audio != null) {
                audio.remove();
            }
            final Elements audioContent = pageContent.getElementsByClass("audio-block");
            if (audioContent != null) {
                audioContent.remove();
            }
            final Elements creditRate = pageContent.getElementsByClass("creditRate");
            if (creditRate != null) {
                creditRate.remove();
            }

            final Element uCreditView = pageContent.getElementById("u-credit-view");
            if (uCreditView != null) {
                uCreditView.remove();
            }
            final Element uCreditOtherwise = pageContent.getElementById("u-credit-otherwise");
            if (uCreditOtherwise != null) {
                uCreditOtherwise.remove();
            }
            //remove audio link from DE version END

            //replace all spans with strike-through with <s>
            final Elements spansWithStrike = pageContent.select("span[style=text-decoration: line-through;]");
            for (final Element element : spansWithStrike) {
//                    Timber.d("element: %s", element);
                element.tagName("s");
                for (final Attribute attribute : element.attributes()) {
                    element.removeAttr(attribute.getKey());
                }
//                    Timber.d("element refactored: %s", element);
            }

            //some fucking articles have all its content in 2 div... WTF?! One more fucking Kludge.
            //see http://scpfoundation.net/scp-2111/offset/2
            final Element divWithAllContent = pageContent.getElementsByClass("list-pages-box").first();
            if (divWithAllContent != null) {
                final Element innerDiv = divWithAllContent.getElementsByClass("list-pages-item").first();
                if (innerDiv != null) {
                    Element prevElement = divWithAllContent;
                    for (final Element contentElement : innerDiv.children()) {
                        prevElement.after(contentElement);
                        prevElement = contentElement;
                    }
                    divWithAllContent.remove();
                }
            }

            //get title
            final Element titleEl = doc.getElementById("page-title");
            String title = "";
            if (titleEl != null) {
                title = titleEl.text();
            } else if (url.contains(SITE_TAGS_PATH)) {
                final String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
                final String tagName = decodedUrl.substring(url.lastIndexOf(SITE_TAGS_PATH) + SITE_TAGS_PATH.length());
                title = "TAG: " + tagName;
            }
            final Element upperDivWithLink = doc.getElementById("breadcrumbs");
            if (upperDivWithLink != null) {
                pageContent.prependChild(upperDivWithLink);
            }
            ParseHtmlUtils.parseImgsTags(pageContent);

            //extract tables, which are single tag in div
            ParseHtmlUtils.extractTablesFromDivs(pageContent);
//            Timber.d("after tables extract: %s", pageContent.getElementsByTag("table").first().outerHtml());

            //put all text which is not in any tag in div tag
            for (final Element element : pageContent.children()) {
                final Node nextSibling = element.nextSibling();
//                    Timber.d("child: ___%s___", nextSibling);
//                    Timber.d("nextSibling.nodeName(): %s", nextSibling.nodeName());
                if (nextSibling != null && !nextSibling.toString().equals(" ") && nextSibling.nodeName().equals("#text")) {
                    element.after(new Element("div").appendChild(nextSibling));
                }

                //also fix scp-3000, where image and spoiler are in div tag, fucking shit! Web monkeys, ARGH!!!
                if (!element.children().isEmpty() && element.children().size() == 2
                        && element.child(0).tagName().equals("img") && element.child(1).className().equals("collapsible-block")) {
                    element.before(element.childNode(0));
                    element.after(element.childNode(1));
                    element.remove();
                }
            }

            //replace styles with underline and strike
            final Elements spans = pageContent.getElementsByTag(TAG_SPAN);
            for (final Element element : spans) {
                //<span style="text-decoration: underline;">PLEASE</span>
                if (element.hasAttr("style") && element.attr("style").equals("text-decoration: underline;")) {
//                    Timber.d("fix underline span: %s", element.outerHtml());
                    final Element uTag = new Element(Tag.valueOf("u"), "").text(element.text());
                    element.replaceWith(uTag);
//                    Timber.d("fixED underline span: %s", uTag.outerHtml());
                }
                //<span style="text-decoration: line-through;">условия содержания.</span>
                if (element.hasAttr("style") && element.attr("style").equals("text-decoration: line-through;")) {
//                    Timber.d("fix strike span");
                    final Element sTag = new Element(Tag.valueOf("s"), "");
                    element.replaceWith(sTag);
                }
            }

            //search for relative urls to add domain
            for (final Element a : pageContent.getElementsByTag("a")) {
                //replace all links to not translated articles
                if (a.className().equals("newpage")) {
                    a.attr("href", Constants.Api.NOT_TRANSLATED_ARTICLE_UTIL_URL
                            + Constants.Api.NOT_TRANSLATED_ARTICLE_URL_DELIMITER
                            + a.attr("href")
                    );
                } else if (a.attr("href").startsWith("/")) {
                    a.attr("href", constantValues.getBaseApiUrl() + a.attr("href"));
                }
            }

            //extract tags
            final RealmList<ArticleTag> articleTags = new RealmList<>();
            final Element tagsContainer = doc.getElementsByClass("page-tags").first();
//                Timber.d("tagsContainer: %s", tagsContainer);
            if (tagsContainer != null) {
                for (final Element a : tagsContainer./*getElementsByTag("span").first().*/getElementsByTag("a")) {
                    articleTags.add(new ArticleTag(a.text()));
//                        Timber.d("tag: %s", articleTags.get(articleTags.size() - 1));
                }
            }

            //search for images and add it to separate field to be able to show it in arts lists
            RealmList<RealmString> imgsUrls = null;
            final Elements imgsOfArticle = pageContent.getElementsByTag("img");
            if (!imgsOfArticle.isEmpty()) {
                imgsUrls = new RealmList<>();
                for (final Element img : imgsOfArticle) {
                    imgsUrls.add(new RealmString(img.attr("src")));
                }
            }

            //search for inner articles
            RealmList<RealmString> innerArticlesUrls = null;
            final Elements innerATags = pageContent.getElementsByTag("a");
            if (!innerATags.isEmpty()) {
                innerArticlesUrls = new RealmList<>();
                for (final Element a : innerATags) {
                    final String innerUrl = a.attr("href");
                    if (SetTextViewHTML.LinkType.getLinkType(innerUrl, constantValues) == SetTextViewHTML.LinkType.INNER) {
                        innerArticlesUrls.add(new RealmString(SetTextViewHTML.LinkType.getFormattedUrl(innerUrl, constantValues)));
                    }
                }
            }

            //type parsing TODO fucking unformatted info!

            //this we store as article text
            final String rawText = pageContent.toString();
//            Timber.d("rawText: %s", rawText);

            //articles textParts
            final RealmList<RealmString> textParts = new RealmList<>();
            final List<String> rawTextParts = ParseHtmlUtils.getArticlesTextParts(rawText);
            for (final String value : rawTextParts) {
                textParts.add(new RealmString(value));
            }
            final RealmList<RealmString> textPartsTypes = new RealmList<>();
            for (@ParseHtmlUtils.TextType final String value : ParseHtmlUtils.getListOfTextTypes(rawTextParts)) {
                textPartsTypes.add(new RealmString(value));
            }

            String commentsUrl = null;
            final Element commentsButtonTag = doc.getElementById("discuss-button");
            if (commentsButtonTag != null) {
                commentsUrl = constantValues.getBaseApiUrl() + commentsButtonTag.attr("href");
            }

            //finally fill article info
            final Article article = new Article();

            article.url = url;
            article.text = rawText;
            article.title = title;
            //textParts
            article.textParts = textParts;
            //log
//                if (article.textParts != null) {
//                    for (RealmString realmString : article.textParts) {
//                        Timber.d("part: %s", realmString.val);
//                    }
//                } else {
//                    Timber.d("article.textParts is NULL!");
//                }
            article.textPartsTypes = textPartsTypes;
            //images
            article.imagesUrls = imgsUrls;
            //inner articles
            article.innerArticlesUrls = innerArticlesUrls;
            //tags
            article.tags = articleTags;
            //rating
            if (rating != 0) {
                article.rating = rating;
            }

            Timber.d("commentsUrl: %s", commentsUrl);
            article.commentsUrl = commentsUrl;

            return article;
        } catch (final Exception e) {
            Timber.e(e);
            throw e;
        }
    }
}