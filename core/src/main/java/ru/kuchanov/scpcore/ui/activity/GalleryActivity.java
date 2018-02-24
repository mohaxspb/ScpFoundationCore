package ru.kuchanov.scpcore.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.VkImage;
import ru.kuchanov.scpcore.monetization.util.MyAdListener;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.mvp.contract.GalleryScreenMvp;
import ru.kuchanov.scpcore.ui.adapter.ImagesAdapter;
import ru.kuchanov.scpcore.ui.adapter.ImagesPagerAdapter;
import ru.kuchanov.scpcore.util.IntentUtils;
import ru.kuchanov.scpcore.util.StorageUtils;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.GALLERY_BANNER_DISABLED;
import static ru.kuchanov.scpcore.ui.activity.MainActivity.EXTRA_SHOW_DISABLE_ADS;

public class GalleryActivity
        extends BaseDrawerActivity<GalleryScreenMvp.View, GalleryScreenMvp.Presenter>
        implements GalleryScreenMvp.View {

    private static final String EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL";
    private static final String EXTRA_IMAGE_DESCRIPTION = "EXTRA_IMAGE_DESCRIPTION";

    @BindView(R2.id.viewPager)
    ViewPager mViewPager;
    @BindView(R2.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R2.id.bottomSheet)
    View mBottomSheet;
    @BindView(R2.id.progressCenter)
    View mProgressContainer;
    @BindView(R2.id.placeHolder)
    View mPlaceHolder;
    @BindView(R2.id.refresh)
    Button mRefresh;

    private ImagesPagerAdapter mPagerAdapter;
    private ImagesAdapter mRecyclerAdapter;
    private int mCurPosition;

    public static void startForImage(final Context context, final String imageUrl, @Nullable final String imageDescription) {
        final Intent intent = new Intent(context, GalleryActivity.class);
        intent.putExtra(EXTRA_IMAGE_URL, imageUrl);
        intent.putExtra(EXTRA_IMAGE_DESCRIPTION, imageDescription);
        context.startActivity(intent);
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_POSITION, mCurPosition);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(EXTRA_SHOW_DISABLE_ADS)) {
            showSnackBarWithAction(Constants.Firebase.CallToActionReason.REMOVE_ADS);
            getIntent().removeExtra(EXTRA_SHOW_DISABLE_ADS);

            @DataSyncActions.ScoreAction final String action = DataSyncActions.ScoreAction.INTERSTITIAL_SHOWN;
            mPresenter.updateUserScoreForScoreAction(action);
        }

        if (savedInstanceState != null) {
            mCurPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        }

        if (mToolbar != null) {
            mToolbar.setTitle(R.string.gallery);
        }
        mPagerAdapter = new ImagesPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                mCurPosition = position;
                if (position != 0 && position % FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.NUM_OF_GALLERY_PHOTOS_BETWEEN_INTERSITIAL) == 0) {
                    final boolean hasSubscription = mMyPreferenceManager.isHasSubscription() || mMyPreferenceManager.isHasNoAdsSubscription();
                    if (!hasSubscription) {
                        if (isAdsLoaded()) {
                            showInterstitial(new MyAdListener() {
                                @Override
                                public void onAdClosed() {
                                    @DataSyncActions.ScoreAction final String action = DataSyncActions.ScoreAction.INTERSTITIAL_SHOWN;
                                    mPresenter.updateUserScoreForScoreAction(action);
                                    showSnackBarWithAction(Constants.Firebase.CallToActionReason.REMOVE_ADS);
                                    requestNewInterstitial();
                                }
                            }, false);
                        } else {
                            requestNewInterstitial();
                        }
                    }
                }
            }
        });

        mRecyclerAdapter = new ImagesAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.setImageClickListener((position, v) -> mViewPager.setCurrentItem(position));

        mViewPager.setCurrentItem(mCurPosition);

        //set data to presenter if we want show only one image
        if (getIntent().hasExtra(EXTRA_IMAGE_URL)) {
            mPresenter.setData(Collections.singletonList(new VkImage(
                    getIntent().getStringExtra(EXTRA_IMAGE_URL),
                    getIntent().getStringExtra(EXTRA_IMAGE_DESCRIPTION)
            )));
            mBottomSheet.setVisibility(View.GONE);
        }
        if (mPresenter.getData() != null) {
            mPagerAdapter.setData(mPresenter.getData());
            mRecyclerAdapter.setData(mPresenter.getData());
        } else {
            mPresenter.getDataFromDb();
            mPresenter.updateData();
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
        return R.layout.activity_gallery;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_gallery;
    }

    @Override
    public boolean onNavigationItemClicked(final int id) {
        Timber.d("onNavigationItemClicked with id: %s", id);
        String link = null;

        if (id == R.id.invite) {
            IntentUtils.firebaseInvite(this);
        } else if (id == R.id.leaderboard) {
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
        } else if (id == R.id.files) {
            startMaterialsActivity();
        } else if (id == R.id.favorite) {
            link = Constants.Urls.FAVORITES;
        } else if (id == R.id.offline) {
            link = Constants.Urls.OFFLINE;
        } else if (id == R.id.read) {
            link = Constants.Urls.READ;
        } else if (id == R.id.gallery) {
            //nothing to do
            return true;
        } else if (id == R.id.siteSearch) {
            link = Constants.Urls.SEARCH;
        } else if (id == R.id.tagsSearch) {
            startTagsSearchActivity();
        } else if (id == R.id.objects_RU) {
            link = mConstantValues.getObjectsRu();
        } else if (id == R.id.news) {
            link = mConstantValues.getNews();
        } else if (id == R.id.objects_IV) {
            link = mConstantValues.getObjects4();
        } else if (id == R.id.stories) {
            link = mConstantValues.getStories();
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
        if (i == R.id.share) {
            if (mPagerAdapter.getData().isEmpty()) {
                return true;
            }
            mPagerAdapter.downloadImage(GalleryActivity.this, mViewPager.getCurrentItem(),
                    new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(final Bitmap resource, final GlideAnimation glideAnimation) {
                            final String desc = mPagerAdapter.getData().get(mViewPager.getCurrentItem()).description;
                            IntentUtils.shareBitmapWithText(GalleryActivity.this, desc, resource);
                        }
                    });
            return true;
        } else if (i == R.id.save_image) {
            if (mPagerAdapter.getData().isEmpty()) {
                return true;
            }
            mPagerAdapter.downloadImage(GalleryActivity.this, mViewPager.getCurrentItem(),
                    new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(final Bitmap resource, final GlideAnimation glideAnimation) {
                            if (StorageUtils.saveImageToGallery(GalleryActivity.this, resource) != null) {
                                Toast.makeText(GalleryActivity.this, R.string.image_saved, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(GalleryActivity.this, R.string.image_saving_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showData(final List<VkImage> data) {
        mPagerAdapter.setData(data);
        mRecyclerAdapter.setData(data);

        mViewPager.setCurrentItem(mCurPosition);
    }

    @Override
    public void showCenterProgress(final boolean show) {
        mProgressContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showEmptyPlaceholder(final boolean show) {
        mPlaceHolder.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @OnClick(R2.id.refresh)
    public void onRefreshClicked() {
        mPresenter.updateData();
    }

    @Override
    public boolean isBannerEnabled() {
        return !FirebaseRemoteConfig.getInstance().getBoolean(GALLERY_BANNER_DISABLED);
    }
}