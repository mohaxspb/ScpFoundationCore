package ru.kuchanov.scpcore.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.joda.time.Period;

import java.util.concurrent.atomic.AtomicReference;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.receivers.ReceiverTimer;
import ru.kuchanov.scpcore.ui.activity.MainActivity;
import timber.log.Timber;

public class MyNotificationManager {

    private static final int ID = 999;

    private final MyPreferenceManager mMyPreferenceManager;

    private final Context mContext;

    public MyNotificationManager(final Context context, final MyPreferenceManager preferenceManager) {
        super();
        mMyPreferenceManager = preferenceManager;
        mContext = context;
    }

    private void setAlarm() {
        Timber.d("Setting alarm");
        cancelAlarm();
        final AlarmManager am = (AlarmManager) mContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        final Intent intentToTimerReceiver = new Intent(mContext.getApplicationContext(), ReceiverTimer.class);
        intentToTimerReceiver.setAction(mContext.getString(R.string.receiver_action_timer));

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(),
                ID,
                intentToTimerReceiver, PendingIntent.FLAG_CANCEL_CURRENT
        );

        final int periodInMinutes = mMyPreferenceManager.getNotificationPeriodInMinutes();
        Timber.d("setting alarm with period: %s", periodInMinutes);
//        long periodInMiliseconds = periodInMinutes * 60 * 1000;
        final long periodInMiliseconds = Period.minutes(periodInMinutes).toStandardDuration().getMillis();
//        //test
//        periodInMiliseconds = 1000 * 20;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + periodInMiliseconds, periodInMiliseconds, pendingIntent);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + periodInMiliseconds, periodInMiliseconds, pendingIntent);
        }
    }

    /**
     * checks if alarm is up and should be up and update its state if need
     */
    public void checkAlarm() {
        Timber.d("checkAlarm");
        final AtomicReference<Intent> intent2check = new AtomicReference<>(new Intent(mContext.getApplicationContext(), ReceiverTimer.class));
        intent2check.get().setAction(mContext.getString(R.string.receiver_action_timer));
        final boolean alarmUp = (PendingIntent.getBroadcast(mContext.getApplicationContext(), ID, intent2check.get(),
                PendingIntent.FLAG_NO_CREATE
        ) != null);
        final boolean isNotificationOn = mMyPreferenceManager.isNotificationEnabled();
        if (alarmUp) {
            Timber.d("Alarm is already active");
            if (!isNotificationOn) {
                Timber.d("But must not be, so...");
                cancelAlarm();
            } else {
                Timber.d("So do nothing");
            }
        } else {
            Timber.d("Alarm IS NOT active");
            if (isNotificationOn) {
                Timber.d("But must be, so...");
                setAlarm();
            } else {
                Timber.d("So do nothing");
            }
        }
    }

    private void cancelAlarm() {
        Timber.d("Canceling alarm");
        final AlarmManager am = (AlarmManager) mContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        final Intent intentToTimerReceiver = new Intent(mContext.getApplicationContext(), ReceiverTimer.class);
        intentToTimerReceiver.setAction(mContext.getString(R.string.receiver_action_timer));
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(),
                ID,
                intentToTimerReceiver,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public void showNotificationSimple(final String title, final String content, final int notificationId) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "free ads disable");
        final PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT), 0);
        builder.setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(content)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher);

        final NotificationManagerCompat manager = NotificationManagerCompat.from(mContext);
        manager.notify(notificationId, builder.build());
    }
}