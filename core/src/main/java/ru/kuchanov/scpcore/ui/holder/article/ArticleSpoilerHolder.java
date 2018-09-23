package ru.kuchanov.scpcore.ui.holder.article;

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
    TextView titleTextView;

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
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);

        titleTextView.setTypeface(FontUtils.getTypeFaceFromName(mMyPreferenceManager.getFontPath()));
        titleTextView.setTextIsSelectable(mMyPreferenceManager.isTextSelectable());

        if (mSpoilerViewModel.isExpanded) {
            titleTextView.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowUp), 0, 0, 0);
            titleTextView.setText(mSpoilerViewModel.titles.get(1));
        } else {
            titleTextView.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowDown), 0, 0, 0);
            titleTextView.setText(mSpoilerViewModel.titles.get(0));
        }

        titleTextView.setOnClickListener(v -> {
            mSpoilerViewModel.isExpanded = !mSpoilerViewModel.isExpanded;
            if (mSpoilerViewModel.isExpanded) {
                titleTextView.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowUp), 0, 0, 0);
                mSpoilerClickListener.onSpoilerExpand(getAdapterPosition());
                titleTextView.setText(mSpoilerViewModel.titles.get(1));
            } else {
                titleTextView.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowDown), 0, 0, 0);
                mSpoilerClickListener.onSpoilerCollapse(getAdapterPosition());
                titleTextView.setText(mSpoilerViewModel.titles.get(0));
            }
        });
    }

    public interface SpoilerClickListener {

        void onSpoilerExpand(int position);

        void onSpoilerCollapse(int position);
    }
}