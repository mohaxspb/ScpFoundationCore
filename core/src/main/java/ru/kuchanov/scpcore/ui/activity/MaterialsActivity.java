package ru.kuchanov.scpcore.ui.activity;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.mvp.contract.MaterialsScreenMvp;
import ru.kuchanov.scpcore.ui.base.BaseActivity;
import ru.kuchanov.scpcore.ui.base.BaseDrawerActivity;
import ru.kuchanov.scpcore.ui.dialog.TextSizeDialogFragment;
import ru.kuchanov.scpcore.ui.fragment.ArticleFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsAllFragment;
import ru.kuchanov.scpcore.util.IntentUtils;
import timber.log.Timber;

import static ru.kuchanov.scpcore.ui.activity.MainActivity.EXTRA_SHOW_DISABLE_ADS;

public class MaterialsActivity
        extends BaseDrawerActivity<MaterialsScreenMvp.View, MaterialsScreenMvp.Presenter>
        implements MaterialsScreenMvp.View, ArticleFragment.ToolbarStateSetter {

    @Inject
    MaterialsActivity.MaterialClickListener mMaterialClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(EXTRA_SHOW_DISABLE_ADS)) {
            showSnackBarWithAction(Constants.Firebase.CallToActionReason.REMOVE_ADS);
            getIntent().removeExtra(EXTRA_SHOW_DISABLE_ADS);

            @DataSyncActions.ScoreAction
            String action = DataSyncActions.ScoreAction.INTERSTITIAL_SHOWN;
            mPresenter.updateUserScoreForScoreAction(action);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, MaterialsAllFragment.newInstance(), MaterialsAllFragment.TAG)
                    .addToBackStack(MaterialsAllFragment.TAG)
                    .commit();
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
    public boolean onNavigationItemClicked(int id) {
        Timber.d("onNavigationItemClicked with id: %s", id);
        String link = null;

        if (id == R.id.invite) {
            IntentUtils.firebaseInvite(this);
        } else  if (id == R.id.about) {
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
        } else if (id == R.id.files) {
            getSupportFragmentManager().popBackStackImmediate(MaterialsAllFragment.TAG, 0);
            return false;
        } else if (id == R.id.favorite) {
            link = Constants.Urls.FAVORITES;
        } else if (id == R.id.offline) {
            link = Constants.Urls.OFFLINE;
        } else if (id == R.id.gallery) {
            startGalleryActivity();
        } else if (id == R.id.siteSearch) {
            link = Constants.Urls.SEARCH;
        } else if (id == R.id.tagsSearch) {
            startTagsSearchActivity();
            return true;
        } else if (id == R.id.objects_RU) {
            link = mConstantValues.getObjectsRu();
        } else if (id == R.id.news) {
            link = mConstantValues.getNews();
        } else if (id == R.id.objects_IV) {
            link = mConstantValues.getObjects4();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("onOptionsItemSelected with id: %s", item);
        int i = item.getItemId();
        if (i == R.id.text_size) {
            BottomSheetDialogFragment fragmentDialogTextAppearance =
                    TextSizeDialogFragment.newInstance(TextSizeDialogFragment.TextSizeType.ALL);
            fragmentDialogTextAppearance.show(getSupportFragmentManager(), TextSizeDialogFragment.TAG);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(String title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    @Override
    public void setFavoriteState(boolean isInFavorite) {
        //nothing to do
    }

    @Override
    public void onMaterialsListItemClicked(int position) {
        List<String> materials = Arrays.asList(getResources().getStringArray(R.array.materials_titles));
        Timber.d("onMaterialsListItemClicked: %s", materials.get(position));

        mMaterialClickListener.onMaterialClick(position, this);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    public interface MaterialClickListener {
        void onMaterialClick(int position, BaseActivity activity);
    }

    @Override
    public boolean isBannerEnabled() {
        //TODO think if we should show banner here
        return false;
    }
}