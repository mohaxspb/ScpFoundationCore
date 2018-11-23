package ru.kuchanov.scpcore.ui.model;

import java.io.Serializable;
import java.util.List;

import ru.kuchanov.scpcore.api.ParseHtmlUtils;

/**
 * Created by mohax on 10.08.2017.
 * <p>
 * for ScpCore
 */
public class SpoilerViewModel implements Serializable {

    public boolean isExpanded;

    public List<String> titles;

    @ParseHtmlUtils.TextType
    public List<String> mSpoilerTextPartsTypes;

    public List<String> mSpoilerTextParts;

    /**
     * just spoiler order in spoilers list
     */
    public int id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpoilerViewModel that = (SpoilerViewModel) o;

        return mSpoilerTextParts.equals(that.mSpoilerTextParts);
    }

    @Override
    public int hashCode() {
        return mSpoilerTextParts.hashCode();
    }

    @Override
    public String toString() {
        return "SpoilerViewModel{" +
                "titles=" + titles +
                ", id=" + id +
                '}';
    }
}