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
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.AttributeGetter;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

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
    @BindView(R2.id.content)
    TextView content;

    private SpoilerViewModel mSpoilerViewModel;

    private SpoilerClickListener mSpoilerClickListener;

    public ArticleSpoilerHolder(View itemView, SpoilerClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mSpoilerClickListener = clickListener;
    }

//    public void bind(String textPart) {
//        Context context = itemView.getContext();
//        int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
//        float articleTextScale = mMyPreferenceManager.getArticleTextScale();
//        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);
//
//        CalligraphyUtils.applyFontToTextView(context, title, mMyPreferenceManager.getFontPath());
//        CalligraphyUtils.applyFontToTextView(context, content, mMyPreferenceManager.getFontPath());
//
//        List<String> spoilerParts = ParseHtmlUtils.getSpoilerParts(textPart);
//
//        title.setText(spoilerParts.get(0));
//        //TODO add settings for it
////            mContent.setTextIsSelectable(true);
//        content.setLinksClickable(true);
//        content.setMovementMethod(LinkMovementMethod.getInstance());
////        Timber.d("spoilerParts.get(1): %s", spoilerParts.get(1));
//        mSetTextViewHTML.setText(content, spoilerParts.get(1), mTextItemsClickListener);
//
//        title.setOnClickListener(v -> {
//            if (content.getVisibility() == View.GONE) {
//                title.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowUp), 0, 0, 0);
//                content.setVisibility(View.VISIBLE);
//            } else {
//                title.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowDown), 0, 0, 0);
//                content.setVisibility(View.GONE);
//            }
//        });
//    }

    public void bind(SpoilerViewModel data) {
        mSpoilerViewModel = data;
        Context context = itemView.getContext();
        int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        float articleTextScale = mMyPreferenceManager.getArticleTextScale();
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);

        CalligraphyUtils.applyFontToTextView(context, title, mMyPreferenceManager.getFontPath());
        CalligraphyUtils.applyFontToTextView(context, content, mMyPreferenceManager.getFontPath());

        title.setText(mSpoilerViewModel.titles.get(0));

        title.setOnClickListener(v -> {
            mSpoilerViewModel.isExpanded = !mSpoilerViewModel.isExpanded;
            if (mSpoilerViewModel.isExpanded) {
                title.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowUp), 0, 0, 0);
                mSpoilerClickListener.onSpoilerExpand(mSpoilerViewModel);
            } else {
                title.setCompoundDrawablesWithIntrinsicBounds(AttributeGetter.getDrawableId(context, R.attr.iconArrowDown), 0, 0, 0);
                mSpoilerClickListener.onSpoilerCollapse(mSpoilerViewModel);
            }
        });
    }

    public interface SpoilerClickListener {
        void onSpoilerExpand(SpoilerViewModel spoilerViewModel);

        void onSpoilerCollapse(SpoilerViewModel spoilerViewModel);
    }
}