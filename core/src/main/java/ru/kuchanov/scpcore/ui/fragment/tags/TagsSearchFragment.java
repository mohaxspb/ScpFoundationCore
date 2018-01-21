package ru.kuchanov.scpcore.ui.fragment.tags;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsSearchMvp;
import ru.kuchanov.scpcore.ui.base.BaseFragment;
import ru.kuchanov.scpcore.ui.fragment.article.ArticleFragment;
import ru.kuchanov.scpcore.ui.view.TagView;
import ru.kuchanov.scpcore.util.DimensionUtils;
import timber.log.Timber;

/**
 * Created by mohax on 25.05.2017.
 * <p>
 * for ScpFoundationRu
 */
public class TagsSearchFragment
        extends BaseFragment<TagsSearchMvp.View, TagsSearchMvp.Presenter>
        implements TagsSearchMvp.View {

    public static final String TAG = TagsSearchFragment.class.getSimpleName();

    @BindView(R2.id.tagsSearch)
    FlexboxLayout mSearchTagsContainer;
    @BindView(R2.id.tagsAll)
    FlexboxLayout mAllTagsContainer;
    @BindView(R2.id.searchFAB)
    FloatingActionButton mSearchFab;
    @BindView(R2.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private ObjectAnimator fabAnimator;

    private List<ArticleTag> mQueryTags = new ArrayList<>();

    private ShowTagsSearchResults mShowTagsSearchResults;

    public static TagsSearchFragment newInstance() {
        return new TagsSearchFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mShowTagsSearchResults = (ShowTagsSearchResults) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mShowTagsSearchResults = null;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tags_search;
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected void initViews() {
        if (mPresenter.getTags() == null) {
            mPresenter.getTagsFromDb();
        } else {
            showAllTags(mPresenter.getTags());
        }

        if (getUserVisibleHint()) {
            if (getActivity() instanceof ArticleFragment.ToolbarStateSetter) {
                ((ArticleFragment.ToolbarStateSetter) getActivity()).setTitle(getString(R.string.tags_search));
            }
        }

        mSearchTagsContainer.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View view, View view1) {
                mSearchFab.show();
            }

            @Override
            public void onChildViewRemoved(View view, View view1) {
                if (mSearchTagsContainer.getChildCount() == 0) {
                    mSearchFab.hide();
                }
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.zbs_color_red);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mPresenter.getTagsFromApi());
        mSwipeRefreshLayout.setProgressViewEndTarget(false, DimensionUtils.getActionBarHeight(getActivity()));

        mSearchFab.hide();
    }

    @Override
    public void showAllTags(List<ArticleTag> data) {
        Timber.d("showAllTags: %s", data.size());
        mAllTagsContainer.removeAllViews();
        for (ArticleTag tag : data) {
//            Timber.d("add tag: %s", tag.title);
            TagView tagView = new TagView(getActivity());
            tagView.setTag(tag);

            tagView.setOnTagClickListener(mAllTagsClickListener);

            mAllTagsContainer.addView(tagView);
        }
    }

    @Override
    public void showSearchResults(List<Article> data) {
        Timber.d("showSearchResults: %s", data);
        if (!isAdded()) {
            return;
        }
        if (mShowTagsSearchResults != null) {
            mShowTagsSearchResults.showResults(data, mQueryTags);
        }
    }

    @OnClick(R2.id.searchFAB)
    void onSearchFabClick() {
        Timber.d("onSearchFabClick");
        mPresenter.searchByTags(mQueryTags);
    }

    @Override
    public void showSwipeProgress(boolean show) {
        if (!mSwipeRefreshLayout.isRefreshing() && !show) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void enableSwipeRefresh(boolean enable) {
        mSwipeRefreshLayout.setEnabled(enable);
    }

    @Override
    public void showProgress(boolean show) {
        Timber.d("showProgress: %s", show);

        mSearchFab.setImageResource(show ? R.drawable.ic_autorenew : R.drawable.ic_search);
        mSearchFab.setColorFilter(Color.WHITE);

        if (show) {
            if (fabAnimator == null) {
                fabAnimator = ObjectAnimator.ofFloat(mSearchFab, "rotation", 0, 360);
                fabAnimator.setDuration(1000);
                fabAnimator.setRepeatCount(ValueAnimator.INFINITE);
            } else {
                fabAnimator.removeAllListeners();
                fabAnimator.end();
                fabAnimator.cancel();
            }
            fabAnimator.start();
        } else {
            fabAnimator.removeAllListeners();
            fabAnimator.end();
            fabAnimator.cancel();
            mSearchFab.setRotation(0);
        }
    }

    private TagView.OnTagClickListener mAllTagsClickListener = new TagView.OnTagClickListener() {
        @Override
        public void onTagClicked(TagView view, ArticleTag tag) {
            Timber.d("mAllTagsClickListener: %s", tag);

            if(!getResources().getBoolean(R.bool.multiTagSearchEnabled)){
                for (int i = 0; i < mSearchTagsContainer.getChildCount(); i++) {
                    TagView tagViewToRemove = (TagView) mSearchTagsContainer.getChildAt(i);
                    TagView tagView = new TagView(getActivity());
                    tagView.setTag(tagViewToRemove.getTag());

                    tagView.setOnTagClickListener(mAllTagsClickListener);

                    mAllTagsContainer.addView(tagView);
                }
                mSearchTagsContainer.removeAllViews();
                mQueryTags.clear();
            }

            mAllTagsContainer.removeView(view);
            mSearchTagsContainer.addView(view);

            mQueryTags.add(tag);

            view.setActionImage(TagView.Action.REMOVE);

            view.setOnTagClickListener(mSearchTagsClickListener);
        }
    };

    private TagView.OnTagClickListener mSearchTagsClickListener = new TagView.OnTagClickListener() {
        @Override
        public void onTagClicked(TagView view, ArticleTag tag) {
            Timber.d("mSearchTagsClickListener: %s", tag);
            mSearchTagsContainer.removeView(view);
            mAllTagsContainer.addView(view);

            mQueryTags.remove(tag);

            view.setActionImage(TagView.Action.ADD);

            view.setOnTagClickListener(mAllTagsClickListener);
        }
    };

    public interface ShowTagsSearchResults {
        void showResults(List<Article> data, List<ArticleTag> queryTags);
    }
}