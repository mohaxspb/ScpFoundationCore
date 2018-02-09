package ru.dante.scpfoundation.util;

import ru.dante.scpfoundation.ConstantValuesImpl;
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
                activity.startArticleActivity(ConstantValuesImpl.Urls.MATERIALS);
                break;
            default:
                throw new RuntimeException("unexpected position in materials list");
        }
    }
}