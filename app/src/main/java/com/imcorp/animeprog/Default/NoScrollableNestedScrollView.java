package com.imcorp.animeprog.Default;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class NoScrollableNestedScrollView extends NestedScrollView {
    public NoScrollableNestedScrollView(@NonNull Context context) {
        super(context);
    }
    public NoScrollableNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public NoScrollableNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean mScrollable=true;

    public void setScrollable(final boolean mScrollable){
        this.mScrollable=mScrollable;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mScrollable) return super.onTouchEvent(ev);
            return false;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mScrollable) return super.onInterceptTouchEvent(ev);
        else return false;
    }
    public void scrollToTop(){
        final boolean isScrollable = this.mScrollable;
        this.post(() -> {
            mScrollable=true;
            fling(0);
            smoothScrollTo(0, 0);
            mScrollable=isScrollable;
        });
    }
}
