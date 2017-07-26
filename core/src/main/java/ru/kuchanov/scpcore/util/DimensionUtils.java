package ru.kuchanov.scpcore.util;

import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;


/**
 * Created by mohax on 08.12.2016.
 * <p>
 * for scp_ru
 */
public class DimensionUtils {

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getDefaultMargin() {
        return BaseApplication.getAppInstance().getResources().getDimensionPixelSize(R.dimen.defaultMargin);
    }

    public static int getDefaultMarginSmall() {
        return BaseApplication.getAppInstance().getResources().getDimensionPixelSize(R.dimen.defaultMarginSmall);
    }

    public static boolean isLandscapeMode() {
        return DimensionUtils.getScreenWidth() > DimensionUtils.getScreenHeight();
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = BaseApplication.getAppInstance().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getActionBarHeight(FragmentActivity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        } else {
            return 0;
        }
    }
}