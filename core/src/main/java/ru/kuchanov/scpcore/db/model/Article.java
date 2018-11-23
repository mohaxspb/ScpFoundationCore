package ru.kuchanov.scpcore.db.model;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class Article extends RealmObject implements Serializable {

    public static final String FIELD_IS_IN_READEN = "isInReaden";

    public static final String FIELD_IS_IN_RECENT = "isInRecent";

    public static final String FIELD_IS_IN_FAVORITE = "isInFavorite";

    public static final String FIELD_IS_IN_MOST_RATED = "isInMostRated";

    public static final String FIELD_IS_IN_OBJECTS_1 = "isInObjects1";

    public static final String FIELD_IS_IN_OBJECTS_2 = "isInObjects2";

    public static final String FIELD_IS_IN_OBJECTS_3 = "isInObjects3";

    public static final String FIELD_IS_IN_OBJECTS_4 = "isInObjects4";

    public static final String FIELD_IS_IN_OBJECTS_5 = "isInObjects5";

    public static final String FIELD_IS_IN_OBJECTS_RU = "isInObjectsRu";

    public static final String FIELD_IS_IN_OBJECTS_FR = "isInObjectsFr";

    public static final String FIELD_IS_IN_OBJECTS_JP = "isInObjectsJp";

    public static final String FIELD_IS_IN_OBJECTS_ES = "isInObjectsEs";

    public static final String FIELD_IS_IN_OBJECTS_PL = "isInObjectsPl";

    public static final String FIELD_IS_IN_OBJECTS_DE = "isInObjectsDe";

    public static final String FIELD_IS_IN_EXPERIMETS = "isInExperiments";

    public static final String FIELD_IS_IN_OTHER = "isInOther";

    public static final String FIELD_IS_IN_INCIDENTS = "isInIncidents";

    public static final String FIELD_IS_IN_INTERVIEWS = "isInInterviews";

    public static final String FIELD_IS_IN_ARCHIVE = "isInArchive";

    public static final String FIELD_IS_IN_JOKES = "isInJokes";

    public static final String FIELD_URL = "url";

    //think if we really need it
    //yes, we need it to delete articles after limit reach
    //we sort by this value and delete old articles first
    public static final String FIELD_LOCAL_UPDATE_TIME_STAMP = "localUpdateTimeStamp";

    public static final String FIELD_TEXT = "text";

    public static final String FIELD_SYNCED = "synced";

    public static final String FIELD_TAGS = "tags";

    public static final String FIELD_INNER_ARTICLES_URLS = "innerArticlesUrls";

    public static final String FIELD_COMMENTS_URL = "commentsUrl";

    public static final int SYNCED_OK = 1;

    public static final int SYNCED_NEED = -1;

    public static final int ORDER_NONE = -1;

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ObjectType.NONE,
            ObjectType.NEUTRAL_OR_NOT_ADDED,
            ObjectType.SAFE,
            ObjectType.EUCLID,
            ObjectType.KETER,
            ObjectType.THAUMIEL
    })
    public @interface ObjectType {

        String NONE = "NONE"; //default
        String NEUTRAL_OR_NOT_ADDED = "na"; //na.png — Не назначен или нейтрализован
        String SAFE = "safe"; //safe.png — Безопасный
        String EUCLID = "euclid"; //euclid.png — Евклид
        String KETER = "keter"; //keter.png — Кетер
        String THAUMIEL = "thaumiel"; //thaumiel.png — Таумиэль
    }

    //util fields
    public long isInRecent = ORDER_NONE;

    public long isInFavorite = ORDER_NONE;

    public long isInMostRated = ORDER_NONE;

    public boolean isInReaden;

    public long isInObjects1 = ORDER_NONE;

    public long isInObjects2 = ORDER_NONE;

    public long isInObjects3 = ORDER_NONE;

    public long isInObjects4 = ORDER_NONE;

    public long isInObjects5 = ORDER_NONE;

    public long isInObjectsRu = ORDER_NONE;

    public long isInObjectsFr = ORDER_NONE;

    public long isInObjectsJp = ORDER_NONE;

    public long isInObjectsEs = ORDER_NONE;

    public long isInObjectsPl = ORDER_NONE;

    public long isInObjectsDe = ORDER_NONE;

    public long isInExperiments = ORDER_NONE;

    public long isInOther = ORDER_NONE;

    public long isInIncidents = ORDER_NONE;

    public long isInInterviews = ORDER_NONE;

    public long isInArchive = ORDER_NONE;

    public long isInJokes = ORDER_NONE;

    public long localUpdateTimeStamp;

    /**
     * we write true while update/write article from firebase data listener
     * <p>
     * default value is Integer.MAX_VALUE
     * <p>
     * if we made changes, that we need to sync we mark articles as unsynced with {@link #SYNCED_NEED}
     * <p>
     * if we made changes, and successfully sync them we mark it as synced with {@link #SYNCED_OK}
     * <p>
     */
    public int synced = Integer.MAX_VALUE;

    public RealmList<RealmString> textParts;

    public RealmList<RealmString> textPartsTypes;

    //images
    public RealmList<RealmString> imagesUrls;

    public RealmList<RealmString> innerArticlesUrls;

    //site info
    @PrimaryKey
    public String url;

    public String title;

    /**
     * raw html
     */
    public String text;

    @ObjectType
    public String type = ObjectType.NONE;

    public RealmList<ArticleTag> tags;

    public int rating;

    public String authorName;

    public String authorUrl;

    /**
     * in format 01:06 01.07.2010
     * <p>
     * or 17 Jan 2017 21:16
     */
    public String createdDate;

    /**
     * in format 01:06 01.07.2010
     * <p>
     * or 17 Jan 2017 21:16
     */
    public String updatedDate;

    /**
     * we use and show it only in site search
     */
    public String preview;

    public String commentsUrl;

    public List<String> getInnerArticlesUrls() {
        return RealmString.toStringList(innerArticlesUrls);
    }

    public static List<String> getListOfUrls(@NotNull final List<Article> articles) {
        final List<String> urls = new ArrayList<>();
        for (final Article article : articles) {
            urls.add(article.url);
        }
        return urls;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }

        if (!(o instanceof Article)) {
            return false;
        }

        final Article post = (Article) o;
        return url.equals(post.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return url;
    }

    @Nullable
    public String nextArticleUrl() {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        final String scpNumberUrlPath = "/scp-";
        final String scpNumberString = url.substring(url.lastIndexOf(scpNumberUrlPath) + scpNumberUrlPath.length());
        try {
            int scpNumber = Integer.parseInt(scpNumberString);
            scpNumber++;
            return url.replace(scpNumberUrlPath + scpNumberString, scpNumberUrlPath + scpNumber);
        } catch (final Exception e) {
            return null;
        }
    }

    //check dates and create proper comparator
/*
    public static final Comparator<Article> COMPARATOR_DATE_CREATED = (a1, a2) -> a1.createdDate == null ? a2.createdDate == null : a1.createdDate.compareTo(a1.createdDate);
    //a1.createdDate.compareTo(a2.createdDate);

    public static final Comparator<Article> COMPARATOR_DATE_UPDATED = (a1, a2) -> a1.updatedDate.compareTo(a2.updatedDate);
*/
    public static final Comparator<Article> COMPARATOR_DATE_RATING = (a1, a2) -> a2.rating - a1.rating;

    public static final Comparator<Article> COMPARATOR_TITLE = (a1, a2) -> a1.title == null ? -1 : a1.title.compareTo(a2.title);

    public static final Comparator<Article> COMPARATOR_READ_STATE = (a1, a2) -> Boolean.compare(a1.isInReaden, a2.isInReaden);
}