package ru.kuchanov.scpcore.api.model.firebase;

import java.io.Serializable;

/**
 * Created by mohax on 26.03.2017.
 * <p>
 * for scp_ru
 */
public class ArticleInFirebase implements Serializable {

    public boolean isFavorite;
    public boolean isRead;
    public String title;
    public String url;
    public long updated;

    public ArticleInFirebase(boolean isFavorite, boolean isRead, String title, String url, long updated) {
        this.isFavorite = isFavorite;
        this.isRead = isRead;
        this.title = title;
        this.url = url;
        this.updated = updated;
    }

    @SuppressWarnings("unused")
    public ArticleInFirebase() {
    }

    @Override
    public String toString() {
        return "ArticleInFirebase{" +
                "isFavorite=" + isFavorite +
                ", isRead=" + isRead +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", updated=" + updated +
                '}';
    }
}