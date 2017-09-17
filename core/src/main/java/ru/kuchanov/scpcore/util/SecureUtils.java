package ru.kuchanov.scpcore.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Created by mohax on 06.03.2017.
 * <p>
 * for Vjux
 */
public class SecureUtils {

    private static final String ONE = "ru.";
    private static final String TWO = "dan";
    private static final String THREE = "te.";
    private static final String FOUR = "scpfoundation";

    public static boolean checkCrack(Context context) {
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        Timber.d("user: %s, %s", user, user != null ? user.getUid() : null);
//        if (user != null) {
//            if (user.getUid().equals(BuildConfig.NON_CRACKED_USER_UID)) {
//                //use this class via dagger and inject preferences manager instead of this awful hack
//                MyPreferenceManager myPreferenceManager = new MyPreferenceManager(context, new Gson());
//                myPreferenceManager.setAppCracked(false);
//
                return false;
//            }
//        }
//        return SecureUtils.checkIfPackageChanged(context) || SecureUtils.checkLuckyPatcher(context);
    }

    private static boolean checkIfPackageChanged(Context context) {
        return !context.getPackageName().equals(ONE + TWO + THREE + FOUR);
    }

    private static boolean checkLuckyPatcher(Context context) {
        return packageExists(context, "com.dimonvideo.luckypatcher") ||
                packageExists(context, "com.android.protips") ||
                packageExists(context, "com.chelpus.lackypatch") ||
                packageExists(context, "com.android.vending.billing.InAppBillingService.LACK")
                //freedom
                || packageExists(context, "cc.madkite.freedom")
                || packageExists(context, "madkite.freedom");
    }

    private static boolean packageExists(Context context, final String packageName) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);

            if (info == null) {
                // No need really to test for null, if the package does not
                // exist it will really rise an exception. but in case Google
                // changes the API in the future lets be safe and test it
                return false;
            }

            return true;
        } catch (Exception ex) {
            // If we get here only means the Package does not exist
        }

        return false;
    }
}