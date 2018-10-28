package ru.dante.scpfoundation.di.module;

import com.google.gson.Gson;

import dagger.Module;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import ru.dante.scpfoundation.ConstantValuesImpl;
import ru.dante.scpfoundation.api.ApiClientImpl;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.api.service.ScpReaderAuthApi;
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
            final Retrofit vpsRetrofit,
            final Retrofit scpRetrofit,
            final Retrofit scpReaderRetrofit,
            final ScpReaderAuthApi scpReaderAuthApi,
            final MyPreferenceManager preferencesManager,
            final Gson gson,
            final ConstantValues constantValues
    ) {
        return new ApiClientImpl(
                okHttpClient,
                vpsRetrofit,
                scpRetrofit,
                scpReaderRetrofit,
                scpReaderAuthApi,
                preferencesManager,
                gson,
                constantValues
        );
    }

    @Override
    protected ConstantValues getConstants() {
        return new ConstantValuesImpl();
    }
}