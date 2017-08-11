package ru.kuchanov.scpcore.ui.model;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import ru.kuchanov.scpcore.api.ParseHtmlUtils;
import ru.kuchanov.scpcore.db.model.ArticleTag;

/**
 * Created by mohax on 11.08.2017.
 * <p>
 * for ScpCore
 */
public class ArticleTextPartViewModel {

    //FIXME seems to be we do not need it...
    public int order;

    @ParseHtmlUtils.TextType
    public String type;

    public Object data;

    public ArticleTextPartViewModel(int order, @ParseHtmlUtils.TextType String type, Object data) {
        this.order = order;
        this.type = type;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArticleTextPartViewModel that = (ArticleTextPartViewModel) o;

        return order == that.order;
    }

    @Override
    public int hashCode() {
        return order;
    }

    @Override
    public String toString() {
//        return data.toString();
        if (data instanceof String) {
            return (String) data;
        } else if (data instanceof SpoilerViewModel) {
            return ((SpoilerViewModel) data).titles.get(0);
        }
//        else if(data instanceof RealmList){
//            return data.toString();
//        }
        else {
            return data.toString();
        }
    }

    public static List<String> convertToStringList(List<ArticleTextPartViewModel> data) {
        List<String> strings = new ArrayList<>();

        for (ArticleTextPartViewModel viewModel : data) {
            strings.add(viewModel.toString());
        }

        return strings;
    }
}