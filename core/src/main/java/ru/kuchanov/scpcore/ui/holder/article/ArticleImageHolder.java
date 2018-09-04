package ru.kuchanov.scpcore.ui.holder.article;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.model.ArticleTextPartViewModel;
import ru.kuchanov.scpcore.ui.util.FontUtils;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.AttributeGetter;
import timber.log.Timber;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleImageHolder extends RecyclerView.ViewHolder {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @Inject
    SetTextViewHTML mSetTextViewHTML;

    private final SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    @BindView(R2.id.image)
    ImageView imageView;

    @BindView(R2.id.title)
    TextView titleTextView;

    @BindView(R2.id.progressCenter)
    ProgressBar progressCenter;

    public ArticleImageHolder(final View itemView, final SetTextViewHTML.TextItemsClickListener clickListener) {
        super(itemView);
        BaseApplication.getAppComponent().inject(this);
        ButterKnife.bind(this, itemView);

        mTextItemsClickListener = clickListener;
    }

    public void bind(final ArticleTextPartViewModel viewModel) {
        final Context context = itemView.getContext();

        final int defaultMargin = context.getResources().getDimensionPixelSize(R.dimen.defaultMargin);
        if (viewModel.isInSpoiler) {
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            params.leftMargin = defaultMargin;
            params.rightMargin = defaultMargin;
            itemView.setBackgroundColor(AttributeGetter.getColor(context, R.attr.windowBackgroundDark));
        } else {
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            params.leftMargin = 0;
            params.rightMargin = 0;
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        final Document document = Jsoup.parse((String) viewModel.data);
        final Element imageTag = document.getElementsByTag("img").first();
        final String imageUrl = imageTag == null ? null : imageTag.attr("src");

        titleTextView.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));

        final int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        final float articleTextScale = mMyPreferenceManager.getArticleTextScale();
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);

        final String title;
        if (!document.getElementsByTag("span").isEmpty()) {
            title = document.getElementsByTag("span").first().html();
//            Timber.d("title: %s", title);
        } else if (!document.getElementsByClass("scp-image-caption").isEmpty()) {
            title = document.getElementsByClass("scp-image-caption").first().html();
        } else {
            title = null;
        }

        if (!TextUtils.isEmpty(title)) {
            titleTextView.setLinksClickable(true);
            titleTextView.setMovementMethod(LinkMovementMethod.getInstance());
            titleTextView.setTextIsSelectable(mMyPreferenceManager.isTextSelectable());
            mSetTextViewHTML.setText(titleTextView, title, mTextItemsClickListener);
        }

        progressCenter.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(imageUrl) && imageUrl.endsWith("gif")) {
            Glide.with(context)
                    .load(imageUrl)
                    .asGif()
                    .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .fitCenter()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GifDrawable>() {
                        @Override
                        public boolean onException(
                                final Exception e,
                                final String model,
                                final Target<GifDrawable> target,
                                final boolean isFirstResource
                        ) {
                            Timber.e(e, "error while download image by glide");
                            progressCenter.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                final GifDrawable resource,
                                final String model,
                                final Target<GifDrawable> target,
                                final boolean isFromMemoryCache,
                                final boolean isFirstResource
                        ) {
                            int width = resource.getIntrinsicWidth();
                            int height = resource.getIntrinsicHeight();

                            final float multiplier = (float) width / height;

                            width = imageView.getMeasuredWidth();

                            height = (int) (width / multiplier);

                            imageView.getLayoutParams().width = width;
                            imageView.getLayoutParams().height = height;

                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                            imageView.setOnClickListener(v -> mTextItemsClickListener.onImageClicked(imageUrl, title));

                            progressCenter.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
        } else {
            //search for saved file
            File file = null;
            if (!TextUtils.isEmpty(imageUrl)) {
                file = new File(context.getFilesDir(), "/image/" + ApiClient.formatUrlToFileName(imageUrl));
            }
            Glide.with(context)
                    .load(file != null && file.exists() ? "file://" + file.getAbsolutePath() : imageUrl)
                    .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .fitCenter()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(
                                final Exception e,
                                final String model,
                                final Target<GlideDrawable> target,
                                final boolean isFirstResource
                        ) {
                            Timber.e(e, "error while download image by glide");
                            progressCenter.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                final GlideDrawable resource,
                                final String model,
                                final Target<GlideDrawable> target,
                                final boolean isFromMemoryCache,
                                final boolean isFirstResource
                        ) {
                            int width = resource.getIntrinsicWidth();
                            int height = resource.getIntrinsicHeight();

                            final float multiplier = (float) width / height;

                            width = imageView.getMeasuredWidth();

                            height = (int) (width / multiplier);

                            imageView.getLayoutParams().width = width;
                            imageView.getLayoutParams().height = height;

                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                            imageView.setOnClickListener(v -> mTextItemsClickListener.onImageClicked(imageUrl, title));
                            progressCenter.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}