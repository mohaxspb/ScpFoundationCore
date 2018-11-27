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
public class ArticleTag extends RealmObject implements Serializable {

    public static final String FIELD_TITLE = "title";

    @PrimaryKey
    public String title;

    public ArticleTag() {
        super();
    }

    public ArticleTag(final String title) {
        super();
        this.title = title;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ArticleTag tag = (ArticleTag) o;

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

    public static List<String> getStringsFromTags(final List<ArticleTag> tags) {
        final List<String> tagsStringlist = new ArrayList<>();
        if (tags == null) {
            return tagsStringlist;
        }
        for (final ArticleTag tag : tags) {
            tagsStringlist.add(tag.title);
        }
        return tagsStringlist;
    }

    public static String getCommaSeparatedStringFromTags(final List<ArticleTag> tags) {
        if (tags == null) {
            return "";
        }
        final StringBuilder result = new StringBuilder();
//        for (final ArticleTag tag : tags) {
        for (int i = 0; i < tags.size(); i++) {
            if (i == 0) {
                result.append(tags.get(i).title);
            } else {
                result.append(",").append(tags.get(i).title);
            }
        }
        return result.toString();
    }

    public static List<ArticleTag> getTagsFromStringList(final List<String> tags) {
        final List<ArticleTag> tagsStringlist = new ArrayList<>();
        if (tags == null) {
            return tagsStringlist;
        }
        for (final String tag : tags) {
            tagsStringlist.add(new ArticleTag(tag));
        }
        return tagsStringlist;
    }
}