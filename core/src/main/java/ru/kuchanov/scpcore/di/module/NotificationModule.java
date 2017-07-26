package ru.kuchanov.scpcore.di.module;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.kuchanov.scpcore.manager.MyNotificationManager;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

/**
 * Created by y.kuchanov on 22.12.16.
 *
 * for scp_ru
 */
@Module
public class NotificationModule {

    @Provides
    @NonNull
    @Singleton
    MyNotificationManager providesNotificationManager(@NonNull Context context, @NonNull MyPreferenceManager preferenceManager) {
        return new MyNotificationManager(context, preferenceManager);
    }
}