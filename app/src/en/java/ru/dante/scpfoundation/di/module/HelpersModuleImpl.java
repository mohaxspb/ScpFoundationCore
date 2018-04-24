package ru.dante.scpfoundation.di.module;

import android.support.annotation.NonNull;

import dagger.Module;
import ru.dante.scpfoundation.service.DownloadAllServiceImpl;
import ru.dante.scpfoundation.util.DialogUtilsImpl;
import ru.dante.scpfoundation.util.MaterialClickListenerImpl;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.di.module.HelpersModule;
import ru.kuchanov.scpcore.downloads.DialogUtils;
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
    protected DialogUtils getDownloadAllDialogUtils(
            @NonNull final MyPreferenceManager preferenceManager,
            @NonNull final DbProviderFactory dbProviderFactory,
            @NonNull final ApiClient apiClient,
            @NonNull final ConstantValues constantValues
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
    protected MaterialsActivity.MaterialClickListener getMaterialClickListenerImpl(@NonNull final ConstantValues constantValues) {
        return new MaterialClickListenerImpl(constantValues);
    }
}