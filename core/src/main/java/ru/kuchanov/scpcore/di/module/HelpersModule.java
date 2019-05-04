package ru.kuchanov.scpcore.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.monetization.util.playmarket.PurchaseListenerImpl;
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
    DialogUtils providesDialogUtils(final ConstantValues constantValues) {
        return new DialogUtils(constantValues);
    }

    @Provides
    @Singleton
    ru.kuchanov.scpcore.downloads.DialogUtils providesDownloadAllDialogUtils(
            final MyPreferenceManager preferenceManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues
    ) {
        return getDownloadAllDialogUtils(preferenceManager, dbProviderFactory, apiClient, constantValues);
    }

    protected ru.kuchanov.scpcore.downloads.DialogUtils getDownloadAllDialogUtils(
            final MyPreferenceManager preferenceManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues
    ) {
        return new DialogUtilsDefault(preferenceManager, dbProviderFactory, apiClient, constantValues, DownloadAllServiceDefault.class);
    }

    @Provides
    @Singleton
    SetTextViewHTML providesSetTextViewHTML(final ConstantValues constantValues) {
        return new SetTextViewHTML(constantValues);
    }

    @Provides
    @Singleton
    MaterialsActivity.MaterialClickListener providesMaterialClickListener(final ConstantValues constantValues) {
        return getMaterialClickListenerImpl(constantValues);
    }

    protected MaterialsActivity.MaterialClickListener getMaterialClickListenerImpl(final ConstantValues constantValues) {
        return new MaterialClickListenerDefault(constantValues);
    }

    @Provides
    @Singleton
    InAppHelper providesInappHelper(
            final MyPreferenceManager preferenceManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient
    ) {
        return new InAppHelper(preferenceManager, dbProviderFactory, apiClient);
    }
}
