package ru.dante.scpfoundation.api;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.api.service.EnScpSiteApi;
import ru.kuchanov.scpcore.api.service.ScpReaderAuthApi;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import rx.Observable;
import rx.Single;
import timber.log.Timber;

/**
 * Created by mohax on 13.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ApiClientImpl extends ApiClient {

    public ApiClientImpl(
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
        super(
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

    @Override
    public Observable<String> getRandomUrl() {
        return Observable.unsafeCreate(subscriber -> {
                    final Request request = new Request.Builder()
                            .url("https://scpdb.org/api/wikidot_random_page")
                            .build();

                    try {
                        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                .followRedirects(false)
                                .build();
                        final Response response = okHttpClient.newCall(request).execute();
                        final ResponseBody body = response.body();
                        if (body != null) {
                            final String responseBody = body.string();
                            //{"name":"scp-2320"}
                            final String scp = new JsonParser().parse(responseBody).getAsJsonObject().get("name").getAsString();
                            subscriber.onNext(mConstantValues.getBaseApiUrl() + "/" + scp);
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(ru.kuchanov.scpcore.R.string.error_parse)));
                        }
                    } catch (final IOException e) {
                        subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(ru.kuchanov.scpcore.R.string.error_connection)));
                    }
                }
        );
    }

    @Override
    public Single<Integer> getRecentArticlesPageCountObservable() {
        return Single.create(subscriber -> {
            final Request request = new Request.Builder()
                    .url(mConstantValues.getNewArticles() + "/p/1")
                    .build();

            final String responseBody;
            try {
                final Response response = mOkHttpClient.newCall(request).execute();
                final AtomicReference<ResponseBody> body = new AtomicReference<>(response.body());
                if (body.get() != null) {
                    responseBody = body.get().string();
                } else {
                    subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(ru.kuchanov.scpcore.R.string.error_parse)));
                    return;
                }
            } catch (final IOException e) {
                subscriber.onError(new IOException(BaseApplication.getAppInstance().getString(ru.kuchanov.scpcore.R.string.error_connection)));
                return;
            }
            try {
                final Document doc = Jsoup.parse(responseBody);

                //get num of pages
                final Element spanWithNumber = doc.getElementsByClass("pager-no").first();
                final String text = spanWithNumber.text();
                final Integer numOfPages = Integer.valueOf(text.substring(text.lastIndexOf(" ") + 1));

                subscriber.onSuccess(numOfPages);
            } catch (final Exception e) {
                Timber.e(e, "error while get arts list");
                subscriber.onError(e);
            }
        });
    }

    @Override
    protected String getScpServerWiki() {
        return "scp-ru";
    }
}