package ru.kuchanov.scpcore.monetization.model;

/**
 * Created by mohax on 24.02.2017.
 * <p>
 * for pacanskiypublic
 */
public class VkGroupToJoin extends BaseModel {

    public String id;
    public String name;
    public String description;

    /**
     * use it to check lists for containing app with specific package
     */
    public VkGroupToJoin(String id) {
        this.id = id;
    }

    public VkGroupToJoin() {
    }

    @Override
    public String toString() {
        return "VkGroupToJoin{" +
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

        VkGroupToJoin that = (VkGroupToJoin) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}