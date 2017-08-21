package ru.kuchanov.scpcore.api;

import android.support.annotation.StringDef;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by mohax on 05.01.2017.
 * <p>
 * for scp_ru
 */
public class ParseHtmlUtils {

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

    public static List<String> getSpoilerParts(String html) {
//        Timber.d("getSpoilerParts: %s", html);
        List<String> spoilerParts = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Element element = document.getElementsByClass("collapsible-block-folded").first();
        Element elementA = element.getElementsByTag("a").first();
//        spoilerParts.add(elementA.text().replaceAll("&nbsp;", " "));
        spoilerParts.add(elementA.text().replaceAll("\\p{Z}", " "));
        Timber.d("spoilerParts: %s", spoilerParts.get(0));

        Element elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first();

        Element elementExpanded = elementUnfolded.getElementsByClass("collapsible-block-link").first();
//        spoilerParts.add(elementExpanded.text().replaceAll("&nbsp;", " "));
        spoilerParts.add(elementExpanded.text().replaceAll("\\p{Z}", " "));
        Timber.d("spoilerParts: %s", spoilerParts.get(1));

        Element elementContent = elementUnfolded.getElementsByClass("collapsible-block-content").first();
        spoilerParts.add(elementContent.html());
        return spoilerParts;
    }
}