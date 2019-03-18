package ru.kuchanov.scpcore.ui.util;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;

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

    public URLImageParser(TextView textView) {
        BaseApplication.getAppComponent().inject(this);
        mTextView = textView;
    }

    @Override
    public Drawable getDrawable(String source) {
        final UrlDrawable urlDrawable = new UrlDrawable();
        int holderId = AttributeGetter.getDrawableId(mTextView.getContext(), R.attr.iconEmptyImage);
        urlDrawable.placeHolder = ContextCompat.getDrawable(mTextView.getContext(), holderId);

        if (myPreferenceManager.imagesEnabled()) {
            Glide
                    .with(mTextView.getContext())
                    .load(source)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String s, Target<GlideDrawable> glideDrawableTarget, boolean b) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable d, String s, Target<GlideDrawable> glideDrawableTarget, boolean b, boolean b2) {
                            setProperImageSize(urlDrawable, d);

                            mTextView.invalidate();
                            mTextView.setText(mTextView.getText());
                            return true;
                        }
                    })
                    .into(new ViewTarget<TextView, GlideDrawable>(mTextView) {
                        @Override
                        public void onResourceReady(GlideDrawable d, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            setProperImageSize(urlDrawable, d);

                            mTextView.invalidate();
                            mTextView.setText(mTextView.getText());
                        }
                    });
        } else {
            Timber.d("Images not enabled!");
            Glide
                    .with(mTextView.getContext())
                    .load(holderId)
                    .listener(new RequestListener<Integer, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Integer s, Target<GlideDrawable> glideDrawableTarget, boolean b) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable d, Integer s, Target<GlideDrawable> glideDrawableTarget, boolean b, boolean b2) {
                            setProperImageSize(urlDrawable, d);

                            mTextView.invalidate();
                            mTextView.setText(mTextView.getText());
                            return true;
                        }
                    })
                    .into(new ViewTarget<TextView, GlideDrawable>(mTextView) {
                        @Override
                        public void onResourceReady(GlideDrawable d, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            setProperImageSize(urlDrawable, d);

                            mTextView.invalidate();
                            mTextView.setText(mTextView.getText());
                        }
                    });
        }
        return urlDrawable;
    }

    private void setProperImageSize(UrlDrawable urlDrawable, GlideDrawable resource) {
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

    private class UrlDrawable extends GlideDrawable {
        public GlideDrawable drawable;
        public Drawable placeHolder;

        public UrlDrawable() {
            super();
        }

        @Override
        public boolean isAnimated() {
            if (drawable != null) {
                return drawable.isAnimated();
            }
            return false;
        }

        @Override
        public void setLoopCount(int i) {
            if (drawable != null) {
                drawable.setLoopCount(i);
            }
        }

        @Override
        public void start() {
            if (drawable != null) {
                drawable.start();
            }
        }

        @Override
        public void stop() {
            if (drawable != null) {
                drawable.stop();
            }
        }

        @Override
        public boolean isRunning() {
            if (drawable != null) {
                return drawable.isRunning();
            }
            return false;
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
                drawable.draw(canvas);
                drawable.start();
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
    }
}
