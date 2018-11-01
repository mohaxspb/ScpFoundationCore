package ru.kuchanov.scpcore.di.module;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by y.kuchanov on 21.12.16.
 *
 * for scp_ru
 */
@Module
public class AppModule {

    private final Application mApplication;

    public AppModule(final Application application) {
        super();
        mApplication = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    Context providesContext() {
        return mApplication;
    }
}