package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
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
import ru.kuchanov.scpcore.ui.model.SpoilerViewModel;
import ru.kuchanov.scpcore.ui.util.FontUtils;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.AttributeGetter;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleSpoilerHolder extends RecyclerView.ViewHolder {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    @Inject
    SetTextViewHTML mSetTextViewHTML;

    @BindView(R2.id.title)
    TextView title;

    private final SpoilerClickListener mSpoilerClickListener;

    public ArticleSpoilerHolder(final View itemView, final SpoilerClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mSpoilerClickListener = clickListener;
    }

    public void bind(final SpoilerViewModel mSpoilerViewModel) {
        final Context context = itemView.getContext();
        final int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        final float articleTextScale = mMyPreferenceManager.getArticleTextScale();
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);

        title.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));

        if (mSpoilerViewModel.isExpanded) {
            title.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowUp), 0, 0, 0);
            title.setText(mSpoilerViewModel.titles.get(1));
        } else {
            title.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowDown), 0, 0, 0);
            title.setText(mSpoilerViewModel.titles.get(0));
        }

        title.setOnClickListener(v -> {
            mSpoilerViewModel.isExpanded = !mSpoilerViewModel.isExpanded;
            if (mSpoilerViewModel.isExpanded) {
                title.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowUp), 0, 0, 0);
                mSpoilerClickListener.onSpoilerExpand(getAdapterPosition());
                title.setText(mSpoilerViewModel.titles.get(1));
            } else {
                title.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowDown), 0, 0, 0);
                mSpoilerClickListener.onSpoilerCollapse(getAdapterPosition());
                title.setText(mSpoilerViewModel.titles.get(0));
            }
        });
    }

    public interface SpoilerClickListener {

        void onSpoilerExpand(int position);

        void onSpoilerCollapse(int position);
    }
}