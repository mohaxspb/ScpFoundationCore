package ru.kuchanov.scpcore.ui.util;

import android.support.v4.app.FragmentTransaction;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.fragment.materials.MaterialsArchiveFragment;
import ru.kuchanov.scpcore.ui.fragment.materials.MaterialsExperimentsFragment;
import ru.kuchanov.scpcore.ui.fragment.materials.MaterialsIncidentsFragment;
import ru.kuchanov.scpcore.ui.fragment.materials.MaterialsInterviewsFragment;
import ru.kuchanov.scpcore.ui.fragment.materials.MaterialsJokesFragment;
import ru.kuchanov.scpcore.ui.fragment.materials.MaterialsOtherFragment;

/**
 * Created by mohax on 24.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class MaterialClickListenerDefault implements MaterialsActivity.MaterialClickListener {

    private ConstantValues mConstantValues;

    public MaterialClickListenerDefault(ConstantValues constantValues) {
        mConstantValues = constantValues;
    }

    @Override
    public void onMaterialClick(int position, BaseActivity activity) {
        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        switch (position) {
            case 0:
                fragmentTransaction.replace(R.id.content, MaterialsExperimentsFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 1:
                fragmentTransaction.replace(R.id.content, MaterialsIncidentsFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 2:
                fragmentTransaction.replace(R.id.content, MaterialsInterviewsFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 3:
                fragmentTransaction.replace(R.id.content, MaterialsJokesFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 4:
                fragmentTransaction.replace(R.id.content, MaterialsArchiveFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 5:
                fragmentTransaction.replace(R.id.content, MaterialsOtherFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 6:
                activity.startArticleActivity(mConstantValues.getLeaks());
                break;
            default:
                throw new RuntimeException("unexpected position in materials list");
        }
    }
}