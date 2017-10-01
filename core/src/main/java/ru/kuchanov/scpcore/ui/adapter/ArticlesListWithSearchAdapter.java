package ru.kuchanov.scpcore.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.db.model.Article;
import timber.log.Timber;

/**
 * Created by Dante on 17.01.2016.
 * <p>
 * for scp_ru
 */
public class ArticlesListWithSearchAdapter extends ArticlesListAdapter {

    private List<Article> mFilteredWithSearchQueryData = new ArrayList<>();
    private String mSearchQuery = "";

    @Override
    public List<Article> getDisplayedData() {
        return mFilteredWithSearchQueryData;
    }

    @Override
    public void setData(List<Article> data) {
//        super.setData(data);

        mData = data;
        sortByType(mSortType);

        sortArticles(mSearchQuery);

        //add native ads to result data list
        createDataWithAdsAndArticles();

        notifyDataSetChanged();
    }

    public void sortArticles(String searchQuery) {
        mSearchQuery = searchQuery;
        if (mData == null) {
            return;
        }
        mFilteredWithSearchQueryData.clear();
        for (Article article : mSortedWithFilterData) {
            if (article.title == null) {
                Timber.wtf("article.title is NULL for some reason...");
                continue;
            }
            if (article.title.toLowerCase().contains(searchQuery.toLowerCase())) {
                mFilteredWithSearchQueryData.add(article);
            }
        }

        //add native ads to result data list
        createDataWithAdsAndArticles();
//
//        notifyDataSetChanged();
    }

    @Override
    public void sortByType(SortType sortType) {
        super.sortByType(sortType);
        sortArticles(mSearchQuery);
    }

//    @Override
//    public void onBindViewHolder(HolderMin holder, int position) {
//        holder.bind(mFilteredWithSearchQueryData.get(position));
//        holder.setShouldShowPreview(shouldShowPreview);
//        holder.setShouldShowPopupOnFavoriteClick(shouldShowPopupOnFavoriteClick);
//    }

//    @Override
//    public int getItemCount() {
//        return mFilteredWithSearchQueryData.size();
//    }
}