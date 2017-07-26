package ru.kuchanov.scpcore.monetization.model;

/**
 * Created by mohax on 24.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class PlayMarketApplication extends BaseModel {

    public String id;
    public String name;
    public String description;

    /**
     * use it to check lists for containing app with specifik package
     */
    public PlayMarketApplication(String id) {
        this.id = id;
    }

    public PlayMarketApplication() {
    }

    @Override
    public String toString() {
        return "PlayMarketApplication{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayMarketApplication that = (PlayMarketApplication) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}