package ru.kuchanov.scpcore.ui.holder.articlelist;

import com.google.android.flexbox.FlexboxLayout;

import com.bumptech.glide.Glide;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter;
import ru.kuchanov.scpcore.ui.util.FontUtils;
import ru.kuchanov.scpcore.ui.view.TagView;
import ru.kuchanov.scpcore.util.AttributeGetter;
import ru.kuchanov.scpcore.util.DateUtils;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class HolderMax extends HolderMin {

    @BindView(R2.id.typeIcon)
    ImageView typeIcon;

    @BindView(R2.id.image)
    ImageView image;

    @BindView(R2.id.rating)
    TextView rating;

    @BindView(R2.id.date)
    TextView date;

    @BindView(R2.id.tags)
    FlexboxLayout mTagsContainer;

    @BindView(R2.id.tagsExpander)
    TextView mTagsExpander;

    public HolderMax(final View itemView, final ArticlesListAdapter.ArticleClickListener clickListener) {
        super(itemView, clickListener);
    }

    @Override
    public void bind(final Article article) {
        super.bind(article);
        final Context context = itemView.getContext();

        rating.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));
        date.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));

        //TODO show them in ViewPager
        //set image
        if (article.imagesUrls != null && !article.imagesUrls.isEmpty() && mMyPreferenceManager.imagesEnabled()) {
            Glide.clear(image);

            final String imageUrl = article.imagesUrls.first().val;
            File file = null;
            if (!TextUtils.isEmpty(imageUrl)) {
                file = new File(context.getFilesDir(), "/image/" + ApiClient.formatUrlToFileName(imageUrl));
            }
            Glide.with(context)
                    .load(file != null && file.exists() ? "file://" + file.getAbsolutePath() : imageUrl)
                    .placeholder(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .animate(android.R.anim.fade_in)
                    .centerCrop()
                    .into(image);
        } else {
            Glide.clear(image);
            Glide.with(context)
                    .load(R.drawable.ic_default_image_big)
                    .placeholder(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .centerCrop()
                    .animate(android.R.anim.fade_in)
                    .into(image);
        }

        rating.setText(article.rating != 0 ? context.getString(R.string.rating, article.rating) : null);
        date.setText(article.updatedDate != null ? DateUtils.getArticleDateShortFormat(article.updatedDate) : null);

        showTags(article);
    }

    @Override
    protected void setTypesIcons(final Article article) {
        switch (article.type) {
            default:
            case Article.ObjectType.NONE:
                typeIcon.setImageResource(R.drawable.ic_none_big);
                break;
            case Article.ObjectType.NEUTRAL_OR_NOT_ADDED:
                typeIcon.setImageResource(R.drawable.ic_not_add_big);
                break;
            case Article.ObjectType.SAFE:
                typeIcon.setImageResource(R.drawable.ic_safe_big);
                break;
            case Article.ObjectType.EUCLID:
                typeIcon.setImageResource(R.drawable.ic_euclid_big);
                break;
            case Article.ObjectType.KETER:
                typeIcon.setImageResource(R.drawable.ic_keter_big);
                break;
            case Article.ObjectType.THAUMIEL:
                typeIcon.setImageResource(R.drawable.ic_thaumiel_big);
                break;
        }
    }

    private void showTags(final Article article) {
//            Timber.d("article.tags: %s", Arrays.toString(article.tags.toArray()));
        final Context context = itemView.getContext();
//            Timber.d("mTagsContainer.getChildCount(): %s", mTagsContainer.getChildCount());
        final int childCount = mTagsContainer.getChildCount();
        for (int i = childCount - 1; i > 0; i--) {
            mTagsContainer.removeViewAt(i);
        }
//            Timber.d("mTagsContainer.getChildCount(): %s", mTagsContainer.getChildCount());
        if (article.tags == null || article.tags.isEmpty()) {
            mTagsContainer.setVisibility(View.GONE);
        } else {
            mTagsContainer.setVisibility(View.VISIBLE);

            mTagsExpander.setCompoundDrawablesWithIntrinsicBounds(0, 0, AttributeGetter.getDrawableId(context, R.attr.iconArrowDownThemed), 0);
            mTagsExpander.setOnClickListener(v -> {
                if (mTagsContainer.getChildAt(1).getVisibility() == View.GONE) {
                    mTagsExpander.setCompoundDrawablesWithIntrinsicBounds(0, 0, AttributeGetter.getDrawableId(context, R.attr.iconArrowUpThemed), 0);
                    for (int i = 1; i < mTagsContainer.getChildCount(); i++) {
                        mTagsContainer.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                } else {
                    mTagsExpander.setCompoundDrawablesWithIntrinsicBounds(0, 0, AttributeGetter.getDrawableId(context, R.attr.iconArrowDownThemed), 0);
                    for (int i = 1; i < mTagsContainer.getChildCount(); i++) {
                        mTagsContainer.getChildAt(i).setVisibility(View.GONE);
                    }
                }
            });

            for (final ArticleTag tag : article.tags) {
                final TagView tagView = new TagView(context);
                tagView.setTag(tag);
                tagView.setTagTextSize(11);
                tagView.setActionImage(TagView.Action.NONE);

                tagView.setOnTagClickListener((tagView1, tag1) -> mArticleClickListener.onTagClick(tag1));
                tagView.setVisibility(View.GONE);

                mTagsContainer.addView(tagView);
            }
        }
    }
}