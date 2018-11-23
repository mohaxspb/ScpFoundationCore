package ru.kuchanov.scpcore.ui.model;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.api.ParseHtmlUtils;
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem;

/**
 * Created by mohax on 11.08.2017.
 * <p>
 * for ScpCore
 */
public class ArticleTextPartViewModel implements MyListItem {

    @ParseHtmlUtils.TextType
    public final String type;

    public final Object data;

    public final boolean isInSpoiler;

    public ArticleTextPartViewModel(
            @ParseHtmlUtils.TextType final String type,
            final Object data,
            final boolean isInSpoiler
    ) {
        super();
        this.type = type;
        this.data = data;
        this.isInSpoiler = isInSpoiler;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ArticleTextPartViewModel viewModel = (ArticleTextPartViewModel) o;

        return type.equals(viewModel.type) && data.equals(viewModel.data);
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
        } else {
            return data.toString();
        }
    }

    public static List<String> convertToStringList(final Iterable<MyListItem> data) {
        final List<String> strings = new ArrayList<>();

        for (final MyListItem viewModel : data) {
            strings.add(viewModel.toString());
        }

        return strings;
    }
}