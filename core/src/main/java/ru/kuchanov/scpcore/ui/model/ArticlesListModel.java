package ru.kuchanov.scpcore.ui.model;

import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter;

/**
 * Created by mohax on 21.09.2017.
 * <p>
 * for ScpCore
 */
public class ArticlesListModel implements MyListItem {

    @ArticlesListAdapter.ArticleListNodeType
    public int type;

    public Object data;

    public ArticlesListModel(@ArticlesListAdapter.ArticleListNodeType final int type, final Object data) {
        super();
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