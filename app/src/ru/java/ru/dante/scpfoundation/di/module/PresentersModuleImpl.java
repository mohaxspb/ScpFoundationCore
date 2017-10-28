package ru.dante.scpfoundation.di.module;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import ru.dante.scpfoundation.ConstantValuesImpl;
import ru.dante.scpfoundation.api.ApiClientImpl;
import ru.dante.scpfoundation.mvp.contract.ObjectsFrArticles;
import ru.dante.scpfoundation.mvp.presenter.ObjectsFrArticlesPresenter;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.di.module.NetModule;
import ru.kuchanov.scpcore.di.module.PresentersModule;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.Objects1Articles;
import ru.kuchanov.scpcore.mvp.presenter.Objects1ArticlesPresenter;

/**
 * Created by mohax on 13.07.2017.
 * <p>
 * for ScpFoundationRu
 */
@Module(includes = PresentersModule.class)
public class PresentersModuleImpl extends PresentersModule {

    @Provides
    @Singleton
    @NonNull
    ObjectsFrArticles.Presenter providesObjectsFrArticlesPresenter(
            @NonNull MyPreferenceManager myPreferencesManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient,
            @NonNull ConstantValues constantValues
    ) {
        return new ObjectsFrArticlesPresenter(myPreferencesManager, dbProviderFactory, apiClient, constantValues);
    }
}