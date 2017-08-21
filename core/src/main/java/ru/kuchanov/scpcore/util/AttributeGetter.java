package ru.kuchanov.scpcore.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

/**
 * Created by Юрий on 28.09.2015 0:54.
 * <p>
 * for scp_ru
 */
public class AttributeGetter {
    /**
     * @param addressInRClass R.color.someColor or R.attr.someReferenceToColor
     * @return not id of recourse, but Color itself. I think so)
     */
    public static int getColor(Context ctx, int addressInRClass) {
        int colorId;
        int[] attrs = new int[]{addressInRClass};
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        colorId = ta.getColor(0, Color.RED);
        ta.recycle();

        return colorId;
    }

//    public static int getAttrColor(Context context, int addressInRClass) {
//        TypedValue typedValue = new TypedValue();
//        Resources.Theme theme = context.getTheme();
//        theme.resolveAttribute(addressInRClass, typedValue, true);
//        Timber.d("typedValue.data: %s", typedValue.data);
//        return typedValue.data;
//    }

//    @ColorRes
//    public static int getColorResId(Context context, int addressInRClass) {
//        int colorId;
//        int[] attrs = new int[]{addressInRClass};
//        TypedArray ta = context.obtainStyledAttributes(attrs);
//        colorId = ta.getResourceId(0, R.color.colorPrimaryDark);
//        ta.recycle();
//
//        return colorId;
//    }

    public static int getDrawableId(Context ctx, int addressInRClass) {
        int drawableId;
        int[] attrs = new int[]{addressInRClass};
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        drawableId = ta.getResourceId(0, 0);
        ta.recycle();

        return drawableId;
    }
}