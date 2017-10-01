package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.model.TabsViewModel;
import ru.kuchanov.scpcore.util.AttributeGetter;
import timber.log.Timber;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleTabsHolder extends RecyclerView.ViewHolder {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    private MyTabClickListener myTabClickListener;

    @BindView(R2.id.tabLayout)
    TabLayout tabLayout;

    public ArticleTabsHolder(View itemView, MyTabClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        myTabClickListener = clickListener;
    }

    public void bind(TabsViewModel data) {
        Timber.d("bin tabs: %s", data.getCurrentTab());
        Context context = itemView.getContext();
        int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        float articleTextScale = mMyPreferenceManager.getArticleTextScale();

        if (data.isInSpoiler) {
            int defaultMargin = context.getResources().getDimensionPixelSize(R.dimen.defaultMargin);
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(defaultMargin, 0, defaultMargin, 0);
            itemView.setBackgroundColor(AttributeGetter.getColor(context, R.attr.windowBackgroundDark));
        } else {
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(0, 0, 0, 0);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        tabLayout.clearOnTabSelectedListeners();
        tabLayout.removeAllTabs();
        for (String title : data.getTitles()) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
        tabLayout.setVisibility(View.VISIBLE);


        TabLayout.Tab selectedTab = tabLayout.getTabAt(data.getCurrentTab());
        if (selectedTab != null) {
            selectedTab.select();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Timber.d("onTabSelected: %s", tab.getPosition());

                data.setCurrentTab(tab.getPosition());

                myTabClickListener.onTabSelected(getAdapterPosition(), tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public interface MyTabClickListener {
        void onTabSelected(int positionInAdapter, int positionInTabs);
    }
}