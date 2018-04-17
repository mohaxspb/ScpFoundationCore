package ru.dante.scpfoundation.di.module;

import com.google.gson.Gson;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import ru.dante.scpfoundation.ConstantValuesImpl;
import ru.dante.scpfoundation.api.ApiClientImpl;
import ru.dante.scpfoundation.api.service.ScpRuApi;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.di.module.NetModule;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

/**
 * Created by mohax on 13.07.2017.
 * <p>
 * for ScpFoundationRu
 */
@Module(includes = NetModule.class)
public class NetModuleImpl extends NetModule {

    @Override
    protected ApiClient getApiClient(
            final OkHttpClient okHttpClient,
            @Named("vps") final Retrofit vpsRetrofit,
            @Named("scp") final Retrofit scpRetrofit,
            final MyPreferenceManager preferencesManager,
            final Gson gson,
            final ConstantValues constantValues
    ) {
        return new ApiClientImpl(
                okHttpClient,
                vpsRetrofit,
                scpRetrofit,
                preferencesManager,
                gson,
                constantValues
        );
    }

    @Override
    protected ConstantValues getConstants() {
        return new ConstantValuesImpl();
    }


    @Provides
    @Named("scpRuApi")
    @Singleton
    Retrofit providesScpRuApiRetrofit(
            final OkHttpClient okHttpClient,
            final Converter.Factory converterFactory,
            final CallAdapter.Factory callAdapterFactory
    ) {
        return new Retrofit.Builder()
                .baseUrl(ScpRuApi.API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }
}