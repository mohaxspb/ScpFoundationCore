package ru.kuchanov.scpcore.ui.adapter;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.VkImage;
import timber.log.Timber;

/**
 * Created by mohax on 14.03.2017.
 * <p>
 * for scp_ru
 */
public class ImagesPagerAdapter extends PagerAdapter {

    private LayoutInflater mLayoutInflater;

    private List<VkImage> mData = new ArrayList<>();

    public List<VkImage> getData() {
        return mData;
    }

    public void downloadImage(Context context, int position, SimpleTarget<Bitmap> target) {
        Toast.makeText(context, R.string.image_loading, Toast.LENGTH_SHORT).show();
        String url = mData.get(position).allUrls.get(mData.get(position).allUrls.size() - 1).getVal();
        Glide.with(context)
                .load(url)
                .asBitmap()
                .into(target);
    }

    public void setData(List<VkImage> urls) {
        mData = urls;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Context context = container.getContext();
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        //image
        View itemView;

        ProgressBar progressBar;
        CardView cardView;
        TextView description;

        itemView = LayoutInflater.from(container.getContext()).inflate(getLayoutRes(), container, false);
        if (position == 0) {
            itemView.setAlpha(1f);
        }
        ImageView imageView = itemView.findViewById(R.id.image);
        progressBar = itemView.findViewById(R.id.progressCenter);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setAlpha(1f);

        cardView = itemView.findViewById(R.id.descriptionContainer);
        description = itemView.findViewById(R.id.description);

        description.setText(mData.get(position).description);

        imageView.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mData.get(position).description)) {
                return;
            }
            cardView.setVisibility(cardView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        String url = mData.get(position).allUrls.get(mData.get(position).allUrls.size() - 1).getVal();
        Timber.d("url: %s", url);
        //remove delay
        Glide.clear(imageView);
        Glide.with(context)
                .load(url)
                .fitCenter()
                .thumbnail(url.endsWith("gif") ? 1f : .1f)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Timber.e(e);
                        progressBar.animate().alpha(0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                progressBar.setAlpha(1f);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Timber.d("onResourceReady");
                        progressBar.animate().alpha(0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                progressBar.setAlpha(1f);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                        return false;
                    }
                })
                .diskCacheStrategy(url.endsWith("gif") ? DiskCacheStrategy.SOURCE : DiskCacheStrategy.RESULT)
                .into(imageView);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
//        //try to clear glide
//        //seems to its at least not crashing app and, may be make it faster...
        if (view == null) {
            return;
        }
        //FROM http://stackoverflow.com/questions/37789091/viewpager-inside-recyclerview-as-row-item
        try {
            // Remove the view from the container
            collection.removeView((View) view);

            //clear customProgress too
//            CustomProgressBar customProgressBar = (CustomProgressBar) ((ViewGroup) view).getChildAt(1);
//            customProgressBar.hide();

            // Try to clear resources used for displaying this view
            Glide.clear((View) ((View) view).findViewById(R.id.image));
            // Remove any resources used by this view
            unbindDrawables((View) view);
            // Invalidate the object
            view = null;
        } catch (Exception ignored) {
//            Log.w(TAG, "destroyItem: failed to destroy item and clear it's used resources", e);
        }
    }

    /**
     * Recursively unbind any resources from the provided view. This method will clear the resources of all the
     * children of the view before invalidating the provided view itself.
     *
     * @param view The view for which to unbind resource.
     *             <p>
     *             from http://stackoverflow.com/questions/37789091/viewpager-inside-recyclerview-as-row-item
     */
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private int getLayoutRes() {
        return R.layout.item_image;
    }
}