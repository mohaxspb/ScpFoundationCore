package ru.kuchanov.scpcore.ui.util;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.util.AttributeGetter;
import ru.kuchanov.scpcore.util.DimensionUtils;
import timber.log.Timber;

/**
 * Created by mohax on 05.01.2017.
 * <p>
 * for scp_ru
 */
public class URLImageParser implements Html.ImageGetter {
    private TextView mTextView;

    @Inject
    MyPreferenceManager myPreferenceManager;

    URLImageParser(TextView textView) {
        BaseApplication.getAppComponent().inject(this);
        mTextView = textView;
    }

    @Override
    public Drawable getDrawable(String source) {
        Timber.d("getDrawable source: %s", source);
        final UrlDrawable urlDrawable = new UrlDrawable();
        int holderId = AttributeGetter.getDrawableId(mTextView.getContext(), R.attr.iconEmptyImage);
        urlDrawable.placeHolder = ContextCompat.getDrawable(mTextView.getContext(), holderId);

        if (myPreferenceManager.imagesEnabled()) {
            Glide
                    .with(mTextView.getContext())
                    .load(source)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            setProperImageSize(urlDrawable, resource);

                            mTextView.invalidate();
                            mTextView.setText(mTextView.getText());
                            return true;
                        }
                    })
                    .into(new CustomViewTarget<TextView, Drawable>(mTextView) {
                        @Override
                        protected void onResourceCleared(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            setProperImageSize(urlDrawable, resource);

                            mTextView.invalidate();
                            mTextView.setText(mTextView.getText());
                        }
                    });
        } else {
            Timber.d("Images not enabled!");
            Glide
                    .with(mTextView.getContext())
                    .load(holderId)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            setProperImageSize(urlDrawable, resource);

                            mTextView.invalidate();
                            mTextView.setText(mTextView.getText());
                            return true;
                        }
                    })
                    .into(new CustomViewTarget<TextView, Drawable>(mTextView) {
                        @Override
                        protected void onResourceCleared(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            setProperImageSize(urlDrawable, resource);

                            mTextView.invalidate();
                            mTextView.setText(mTextView.getText());
                        }

                    });
        }
        return urlDrawable;
    }

    private void setProperImageSize(UrlDrawable urlDrawable, Drawable resource) {
        int width = resource.getIntrinsicWidth();
        int height = resource.getIntrinsicHeight();

        float multiplier = (float) width / height;
        if (width > DimensionUtils.getScreenWidth()) {
            width = DimensionUtils.getScreenWidth();
        }
        height = (int) (width / multiplier);

        resource.setBounds(0, 0, width, height);
        urlDrawable.setBounds(0, 0, width, height);

        urlDrawable.drawable = resource;
    }

    private class UrlDrawable extends Drawable {
        public Drawable drawable;
        public Drawable placeHolder;

        UrlDrawable() {
            super();
        }

        @Override
        public void setAlpha(int alpha) {
            if (drawable != null) {
                drawable.setAlpha(alpha);
            }
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            if (drawable != null) {
                drawable.setColorFilter(cf);
            }
        }

        @Override
        public int getOpacity() {
            if (drawable != null) {
                return drawable.getOpacity();
            }
            return PixelFormat.UNKNOWN;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (drawable != null) {
//                Timber.d("draw canvas: %s/%s", canvas.getHeight(), canvas.getWidth());
//                Timber.d("draw drawable: %s/%s", drawable.getMinimumHeight(), drawable.getMinimumWidth());
//                Timber.d("draw drawable: %s", drawable.getBounds());
//                Timber.d("draw drawable: %s/%s", DimensionUtils.getScreenHeight(), DimensionUtils.getScreenWidth());
                try {
//                    drawable = resizeToScreenSize(drawable);
                    drawable.draw(canvas);
                } catch (Exception e) {
                    Timber.e(e, "Error while draw on canvas");
                }
            } else {
                placeHolder.draw(canvas);
            }
        }

        @NonNull
        @Override
        public Drawable getCurrent() {
            if (drawable == null) {
                return placeHolder;
            }
            return super.getCurrent();
        }

//        private Drawable resizeToScreenSize(Drawable image) {
//            int screenHeight = DimensionUtils.getScreenHeight();
//            int screenWidth = DimensionUtils.getScreenWidth();
//            int drawableHeight = image.getIntrinsicHeight();
//            int drawableWidth = image.getIntrinsicWidth();
//
//            int desiredHeight = drawableHeight;
//            int desiredWidth = drawableWidth;
//            if (desiredHeight > screenHeight) {
//                float multiplier = (float) desiredHeight / screenHeight;
//                Timber.d("multiplier: %s", multiplier);
//                desiredHeight = (int) (desiredHeight/multiplier);
//                desiredWidth = (int) (desiredWidth/multiplier);
//            } else if (desiredWidth > screenWidth) {
//                float multiplier = (float) desiredWidth / screenWidth;
//                Timber.d("multiplier: %s", multiplier);
//                desiredHeight = (int) (desiredHeight/multiplier);
//                desiredWidth = (int) (desiredWidth/multiplier);
//            }
//            Bitmap b = ((BitmapDrawable) image).getBitmap();
//            Bitmap bitmapResized = Bitmap.createScaledBitmap(b, desiredWidth, desiredHeight, false);
//            return new BitmapDrawable(BaseApplication.getAppInstance().getResources(), bitmapResized);
//        }
    }
}
