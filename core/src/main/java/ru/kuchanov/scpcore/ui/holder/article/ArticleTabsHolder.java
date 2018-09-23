package ru.kuchanov.scpcore.ui.holder.article;

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

    private final MyTabClickListener myTabClickListener;

    @BindView(R2.id.tabLayout)
    TabLayout tabLayout;

    public ArticleTabsHolder(final View itemView, final MyTabClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        myTabClickListener = clickListener;
    }

    public void bind(final TabsViewModel data) {
        Timber.d("bin tabs: %s", data.getCurrentTab());
        final Context context = itemView.getContext();

        if (data.isInSpoiler) {
            final int defaultMargin = context.getResources().getDimensionPixelSize(R.dimen.defaultMargin);
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(defaultMargin, 0, defaultMargin, 0);
            itemView.setBackgroundColor(AttributeGetter.getColor(context, R.attr.windowBackgroundDark));
        } else {
            ((RecyclerView.LayoutParams) itemView.getLayoutParams()).setMargins(0, 0, 0, 0);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        tabLayout.clearOnTabSelectedListeners();
        tabLayout.removeAllTabs();
        for (final String title : data.getTitles()) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
        tabLayout.setVisibility(View.VISIBLE);

        final TabLayout.Tab selectedTab = tabLayout.getTabAt(data.getCurrentTab());
        if (selectedTab != null) {
            selectedTab.select();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                Timber.d("onTabSelected: %s", tab.getPosition());
                data.setCurrentTab(tab.getPosition());

                myTabClickListener.onTabSelected(getAdapterPosition(), tab.getPosition());
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {
            }
        });
    }

    public interface MyTabClickListener {

        void onTabSelected(int positionInAdapter, int positionInTabs);
    }
}