package com.base.swipview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class SideSwapLayout extends RecyclerView {

    private Scroller mScroller;
    private ViewGroup currentTouchView;
    private ViewGroup lastTouchView;
    private float mLastX;
    private float mLastY;
    private int mMenuWidth;
    private boolean isExpanded = false;
    private int mScaledTouchSlop;

    public SideSwapLayout(Context context){
        super(context);
        init(context);
    }

    public SideSwapLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mScroller = new Scroller(context);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        float xPosition = event.getX();
        float yPosition = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                currentTouchView = (ViewGroup) findChildViewUnder(xPosition, yPosition);
                if (currentTouchView.getChildCount() > 1) {
                    View menuChild = currentTouchView.getChildAt(currentTouchView.getChildCount() - 1);
                    mMenuWidth = menuChild.getWidth();
                } else {
                    mMenuWidth = 0;
                }
                if (isExpanded) {
                    mScroller.startScroll(lastTouchView.getScrollX(),0,-lastTouchView.getScrollX(),0,400);
                    invalidate();
                    isExpanded = false;
                    return true;
                }
                break;
        }
        mLastX = xPosition;
        mLastY = yPosition;
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx = mLastX-x;
                float dy = mLastY-y;
                if(Math.abs(dx) >= Math.abs(dy) && Math.abs(dx) > mScaledTouchSlop){
                    float realScrollWidth = currentTouchView.getScrollX() + dx;
                    if (realScrollWidth <= 0){
                        currentTouchView.scrollTo(0,0);
                        isExpanded = false;
                        return true;
                    }
                    if (realScrollWidth >= mMenuWidth){
                        currentTouchView.scrollTo(mMenuWidth,0);
                        isExpanded = true;
                        return true;
                    }
                    currentTouchView.scrollBy((int)dx,0);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mScroller.isFinished()){
                    mScroller.abortAnimation();
                    lastTouchView.scrollTo(mScroller.getFinalX(),0);
                }
                float deltaX;
                int scrollX = currentTouchView.getScrollX();
                if(scrollX >= mMenuWidth/2){
                    deltaX = mMenuWidth - scrollX;
                    isExpanded = true;
                }else {
                    deltaX = -scrollX;
                    isExpanded = false;
                }
                lastTouchView = currentTouchView;
                mScroller.startScroll(scrollX,0, (int) deltaX,0);
                invalidate();
                break;
        }
        mLastX = x;
        mLastY = y;
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            lastTouchView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}
