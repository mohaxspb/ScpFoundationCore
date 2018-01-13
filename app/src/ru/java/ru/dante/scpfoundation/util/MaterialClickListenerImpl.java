package ru.dante.scpfoundation.util;

import android.support.v4.app.FragmentTransaction;

import ru.dante.scpfoundation.ui.fragment.ObjectsDeArticlesFragment;
import ru.dante.scpfoundation.ui.fragment.ObjectsEsArticlesFragment;
import ru.dante.scpfoundation.ui.fragment.ObjectsFrArticlesFragment;
import ru.dante.scpfoundation.ui.fragment.ObjectsJpArticlesFragment;
import ru.dante.scpfoundation.ui.fragment.ObjectsPlArticlesFragment;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.base.BaseActivity;
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
public class MaterialClickListenerImpl implements MaterialsActivity.MaterialClickListener {

    private ConstantValues mConstantValues;

    public MaterialClickListenerImpl(ConstantValues constantValues) {
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
            case 7:
                fragmentTransaction.replace(R.id.content, ObjectsFrArticlesFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 8:
                fragmentTransaction.replace(R.id.content, ObjectsJpArticlesFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 9:
                fragmentTransaction.replace(R.id.content, ObjectsEsArticlesFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 10:
                fragmentTransaction.replace(R.id.content, ObjectsPlArticlesFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case 11:
                fragmentTransaction.replace(R.id.content, ObjectsDeArticlesFragment.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            default:
                throw new RuntimeException("unexpected position in materials list");
        }
    }
}