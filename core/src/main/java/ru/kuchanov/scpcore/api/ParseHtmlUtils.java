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

import io.realm.RealmList;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.ui.model.TabsViewModel;
import timber.log.Timber;

/**
 * Created by mohax on 05.01.2017.
 * <p>
 * for scp_ru
 */
public class ParseHtmlUtils {

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
                Elements imgs = rimg.getElementsByTag("img");
                Elements descriptions = rimg.getElementsByTag("span");
                List<Element> rimgsToAdd = new ArrayList<>();
                if (imgs != null && imgs.size() > 1 && descriptions.size() == imgs.size()) {
                    for (int i = 0; i < imgs.size(); i++) {
                        Element img = imgs.get(i);
                        Element description = descriptions.get(i);
                        Element newRimg = new Element("div");
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

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TextType.TEXT, TextType.SPOILER, TextType.IMAGE, TextType.TABLE, TextType.TITLE, TextType.TAGS})
    public @interface TextType {
        String TEXT = "TEXT";
        String SPOILER = "SPOILER";
        String IMAGE = "IMAGE";
        String TABLE = "TABLE";
        String TITLE = "TITLE";
        String TAGS = "TAGS";
    }

    public static List<String> getArticlesTextParts(String html) {
        List<String> articlesTextParts = new ArrayList<>();
        Document document = Jsoup.parse(html);
//        Timber.d(document.outerHtml());
        Element contentPage = document.getElementById("page-content");
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
            Element ourElement = element.getElementsByTag("body").first().children().first();
            if (ourElement == null) {
                listOfTextTypes.add(TextType.TEXT);
                continue;
            }
            if (ourElement.tagName().equals("p")) {
                listOfTextTypes.add(TextType.TEXT);
                continue;
            }
            if (ourElement.className().equals("collapsible-block")) {
                listOfTextTypes.add(TextType.SPOILER);
                continue;
            }
            if (ourElement.tagName().equals("table")) {
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
        Timber.d("spoilerParts: %s", spoilerParts.get(0));

        Element elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first();

        Element elementExpanded = elementUnfolded.getElementsByClass("collapsible-block-link").first();
        //replacing non-breaking-spaces
//        spoilerParts.add(elementExpanded.text().replaceAll("&nbsp;", " "));
        spoilerParts.add(elementExpanded.text().replaceAll("\\p{Z}", " "));
        Timber.d("spoilerParts: %s", spoilerParts.get(1));

        Element elementContent = elementUnfolded.getElementsByClass("collapsible-block-content").first();
        spoilerParts.add(elementContent.html());
        return spoilerParts;
    }

    public static TabsViewModel parseTabs(Document document) {
//        Element yuiNavset = document.getElementsByAttributeValueStarting("class", "yui-navset").first();
        Element yuiNavset = document.getElementsByClass("yui-navset").first();
        if (yuiNavset != null) {
            Element titles = yuiNavset.getElementsByClass("yui-nav").first();
            Elements liElements = titles.getElementsByTag("li");
            Element yuiContent = yuiNavset.getElementsByClass("yui-content").first();

            List<String> tabsTitles = new ArrayList<>();
            for (Element element : liElements) {
                tabsTitles.add(element.text());
            }

            //TODO add supporting inner articles ??? wtf where it can be found on site?
            List<TabsViewModel.TabData> tabDataList = new ArrayList<>();
            for (Element tab : yuiContent.children()) {
                tab.attr("id", "page-content");
                String tabText = tab.outerHtml();

                List<String> tabsTextParts = getArticlesTextParts(tabText);
                List<String> tabsTextPartTypes = getListOfTextTypes(tabsTextParts);
                tabDataList.add(new TabsViewModel.TabData(tabsTextPartTypes, tabsTextParts));
            }

            return new TabsViewModel(tabsTitles, tabDataList);
        } else {
            return null;
        }
    }
}