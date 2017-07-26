package ru.kuchanov.scpcore.db.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mohax on 24.05.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleTag extends RealmObject implements Serializable{

    public static final String FIELD_TITLE = "title";

    @PrimaryKey
    public String title;

    public ArticleTag() {
    }

    public ArticleTag(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArticleTag tag = (ArticleTag) o;

        return title.equals(tag.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString() {
        return "ArticleTag{" +
                "title='" + title + '\'' +
                '}';
    }

    public static List<String> getStringsFromTags(List<ArticleTag> tags) {
        List<String> tagsStringlist = new ArrayList<>();
        if (tags == null) {
            return tagsStringlist;
        }
        for (ArticleTag tag : tags) {
            tagsStringlist.add(tag.title);
        }
        return tagsStringlist;
    }

    public static List<ArticleTag> getTagsFromStringList(List<String> tags) {
        List<ArticleTag> tagsStringlist = new ArrayList<>();
        if (tags == null) {
            return tagsStringlist;
        }
        for (String tag : tags) {
            tagsStringlist.add(new ArticleTag(tag));
        }
        return tagsStringlist;
    }
}