package ru.kuchanov.scpcore.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.receivers.ReceiverTimer;
import timber.log.Timber;

public class MyNotificationManager {

    private static final int ID = 999;

    private MyPreferenceManager mMyPreferenceManager;
    private Context mContext;

    public MyNotificationManager(Context context, MyPreferenceManager preferenceManager) {
        mMyPreferenceManager = preferenceManager;
        mContext = context;
    }

    public void setAlarm() {
        Timber.d("Setting alarm");
        cancelAlarm();
        AlarmManager am = (AlarmManager) mContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentToTimerReceiver = new Intent(mContext.getApplicationContext(), ReceiverTimer.class);
        intentToTimerReceiver.setAction(mContext.getString(R.string.receiver_action_timer));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(),
                ID,
                intentToTimerReceiver, PendingIntent.FLAG_CANCEL_CURRENT
        );

        int periodInMinutes = mMyPreferenceManager.getNotificationPeriodInMinutes();
        Timber.d("setting alarm with period: %s", periodInMinutes);
        long periodInMiliseconds = periodInMinutes * 60 * 1000;
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
        Intent intent2check = new Intent(mContext.getApplicationContext(), ReceiverTimer.class);
        intent2check.setAction(mContext.getString(R.string.receiver_action_timer));
        boolean alarmUp = (PendingIntent.getBroadcast(mContext.getApplicationContext(), ID, intent2check,
                PendingIntent.FLAG_NO_CREATE) != null);
        boolean isNotificationOn = mMyPreferenceManager.isNotificationEnabled();
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

    public void cancelAlarm() {
        Timber.d("Canceling alarm");
        final AlarmManager am = (AlarmManager) mContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentToTimerReceiver = new Intent(mContext.getApplicationContext(), ReceiverTimer.class);
        intentToTimerReceiver.setAction(mContext.getString(R.string.receiver_action_timer));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(),
                ID,
                intentToTimerReceiver,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}