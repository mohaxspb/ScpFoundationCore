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
public class ArticlesListWithSearchRecyclerAdapter extends ArticlesListRecyclerAdapter {

    private List<Article> mFilteredWithSearchQueryData = new ArrayList<>();
    private String mSearchQuery = "";

    @Override
    public List<Article> getDisplayedData() {
        return mFilteredWithSearchQueryData;
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
//        notifyDataSetChanged();
    }

//    @Override
//    public void sortByType(SortType sortType) {
//        super.sortByType(sortType);
//        sortArticles(mSearchQuery);
//    }

    //TODO refactor this class
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