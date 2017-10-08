package ru.kuchanov.scpcore.api;

import android.support.annotation.StringDef;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.ui.model.TabsViewModel;

/**
 * Created by mohax on 05.01.2017.
 * <p>
 * for scp_ru
 */
public class ParseHtmlUtils {

    public static final String TAG_IMG = "img";
    public static final String TAG_SPAN = "span";
    public static final String TAG_DIV = "div";
    public static final String TAG_P = "p";
    public static final String TAG_BODY = "body";
    public static final String TAG_TABLE = "table";
    public static final String TAG_LI = "li";

    public static final String ID_PAGE_CONTENT = "page-content";

    public static final String CLASS_TABS = "yui-navset";
    public static final String CLASS_SPOILER = "collapsible-block";

    public static void parseImgsTags(Element pageContent) {
        parseRimgLimgCimgImages("rimg", pageContent);
        parseRimgLimgCimgImages("limg", pageContent);
        parseRimgLimgCimgImages("cimg", pageContent);
    }

    private static void parseRimgLimgCimgImages(String className, Element pageContent) {
        //parse multiple imgs in "rimg" tag
        Elements rimgs = pageContent.getElementsByClass(className);
//            Timber.d("rimg: %s", rimg);
        if (rimgs != null) {
            for (Element rimg : rimgs) {
                Elements imgs = rimg.getElementsByTag(TAG_IMG);
                Elements descriptions = rimg.getElementsByTag(TAG_SPAN);
                List<Element> rimgsToAdd = new ArrayList<>();
                if (imgs != null && imgs.size() > 1 && descriptions.size() == imgs.size()) {
                    for (int i = 0; i < imgs.size(); i++) {
                        Element img = imgs.get(i);
                        Element description = descriptions.get(i);
                        Element newRimg = new Element(TAG_DIV);
                        newRimg.addClass(className);
                        newRimg.appendChild(img).appendChild(description);
                        rimgsToAdd.add(newRimg);
                    }
                    Element rimgLast = rimg;
                    for (Element newRimg : rimgsToAdd) {
                        rimgLast.after(newRimg);
                        rimgLast = newRimg;
                    }
                    rimg.remove();
                }
            }
        }
//            Timber.d("pageContent.getElementsByClass(\"rimg\"): %s", pageContent.getElementsByClass("rimg"));
    }

    //todo remake to intDef to be able to use as viewType in adapter
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TextType.TEXT, TextType.SPOILER, TextType.IMAGE,
            TextType.TABLE, TextType.TITLE, TextType.TAGS,
            TextType.TABS, TextType.NATIVE_ADS_AD_MOB, TextType.NATIVE_ADS_APPODEAL})
    public @interface TextType {
        String TEXT = "TEXT";
        String SPOILER = "SPOILER";
        String IMAGE = "IMAGE";
        String TABLE = "TABLE";
        String TITLE = "TITLE";
        String TAGS = "TAGS";
        String TABS = "TABS";
        String NATIVE_ADS_AD_MOB = "NATIVE_ADS_AD_MOB";
        String NATIVE_ADS_APPODEAL = "NATIVE_ADS_APPODEAL";
    }

    public static List<String> getArticlesTextParts(String html) {
        List<String> articlesTextParts = new ArrayList<>();
        Document document = Jsoup.parse(html);
//        Timber.d(document.outerHtml());
        Element contentPage = document.getElementById(ID_PAGE_CONTENT);
        if (contentPage == null) {
            contentPage = document.body();
        }
        for (Element element : contentPage.children()) {
            articlesTextParts.add(element.outerHtml());
        }
        return articlesTextParts;
    }

    @TextType
    public static List<String> getListOfTextTypes(List<String> articlesTextParts) {
        @TextType
        List<String> listOfTextTypes = new ArrayList<>();
        for (String textPart : articlesTextParts) {
//            Timber.d("getListOfTextTypes: %s", textPart);
            Element element = Jsoup.parse(textPart);
            Element ourElement = element.getElementsByTag(TAG_BODY).first().children().first();
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

    public static List<String> parseSpoilerParts(String html) {
//        Timber.d("parseSpoilerParts: %s", html);
        List<String> spoilerParts = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Element element = document.getElementsByClass("collapsible-block-folded").first();
        Element elementA = element.getElementsByTag("a").first();
//        spoilerParts.add(elementA.text().replaceAll("&nbsp;", " "));
        spoilerParts.add(elementA.text().replaceAll("\\p{Z}", " "));
//        Timber.d("spoilerParts: %s", spoilerParts.get(0));

        Element elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first();

        Element elementExpanded = elementUnfolded.getElementsByClass("collapsible-block-link").first();
        //replacing non-breaking-spaces
//        spoilerParts.add(elementExpanded.text().replaceAll("&nbsp;", " "));
        spoilerParts.add(elementExpanded.text().replaceAll("\\p{Z}", " "));
//        Timber.d("spoilerParts: %s", spoilerParts.get(1));

        Element elementContent = elementUnfolded.getElementsByClass("collapsible-block-content").first();
        spoilerParts.add(elementContent.html());
        return spoilerParts;
    }

    public static TabsViewModel parseTabs(String html) {
//        Element yuiNavset = document.getElementsByAttributeValueStarting("class", "yui-navset").first();
        Document document = Jsoup.parse(html);
        Element yuiNavset = document.getElementsByClass(CLASS_TABS).first();
        if (yuiNavset != null) {
            Element titles = yuiNavset.getElementsByClass("yui-nav").first();
            Elements liElements = titles.getElementsByTag(TAG_LI);
            Element yuiContent = yuiNavset.getElementsByClass("yui-content").first();

            List<String> tabsTitles = new ArrayList<>();
            for (Element element : liElements) {
                tabsTitles.add(element.text());
            }

            //TODO add supporting inner articles ??? wtf where it can be found on site?
            List<TabsViewModel.TabData> tabDataList = new ArrayList<>();
            for (Element tab : yuiContent.children()) {
                tab.attr("id", ID_PAGE_CONTENT);
                String tabText = tab.outerHtml();

                List<String> tabsTextParts = getArticlesTextParts(tabText);
                List<String> tabsTextPartTypes = getListOfTextTypes(tabsTextParts);
                tabDataList.add(new TabsViewModel.TabData(tabsTextPartTypes, tabsTextParts));
            }

            return new TabsViewModel(tabsTitles, tabDataList, false);
        } else {
            throw new IllegalArgumentException("error parse tabs");
        }
    }
}