package ru.kuchanov.scpcore.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.db.model.Article;

/**
 * Created by mohax on 25.05.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleFromSearchTagsOnSite {

    public int id;
    @SerializedName("name")
    public String url;
    public String title;
    @SerializedName("all_tags")
    public List<String> allTags;

    @Override
    public String toString() {
        return "ArticleFromSearchTagsOnSite{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", allTags=" + allTags +
                '}';
    }

    public static List<Article> getArticlesFromSiteArticles(List<ArticleFromSearchTagsOnSite> data) {
        List<Article> articles = new ArrayList<>();
        for (ArticleFromSearchTagsOnSite articleFromSearchTagsOnSite : data) {
            Article article = new Article();
            article.title = articleFromSearchTagsOnSite.title;
            article.url = articleFromSearchTagsOnSite.url;

            //TODO add tags

            articles.add(article);
        }
        return articles;
    }
}