package ru.kuchanov.scpcore.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.VkImage;
import ru.kuchanov.scpcore.monetization.util.MyAdListener;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.mvp.contract.GalleryScreenMvp;
import ru.kuchanov.scpcore.ui.adapter.ImagesPagerAdapter;
import ru.kuchanov.scpcore.ui.adapter.ImagesRecyclerAdapter;
import ru.kuchanov.scpcore.ui.base.BaseDrawerActivity;
import ru.kuchanov.scpcore.util.IntentUtils;
import ru.kuchanov.scpcore.util.StorageUtils;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.GALLERY_BANNER_DISABLED;
import static ru.kuchanov.scpcore.ui.activity.MainActivity.EXTRA_SHOW_DISABLE_ADS;

public class GalleryActivity
        extends BaseDrawerActivity<GalleryScreenMvp.View, GalleryScreenMvp.Presenter>
        implements GalleryScreenMvp.View {

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

    @BindView(R2.id.banner)
    AdView mAdView;

    private ImagesPagerAdapter mAdapter;
    private ImagesRecyclerAdapter mRecyclerAdapter;
    private int mCurPosition;

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_POSITION, mCurPosition);
    }

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

        if (savedInstanceState != null) {
            mCurPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        }

        if (mToolbar != null) {
            mToolbar.setTitle(R.string.gallery);
        }
        mAdapter = new ImagesPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurPosition = position;
                if (position % FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.NUM_OF_GALLERY_PHOTOS_BETWEEN_INTERSITIAL) == 0) {
                    if (getOwnedItems().isEmpty()) {
                        if (isAdsLoaded()) {
                            showInterstitial(new MyAdListener() {
                                @Override
                                public void onAdClosed() {
                                    @DataSyncActions.ScoreAction
                                    String action = DataSyncActions.ScoreAction.INTERSTITIAL_SHOWN;
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

        mRecyclerAdapter = new ImagesRecyclerAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.setImageClickListener((position, v) -> mViewPager.setCurrentItem(position));

        mViewPager.setCurrentItem(mCurPosition);

        if (mPresenter.getData() != null) {
            mAdapter.setData(mPresenter.getData());
            mRecyclerAdapter.setData(mPresenter.getData());
        } else {
            mPresenter.getDataFromDb();
            mPresenter.updateData();
        }
    }

    @Override
    public void initAds() {
        super.initAds();

        if (!isAdsLoaded()) {
            requestNewInterstitial();
        }

        AdRequest.Builder adRequest = new AdRequest.Builder();

        if (BuildConfig.DEBUG) {
            @SuppressLint("HardwareIds")
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId;
            deviceId = SystemUtils.MD5(androidId);
            if (deviceId != null) {
                deviceId = deviceId.toUpperCase();
                adRequest.addTestDevice(deviceId);
            }
            adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        }

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        if (mMyPreferenceManager.isHasNoAdsSubscription()
                || mMyPreferenceManager.isHasSubscription()
                || remoteConfig.getBoolean(GALLERY_BANNER_DISABLED)) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadAd(adRequest.build());
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
    public boolean onNavigationItemClicked(int id) {
        Timber.d("onNavigationItemClicked with id: %s", id);
        String link = null;

        if (id == R.id.about) {
            link = mConstantValues.getUrlsValues().getAbout();
        } else if (id == R.id.news) {
            link = mConstantValues.getUrlsValues().getNews();
        } else if (id == R.id.mostRatedArticles) {
            link = mConstantValues.getUrlsValues().getMostRated();
        } else if (id == R.id.mostRecentArticles) {
            link = mConstantValues.getUrlsValues().getNewArticles();
        } else if (id == R.id.random_page) {
            mPresenter.getRandomArticleUrl();
        } else if (id == R.id.objects_I) {
            link = mConstantValues.getUrlsValues().getObjects1();
        } else if (id == R.id.objects_II) {
            link = mConstantValues.getUrlsValues().getObjects2();
        } else if (id == R.id.objects_III) {
            link = mConstantValues.getUrlsValues().getObjects3();
        } else if (id == R.id.objects_IV) {
            link = mConstantValues.getUrlsValues().getObjects4();
        } else if (id == R.id.files) {
            startMaterialsActivity();
        } else if (id == R.id.favorite) {
            link = Constants.Urls.FAVORITES;
        } else if (id == R.id.offline) {
            link = Constants.Urls.OFFLINE;
        } else if (id == R.id.gallery) {
            //nothing to do
        } else if (id == R.id.siteSearch) {
            link = Constants.Urls.SEARCH;
        } else if (id == R.id.tagsSearch) {
            startTagsSearchActivity();
        } else if (id == R2.id.objects_RU) {
            link = mConstantValues.getUrlsValues().getObjectsRu();
        } else if (id == R2.id.stories) {
            link = mConstantValues.getUrlsValues().getStories();
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
        if (i == R.id.share) {
            if (mAdapter.getData().isEmpty()) {
                return true;
            }
            mAdapter.downloadImage(GalleryActivity.this, mViewPager.getCurrentItem(),
                    new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            String desc = mAdapter.getData().get(mViewPager.getCurrentItem()).description;
                            IntentUtils.shareBitmapWithText(GalleryActivity.this, desc, resource);
                        }
                    });
            return true;
        } else if (i == R.id.save_image) {
            if (mAdapter.getData().isEmpty()) {
                return true;
            }
            mAdapter.downloadImage(GalleryActivity.this, mViewPager.getCurrentItem(),
                    new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
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
    public void showData(List<VkImage> data) {
        Timber.d("showData: %s", data.size());
        mAdapter.setData(data);
        mRecyclerAdapter.setData(data);

        mViewPager.setCurrentItem(mCurPosition);
    }

    @Override
    public void showCenterProgress(boolean show) {
        Timber.d("showCenterProgress: %s", show);
        mProgressContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showEmptyPlaceholder(boolean show) {
        Timber.d("showEmptyPlaceholder: %s", show);
        mPlaceHolder.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @OnClick(R2.id.refresh)
    public void onRefreshClicked() {
        Timber.d("onRefreshClicked");
        mPresenter.updateData();
    }
}