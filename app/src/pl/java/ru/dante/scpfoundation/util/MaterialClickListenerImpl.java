package ru.dante.scpfoundation.util;

import ru.dante.scpfoundation.ConstantValuesImpl;
import ru.kuchanov.scpcore.ConstantValues;
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