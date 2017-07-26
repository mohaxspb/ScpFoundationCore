package ru.kuchanov.scpcore.db.model;

import com.facebook.Profile;
import com.vk.sdk.VKAccessToken;

import java.io.Serializable;

import io.realm.RealmObject;
import ru.kuchanov.scpcore.Constants;

/**
 * Created by mohax on 25.03.2017.
 * <p>
 * for scp_ru
 */
public class SocialProviderModel extends RealmObject implements Serializable {

    public static final String FIELD_PROVIDER = "provider";
    public static final String FIELD_ID = "id";

    public String provider;

    public String id;

    public SocialProviderModel(String provider, String id) {
        this.provider = provider;
        this.id = id;
    }

    public SocialProviderModel() {
    }

    public static SocialProviderModel getSocialProviderModelForProvider(Constants.Firebase.SocialProvider provider) {
        switch (provider) {
            case VK:
                return new SocialProviderModel(provider.name(), VKAccessToken.currentToken().userId);
            case GOOGLE:
                return new SocialProviderModel(provider.name(), null);
            case FACEBOOK:
                return new SocialProviderModel(provider.name(), Profile.getCurrentProfile().getId());
            default:
                throw new IllegalArgumentException("unexpected provider");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocialProviderModel that = (SocialProviderModel) o;

        return provider.equals(that.provider);
    }

    @Override
    public int hashCode() {
        return provider.hashCode();
    }

    @Override
    public String toString() {
        return "SocialProviderModel{" +
                "provider='" + provider + '\'' +
                ", id=" + id +
                '}';
    }
}