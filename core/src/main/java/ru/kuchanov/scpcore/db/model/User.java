package ru.kuchanov.scpcore.db.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class User extends RealmObject {

    public static final String FIELD_SCORE = "score";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_SOCIAL_PROVIDERS = "socialProviders";
    public static final String FIELD_EMAIL = "email";

    public String uid;
    public String fullName;

    public String avatar;

    public String email;

    public int score;

    private RealmList<SocialProviderModel> socialProviders;

    public User(
            final String uid,
            final String fullName,
            final String avatar,
            final String email,
            final int score,
            final RealmList<SocialProviderModel> socialProviders
    ) {
        this.uid = uid;
        this.fullName = fullName;
        this.avatar = avatar;
        this.email = email;
        this.score = score;
        this.socialProviders = socialProviders;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", fullName='" + fullName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", email='" + email + '\'' +
                ", score=" + score +
                ", socialProviders=" + socialProviders +
                '}';
    }
}