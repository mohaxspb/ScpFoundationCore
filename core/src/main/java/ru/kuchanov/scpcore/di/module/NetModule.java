package ru.kuchanov.scpcore.di.module;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmList;
import io.realm.RealmObject;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.ConstantValuesDefault;
import ru.kuchanov.scpcore.api.ApiClient;
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

    @Provides
    @Named("logging")
    @Singleton
    Interceptor providesLoggingInterceptor() {
        return new HttpLoggingInterceptor(message -> Timber.d(message)).setLevel(
                BuildConfig.FLAVOR.equals("dev")
                ? HttpLoggingInterceptor.Level.BODY
                :
                HttpLoggingInterceptor.Level.NONE
        );
    }

    @Provides
    @Named("headers")
    @Singleton
    Interceptor providesHeadersInterceptor() {
        return chain -> {
            final Request original = chain.request();

            final Request request = original.newBuilder()
                    .header("Accept", "application/json")
                    .build();

            return chain.proceed(request);
        };
    }

    @Provides
    @Singleton
    CallAdapter.Factory providesCallAdapterFactory() {
        return RxJavaCallAdapterFactory.create();
    }

    @Provides
    @Singleton
    Converter.Factory providesConverterFactory(TypeAdapterFactory typeAdapterFactory) {
        return GsonConverterFactory.create(
                new GsonBuilder()
                        .setExclusionStrategies(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                return f.getDeclaringClass().equals(RealmObject.class);
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        })
                        .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
                        }.getType(), new TypeAdapter<RealmList<RealmString>>() {

                            @Override
                            public void write(JsonWriter out, RealmList<RealmString> value) throws IOException {
                                // Ignore
                            }

                            @Override
                            public RealmList<RealmString> read(JsonReader in) throws IOException {
                                RealmList<RealmString> list = new RealmList<>();
                                in.beginArray();
                                while (in.hasNext()) {
                                    list.add(new RealmString(in.nextString()));
                                }
                                in.endArray();
                                return list;
                            }
                        })
                        .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                        .registerTypeAdapterFactory(typeAdapterFactory)
                        .create()
        );
    }

    @Provides
    @Singleton
    OkHttpClient providesOkHttpClient(@Named("headers") Interceptor headersInterceptor,
                                      @Named("logging") Interceptor loggingInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(headersInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(BuildConfig.TIMEOUT_SECONDS_CONNECT, TimeUnit.SECONDS)
                .readTimeout(BuildConfig.TIMEOUT_SECONDS_READ, TimeUnit.SECONDS)
                .writeTimeout(BuildConfig.TIMEOUT_SECONDS_WRITE, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Named("vps")
    @Singleton
    Retrofit providesVpsRetrofit(
            OkHttpClient okHttpClient,
            Converter.Factory converterFactory,
            CallAdapter.Factory callAdapterFactory
    ) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.TOOLS_API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }

    @Provides
    @Named("scp")
    @Singleton
    Retrofit providesScpRetrofit(
            OkHttpClient okHttpClient,
            Converter.Factory converterFactory,
            CallAdapter.Factory callAdapterFactory
    ) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SCP_API_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }

    @Provides
    @Singleton
    ApiClient providerApiClient(
            OkHttpClient okHttpClient,
            @Named("vps") Retrofit vpsRetrofit,
            @Named("scp") Retrofit scpRetrofit,
            MyPreferenceManager preferencesManager,
            Gson gson,
            ConstantValues constantValues
    ) {
        return getApiClient(okHttpClient, vpsRetrofit, scpRetrofit, preferencesManager, gson, constantValues);
    }

    protected ApiClient getApiClient(
            OkHttpClient okHttpClient,
            @Named("vps") Retrofit vpsRetrofit,
            @Named("scp") Retrofit scpRetrofit,
            MyPreferenceManager preferencesManager,
            Gson gson,
            ConstantValues constantValues
    ) {
        return new ApiClient(okHttpClient, vpsRetrofit, scpRetrofit, preferencesManager, gson, constantValues);
    }

    @Provides
    @Singleton
    TypeAdapterFactory providesTypeAdapterFactory() {
        return new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
                final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        delegate.write(out, value);
                    }

                    @Override
                    public T read(JsonReader in) throws IOException {
                        JsonElement jsonElement = elementAdapter.read(in);
                        if (jsonElement.isJsonObject()) {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            if (jsonObject.has("result") && jsonObject.get("result").isJsonObject()) {
                                jsonElement = jsonObject.get("result");
                            }
                        }
                        return delegate.fromJsonTree(jsonElement);
                    }
                }.nullSafe();
            }
        };
    }

    @Provides
    @Singleton
    Gson providesGson() {
        return new GsonBuilder().create();
    }

    @Provides
    @Singleton
    ConstantValues providesConstants() {
        return getConstants();
    }

    protected ConstantValues getConstants() {
        return new ConstantValuesDefault();
    }
}