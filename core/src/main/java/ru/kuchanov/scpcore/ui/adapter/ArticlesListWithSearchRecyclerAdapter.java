package ru.kuchanov.scpcore.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.ui.holder.HolderMin;
import timber.log.Timber;

/**
 * Created by Dante on 17.01.2016.
 * <p>
 * for scp_ru
 */
public class ArticlesListWithSearchRecyclerAdapter extends ArticlesListRecyclerAdapter {

    private List<Article> mSortedData = new ArrayList<>();
    private String mSearchQuery = "";

    public List<Article> getDisplayedData() {
        return mSortedData;
    }

    public void sortArticles(String searchQuery) {
        mSearchQuery = searchQuery;
        if (mData == null) {
            return;
        }
        mSortedData.clear();
        for (Article article : mSortedWithFilterData) {
            if (article.title == null) {
                Timber.wtf("article.title is NULL for some reason...");
                continue;
            }
            if (article.title.toLowerCase().contains(searchQuery.toLowerCase())) {
                mSortedData.add(article);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void sortByType(SortType sortType) {
        super.sortByType(sortType);
        sortArticles(mSearchQuery);
    }

    @Override
    public void onBindViewHolder(HolderMin holder, int position) {
        holder.bind(mSortedData.get(position));
        holder.setShouldShowPreview(shouldShowPreview);
        holder.setShouldShowPopupOnFavoriteClick(shouldShowPopupOnFavoriteClick);
    }

    @Override
    public int getItemCount() {
        return mSortedData.size();
    }
}