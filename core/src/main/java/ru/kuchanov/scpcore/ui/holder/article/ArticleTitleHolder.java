package ru.kuchanov.scpcore.ui.holder.article;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.model.ArticleTextPartViewModel;
import ru.kuchanov.scpcore.util.AttributeGetter;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleTitleHolder extends RecyclerView.ViewHolder {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @BindView(R2.id.text)
    TextView textView;

    public ArticleTitleHolder(final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);
    }

    public void bind(final ArticleTextPartViewModel viewModel) {
        final Context context = itemView.getContext();

        if (viewModel.isInSpoiler) {
            final int defaultMargin = context.getResources().getDimensionPixelSize(R.dimen.defaultMargin);
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(defaultMargin, 0, defaultMargin, 0);
            itemView.setBackgroundColor(AttributeGetter.getColor(context, R.attr.windowBackgroundDark));
        } else {
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(0, 0, 0, 0);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        final float articleTextScale = mMyPreferenceManager.getArticleTextScale();

        final int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);
        textView.setTextIsSelectable(mMyPreferenceManager.isTextSelectable());
        textView.setText((String) viewModel.data);
    }
}