package ru.kuchanov.scpcore.ui.model;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.api.ParseHtmlUtils;

/**
 * Created by mohax on 11.08.2017.
 * <p>
 * for ScpCore
 */
public class ArticleTextPartViewModel {

    @ParseHtmlUtils.TextType
    public String type;

    public Object data;

    public boolean isInSpoiler;

    public ArticleTextPartViewModel(
            @ParseHtmlUtils.TextType String type,
            Object data,
            boolean isInSpoiler
    ) {
        this.type = type;
        this.data = data;
        this.isInSpoiler = isInSpoiler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArticleTextPartViewModel viewModel = (ArticleTextPartViewModel) o;

        if (!type.equals(viewModel.type)) return false;
        return data.equals(viewModel.data);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
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