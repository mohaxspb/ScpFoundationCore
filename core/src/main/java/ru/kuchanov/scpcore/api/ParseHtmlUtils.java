package ru.kuchanov.scpcore.api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.kuchanov.scpcore.ui.model.TabsViewModel;

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

    /**
     * formats HTML img tags to common format
     */
    public static void parseImgsTags(final Element pageContent) {
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
                final Elements descriptions = rimg.getElementsByTag(TAG_SPAN);
                if (imgs != null && imgs.size() > 1 && descriptions.size() == imgs.size()) {
                    final Collection<Element> rimgsToAdd = new ArrayList<>();
                    for (int i = 0; i < imgs.size(); i++) {
                        final Element img = imgs.get(i);
                        final Element description = descriptions.get(i);
                        final Element newRimg = new Element(TAG_DIV);
                        newRimg.addClass(className);
                        newRimg.appendChild(img).appendChild(description);
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
//            Timber.d("pageContent.getElementsByClass(\"rimg\"): %s", pageContent.getElementsByClass("rimg"));
    }

    public static void extractTablesFromDivs(final Element pageContent) {
        for (final Element ourElement : pageContent.getElementsByTag(TAG_DIV)) {
            if (ourElement.children().size() == 1
                    && ourElement.child(0).tagName().equals(TAG_TABLE)
                    && !ourElement.hasClass("collapsible-block-content")) {
                ourElement.appendChild(ourElement.child(0));
                ourElement.remove();
            }
        }
    }

    //todo remake to intDef to be able to use as viewType in adapter
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            TextType.TEXT, TextType.SPOILER, TextType.IMAGE,
            TextType.TABLE, TextType.TITLE, TextType.TAGS,
            TextType.TABS,
            TextType.NATIVE_ADS_APPODEAL, TextType.NATIVE_ADS_SCP_ART, TextType.NATIVE_ADS_SCP_QUIZ

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
        String NATIVE_ADS_SCP_ART = "NATIVE_ADS_SCP_ART";
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
//        spoilerParts.add(elementExpanded.text().replaceAll("&nbsp;", " "));
        spoilerParts.add(elementExpanded.text().replaceAll("\\p{Z}", " "));
//        Timber.d("spoilerParts: %s", spoilerParts.get(1));

        final Element elementContent = elementUnfolded.getElementsByClass("collapsible-block-content").first();
        spoilerParts.add(elementContent.html());
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
                tabDataList.add(new TabsViewModel.TabData(tabsTextPartTypes, tabsTextParts));
            }

            return new TabsViewModel(tabsTitles, tabDataList, false);
        } else {
            throw new IllegalArgumentException("error parse tabs");
        }
    }
}