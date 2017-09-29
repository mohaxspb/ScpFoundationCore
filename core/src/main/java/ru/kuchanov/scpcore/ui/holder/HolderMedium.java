package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class HolderMedium extends HolderMax {

    public HolderMedium(View itemView, ArticlesListAdapter.ArticleClickListener clickListener) {
        super(itemView, clickListener);
    }

    @Override
    public void bind(Article article) {
        super.bind(article);

        Context context = itemView.getContext();
        float uiTextScale = mMyPreferenceManager.getUiTextScale();
        int textSizeLarge = context.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeLarge);
    }

    @Override
    protected void setTypesIcons(Article article) {
        switch (article.type) {
            default:
            case Article.ObjectType.NONE:
                typeIcon.setImageResource(R.drawable.ic_none_medium);
                break;
            case Article.ObjectType.NEUTRAL_OR_NOT_ADDED:
                typeIcon.setImageResource(R.drawable.ic_not_add_medium);
                break;
            case Article.ObjectType.SAFE:
                typeIcon.setImageResource(R.drawable.ic_safe_medium);
                break;
            case Article.ObjectType.EUCLID:
                typeIcon.setImageResource(R.drawable.ic_euclid_medium);
                break;
            case Article.ObjectType.KETER:
                typeIcon.setImageResource(R.drawable.ic_keter_medium);
                break;
            case Article.ObjectType.THAUMIEL:
                typeIcon.setImageResource(R.drawable.ic_thaumiel_medium);
                break;
        }
    }
}