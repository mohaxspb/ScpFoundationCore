package ru.kuchanov.scpcore.db.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mohax on 03.12.2016.
 * <p>
 * for pacanskiypublic
 */
public class VkImage extends RealmObject {

    @PrimaryKey
    public int id;
    public int ownerId;
    public String photo75;
    public String photo130;
    public String photo604;
    public String photo807;
    public String photo1280;
    public String photo2560;
    public int width;
    public int height;
    public long date;
    public String description;

    //util field
    public RealmList<RealmString> allUrls;

    public VkImage() {
    }

    public VkImage(String imageUrl, String description) {
        this.allUrls = new RealmList<>(new RealmString(imageUrl));
        this.description = description;
    }

    @Override
    public String toString() {
        return "VkImage{" +
                "id='" + id + '\'' +
                ", ownerId=" + ownerId +
                ", photo75='" + photo75 + '\'' +
                ", photo130='" + photo130 + '\'' +
                ", photo604='" + photo604 + '\'' +
                ", photo807='" + photo807 + '\'' +
                ", photo1280='" + photo1280 + '\'' +
                ", photo2560='" + photo2560 + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", date=" + date +
                '}';
    }
}