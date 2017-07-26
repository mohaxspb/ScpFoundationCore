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

    public ArticleTitleHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);
    }

    public void bind(String title) {
        Context ctx = itemView.getContext();
        float articleTextScale = mMyPreferenceManager.getArticleTextScale();

        int textSizePrimary = ctx.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTextScale * textSizePrimary);
        //TODO add settings for it
//            textView.setTextIsSelectable(true);
        textView.setText(title);
    }
}