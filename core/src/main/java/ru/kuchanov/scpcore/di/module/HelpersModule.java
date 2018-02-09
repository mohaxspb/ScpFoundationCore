package ru.kuchanov.scpcore.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.InAppHelper;
import ru.kuchanov.scpcore.service.DownloadAllServiceDefault;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.util.DialogUtils;
import ru.kuchanov.scpcore.ui.util.DialogUtilsDefault;
import ru.kuchanov.scpcore.ui.util.MaterialClickListenerDefault;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;

/**
 * Created by y.kuchanov on 22.12.16.
 * <p>
 * for scp_ru
 */
@Module
public class HelpersModule {

    @Provides
//    @Singleton
    DialogUtils providesDialogUtils(
             MyPreferenceManager preferenceManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient
    ) {
        return new DialogUtils(preferenceManager, dbProviderFactory, apiClient);
    }

    @Provides
    @Singleton
    ru.kuchanov.scp.downloads.DialogUtils<Article> providesDownloadAllDialogUtils(
             MyPreferenceManager preferenceManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient,
             ConstantValues constantValues
    ) {
        return getDownloadAllDialogUtils(preferenceManager, dbProviderFactory, apiClient, constantValues);
    }

    protected ru.kuchanov.scp.downloads.DialogUtils<Article> getDownloadAllDialogUtils(
             MyPreferenceManager preferenceManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient,
             ConstantValues constantValues
    ) {
        return new DialogUtilsDefault(preferenceManager, dbProviderFactory, apiClient, constantValues, DownloadAllServiceDefault.class);
    }

    @Provides
    @Singleton
    SetTextViewHTML providesSetTextViewHTML( ConstantValues constantValues) {
        return new SetTextViewHTML(constantValues);
    }

    @Provides
    @Singleton
    MaterialsActivity.MaterialClickListener providesMaterialClickListener( ConstantValues constantValues) {
        return getMaterialClickListenerImpl(constantValues);
    }

    protected MaterialsActivity.MaterialClickListener getMaterialClickListenerImpl( ConstantValues constantValues) {
        return new MaterialClickListenerDefault(constantValues);
    }

    @Provides
    @Singleton
    InAppHelper providesInappHelper(
             MyPreferenceManager preferenceManager,
             DbProviderFactory dbProviderFactory,
             ApiClient apiClient
    ) {
        return new InAppHelper(preferenceManager, dbProviderFactory, apiClient);
    }
}