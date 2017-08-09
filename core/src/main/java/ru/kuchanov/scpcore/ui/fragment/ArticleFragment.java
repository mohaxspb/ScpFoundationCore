package ru.kuchanov.scpcore.ui.fragment;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.ArticleMvp;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import ru.kuchanov.scpcore.ui.adapter.ArticleRecyclerAdapter;
import ru.kuchanov.scpcore.ui.base.BaseFragment;
import ru.kuchanov.scpcore.ui.util.DialogUtils;
import ru.kuchanov.scpcore.ui.util.ReachBottomRecyclerScrollListener;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.IntentUtils;
import timber.log.Timber;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class ArticleFragment
        extends BaseFragment<ArticleMvp.View, ArticleMvp.Presenter>
        implements ArticleMvp.View, SetTextViewHTML.TextItemsClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = ArticleFragment.class.getSimpleName();

    public static final String EXTRA_URL = "EXTRA_URL";

    //tabs
    private static final String KEY_CURRENT_SELECTED_TAB = "KEY_CURRENT_SELECTED_TAB";

    @BindView(R2.id.progressCenter)
    ProgressBar mProgressBarCenter;
    @BindView(R2.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R2.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R2.id.tabLayout)
    TabLayout tabLayout;

    @Inject
    DialogUtils mDialogUtils;
    @Inject
    ConstantValues mConstantValues;

    //tabs
    private int mCurrentSelectedTab = 0;

    private String url;

    private ArticleRecyclerAdapter mAdapter;
    private Article mArticle;

    public static ArticleFragment newInstance(String url) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //tabs
        outState.putInt(KEY_CURRENT_SELECTED_TAB, mCurrentSelectedTab);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate");
        super.onCreate(savedInstanceState);
        url = getArguments().getString(EXTRA_URL);
        if (savedInstanceState != null) {
            mCurrentSelectedTab = savedInstanceState.getInt(KEY_CURRENT_SELECTED_TAB);
        }

        mPresenter.onCreate();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_article;
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected void initViews() {
        Timber.d("initViews");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ArticleRecyclerAdapter();
        mAdapter.setTextItemsClickListener(this);
        mAdapter.setHasStableIds(true);

        //we need this as it's the only way to be able to scroll
        //articles, which have a lot of tables, which are shown in webView
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setItemViewCacheSize(20);

        mRecyclerView.setAdapter(mAdapter);

        mPresenter.setArticleId(url);
        mPresenter.getDataFromDb();

        if (mArticle != null) {
            showData(mArticle);
        }

        mSwipeRefreshLayout.setColorSchemeResources(R.color.zbs_color_red);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            Timber.d("onRefresh");
            mPresenter.getDataFromApi();
        });
    }

    @Override
    public void enableSwipeRefresh(boolean enable) {
        if (!isAdded() || mSwipeRefreshLayout == null) {
            return;
        }
        mSwipeRefreshLayout.setEnabled(enable);
    }

    @Override
    public void showSwipeProgress(boolean show) {
        if (!isAdded()) {
            return;
        }
        if (!mSwipeRefreshLayout.isRefreshing() && !show) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void showCenterProgress(boolean show) {
        if (!isAdded() || mProgressBarCenter == null) {
            return;
        }
        mProgressBarCenter.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        Timber.d("setUserVisibleHint url: %s, isVisibleToUser: %b", url, isVisibleToUser);
        if (isVisibleToUser && mArticle != null) {
            updateActivityMenuState();
        }
    }

    @Override
    public void showData(Article article) {
        Timber.d("showData: %s", article);
        mArticle = article;
        if (!isAdded()) {
            return;
        }
        if (mArticle == null || mArticle.text == null) {
            return;
        }
//        Timber.d("setUserVisibleHint url: %s, value: %b", url, getUserVisibleHint());
        if (getUserVisibleHint()) {
            updateActivityMenuState();
        }
        if (mArticle.hasTabs) {
            tabLayout.clearOnTabSelectedListeners();
            tabLayout.removeAllTabs();
            for (String title : RealmString.toStringList(article.tabsTitles)) {
                tabLayout.addTab(tabLayout.newTab().setText(title));
            }
            tabLayout.setVisibility(View.VISIBLE);

            Article currentTabArticle = new Article();
            currentTabArticle.hasTabs = true;
            currentTabArticle.text = RealmString.toStringList(mArticle.tabsTexts).get(mCurrentSelectedTab);
            mAdapter.setData(currentTabArticle);

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    Timber.d("onTabSelected: %s", tab.getPosition());
                    mCurrentSelectedTab = tab.getPosition();
                    Article currentTabArticle = new Article();
                    currentTabArticle.hasTabs = true;
                    currentTabArticle.text = RealmString.toStringList(mArticle.tabsTexts).get(mCurrentSelectedTab);
                    mAdapter.setData(currentTabArticle);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            TabLayout.Tab selectedTab = tabLayout.getTabAt(mCurrentSelectedTab);
            if (selectedTab != null) {
                selectedTab.select();
            }
        } else {
            tabLayout.setVisibility(View.GONE);
            mAdapter.setData(mArticle);
        }

        mRecyclerView.addOnScrollListener(new ReachBottomRecyclerScrollListener() {
            @Override
            public void onBottomReached() {
                Timber.d("onBottomReached");
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    //do not show, in this case
//                    showNeedLoginPopup();
                    return;
                }

                if (mArticle.text != null && !mArticle.isInReaden) {
                    mPresenter.setArticleIsReaden(mArticle.url);
                } else {
                    Timber.d("mArticle.text != null && !mArticle.isInReaden is FALSE, can't mark read");
                }
            }
        });
    }

    private void updateActivityMenuState() {
        if (getActivity() instanceof ToolbarStateSetter) {
            if (mArticle.title != null) {
                ((ToolbarStateSetter) getActivity()).setTitle(mArticle.title);
            }
            ((ToolbarStateSetter) getActivity()).setFavoriteState(mArticle.isInFavorite != Article.ORDER_NONE);
        }
    }

    @Override
    public void onLinkClicked(String link) {
        Timber.d("onLinkClicked: %s", link);
        //open predefined main activities link clicked
        for (String pressedLink : mConstantValues.getAllLinksArray()) {
            if (link.equals(pressedLink)) {
                MainActivity.startActivity(getActivity(), link);
                return;
            }
        }

        getBaseActivity().startArticleActivity(link);
    }

    @Override
    public void onSnoskaClicked(String link) {
        List<String> articlesTextParts = mAdapter.getArticlesTextParts();
        if (TextUtils.isDigitsOnly(link)) {
            String linkToFind = "footnote-" + link;
            for (int i = articlesTextParts.size() - 1; i >= 0; i--) {
                Document document = Jsoup.parse(articlesTextParts.get(i));
                Elements divTag = document.getElementsByAttributeValue("id", linkToFind);
                if (divTag.size() != 0) {
                    String textThatWeTryToFindSoManyTime = divTag.text();
                    textThatWeTryToFindSoManyTime = textThatWeTryToFindSoManyTime.substring(3, textThatWeTryToFindSoManyTime.length());
                    new MaterialDialog.Builder(getActivity())
                            .title("Сноска " + link)
                            .content(textThatWeTryToFindSoManyTime)
                            .show();
                    break;
                }
            }
        }
    }

    @Override
    public void onBibliographyClicked(String link) {
        List<String> articlesTextParts = mAdapter.getArticlesTextParts();
        for (int i = articlesTextParts.size() - 1; i >= 0; i--) {
            Document document = Jsoup.parse(articlesTextParts.get(i));
            Elements divTag = document.getElementsByAttributeValue("id", link);
            if (divTag.size() != 0) {
                String textThatWeTryToFindSoManyTime = divTag.text();
                textThatWeTryToFindSoManyTime = textThatWeTryToFindSoManyTime.substring(3, textThatWeTryToFindSoManyTime.length());
                new MaterialDialog.Builder(getActivity())
                        .title("Библиография")
                        .content(textThatWeTryToFindSoManyTime)
                        .show();
                break;
            }
        }
    }

    @Override
    public void onTocClicked(String link) {
        Timber.d("onTocClicked: %s", link);
        List<String> articlesTextParts = mAdapter.getArticlesTextParts();
        String digits = "";
        for (char c : link.toCharArray()) {
            if (TextUtils.isDigitsOnly(String.valueOf(c))) {
                digits += String.valueOf(c);
            }
        }
        for (int i = 0; i < articlesTextParts.size(); i++) {
            if (articlesTextParts.get(i).contains("id=\"" + "toc" + digits + "\"")) {
//                (i+1 так как в адаптере есть еще элемент для заголовка)
                Timber.d("found part: %s", articlesTextParts.get(i));
                mRecyclerView.scrollToPosition(i + 1);
                return;
            }
        }
        Timber.d("check for a with name");
        //if reach here, so it's one of awful toc with bad style
        String srtToCheck = "name=\"" + link + "\"";
        String srtToCheck1 = "name=\"" + link.replace("#", "") + "\"";
//        Timber.d("srtToCheck: %s", srtToCheck);
//        Timber.d("srtToCheck1: %s", srtToCheck1);
        for (int i = 0; i < articlesTextParts.size(); i++) {
            if (articlesTextParts.get(i).contains(srtToCheck) ||
                    articlesTextParts.get(i).contains(srtToCheck1)) {
//                Timber.d("found part: %s", articlesTextParts.get(i));
//                (i+1 так как в адаптере есть еще элемент для заголовка)
                mRecyclerView.scrollToPosition(i + 1);
                return;
            }
        }
    }

    @Override
    public void onImageClicked(String link) {
        if (!isAdded()) {
            return;
        }
        mDialogUtils.showImageDialog(getActivity(), link);
    }

    @Override
    public void onUnsupportedLinkPressed(String link) {
        if (!isAdded()) {
            return;
        }
        showMessage(R.string.unsupported_link);
    }

    @Override
    public void onMusicClicked(String link) {
        if (!isAdded()) {
            return;
        }
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(link);
            mp.prepareAsync();
            mp.setOnPreparedListener(mediaPlayer -> mp.start());
            mp.setOnCompletionListener(MediaPlayer::release);
        } catch (IOException e) {
            Timber.e(e, "error play music");
            showError(e);
        }
    }

    @Override
    public void onExternalDomenUrlClicked(String link) {
        if (!isAdded()) {
            return;
        }
        IntentUtils.openUrl(link);
    }

    @Override
    public void onTagClicked(ArticleTag tag) {
        if (!isAdded()) {
            return;
        }
        getBaseActivity().startTagsSearchActivity(Collections.singletonList(tag));
    }

    @Override
    public void onNotTranslatedArticleClick(String link) {
        if (!isAdded()) {
            return;
        }
        showMessage(R.string.article_not_translated);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case MyPreferenceManager.Keys.TEXT_SCALE_ARTICLE:
                mAdapter.notifyDataSetChanged();
                break;
            case MyPreferenceManager.Keys.DESIGN_FONT_PATH:
                mAdapter.notifyDataSetChanged();
                break;
            default:
                //do nothing
                break;
        }
    }

    public interface ToolbarStateSetter {

        void setTitle(String title);

        void setFavoriteState(boolean isInFavorite);
    }
}