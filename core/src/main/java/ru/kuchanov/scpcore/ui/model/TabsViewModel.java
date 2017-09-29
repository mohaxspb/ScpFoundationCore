package ru.kuchanov.scpcore.ui.model;

import java.io.Serializable;
import java.util.List;

import ru.kuchanov.scpcore.api.ParseHtmlUtils;

/**
 * Created by mohax on 10.08.2017.
 * <p>
 * for ScpCore
 */
public class TabsViewModel implements Serializable {

    private List<String> mTitles;
    private List<TabData> mTabDataList;

    public TabsViewModel(List<String> titles, List<TabData> tabDataList) {
        mTitles = titles;
        mTabDataList = tabDataList;
    }

    public List<String> getTitles() {
        return mTitles;
    }

    public List<TabData> getTabDataList() {
        return mTabDataList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TabsViewModel that = (TabsViewModel) o;

        return mTabDataList.equals(that.mTabDataList);
    }

    @Override
    public int hashCode() {
        return mTabDataList.hashCode();
    }

    public static class TabData {
        @ParseHtmlUtils.TextType
        private List<String> mTextPartsTypes;

        private List<String> mTextParts;

        public TabData(List<String> textPartsTypes, List<String> textParts) {
            this.mTextPartsTypes = textPartsTypes;
            this.mTextParts = textParts;
        }

        public List<String> getTextPartsTypes() {
            return mTextPartsTypes;
        }

        public List<String> getTextParts() {
            return mTextParts;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TabData tabData = (TabData) o;

            if (!mTextPartsTypes.equals(tabData.mTextPartsTypes)) return false;
            return mTextParts.equals(tabData.mTextParts);

        }

        @Override
        public int hashCode() {
            int result = mTextPartsTypes.hashCode();
            result = 31 * result + mTextParts.hashCode();
            return result;
        }
    }
}