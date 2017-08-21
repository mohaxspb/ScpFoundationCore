package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
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
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.AttributeGetter;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleTextHolder extends RecyclerView.ViewHolder {

    @Inject
    MyPreferenceManager mMyPreferenceManager;
    @Inject
    SetTextViewHTML mSetTextViewHTML;

    private SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    @BindView(R2.id.text)
    TextView textView;

    public ArticleTextHolder(View itemView, SetTextViewHTML.TextItemsClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mTextItemsClickListener = clickListener;
    }

    public void bind(ArticleTextPartViewModel viewModel) {
        Context context = itemView.getContext();

        if (viewModel.isInSpoiler) {
            int defaultMargin = context.getResources().getDimensionPixelSize(R.dimen.defaultMargin);
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(defaultMargin, 0, defaultMargin, 0);
            itemView.setBackgroundColor(AttributeGetter.getColor(context, R.attr.windowBackgroundDark));
        } else {
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(0, 0, 0, 0);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);
        float articleTextScale = mMyPreferenceManager.getArticleTextScale();

        CalligraphyUtils.applyFontToTextView(context, textView, mMyPreferenceManager.getFontPath());

        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);
        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        //TODO add settings for it
//            textView.setTextIsSelectable(true);
        mSetTextViewHTML.setText(textView, (String) viewModel.data, mTextItemsClickListener);
    }
}