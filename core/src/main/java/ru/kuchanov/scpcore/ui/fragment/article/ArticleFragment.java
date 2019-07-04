package ru.kuchanov.scpcore.ui.fragment.article;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.article.ArticleMvp;
import ru.kuchanov.scpcore.ui.activity.GalleryActivity;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import ru.kuchanov.scpcore.ui.adapter.ArticleAdapter;
import ru.kuchanov.scpcore.ui.dialog.AdsSettingsBottomSheetDialogFragment;
import ru.kuchanov.scpcore.ui.fragment.BaseFragment;
import ru.kuchanov.scpcore.ui.model.SpoilerViewModel;
import ru.kuchanov.scpcore.ui.model.TabsViewModel;
import ru.kuchanov.scpcore.ui.util.DialogUtils;
import ru.kuchanov.scpcore.ui.util.MyHtmlTagHandler;
import ru.kuchanov.scpcore.ui.util.ReachBottomRecyclerScrollListener;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.IntentUtils;
import timber.log.Timber;

/**
 * Created by mohax on 03.01.2017.
 */
public class ArticleFragment
        extends BaseFragment<ArticleMvp.View, ArticleMvp.Presenter>
        implements ArticleMvp.View,
        SetTextViewHTML.TextItemsClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = ArticleFragment.class.getSimpleName();

    public static final String EXTRA_URL = "EXTRA_URL";

    private static final String KEY_EXPANDED_SPOILERS = "KEY_EXPANDED_SPOILERS";

    private static final String KEY_TABS = "KEY_TABS";

    public static final int ITEM_VIEW_CACHE_SIZE = 20;

    @BindView(R2.id.progressCenter)
    ProgressBar mProgressBarCenter;

    @BindView(R2.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R2.id.recyclerView)
    RecyclerView mRecyclerView;

    @Inject
    MyPreferenceManager myPreferenceManager;

    @Inject
    DialogUtils mDialogUtils;

    @Inject
    ConstantValues mConstantValues;

    private String url;

    private ArticleAdapter mAdapter;

    private Article mArticle;

    private List<SpoilerViewModel> mExpandedSpoilers = new ArrayList<>();

    private List<TabsViewModel> mTabsViewModels = new ArrayList<>();

    public static ArticleFragment newInstance(final String url) {
        final ArticleFragment fragment = new ArticleFragment();
        final Bundle args = new Bundle();
        args.putString(EXTRA_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public ArticleMvp.Presenter createPresenter() {
        return mPresenter;
    }

    @Override
    public void onSaveInstanceState(@NotNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_TABS, (ArrayList<TabsViewModel>) mTabsViewModels);
        outState.putSerializable(KEY_EXPANDED_SPOILERS, (ArrayList<SpoilerViewModel>) mExpandedSpoilers);
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
//        Timber.d("onCreate");
        super.onCreate(savedInstanceState);
        url = getArguments().getString(EXTRA_URL);
        if (savedInstanceState != null) {
            mExpandedSpoilers = (List<SpoilerViewModel>) savedInstanceState.getSerializable(KEY_EXPANDED_SPOILERS);
            mTabsViewModels = (List<TabsViewModel>) savedInstanceState.getSerializable(KEY_TABS);
        }
    }

    @Override
    protected boolean isHasOptionsMenu() {
        return true;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_article_single;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menuItemCommentsBrowser) {
            if (presenter.getData() == null || TextUtils.isEmpty(presenter.getData().commentsUrl)) {
                showMessageLong(R.string.no_comments_url_found);
            } else {
                IntentUtils.openUrl(presenter.getData().commentsUrl);
            }
            return true;
        } else if (item.getItemId() == R.id.menuItemNextNumberArticle) {
            if (presenter.getData() == null || TextUtils.isEmpty(presenter.getData().getUrl())) {
                showMessageLong(R.string.cant_find_next_article);
            } else if (!TextUtils.isEmpty(presenter.getData().nextArticleUrl())) {
                getBaseActivity().startArticleActivity(presenter.getData().nextArticleUrl());
            } else {
                showMessageLong(R.string.cant_find_next_article);
            }
        } else if (item.getItemId() == R.id.menuItemFavorite) {
            presenter.toggleFavorite(mArticle.url);
        } else if (item.getItemId() == R.id.menuItemRead) {
            presenter.setArticleIsReaden(mArticle.url);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem commentsMenuItem = menu.findItem(R.id.menuItemCommentsBrowser);
        final MenuItem nextArticleMenuItem = menu.findItem(R.id.menuItemNextNumberArticle);
        final MenuItem favoriteMenuItem = menu.findItem(R.id.menuItemFavorite);
        final MenuItem readMenuItem = menu.findItem(R.id.menuItemRead);
        if (presenter.getData() == null) {
            commentsMenuItem.setVisible(false);
            nextArticleMenuItem.setVisible(false);
            favoriteMenuItem.setVisible(false);
            readMenuItem.setVisible(false);
        } else {
            commentsMenuItem.setVisible(!TextUtils.isEmpty(presenter.getData().commentsUrl));
            nextArticleMenuItem.setVisible(!TextUtils.isEmpty(presenter.getData().nextArticleUrl()));

            favoriteMenuItem.setVisible(true);
            final boolean isInFavorite = mArticle.isInFavorite != Article.ORDER_NONE;
            favoriteMenuItem.setIcon(isInFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp);
            favoriteMenuItem.setTitle(isInFavorite ? R.string.favorites_remove : R.string.favorites_add);

            readMenuItem.setVisible(true);
            readMenuItem.setIcon(mArticle.isInReaden ? R.drawable.ic_drafts_black_24dp : R.drawable.ic_email_white_24dp);
            readMenuItem.setTitle(mArticle.isInReaden ? R.string.remove_from_read : R.string.add_to_read);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_article;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        //fix no presenter attach
        mPresenter.attachView(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void initViews() {
        Timber.d("initViews");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ArticleAdapter();
        mAdapter.setTextItemsClickListener(this);
        mAdapter.setHasStableIds(true);

        //we need this as it's the only way to be able to scroll
        //articles, which have a lot of tables, which are shown in webView
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);

        mRecyclerView.setAdapter(mAdapter);

        mPresenter.setArticleId(url);
        mPresenter.getDataFromDb();

        if (getUserVisibleHint()) {
            mPresenter.onVisibleToUser();
        }

        if (mArticle != null) {
            showData(mArticle);
        }

        mSwipeRefreshLayout.setColorSchemeResources(R.color.zbs_color_red);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mPresenter.getDataFromApi());
    }

    @Override
    public void enableSwipeRefresh(final boolean enable) {
        if (!isAdded() || mSwipeRefreshLayout == null) {
            return;
        }
        mSwipeRefreshLayout.setEnabled(enable);
    }

    @Override
    public void showSwipeProgress(final boolean show) {
        if (!isAdded()) {
            return;
        }
        if (!mSwipeRefreshLayout.isRefreshing() && !show) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void showCenterProgress(final boolean show) {
        if (!isAdded() || mProgressBarCenter == null) {
            return;
        }
        mProgressBarCenter.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        Timber.d("setUserVisibleHint url: %s, isVisibleToUser: %b", url, isVisibleToUser);
        if (isVisibleToUser && mArticle != null) {
            updateActivityMenuState();
            mPresenter.onVisibleToUser();
        }
    }

    @Override
    public void showData(final Article article) {
        Timber.d("showData: %s", article);
        mArticle = article;
        if (!isAdded()) {
            return;
        }
        if (mArticle == null || mArticle.text == null) {
            return;
        }
        Timber.d("setUserVisibleHint url: %s, value: %b, isFavorite: %s", url, getUserVisibleHint(), article.isInFavorite);
        if (getUserVisibleHint()) {
            updateActivityMenuState();
        }
        mAdapter.setData(mArticle, mExpandedSpoilers, mTabsViewModels);

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
        getActivity().invalidateOptionsMenu();
        if (getActivity() instanceof ToolbarStateSetter) {
            if (mArticle.title != null) {
                ((ToolbarStateSetter) getActivity()).setTitle(mArticle.title);
            }
        }
    }

    @Override
    public void onLinkClicked(final String link) {
        Timber.d("onLinkClicked: %s", link);
        //open predefined main activities link clicked
        for (final String pressedLink : mConstantValues.getAllLinksArray()) {
            if (link.equals(pressedLink)) {
                MainActivity.startActivity(getActivity(), link);
                return;
            }
        }

        getBaseActivity().startArticleActivity(link);
    }

    @Override
    public void onSnoskaClicked(final String link) {
        final List<String> articlesTextParts = mAdapter.getArticlesTextParts();
        if (TextUtils.isDigitsOnly(link)) {
            final String linkToFind = "footnote-" + link;
            for (int i = articlesTextParts.size() - 1; i >= 0; i--) {
                final Document document = Jsoup.parse(articlesTextParts.get(i));
                final Elements divTag = document.getElementsByAttributeValue("id", linkToFind);
                if (!divTag.isEmpty()) {
                    try {
                        String textThatWeTryToFindSoManyTime = divTag.html();
                        textThatWeTryToFindSoManyTime = textThatWeTryToFindSoManyTime.substring(3, textThatWeTryToFindSoManyTime.length());
                        Timber.d("textThatWeTryToFindSoManyTime: %s", textThatWeTryToFindSoManyTime);
                        new MaterialDialog.Builder(getActivity())
                                .title(getString(R.string.snoska, link))
                                .content(Html.fromHtml(textThatWeTryToFindSoManyTime, null, new MyHtmlTagHandler()))
                                .show();
                        break;
                    } catch (final Exception e) {
                        Timber.wtf(e, "Error while parse snoska");
                    }
                }
            }
        }
    }

    @Override
    public void onBibliographyClicked(final String link) {
        final List<String> articlesTextParts = mAdapter.getArticlesTextParts();
        for (int i = articlesTextParts.size() - 1; i >= 0; i--) {
            final Document document = Jsoup.parse(articlesTextParts.get(i));
            final Elements divTag = document.getElementsByAttributeValue("id", link);
            if (!divTag.isEmpty()) {
                String textThatWeTryToFindSoManyTime = divTag.text();
                textThatWeTryToFindSoManyTime = textThatWeTryToFindSoManyTime.substring(3, textThatWeTryToFindSoManyTime.length());
                new MaterialDialog.Builder(getActivity())
                        .title(getString(R.string.bibliography))
                        .content(textThatWeTryToFindSoManyTime)
                        .show();
                break;
            }
        }
    }

    @Override
    public void onTocClicked(final String link) {
        if (!isAdded()) {
            return;
        }
//        Timber.d("onTocClicked: %s", link);
        final List<String> articlesTextParts = mAdapter.getArticlesTextParts();
        String digits = "";
        for (final char c : link.toCharArray()) {
            if (TextUtils.isDigitsOnly(String.valueOf(c))) {
                digits += String.valueOf(c);
            }
        }
        for (int i = 0; i < articlesTextParts.size(); i++) {
            if (articlesTextParts.get(i).contains("id=\"" + "toc" + digits + "\"")) {
                Timber.d("found part: %s", articlesTextParts.get(i));
                mRecyclerView.scrollToPosition(i);
                return;
            }
        }
        Timber.d("check for a with name");
        //if reach here, so it's one of awful toc with bad style
        final String srtToCheck = "name=\"" + link + "\"";
        final String srtToCheck1 = "name=\"" + link.replace("#", "") + "\"";
//        Timber.d("srtToCheck: %s", srtToCheck);
//        Timber.d("srtToCheck1: %s", srtToCheck1);
        for (int i = 0; i < articlesTextParts.size(); i++) {
            if (articlesTextParts.get(i).contains(srtToCheck) ||
                    articlesTextParts.get(i).contains(srtToCheck1)) {
//                Timber.d("found part: %s", articlesTextParts.get(i));
                mRecyclerView.scrollToPosition(i);
                return;
            }
        }
    }

    @Override
    public void onImageClicked(final String link, @Nullable final String description) {
        if (!isAdded()) {
            return;
        }
        if (myPreferenceManager.imagesEnabled()) {
            GalleryActivity.startForImage(getActivity(), link, description);
        }
    }

    @Override
    public void onUnsupportedLinkPressed(final String link) {
        if (!isAdded()) {
            return;
        }
        showMessage(R.string.unsupported_link);
    }

    @Override
    public void onMusicClicked(final String link) {
        if (!isAdded()) {
            return;
        }
        try {
            final MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(link);
            mp.prepareAsync();
            mp.setOnPreparedListener(mediaPlayer -> mp.start());
            mp.setOnCompletionListener(MediaPlayer::release);
        } catch (final IOException e) {
            Timber.e(e, "error play music");
            showError(e);
        }
    }

    @Override
    public void onExternalDomenUrlClicked(final String link) {
        if (!isAdded()) {
            return;
        }
        IntentUtils.openUrl(link);
    }

    @Override
    public void onTagClicked(final ArticleTag tag) {
        if (!isAdded()) {
            return;
        }
        getBaseActivity().startTagsSearchActivity(Collections.singletonList(tag));
    }

    @Override
    public void onNotTranslatedArticleClick(final String link) {
        if (!isAdded()) {
            return;
        }
        showMessage(R.string.article_not_translated);
    }

    @Override
    public void onSpoilerExpand(final SpoilerViewModel spoilerViewModel) {
        if (!isAdded()) {
            return;
        }
        mExpandedSpoilers.add(spoilerViewModel);
    }

    @Override
    public void onSpoilerCollapse(final SpoilerViewModel spoilerViewModel) {
        if (!isAdded()) {
            return;
        }
        mExpandedSpoilers.remove(spoilerViewModel);
    }

    @Override
    public void onTabSelected(final TabsViewModel tabsViewModel) {
        if (!isAdded()) {
            return;
        }
        if (mTabsViewModels.contains(tabsViewModel)) {
            mTabsViewModels.set(mTabsViewModels.indexOf(tabsViewModel), tabsViewModel);
        } else {
            mTabsViewModels.add(tabsViewModel);
        }
    }

    @Override
    public void onRewardedVideoClick() {
        if (!isAdded()) {
            return;
        }
        getBaseActivity().startRewardedVideoFlow();
    }

    @Override
    public void onAdsSettingsClick() {
        if (!isAdded()) {
            return;
        }
        final BottomSheetDialogFragment subsDF = AdsSettingsBottomSheetDialogFragment.newInstance();
        subsDF.show(getActivity().getSupportFragmentManager(), subsDF.getTag());
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        //ignore facebook spam
        if (key.startsWith("com.facebook")) {
            return;
        }
        Timber.d("onSharedPreferenceChanged: key: %s", key);
        switch (key) {
            case MyPreferenceManager.Keys.TEXT_SCALE_ARTICLE:
            case MyPreferenceManager.Keys.DESIGN_FONT_PATH:
            case MyPreferenceManager.Keys.IS_TEXT_SELECTABLE:
                mAdapter.notifyDataSetChanged();
                break;
            case MyPreferenceManager.Keys.ADS_BANNER_IN_ARTICLE:
                showData(mPresenter.getData());
                break;
            case MyPreferenceManager.Keys.TIME_FOR_WHICH_BANNERS_DISABLED:
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd EEE HH:mm:ss", Locale.getDefault());
                Timber.d("Nex time is: %s", simpleDateFormat.format(new Date(sharedPreferences.getLong(key, 0))));
                showData(mPresenter.getData());
                break;
            default:
                //do nothing
                break;
        }
    }

    public interface ToolbarStateSetter {

        void setTitle(String title);
    }
}