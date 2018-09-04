package ru.kuchanov.scpcore.ui.holder;

import com.google.android.flexbox.FlexboxLayout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.util.FontUtils;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.ui.view.TagView;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleTagsHolder extends RecyclerView.ViewHolder {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    private final SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    @BindView(R2.id.tags)
    TextView title;

    @BindView(R2.id.tagsContainer)
    FlexboxLayout mTagsContainer;

    public ArticleTagsHolder(final View itemView, final SetTextViewHTML.TextItemsClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mTextItemsClickListener = clickListener;
    }

    public void bind(final List<ArticleTag> data) {
        final Context context = itemView.getContext();
        final int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        final float articleTextScale = mMyPreferenceManager.getArticleTextScale();
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);

        title.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));

        mTagsContainer.removeAllViews();
        if (data != null) {
            for (final ArticleTag tag : data) {
                final TagView tagView = new TagView(context);
                tagView.setTag(tag);
                tagView.setActionImage(TagView.Action.NONE);

                tagView.setOnTagClickListener((tagView1, tag1) -> mTextItemsClickListener.onTagClicked(tag1));

                mTagsContainer.addView(tagView);
            }
        }
    }
}