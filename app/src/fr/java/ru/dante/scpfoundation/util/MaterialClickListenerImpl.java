package ru.dante.scpfoundation.util;

import ru.dante.scpfoundation.ConstantValuesImpl;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.base.BaseActivity;

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
//            case 1:
//                activity.startArticleActivity(ConstantValuesImpl.Urls.SCP_EX);
//                break;
//            case 2:
//                activity.startArticleActivity(ConstantValuesImpl.Urls.ARCHIVE);
//                break;
            case 1:
                activity.startArticleActivity(ConstantValuesImpl.Urls.ANOMALS);
                break;
            case 2:
                activity.startArticleActivity(ConstantValuesImpl.Urls.EVENTS);
                break;
            default:
                throw new RuntimeException("unexpected position in materials list");
        }
    }
}