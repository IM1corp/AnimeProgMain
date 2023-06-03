package com.imcorp.animeprog.Default;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class NotSwipeableViewPager extends androidx.viewpager.widget.ViewPager {
    private boolean swipeable = true;

    public NotSwipeableViewPager(Context context) {
        super(context);
    }

    public NotSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return this.swipeable && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return this.swipeable && super.onInterceptTouchEvent(arg0);
    }
}
