package ru.kuchanov.scpcore.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import ru.kuchanov.scpcore.ui.fragment.article.ArticleFragment;

/**
 * Created by mohax on 07.01.2017.
 * <p>
 * for scp_ru
 */
public class ArticlesPagerAdapter extends FragmentStatePagerAdapter {
    private List<String> mData;

    public ArticlesPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setData(List<String> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return ArticleFragment.newInstance(mData.get(position));
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }
}