package ru.kuchanov.scpcore.ui.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * Created by mohax on 08.05.2017.
 * <p>
 * for scp_ru
 * <p>
 *
 * @see <a href="https://github.com/chrisbanes/PhotoView/issues/31#issuecomment-19803926">en-SO</a>
 */
public class ViewPagerFixed extends android.support.v4.view.ViewPager {

    public ViewPagerFixed(Context context) {
        super(context);
    }

    public ViewPagerFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}