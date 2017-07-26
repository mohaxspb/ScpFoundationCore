package ru.kuchanov.scpcore.api.model.firebase;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import ru.kuchanov.scpcore.db.model.SocialProviderModel;
import ru.kuchanov.scpcore.db.model.User;

/**
 * Created by mohax on 26.03.2017.
 * <p>
 * for scp_ru
 * <p>
 * We need it as Realm stores List as RealmList, but firebase uses ArrayList
 * so we need to convert types...
 */
public class FirebaseObjectUser implements Serializable{

    public String uid;

    public String fullName;

    public String avatar;

    public String email;

    public int score;

    public List<SocialProviderModel> socialProviders;

    public Map<String, ArticleInFirebase> articles;

    public User toRealmUser() {
        return new User(uid, fullName, avatar, email, score, new RealmList<SocialProviderModel>() {{
            addAll(socialProviders);
        }});
    }

    @Override
    public String toString() {
        return "FirebaseObjectUser{" +
                "uid='" + uid + '\'' +
                ", fullName='" + fullName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", email='" + email + '\'' +
                ", score=" + score +
                ", socialProviders=" + socialProviders +
                ", articles=" + articles +
                '}';
    }
}