package ru.dante.scpfoundation.util;

import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.activity.BaseActivity;

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
                activity.startArticleActivity(mConstantValues.getArchive());
                break;
            case 2:
                activity.startArticleActivity(mConstantValues.getExperiments());
                break;
            case 3:
                activity.startArticleActivity(mConstantValues.getInterviews());
                break;
            default:
                throw new RuntimeException("unexpected position in materials list");
        }
    }
}