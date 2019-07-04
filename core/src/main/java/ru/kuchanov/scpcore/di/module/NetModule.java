package ru.kuchanov.scpcore.di.module;

import android.text.TextUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmList;
import io.realm.RealmObject;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.ConstantValuesDefault;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.api.model.response.scpreaderapi.AccessTokenResponse;
import ru.kuchanov.scpcore.api.service.EnScpSiteApi;
import ru.kuchanov.scpcore.api.service.ScpReaderAuthApi;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 22.12.16.
 * <p>
 * for scp_ru
 */
@Module
public class NetModule {

    //qualifiers
    private static final String QUALIFIER_TOKEN_INTERCEPTOR = "QUALIFIER_TOKEN_INTERCEPTOR";

    private static final String QUALIFIER_LOGGING_INTERCEPTOR = "QUALIFIER_LOGGING_INTERCEPTOR";

    private static final String QUALIFIER_VPS_API = "QUALIFIER_VPS_API";

    private static final String QUALIFIER_SCP_READER_API = "QUALIFIER_SCP_READER_API";

    private static final String QUALIFIER_SCP_SITE_API = "QUALIFIER_SCP_SITE_API";

    //fixme delete it
    private static final String QUALIFIER_EN_SCP_SITE_API = "QUALIFIER_EN_SCP_SITE_API";

    private static final String QUALIFIER_OKHTTP_SCP_READER_API = "QUALIFIER_OKHTTP_SCP_READER_API";

    private static final String QUALIFIER_OKHTTP_COMMON = "QUALIFIER_OKHTTP_COMMON";

    private static final String QUALIFIER_UNAUTHORIZE_INTERCEPTOR = "QUALIFIER_UNAUTHORIZE_INTERCEPTOR";

    private static final String QUALIFIER_SCP_READER_API_AUTH = "QUALIFIER_SCP_READER_API_AUTH";

    private static final String QUALIFIER_CONVERTER_FACTORY_GSON = "QUALIFIER_CONVERTER_FACTORY_GSON";
    private static final String QUALIFIER_CONVERTER_FACTORY_XML = "QUALIFIER_CONVERTER_FACTORY_XML";
    //qualifiers END

    private static final String HEADER_AUTHORIZATION = "Authorization";

    private static final String HEADER_VALUE_PART_BEARER = "Bearer";

    @SuppressWarnings("ConstantConditions")
    @Provides
    @Named(QUALIFIER_LOGGING_INTERCEPTOR)
    @Singleton
    Interceptor providesLoggingInterceptor() {
        return new HttpLoggingInterceptor(message -> Timber.d(message)).setLevel(
                BuildConfig.FLAVOR_mode.equals("dev")
                        ?
                        HttpLoggingInterceptor.Level.BODY
                        :
                        HttpLoggingInterceptor.Level.NONE
        );
    }

    @Provides
    @Named(QUALIFIER_TOKEN_INTERCEPTOR)
    @Singleton
    Interceptor providesTokenInterceptor(@NotNull final MyPreferenceManager myPreferenceManager) {
        return chain -> {
            final Request original = chain.request();

            final Request request;
            if (TextUtils.isEmpty(myPreferenceManager.getAccessToken())) {
                request = original;
            } else {
                request = original.newBuilder()
                        .header(HEADER_AUTHORIZATION, HEADER_VALUE_PART_BEARER + myPreferenceManager.getAccessToken())
                        .build();
            }

            return chain.proceed(request);
        };
    }

    @Provides
    @Named(QUALIFIER_UNAUTHORIZE_INTERCEPTOR)
    @Singleton
    Interceptor providesUnauthorizeInterceptor(
            @NotNull final ScpReaderAuthApi scpReaderAuthApi,
            @NotNull final MyPreferenceManager myPreferenceManager
    ) {
        return chain -> {
            Request request = chain.request();
            Response response = chain.proceed(request);
            if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                final AccessTokenResponse tokenResponse = scpReaderAuthApi
                        .getAccessTokenByRefreshToken(
                                Credentials.basic(BuildConfig.SCP_READER_API_CLIENT_ID, BuildConfig.SCP_READER_API_CLIENT_SECRET),
                                Constants.Api.ScpReader.GRANT_TYPE_REFRESH_TOKEN,
                                myPreferenceManager.getRefreshToken()
                        )
                        .toBlocking()
                        .value();
                myPreferenceManager.setAccessToken(tokenResponse.getAccessToken());
                myPreferenceManager.setRefreshToken(tokenResponse.getRefreshToken());
                request = request.newBuilder()
                        .header(HEADER_AUTHORIZATION, HEADER_VALUE_PART_BEARER + tokenResponse.getAccessToken())
                        .method(request.method(), request.body()).url(request.url()).build();
                response = chain.proceed(request);
            }
            return response;
        };
    }

    @Provides
    @Named(QUALIFIER_OKHTTP_COMMON)
    @Singleton
    OkHttpClient providesCommonOkHttpClient(
            @Named(QUALIFIER_LOGGING_INTERCEPTOR) final Interceptor loggingInterceptor
    ) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(BuildConfig.TIMEOUT_SECONDS_CONNECT, TimeUnit.SECONDS)
                .readTimeout(BuildConfig.TIMEOUT_SECONDS_READ, TimeUnit.SECONDS)
                .writeTimeout(BuildConfig.TIMEOUT_SECONDS_WRITE, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Named(QUALIFIER_OKHTTP_SCP_READER_API)
    @Singleton
    OkHttpClient providesScpReaderApiOkHttpClient(
            @Named(QUALIFIER_TOKEN_INTERCEPTOR) final Interceptor tokenInterceptor,
            @Named(QUALIFIER_LOGGING_INTERCEPTOR) final Interceptor loggingInterceptor,
            @Named(QUALIFIER_UNAUTHORIZE_INTERCEPTOR) final Interceptor unauthInterceptor
    ) {
        return new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(unauthInterceptor)
                .connectTimeout(BuildConfig.TIMEOUT_SECONDS_CONNECT, TimeUnit.SECONDS)
                .readTimeout(BuildConfig.TIMEOUT_SECONDS_READ, TimeUnit.SECONDS)
                .writeTimeout(BuildConfig.TIMEOUT_SECONDS_WRITE, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    CallAdapter.Factory providesCallAdapterFactory() {
        return RxJavaCallAdapterFactory.create();
    }

    @Provides
    @Named(QUALIFIER_CONVERTER_FACTORY_GSON)
    @Singleton
    Converter.Factory providesConverterFactory() {
        return GsonConverterFactory.create(
                new GsonBuilder()
                        .setExclusionStrategies(new MyExclusionStrategy())
                        .registerTypeAdapter(new RealmListTypeToken().getType(), new RealmListTypeAdapter())
                        .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                        .setLenient()
                        .create()
        );
    }

    @Provides
    @Named(QUALIFIER_SCP_READER_API_AUTH)
    @Singleton
    Retrofit providesScpReaderApiAuthRetrofit(
            @Named(QUALIFIER_OKHTTP_COMMON) final OkHttpClient okHttpClient,
            @Named(QUALIFIER_CONVERTER_FACTORY_GSON) final Converter.Factory converterFactory,
            final CallAdapter.Factory callAdapterFactory
    ) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SCP_READER_API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }

    @Provides
    @Singleton
    ScpReaderAuthApi providesScpReaderAuthApi(
            @Named(QUALIFIER_SCP_READER_API_AUTH) final Retrofit retrofit
    ) {
        return retrofit.create(ScpReaderAuthApi.class);
    }

    @Provides
    @Named(QUALIFIER_SCP_READER_API)
    @Singleton
    Retrofit providesScpReaderApiRetrofit(
            @Named(QUALIFIER_OKHTTP_SCP_READER_API) final OkHttpClient okHttpClient,
            @Named(QUALIFIER_CONVERTER_FACTORY_GSON) final Converter.Factory converterFactory,
            final CallAdapter.Factory callAdapterFactory
    ) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SCP_READER_API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }

    @Provides
    @Named(QUALIFIER_VPS_API)
    @Singleton
    Retrofit providesVpsRetrofit(
            @Named(QUALIFIER_OKHTTP_COMMON) final OkHttpClient okHttpClient,
            @Named(QUALIFIER_CONVERTER_FACTORY_GSON) final Converter.Factory converterFactory,
            final CallAdapter.Factory callAdapterFactory
    ) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.TOOLS_API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }

    @Provides
    @Named(QUALIFIER_SCP_SITE_API)
    @Singleton
    Retrofit providesScpRetrofit(
            @Named(QUALIFIER_OKHTTP_COMMON) final OkHttpClient okHttpClient,
            @Named(QUALIFIER_CONVERTER_FACTORY_GSON) final Converter.Factory converterFactory,
            final CallAdapter.Factory callAdapterFactory
    ) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SCP_API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }

    //fixme delete it
    @Provides
    @Named(QUALIFIER_EN_SCP_SITE_API)
    @Singleton
    Retrofit providesEnScpRetrofit(
            @Named(QUALIFIER_OKHTTP_COMMON) final OkHttpClient okHttpClient,
            final CallAdapter.Factory callAdapterFactory
    ) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.EN_SCP_API_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }

    //fixme delete it
    @Provides
    @Singleton
    EnScpSiteApi providesEnScpSiteApi(
            @Named(QUALIFIER_EN_SCP_SITE_API) final Retrofit retrofit
    ) {
        return retrofit.create(EnScpSiteApi.class);
    }

    @Provides
    @Singleton
    ApiClient providerApiClient(
            @Named(QUALIFIER_OKHTTP_COMMON) final OkHttpClient okHttpClient,
            @Named(QUALIFIER_VPS_API) final Retrofit vpsRetrofit,
            @Named(QUALIFIER_SCP_SITE_API) final Retrofit scpRetrofit,
            @Named(QUALIFIER_SCP_READER_API) final Retrofit scpReaderRetrofit,
            final ScpReaderAuthApi scpReaderAuthApi,
            final EnScpSiteApi enScpSiteApi,
            final MyPreferenceManager preferencesManager,
            final Gson gson,
            final ConstantValues constantValues
    ) {
        return getApiClient(
                okHttpClient,
                vpsRetrofit,
                scpRetrofit,
                scpReaderRetrofit,
                scpReaderAuthApi,
                enScpSiteApi,
                preferencesManager,
                gson,
                constantValues
        );
    }

    protected ApiClient getApiClient(
            final OkHttpClient okHttpClient,
            final Retrofit vpsRetrofit,
            final Retrofit scpRetrofit,
            final Retrofit scpReaderRetrofit,
            final ScpReaderAuthApi scpReaderAuthApi,
            final EnScpSiteApi enScpSiteApi,
            final MyPreferenceManager preferencesManager,
            final Gson gson,
            final ConstantValues constantValues
    ) {
        return new ApiClient(
                okHttpClient,
                vpsRetrofit,
                scpRetrofit,
                scpReaderRetrofit,
                scpReaderAuthApi,
                enScpSiteApi,
                preferencesManager,
                gson,
                constantValues
        );
    }

    @Provides
    @Singleton
    Gson providesGson() {
        return new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
    }

    @Provides
    @Singleton
    ConstantValues providesConstants() {
        return getConstants();
    }

    protected ConstantValues getConstants() {
        return new ConstantValuesDefault();
    }

    private static class MyExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(final FieldAttributes f) {
            return f.getDeclaringClass().equals(RealmObject.class);
        }

        @Override
        public boolean shouldSkipClass(final Class<?> clazz) {
            return false;
        }
    }

    private static class RealmListTypeToken extends TypeToken<RealmList<RealmString>> {
    }

    private static class RealmListTypeAdapter extends TypeAdapter<RealmList<RealmString>> {

        @Override
        public void write(final JsonWriter out, final RealmList<RealmString> value) {
            // Ignore
        }

        @Override
        public RealmList<RealmString> read(final JsonReader in) throws IOException {
            in.beginArray();
            final RealmList<RealmString> list = new RealmList<>();
            while (in.hasNext()) {
                list.add(new RealmString(in.nextString()));
            }
            in.endArray();
            return list;
        }
    }
}