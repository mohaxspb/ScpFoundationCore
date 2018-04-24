package ru.dante.scpfoundation.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.downloads.DownloadEntry;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.util.DialogUtilsDefault;

/**
 * Created by mohax on 01.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DialogUtilsImpl extends DialogUtilsDefault {

    public DialogUtilsImpl(
            MyPreferenceManager preferenceManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient,
            ConstantValues constantValues,
            Class clazz
    ) {
        super(preferenceManager, dbProviderFactory, apiClient, constantValues, clazz);
    }

    @Override
    public List<DownloadEntry> getDownloadTypesEntries(Context context) {
        List<DownloadEntry> downloadEntries = new ArrayList<>();

        downloadEntries.add(new DownloadEntry(R.string.type_1, context.getString(R.string.type_1), mConstantValues.getObjects1(), Article.FIELD_IS_IN_OBJECTS_1));
        downloadEntries.add(new DownloadEntry(R.string.type_2, context.getString(R.string.type_2), mConstantValues.getObjects2(), Article.FIELD_IS_IN_OBJECTS_2));
        downloadEntries.add(new DownloadEntry(R.string.type_3, context.getString(R.string.type_3), mConstantValues.getObjects3(), Article.FIELD_IS_IN_OBJECTS_3));
        downloadEntries.add(new DownloadEntry(R.string.type_4, context.getString(R.string.type_4), mConstantValues.getObjects4(), Article.FIELD_IS_IN_OBJECTS_4));
        downloadEntries.add(new DownloadEntry(R.string.type_ru, context.getString(R.string.type_ru), mConstantValues.getObjectsRu(), Article.FIELD_IS_IN_OBJECTS_RU));

        downloadEntries.add(new DownloadEntry(R.string.type_all, context.getString(R.string.type_all), mConstantValues.getNewArticles(), Article.FIELD_IS_IN_RECENT));
        return downloadEntries;
    }
}