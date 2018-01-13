package ru.kuchanov.scpcore.ui.fragment.tags;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsSearchResultsArticlesMvp;
import ru.kuchanov.scpcore.ui.activity.ArticleActivity;
import ru.kuchanov.scpcore.ui.base.BaseActivity;
import ru.kuchanov.scpcore.ui.fragment.article.ArticleFragment;
import ru.kuchanov.scpcore.ui.fragment.articleslists.BaseListArticlesWithSearchFragment;
import timber.log.Timber;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class TagsSearchResultsArticlesFragment
        extends BaseListArticlesWithSearchFragment<TagsSearchResultsArticlesMvp.View, TagsSearchResultsArticlesMvp.Presenter>
        implements TagsSearchResultsArticlesMvp.View {

    public static final String TAG = TagsSearchResultsArticlesFragment.class.getSimpleName();
//    private static final String EXTRA_ARTICLES = "EXTRA_ARTICLES";

    public static TagsSearchResultsArticlesFragment newInstance(List<Article> articles, List<ArticleTag> tags) {
        TagsSearchResultsArticlesFragment fragment = new TagsSearchResultsArticlesFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ArticleActivity.EXTRA_TAGS, (ArrayList<String>) ArticleTag.getStringsFromTags(tags));
        if (articles != null) {
            args.putStringArrayList(ArticleActivity.EXTRA_ARTICLES_URLS_LIST, (ArrayList<String>) Article.getListOfUrls(articles));
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Timber.d("onViewCreated");
        List<ArticleTag> tags = ArticleTag.getTagsFromStringList (getArguments().getStringArrayList(BaseActivity.EXTRA_TAGS));
        List<String> articles = null;
        if(getArguments().containsKey(ArticleActivity.EXTRA_ARTICLES_URLS_LIST)) {
          articles = getArguments().getStringArrayList(ArticleActivity.EXTRA_ARTICLES_URLS_LIST);
        }
//        Timber.d("articles: %s", articles);
//        Timber.d("tags: %s", tags);
        mPresenter.attachView(this);
        mPresenter.setQueryTags(tags);
        mPresenter.setArticlesUrls(articles);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void initViews() {
        super.initViews();

        if (getUserVisibleHint()) {
            if (getActivity() instanceof ArticleFragment.ToolbarStateSetter) {
                ((ArticleFragment.ToolbarStateSetter) getActivity()).setTitle(getString(R.string.tags_search_results));
            }
        }
    }

    @Override
    public void enableSwipeRefresh(boolean enable) {
        //do not need to diasble it
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public void resetOnScrollListener() {
        //we do not have paging
    }

    @Override
    protected boolean shouldUpdateThisListOnLaunch() {
        return false;
    }
}