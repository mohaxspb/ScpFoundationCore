package ru.dante.scpfoundation.util;

import android.support.v4.app.FragmentTransaction;

import ru.dante.scpfoundation.ConstantValuesImpl;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.base.BaseActivity;
import ru.kuchanov.scpcore.ui.fragment.MaterialsArchiveFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsExperimentsFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsIncidentsFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsInterviewsFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsJokesFragment;
import ru.kuchanov.scpcore.ui.fragment.MaterialsOtherFragment;

/**
 * Created by mohax on 24.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class MaterialClickListenerImpl implements MaterialsActivity.MaterialClickListener {

    private ConstantValues mConstantValues;

    public MaterialClickListenerImpl(ConstantValues constantValues) {
        mConstantValues = constantValues;
    }

    @Override
    public void onMaterialClick(int position, BaseActivity activity) {
        switch (position) {
            case 0:
                activity.startArticleActivity(mConstantValues.getJokes());
                break;
            case 1:
                activity.startArticleActivity(ConstantValuesImpl.Urls.ANOMALS);
                break;
            case 2:
                activity.startArticleActivity(ConstantValuesImpl.Urls.NADNATURALNE);
                break;
            case 3:
                activity.startArticleActivity(ConstantValuesImpl.Urls.LOCATIONS);
                break;
            default:
                throw new RuntimeException("unexpected position in materials list");
        }
    }
}