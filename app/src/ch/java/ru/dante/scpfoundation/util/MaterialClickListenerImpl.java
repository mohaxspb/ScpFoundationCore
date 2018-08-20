package ru.dante.scpfoundation.util;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;

/**
 * Created by mohax on 24.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class MaterialClickListenerImpl implements MaterialsActivity.MaterialClickListener {

    private final ConstantValues mConstantValues;

    public MaterialClickListenerImpl(final ConstantValues constantValues) {
        super();
        mConstantValues = constantValues;
    }

    @Override
    public void onMaterialClick(final int position, final BaseActivity activity) {
        switch (position) {
            case 0:
                activity.startArticleActivity(mConstantValues.getJokes());
                break;
            case 1:
                activity.startArticleActivity(mConstantValues.getExperiments());
                break;
            case 2:
                activity.startArticleActivity(mConstantValues.getIncidents());
                break;
            case 3:
                activity.startArticleActivity(mConstantValues.getLeaks());
                break;
            default:
                throw new RuntimeException("unexpected position in materials list");
        }
    }
}