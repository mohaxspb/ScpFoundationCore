package ru.kuchanov.scpcore.ui.activity;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import java.util.List;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsScreenMvp;
import ru.kuchanov.scpcore.ui.dialog.TextSizeDialogFragment;
import ru.kuchanov.scpcore.ui.fragment.article.ArticleFragment;
import ru.kuchanov.scpcore.ui.fragment.materials.MaterialsAllFragment;
import ru.kuchanov.scpcore.ui.fragment.tags.TagsSearchFragment;
import ru.kuchanov.scpcore.ui.fragment.tags.TagsSearchResultsArticlesFragment;
import ru.kuchanov.scpcore.util.IntentUtils;
import timber.log.Timber;

import static ru.kuchanov.scpcore.ui.activity.MainActivity.EXTRA_SHOW_DISABLE_ADS;

public class TagSearchActivity
        extends BaseDrawerActivity<TagsScreenMvp.View, TagsScreenMvp.Presenter>
        implements TagsScreenMvp.View, ArticleFragment.ToolbarStateSetter, TagsSearchFragment.ShowTagsSearchResults {

    private List<ArticleTag> mTags;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(EXTRA_SHOW_DISABLE_ADS)) {
            showSnackBarWithAction(Constants.Firebase.CallToActionReason.REMOVE_ADS);
            getIntent().removeExtra(EXTRA_SHOW_DISABLE_ADS);

            @DataSyncActions.ScoreAction final String action = DataSyncActions.ScoreAction.INTERSTITIAL_SHOWN;
            mPresenter.updateUserScoreForScoreAction(action);
        }

        if (getIntent().hasExtra(EXTRA_TAGS)) {
            mTags = ArticleTag.getTagsFromStringList(getIntent().getStringArrayListExtra(EXTRA_TAGS));
        }

        if (savedInstanceState == null) {
            if (mTags != null && !mTags.isEmpty()) {
                showResults(null, mTags);
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.content, TagsSearchFragment.newInstance(), TagsSearchFragment.TAG)
                        .addToBackStack(MaterialsAllFragment.TAG)
                        .commit();
            }
        }
    }

    @Override
    protected boolean isDrawerIndicatorEnabled() {
        return false;
    }

    @Override
    protected int getDefaultNavItemId() {
        return SELECTED_DRAWER_ITEM_NONE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_materials;
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_main;
    }

    @Override
    public boolean onNavigationItemClicked(final int id) {
        Timber.d("onNavigationItemClicked with id: %s", id);
        String link = null;

        if (id == R.id.leaderboard) {
            SubscriptionsActivity.start(this, SubscriptionsActivity.TYPE_LEADERBOARD);
        } else if (id == R.id.about) {
            link = mConstantValues.getAbout();
        } else if (id == R.id.mostRatedArticles) {
            link = mConstantValues.getMostRated();
        } else if (id == R.id.mostRecentArticles) {
            link = mConstantValues.getNewArticles();
        } else if (id == R.id.random_page) {
            mPresenter.getRandomArticleUrl();
        } else if (id == R.id.objects_I) {
            link = mConstantValues.getObjects1();
        } else if (id == R.id.objects_II) {
            link = mConstantValues.getObjects2();
        } else if (id == R.id.objects_III) {
            link = mConstantValues.getObjects3();
        } else if (id == R.id.objects_IV) {
            link = mConstantValues.getObjects4();
        } else if (id == R.id.objects_V) {
            link = mConstantValues.getObjects5();
        } else if (id == R.id.files) {
            startMaterialsActivity();
        } else if (id == R.id.favorite) {
            link = Constants.Urls.FAVORITES;
        } else if (id == R.id.offline) {
            link = Constants.Urls.OFFLINE;
        } else if (id == R.id.read) {
            link = Constants.Urls.READ;
        } else if (id == R.id.gallery) {
            startGalleryActivity();
        } else if (id == R.id.siteSearch) {
            link = Constants.Urls.SEARCH;
        } else if (id == R.id.tagsSearch) {
            if (getSupportFragmentManager().findFragmentByTag(TagsSearchFragment.TAG) != null) {
                getSupportFragmentManager().popBackStackImmediate(TagsSearchFragment.TAG, 0);
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, TagsSearchFragment.newInstance(), TagsSearchFragment.TAG)
                        .commit();
            }
        } else if (id == R.id.objects_RU) {
            link = mConstantValues.getObjectsRu();
        } else if (id == R.id.news) {
            link = mConstantValues.getNews();
        } else if (id == R.id.stories) {
            link = Constants.Urls.STORIES;
        } else {
            Timber.e("unexpected item ID");
        }
        if (link != null) {
            MainActivity.startActivity(this, link);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Timber.d("onOptionsItemSelected with id: %s", item);
        final int i = item.getItemId();
        if (i == R.id.text_size) {
            final BottomSheetDialogFragment fragmentDialogTextAppearance =
                    TextSizeDialogFragment.newInstance(TextSizeDialogFragment.TextSizeType.ALL);
            fragmentDialogTextAppearance.show(getSupportFragmentManager(), TextSizeDialogFragment.TAG);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(final String title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void showResults(final List<Article> data, final List<ArticleTag> tags) {
        final Fragment fragmentResults = TagsSearchResultsArticlesFragment.newInstance(data, tags);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, fragmentResults, TagsSearchResultsArticlesFragment.TAG)
                .addToBackStack(TagsSearchResultsArticlesFragment.TAG)
                .commit();
    }

    @Override
    public boolean isBannerEnabled() {
        return false;
    }
}
