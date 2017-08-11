package ru.kuchanov.scpcore.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ParseHtmlUtils;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.ui.holder.ArticleImageHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleSpoilerHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTableHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTagsHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTextHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTitleHolder;
import ru.kuchanov.scpcore.ui.model.SpoilerViewModel;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import timber.log.Timber;

/**
 * Created by Dante on 17.01.2016.
 * <p>
 * for scp_ru
 */
public class ArticleRecyclerAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ArticleSpoilerHolder.SpoilerClickListener {

    //TODO realize via enum
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_SPOILER = 1;
    private static final int TYPE_IMAGE = 2;
    private static final int TYPE_TITLE = 3;
    private static final int TYPE_TABLE = 4;
    private static final int TYPE_TAGS = 5;

    private Article mArticle;
    private List<String> mArticlesTextParts;
    @ParseHtmlUtils.TextType
    private List<String> mArticlesTextPartsTypes;

    //    private Map<String, Object> mTopLevelItems = new LinkedHashMap<>();
    private List<Object> mTopLevelItems = new ArrayList<>();

    //use it to manage spoilers and its state, from which items size depends
//    private List<SpoilerViewModel> mSpoilerViewModels = new ArrayList<>();

    public List<String> getArticlesTextParts() {
        return mArticlesTextParts;
    }

    private SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    public void setTextItemsClickListener(SetTextViewHTML.TextItemsClickListener textItemsClickListener) {
        mTextItemsClickListener = textItemsClickListener;
    }

    public void setData(Article article) {
//        Timber.d("setData: %s", article);
        mArticle = article;
        if (mArticle.hasTabs) {
            mArticlesTextParts = ParseHtmlUtils.getArticlesTextParts(mArticle.text);
            mArticlesTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(mArticlesTextParts);
        } else {
            mArticlesTextParts = RealmString.toStringList(mArticle.textParts);
            mArticlesTextPartsTypes = RealmString.toStringList(mArticle.textPartsTypes);
        }

        Timber.d("mArticlesTextPartsTypes: %s", mArticlesTextPartsTypes);

        //set SpoilerViewModels
//        mSpoilerViewModels.clear();
//        mTopLevelItems.put(ParseHtmlUtils.TextType.TITLE, mArticle.title);

        mTopLevelItems.clear();
        int counter = 0;
        for (String textPartType : mArticlesTextPartsTypes) {
            if (textPartType.equals(ParseHtmlUtils.TextType.SPOILER)) {
                String spoilerData = mArticlesTextParts.get(counter);
                List<String> spoilerParts = ParseHtmlUtils.getSpoilerParts(spoilerData);

                SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
                spoilerViewModel.titles = Collections.singletonList(spoilerParts.get(0));
                spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerParts.get(1));
                spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);
//                spoilerViewModel.id = counter;

                mTopLevelItems.add(spoilerViewModel);
            } else {
                mTopLevelItems.add(mArticlesTextParts.get(counter));
            }
            counter++;
        }
//        mTopLevelItems.put(ParseHtmlUtils.TextType.TAGS, mArticle.tags);

        Timber.d("mTopLevelItems: %s", mTopLevelItems);

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_TITLE;
        }
        if (position == getItemCount() - 1) {
            return TYPE_TAGS;
        }
        String type = mArticlesTextPartsTypes.get(position - 1);//-1 for title
        switch (type) {
            case ParseHtmlUtils.TextType.TEXT:
                return TYPE_TEXT;
            case ParseHtmlUtils.TextType.IMAGE:
                return TYPE_IMAGE;
            case ParseHtmlUtils.TextType.SPOILER:
                return TYPE_SPOILER;
            case ParseHtmlUtils.TextType.TABLE:
                return TYPE_TABLE;
            default:
                throw new IllegalArgumentException("unexpected type: " + type);
        }
//        @ParseHtmlUtils.TextType
//        String type = mArticlesTextPartsTypes.get(position);//-1 for title
//        if (position == 0) {
//            return TYPE_TITLE;
//        }
//        if (position == getItemCount() - 1) {
//            return TYPE_TAGS;
//        }
//
//        switch (type) {
//            case ParseHtmlUtils.TextType.TEXT:
//                return TYPE_TEXT;
//            case ParseHtmlUtils.TextType.IMAGE:
//                return TYPE_IMAGE;
//            case ParseHtmlUtils.TextType.SPOILER:
//                return TYPE_SPOILER;
//            case ParseHtmlUtils.TextType.TABLE:
//                return TYPE_TABLE;
//            default:
//                throw new IllegalArgumentException("unexpected type: " + type);
//        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_TITLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_title, parent, false);
                return new ArticleTitleHolder(view);
            case TYPE_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_r_img, parent, false);
                return new ArticleImageHolder(view, mTextItemsClickListener);
            case TYPE_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_text, parent, false);
                return new ArticleTextHolder(view, mTextItemsClickListener);
            case TYPE_SPOILER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_spoiler, parent, false);
                return new ArticleSpoilerHolder(view, this);
            case TYPE_TABLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_table, parent, false);
                return new ArticleTableHolder(view, mTextItemsClickListener);
            case TYPE_TAGS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_tags, parent, false);
                return new ArticleTagsHolder(view, mTextItemsClickListener);
            default:
                throw new IllegalArgumentException("unexpected type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_TEXT:
                ((ArticleTextHolder) holder).bind(mArticlesTextParts.get(position - 1));
                break;
            case TYPE_IMAGE:
                ((ArticleImageHolder) holder).bind(mArticlesTextParts.get(position - 1));
                break;
            case TYPE_SPOILER:
                String spoilerData = mArticlesTextParts.get(position - 1);
                List<String> spoilerParts = ParseHtmlUtils.getSpoilerParts(spoilerData);

                SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
                spoilerViewModel.titles = Collections.singletonList(spoilerParts.get(0));
                spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerParts.get(1));
                spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);

                int indexInAllItemsList = mTopLevelItems.indexOf(spoilerViewModel);
                SpoilerViewModel spoilerViewModelInDataList = ((SpoilerViewModel) mTopLevelItems.get(indexInAllItemsList));

                ((ArticleSpoilerHolder) holder).bind(spoilerViewModelInDataList);
                break;
            case TYPE_TITLE:
                ((ArticleTitleHolder) holder).bind(mArticle.title);
                break;
            case TYPE_TABLE:
                ((ArticleTableHolder) holder).bind(mArticlesTextParts.get(position - 1));
                break;
            case TYPE_TAGS:
                ((ArticleTagsHolder) holder).bind(mArticle.tags);
                break;
            default:
                throw new IllegalArgumentException("unexpected item type: " + getItemViewType(position));
        }
    }

    @Override
    public int getItemCount() {
        return mArticlesTextParts == null ? 0 : mArticlesTextParts.size() + 1 + 1; //+1 for title and +1 for tags
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //FIXME always calling this method
    @Override
    public void onSpoilerExpand(SpoilerViewModel spoilerViewModel) {
        Timber.d("onSpoilerExpand: %s", spoilerViewModel);

        int indexInAllItemsList = mTopLevelItems.indexOf(spoilerViewModel);
        ((SpoilerViewModel) mTopLevelItems.get(indexInAllItemsList)).isExpanded = true;
//        Timber.d("indexInAllItemsList: %s", indexInAllItemsList);

//        Timber.d("mArticlesTextPartsTypes.size: %s", mArticlesTextPartsTypes.size());
//        Timber.d("mArticlesTextParts.size: %s", mArticlesTextParts.size());
        mArticlesTextPartsTypes.addAll(indexInAllItemsList + 1, spoilerViewModel.mSpoilerTextPartsTypes);
        mArticlesTextParts.addAll(indexInAllItemsList + 1, spoilerViewModel.mSpoilerTextParts);
//        Timber.d("mArticlesTextPartsTypes.size: %s", mArticlesTextPartsTypes.size());
//        Timber.d("mArticlesTextParts.size: %s", mArticlesTextParts.size());

        notifyItemRangeInserted(indexInAllItemsList + 1, spoilerViewModel.mSpoilerTextPartsTypes.size());
    }

    @Override
    public void onSpoilerCollapse(SpoilerViewModel spoilerViewModel) {
        Timber.d("onSpoilerCollapse: %s", spoilerViewModel);

        int indexInAllItemsList = mTopLevelItems.indexOf(spoilerViewModel);
        ((SpoilerViewModel) mTopLevelItems.get(indexInAllItemsList)).isExpanded = false;
//        Timber.d("indexInAllItemsList: %s", indexInAllItemsList);

//        Timber.d("mArticlesTextPartsTypes.size: %s", mArticlesTextPartsTypes.size());
//        Timber.d("mArticlesTextParts.size: %s", mArticlesTextParts.size());
//        mArticlesTextPartsTypes.removeAll(spoilerViewModel.mSpoilerTextPartsTypes);
//        mArticlesTextParts.removeAll(spoilerViewModel.mSpoilerTextParts);

        mArticlesTextPartsTypes
                .subList(indexInAllItemsList + 1, indexInAllItemsList + 1 + spoilerViewModel.mSpoilerTextPartsTypes.size())
                .clear();
        mArticlesTextParts
                .subList(indexInAllItemsList + 1, indexInAllItemsList + 1 + spoilerViewModel.mSpoilerTextParts.size())
                .clear();

//        Timber.d("mArticlesTextPartsTypes.size: %s", mArticlesTextPartsTypes.size());
//        Timber.d("mArticlesTextParts.size: %s", mArticlesTextParts.size());

        notifyItemRangeRemoved(indexInAllItemsList + 1, spoilerViewModel.mSpoilerTextPartsTypes.size());
    }
}