package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.model.TabsViewModel;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleTabsHolder extends RecyclerView.ViewHolder {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    private SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    @BindView(R2.id.tabLayout)
    TabLayout mTabLayout;

    public ArticleTabsHolder(View itemView, SetTextViewHTML.TextItemsClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mTextItemsClickListener = clickListener;
    }

    public void bind(TabsViewModel data) {
        Context context = itemView.getContext();
        int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        float articleTextScale = mMyPreferenceManager.getArticleTextScale();

        //TODO

//        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);
//
//        CalligraphyUtils.applyFontToTextView(context, title, mMyPreferenceManager.getFontPath());
//
//        mTagsContainer.removeAllViews();
//        if (data != null) {
//            for (ArticleTag tag : data) {
//                TagView tagView = new TagView(context);
//                tagView.setTag(tag);
//                tagView.setActionImage(TagView.Action.NONE);
//
//                tagView.setOnTagClickListener((tagView1, tag1) -> mTextItemsClickListener.onTagClicked(tag1));
//
//                mTagsContainer.addView(tagView);
//            }
//        }
    }

    //            tabLayout.clearOnTabSelectedListeners();
//            tabLayout.removeAllTabs();
//            for (String title : RealmString.toStringList(article.tabsTitles)) {
//                tabLayout.addTab(tabLayout.newTab().setText(title));
//            }
//            tabLayout.setVisibility(View.VISIBLE);
//
//            Article currentTabArticle = new Article();
//            currentTabArticle.hasTabs = true;
//            currentTabArticle.text = RealmString.toStringList(mArticle.tabsTexts).get(mCurrentSelectedTab);
//            currentTabArticle.tags = mArticle.tags;
//            currentTabArticle.title = mArticle.title;
//            mAdapter.setData(currentTabArticle, mExpandedSpoilers);
//
//            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//                @Override
//                public void onTabSelected(TabLayout.Tab tab) {
//                    Timber.d("onTabSelected: %s", tab.getPosition());
//
//                    mExpandedSpoilers.clear();
//
//                    mCurrentSelectedTab = tab.getPosition();
//                    Article currentTabArticle = new Article();
//                    currentTabArticle.hasTabs = true;
//                    currentTabArticle.text = RealmString.toStringList(mArticle.tabsTexts).get(mCurrentSelectedTab);
//                    currentTabArticle.tags = mArticle.tags;
//                    currentTabArticle.title = mArticle.title;
//                    mAdapter.setData(currentTabArticle, mExpandedSpoilers);
//                }
//
//                @Override
//                public void onTabUnselected(TabLayout.Tab tab) {
//
//                }
//
//                @Override
//                public void onTabReselected(TabLayout.Tab tab) {
//
//                }
//            });
//
//            TabLayout.Tab selectedTab = tabLayout.getTabAt(mCurrentSelectedTab);
//            if (selectedTab != null) {
//                selectedTab.select();
//            }
}