package ru.kuchanov.scpcore.monetization.model;

/**
 * Created by mohax on 04.03.2017.
 * <p>
 * for Vjux
 */
public class BaseModel {

    public String title;
    public String content;
    public String imageUrl;

    public BaseModel() {
    }

    public BaseModel(String title) {
        this.title = title;
    }

    public BaseModel(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public BaseModel(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "BaseModel{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}