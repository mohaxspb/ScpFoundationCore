package ru.kuchanov.scpcore.ui.model;

import ru.kuchanov.scpcore.ui.adapter.ArticlesListRecyclerAdapter;

/**
 * Created by mohax on 21.09.2017.
 * <p>
 * for ScpCore
 */
public class ArticlesListModel {

    @ArticlesListRecyclerAdapter.ArticleListNodeType
    public int type;

    public Object data;

    public ArticlesListModel(@ArticlesListRecyclerAdapter.ArticleListNodeType int type, Object data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return "ArticlesListModel{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}