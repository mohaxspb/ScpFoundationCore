package ru.dante.scpfoundation.di.module;

import android.support.annotation.NonNull;

import dagger.Module;
import ru.dante.scpfoundation.util.DialogUtilsImpl;
import ru.dante.scpfoundation.service.DownloadAllServiceImpl;
import ru.dante.scpfoundation.util.MaterialClickListenerImpl;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scp.downloads.DialogUtils;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.di.module.HelpersModule;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;

/**
 * Created by mohax on 10.07.2017.
 * <p>
 * for ScpFoundationRu
 */
@Module(includes = HelpersModule.class)
public class HelpersModuleImpl extends HelpersModule {

    @Override
    protected DialogUtils<Article> getDownloadAllDialogUtils(
            @NonNull MyPreferenceManager preferenceManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new DialogUtilsImpl(
                preferenceManager,
                dbProviderFactory,
                apiClient,
                constantValues,
                DownloadAllServiceImpl.class
        );
    }

    @Override
    protected MaterialsActivity.MaterialClickListener getMaterialClickListenerImpl(@NonNull ConstantValues constantValues) {
        return new MaterialClickListenerImpl(constantValues);
    }
}