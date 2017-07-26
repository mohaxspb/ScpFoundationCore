package ru.kuchanov.scpcore.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.util.MigrationUtils;
import ru.kuchanov.scpcore.util.StorageUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static ru.kuchanov.scpcore.ui.activity.LicenceActivity.EXTRA_SHOW_ABOUT;

/**
 * Created by Ivan Semkin on 4/23/2017.
 * <p>
 * for ScpFoundationRu
 */
public class SplashActivity extends AppCompatActivity {

    @Inject
    MyPreferenceManager mMyPreferenceManager;
    @Inject
    DbProviderFactory mDbProviderFactory;

    MaterialDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaseApplication.getAppComponent().inject(this);

        checkAndRestoreDataIfNeed();
    }

    private void startNextActivity() {
        Intent intent;
        if (StorageUtils.fileExistsInAssets("licence.txt") && !mMyPreferenceManager.isLicenceAccepted()) {
            intent = new Intent(this, LicenceActivity.class);
        } else {
            if (!mMyPreferenceManager.isLicenceAccepted()) {
                intent = new Intent(this, MainActivity.class).putExtra(EXTRA_SHOW_ABOUT, true);
                mMyPreferenceManager.setLicenceAccepted(true);
            } else {
                intent = new Intent(this, MainActivity.class);
            }
        }
        startActivity(intent);
        finishAffinity();
    }

    private void checkAndRestoreDataIfNeed() {
        if (!mMyPreferenceManager.isDataRestored()) {
            if (!MigrationUtils.hasDataToRestore(this)) {
                mMyPreferenceManager.setDataIsRestored(true);
                startNextActivity();
                return;
            }

            mProgressDialog = new MaterialDialog.Builder(this)
                    .content(R.string.restoring_data)
                    .progress(true, 100)
                    .build();
            mProgressDialog.show();

            restoreDataObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(readAndFavsCount -> {
                        mProgressDialog.setContent(getString(R.string.restored_data, readAndFavsCount.first, readAndFavsCount.second));
                        return Observable.just(readAndFavsCount).delay(5, TimeUnit.SECONDS);
                    })
                    .subscribe(
                            readAndFavsCount -> {
                                Timber.d("restored read: %s, favs: %s", readAndFavsCount.first, readAndFavsCount.second);

                                mMyPreferenceManager.setDataIsRestored(true);

                                startNextActivity();
                            },
                            e -> {
                                Timber.e(e);

                                mMyPreferenceManager.setDataIsRestored(true);

                                startNextActivity();
                            }
                    );
        } else {
            startNextActivity();
        }
    }

    private Observable<Pair<Integer, Integer>> restoreDataObservable() {
        return Observable.<Pair<Pair<Integer, Integer>, List<Article>>>unsafeCreate(subscriber -> {
            List<Article> articles = new ArrayList<>();

            List<String> readUrls = new ArrayList<>(MigrationUtils.getAllReadUrls(this));
            Timber.d("readUrls: %s", readUrls);
            for (String url : readUrls) {
                Article article = new Article();
                article.url = url;
                article.isInReaden = true;
                articles.add(article);
            }

            Map<String, String> favorites = MigrationUtils.getAllFavorites(this);
            Timber.d("favorites: %s", favorites);
            int counter = 0;
            for (String key : favorites.keySet()) {
                String title = favorites.get(key);
                Article article = new Article();
                article.url = key;
                if (articles.contains(article)) {
                    Article readArticle = articles.get(articles.indexOf(article));
                    readArticle.isInFavorite = counter;
                    readArticle.title = title;
                } else {
                    article.isInFavorite = counter;
                    article.title = title;
                    articles.add(article);
                }
                counter++;
            }

            subscriber.onNext(new Pair<>(new Pair<>(readUrls.size(), favorites.size()), articles));
            subscriber.onCompleted();
        })
                .flatMap(articles -> mDbProviderFactory.getDbProvider()
                        .insertRestoredArticlesSync(articles.second)
                        .map(articles1 -> articles.first)
                );
    }
}