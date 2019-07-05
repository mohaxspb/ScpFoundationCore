package ru.kuchanov.scpcore.ui.holder.articlelist;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter;
import ru.kuchanov.scpcore.ui.util.FontUtils;
import ru.kuchanov.scpcore.util.AttributeGetter;

/**
 * Created by mohax on 11.06.2017.
 */
public class HolderMin extends RecyclerView.ViewHolder {

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    ArticlesListAdapter.ArticleClickListener mArticleClickListener;

    protected Article mData;

    @BindView(R2.id.favorite)
    ImageView favorite;

    @BindView(R2.id.read)
    ImageView read;

    @BindView(R2.id.offline)
    ImageView offline;

    @BindView(R2.id.title)
    TextView title;

    @BindView(R2.id.preview)
    TextView preview;

    @BindView(R2.id.typeIcon)
    ImageView typeIcon;

    public HolderMin(final View itemView, final ArticlesListAdapter.ArticleClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mArticleClickListener = clickListener;
    }

    public void bind(final Article article) {
        mData = article;
        final Context context = itemView.getContext();

        final float uiTextScale = mMyPreferenceManager.getUiTextScale();
        final int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);

        title.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));
        preview.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));

        itemView.setOnClickListener(v -> mArticleClickListener.onArticleClick(article));

        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
        if (!TextUtils.isEmpty(article.title)) {
            title.setText(Html.fromHtml(article.title));
        }
        //(отмечание прочитанного)
        final int readIconId;
        final int readColorId;
        if (article.isInReaden) {
            readColorId = AttributeGetter.getColor(context, R.attr.readTextColor);
            readIconId = AttributeGetter.getDrawableId(context, R.attr.readIconUnselected);
        } else {
            readColorId = AttributeGetter.getColor(context, R.attr.newArticlesTextColor);
            readIconId = AttributeGetter.getDrawableId(context, R.attr.readIcon);
        }
        title.setTextColor(readColorId);
        read.setImageResource(readIconId);
        read.setOnClickListener(v -> mArticleClickListener.toggleReadenState(article));
        //(отмтка избранных статей)
        final int favsIconId;
        if (article.isInFavorite != Article.ORDER_NONE) {
            favsIconId = AttributeGetter.getDrawableId(context, R.attr.favoriteIcon);
        } else {
            favsIconId = AttributeGetter.getDrawableId(context, R.attr.favoriteIconUnselected);
        }
        favorite.setImageResource(favsIconId);

        //Кнопки Offline
        final int offlineIconId;
        if (article.text != null) {
            offlineIconId = AttributeGetter.getDrawableId(context, R.attr.iconOfflineRemove);
        } else {
            offlineIconId = AttributeGetter.getDrawableId(context, R.attr.iconOfflineAdd);
        }
        offline.animate().cancel();
        offline.setRotation(0f);
        offline.setImageResource(offlineIconId);
        offline.setOnClickListener(v -> {
            if (mArticleClickListener != null) {
                if (article.text != null) {
                    final PopupMenu popup = new PopupMenu(context, offline);
                    popup.getMenu().add(0, 0, 0, R.string.delete);
                    popup.setOnMenuItemClickListener(item -> {
                        mArticleClickListener.onOfflineClick(article);
                        return true;
                    });
                    popup.show();
                } else {
                    mArticleClickListener.onOfflineClick(article);
                }
            }
        });

        if (context.getResources().getBoolean(R.bool.filter_by_type_enabled)) {
            setTypesIcons(article);
        } else {
            typeIcon.setVisibility(View.GONE);
        }
    }

    protected void setTypesIcons(final Article article) {
        switch (article.type) {
            case Article.ObjectType.NONE:
                typeIcon.setImageResource(R.drawable.ic_none_small);
                break;
            case Article.ObjectType.NEUTRAL_OR_NOT_ADDED:
                typeIcon.setImageResource(R.drawable.ic_not_add_small);
                break;
            case Article.ObjectType.SAFE:
                typeIcon.setImageResource(R.drawable.ic_safe_small);
                break;
            case Article.ObjectType.EUCLID:
                typeIcon.setImageResource(R.drawable.ic_euclid_small);
                break;
            case Article.ObjectType.KETER:
                typeIcon.setImageResource(R.drawable.ic_keter_small);
                break;
            case Article.ObjectType.THAUMIEL:
                typeIcon.setImageResource(R.drawable.ic_thaumiel_small);
                break;
            default:
                throw new IllegalArgumentException("unexpected article type: " + article.type);
        }
    }

    public void setShouldShowPreview(final boolean shouldShowPreview) {
        final Context context = itemView.getContext();
        final float uiTextScale = mMyPreferenceManager.getUiTextScale();
        final int textSizeTertiary = context.getResources().getDimensionPixelSize(R.dimen.text_size_tertiary);
        //show preview only on siteSearch fragment
        if (shouldShowPreview) {
            preview.setVisibility(View.VISIBLE);
            preview.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeTertiary);
            preview.setText(Html.fromHtml(mData.preview));
        } else {
            preview.setVisibility(View.GONE);
        }
    }

    public void setShouldShowPopupOnFavoriteClick(final boolean shouldShowPopupOnFavoriteClick) {
        final Context context = itemView.getContext();
        favorite.setOnClickListener(v -> {
            if (shouldShowPopupOnFavoriteClick && mData.isInFavorite != Article.ORDER_NONE) {
                final PopupMenu popup = new PopupMenu(context, favorite);
                popup.getMenu().add(0, 0, 0, R.string.delete);
                popup.setOnMenuItemClickListener(item -> {
                    mArticleClickListener.toggleFavoriteState(mData);
                    return true;
                });
                popup.show();
            } else {
                mArticleClickListener.toggleFavoriteState(mData);
            }
        });
    }
}