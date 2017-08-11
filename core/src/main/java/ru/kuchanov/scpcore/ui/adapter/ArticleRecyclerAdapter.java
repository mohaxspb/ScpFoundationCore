package ru.kuchanov.scpcore.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmList;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ParseHtmlUtils;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.ui.holder.ArticleImageHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleSpoilerHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTableHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTagsHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTextHolder;
import ru.kuchanov.scpcore.ui.holder.ArticleTitleHolder;
import ru.kuchanov.scpcore.ui.model.ArticleTextPartViewModel;
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
//    private List<String> mArticlesTextParts;
//    @ParseHtmlUtils.TextType
//    private List<String> mArticlesTextPartsTypes;

//    private List<Object> mTopLevelItems = new ArrayList<>();

    private List<ArticleTextPartViewModel> mViewModels = new ArrayList<>();

    //    public List<String> getArticlesTextParts() {
//        return mArticlesTextParts;
//    }
    public List<String> getArticlesTextParts() {
        return ArticleTextPartViewModel.convertToStringList(mViewModels);
    }

    private SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    public void setTextItemsClickListener(SetTextViewHTML.TextItemsClickListener textItemsClickListener) {
        mTextItemsClickListener = textItemsClickListener;
    }

    public void setData(Article article, List<SpoilerViewModel> expandedSpoilers) {
//        Timber.d("setData: %s", article);
        mArticle = article;
        //TODO refactor it
//        if (mArticle.hasTabs) {
//            mArticlesTextParts = ParseHtmlUtils.getArticlesTextParts(mArticle.text);
//            mArticlesTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(mArticlesTextParts);
//        } else {
//            mArticlesTextParts = RealmString.toStringList(mArticle.textParts);
//            mArticlesTextPartsTypes = RealmString.toStringList(mArticle.textPartsTypes);
//        }

//        Timber.d("mArticlesTextPartsTypes: %s", mArticlesTextPartsTypes);

        //set SpoilerViewModels
//        mTopLevelItems.put(ParseHtmlUtils.TextType.TITLE, mArticle.title);

//        mTopLevelItems.clear();
//        int counter = 0;
//        for (String textPartType : mArticlesTextPartsTypes) {
//            if (textPartType.equals(ParseHtmlUtils.TextType.SPOILER)) {
//                String spoilerData = mArticlesTextParts.get(counter);
//                List<String> spoilerParts = ParseHtmlUtils.getSpoilerParts(spoilerData);
//
//                SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
//                spoilerViewModel.titles = Collections.singletonList(spoilerParts.get(0));
//                spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerParts.get(1));
//                spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);
////                spoilerViewModel.id = counter;
//
//                mTopLevelItems.add(spoilerViewModel);
//            } else {
//                mTopLevelItems.add(mArticlesTextParts.get(counter));
//            }
//            counter++;
//        }

//        Timber.d("mTopLevelItems: %s", mTopLevelItems);

        /////////////////////////////
        mViewModels.clear();

        List<String> mArticlesTextParts = new ArrayList<>();
        @ParseHtmlUtils.TextType
        List<String> mArticlesTextPartsTypes = new ArrayList<>();

        mArticlesTextParts.add(mArticle.title);
        mArticlesTextPartsTypes.add(ParseHtmlUtils.TextType.TITLE);

        if (mArticle.hasTabs) {
            mArticlesTextParts = ParseHtmlUtils.getArticlesTextParts(mArticle.text);
            mArticlesTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(mArticlesTextParts);
        } else {
            mArticlesTextParts = RealmString.toStringList(mArticle.textParts);
            mArticlesTextPartsTypes = RealmString.toStringList(mArticle.textPartsTypes);
        }

        //DO NOT USE THIS VALUE!!!
        mArticlesTextParts.add(mArticle.tags.toString());
        mArticlesTextPartsTypes.add(ParseHtmlUtils.TextType.TAGS);

        for (int order = 0; order < mArticlesTextParts.size(); order++) {
            @ParseHtmlUtils.TextType
            String type = mArticlesTextPartsTypes.get(order);
            Object data;
            switch (type) {
                case ParseHtmlUtils.TextType.SPOILER:
                    String spoilerData = mArticlesTextParts.get(order);
                    List<String> spoilerParts = ParseHtmlUtils.getSpoilerParts(spoilerData);

                    SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
                    spoilerViewModel.titles = Collections.singletonList(spoilerParts.get(0));
                    spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerParts.get(1));
                    spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);
                    spoilerViewModel.isExpanded = expandedSpoilers.contains(spoilerViewModel);

                    data = spoilerViewModel;
                    break;
                case ParseHtmlUtils.TextType.TAGS:
                    data = mArticle.tags;
                    break;
                default:
                    data = mArticlesTextParts.get(order);
                    break;
            }

            mViewModels.add(new ArticleTextPartViewModel(order, type, data));
            //add textParts for expanded spoilers
            if (data instanceof SpoilerViewModel) {
                SpoilerViewModel spoilerViewModel = ((SpoilerViewModel) data);
                List<ArticleTextPartViewModel> viewModels = new ArrayList<>();
                for (int i = 0; i < spoilerViewModel.mSpoilerTextPartsTypes.size(); i++) {
                    @ParseHtmlUtils.TextType
                    String typeInSpoiler = spoilerViewModel.mSpoilerTextPartsTypes.get(i);
                    String dataInSpoiler = spoilerViewModel.mSpoilerTextParts.get(i);
                    viewModels.add(new ArticleTextPartViewModel(i, typeInSpoiler, dataInSpoiler));
                }
                mViewModels.addAll(viewModels);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        @ParseHtmlUtils.TextType
        String type = mViewModels.get(position).type;
        switch (type) {
            case ParseHtmlUtils.TextType.TITLE:
                return TYPE_TITLE;
            case ParseHtmlUtils.TextType.TEXT:
                return TYPE_TEXT;
            case ParseHtmlUtils.TextType.IMAGE:
                return TYPE_IMAGE;
            case ParseHtmlUtils.TextType.SPOILER:
                return TYPE_SPOILER;
            case ParseHtmlUtils.TextType.TABLE:
                return TYPE_TABLE;
            case ParseHtmlUtils.TextType.TAGS:
                return TYPE_TAGS;
            default:
                throw new IllegalArgumentException("unexpected type: " + type);
        }
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
//        switch (getItemViewType(position)) {
//            case TYPE_TEXT:
//                ((ArticleTextHolder) holder).bind(mArticlesTextParts.get(position - 1));
//                break;
//            case TYPE_IMAGE:
//                ((ArticleImageHolder) holder).bind(mArticlesTextParts.get(position - 1));
//                break;
//            case TYPE_SPOILER:
//                String spoilerData = mArticlesTextParts.get(position - 1);
//                List<String> spoilerParts = ParseHtmlUtils.getSpoilerParts(spoilerData);
//
//                SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
//                spoilerViewModel.titles = Collections.singletonList(spoilerParts.get(0));
//                spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerParts.get(1));
//                spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);
//
//                int indexInAllItemsList = mTopLevelItems.indexOf(spoilerViewModel);
//                SpoilerViewModel spoilerViewModelInDataList = ((SpoilerViewModel) mTopLevelItems.get(indexInAllItemsList));
//
//                ((ArticleSpoilerHolder) holder).bind(spoilerViewModelInDataList);
//                break;
//            case TYPE_TITLE:
//                ((ArticleTitleHolder) holder).bind(mArticle.title);
//                break;
//            case TYPE_TABLE:
//                ((ArticleTableHolder) holder).bind(mArticlesTextParts.get(position - 1));
//                break;
//            case TYPE_TAGS:
//                ((ArticleTagsHolder) holder).bind(mArticle.tags);
//                break;
//            default:
//                throw new IllegalArgumentException("unexpected item type: " + getItemViewType(position));
//        }

        switch (getItemViewType(position)) {
            case TYPE_TEXT:
                ((ArticleTextHolder) holder).bind((String) mViewModels.get(position).data);
                break;
            case TYPE_IMAGE:
                ((ArticleImageHolder) holder).bind((String) mViewModels.get(position).data);
                break;
            case TYPE_SPOILER:
                ((ArticleSpoilerHolder) holder).bind((SpoilerViewModel) mViewModels.get(position).data);
                break;
            case TYPE_TITLE:
                ((ArticleTitleHolder) holder).bind((String) mViewModels.get(position).data);
                break;
            case TYPE_TABLE:
                ((ArticleTableHolder) holder).bind((String) mViewModels.get(position).data);
                break;
            case TYPE_TAGS:
                ((ArticleTagsHolder) holder).bind((RealmList<ArticleTag>) mViewModels.get(position).data);
                break;
            default:
                throw new IllegalArgumentException("unexpected item type: " + getItemViewType(position));
        }
    }

    @Override
    public int getItemCount() {
        return mViewModels.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
//    public void onSpoilerExpand(SpoilerViewModel spoilerViewModel) {
    public void onSpoilerExpand(int position) {
        Timber.d("onSpoilerExpand: %s", position);

//        int indexInAllItemsList = mTopLevelItems.indexOf(spoilerViewModel);
//        ((SpoilerViewModel) mTopLevelItems.get(indexInAllItemsList)).isExpanded = true;
//
//        mArticlesTextPartsTypes.addAll(indexInAllItemsList + 1, spoilerViewModel.mSpoilerTextPartsTypes);
//        mArticlesTextParts.addAll(indexInAllItemsList + 1, spoilerViewModel.mSpoilerTextParts);

//        notifyItemRangeInserted(indexInAllItemsList + 1, spoilerViewModel.mSpoilerTextPartsTypes.size());
        SpoilerViewModel spoilerViewModel = ((SpoilerViewModel) mViewModels.get(position).data);
        List<ArticleTextPartViewModel> viewModels = new ArrayList<>();
        for (int order = 0; order < spoilerViewModel.mSpoilerTextPartsTypes.size(); order++) {
            @ParseHtmlUtils.TextType
            String type = spoilerViewModel.mSpoilerTextPartsTypes.get(order);
            String data = spoilerViewModel.mSpoilerTextParts.get(order);
            viewModels.add(new ArticleTextPartViewModel(order, type, data));
        }
        mViewModels.addAll(position + 1, viewModels);

        notifyItemRangeInserted(position + 1, viewModels.size());

        mTextItemsClickListener.onSpoilerExpand(spoilerViewModel);
    }

    @Override
//    public void onSpoilerCollapse(SpoilerViewModel spoilerViewModel) {
    public void onSpoilerCollapse(int position) {
        Timber.d("onSpoilerCollapse: %s", position);

//        int indexInAllItemsList = mTopLevelItems.indexOf(spoilerViewModel);
//        ((SpoilerViewModel) mTopLevelItems.get(indexInAllItemsList)).isExpanded = false;
//
//        mArticlesTextPartsTypes
//                .subList(indexInAllItemsList + 1, indexInAllItemsList + 1 + spoilerViewModel.mSpoilerTextPartsTypes.size())
//                .clear();
//        mArticlesTextParts
//                .subList(indexInAllItemsList + 1, indexInAllItemsList + 1 + spoilerViewModel.mSpoilerTextParts.size())
//                .clear();
//
//        notifyItemRangeRemoved(indexInAllItemsList + 1, spoilerViewModel.mSpoilerTextPartsTypes.size());

        SpoilerViewModel spoilerViewModel = ((SpoilerViewModel) mViewModels.get(position).data);
        List<ArticleTextPartViewModel> viewModels = new ArrayList<>();
        for (int order = 0; order < spoilerViewModel.mSpoilerTextPartsTypes.size(); order++) {
            @ParseHtmlUtils.TextType
            String type = spoilerViewModel.mSpoilerTextPartsTypes.get(order);
            String data = spoilerViewModel.mSpoilerTextParts.get(order);
            viewModels.add(new ArticleTextPartViewModel(order, type, data));
        }

        mViewModels
                .subList(position + 1, position + 1 + viewModels.size())
                .clear();
        mViewModels
                .subList(position + 1, position + 1 + viewModels.size())
                .clear();

        notifyItemRangeRemoved(position + 1, viewModels.size());

        mTextItemsClickListener.onSpoilerCollapse(spoilerViewModel);
    }
}