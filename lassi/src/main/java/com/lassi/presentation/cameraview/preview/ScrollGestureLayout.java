package com.lassi.presentation.cameraview.preview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.lassi.presentation.cameraview.audio.Gesture;
import com.lassi.presentation.cameraview.utils.CameraLogger;

public class ScrollGestureLayout extends GestureLayout {

    private static final String TAG = ScrollGestureLayout.class.getSimpleName();
    private static final CameraLogger LOG = CameraLogger.create(TAG);
    /* tests */ float mFactor;
    private GestureDetector mDetector;
    private boolean mNotify;

    public ScrollGestureLayout(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onInitialize(@NonNull Context context) {
        super.onInitialize(context);
        mPoints = new PointF[]{new PointF(0, 0), new PointF(0, 0)};
        mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                boolean horizontal;
                LOG.i("onScroll:", "distanceX=" + distanceX, "distanceY=" + distanceY);
                if (e1 == null || e2 == null) return false; // Got some crashes about this.
                if (e1.getX() != getPoints()[0].x || e1.getY() != getPoints()[0].y) {
                    // First step. We choose now if it's a vertical or horizontal scroll, and
                    // stick to it for the whole gesture.
                    horizontal = Math.abs(distanceX) >= Math.abs(distanceY);
                    setGestureType(horizontal ? Gesture.SCROLL_HORIZONTAL : Gesture.SCROLL_VERTICAL);
                    getPoints()[0].set(e1.getX(), e1.getY());
                } else {
                    // Not the first step. We already defined the type.
                    horizontal = getGestureType() == Gesture.SCROLL_HORIZONTAL;
                }
                getPoints()[1].set(e2.getX(), e2.getY());
                mFactor = horizontal ? (distanceX / getWidth()) : (distanceY / getHeight());
                mFactor = horizontal ? -mFactor : mFactor; // When vertical, up = positive
                mNotify = true;
                return true;
            }
        });

        mDetector.setIsLongpressEnabled(false); // Looks important.
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnabled) return false;

        // Reset the mNotify flag on a new gesture.
        // This is to ensure that the mNotify flag stays on until the
        // previous gesture ends.
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mNotify = false;
        }

        // Let's see if we detect something.
        mDetector.onTouchEvent(event);

        // Keep notifying CameraView as long as the gesture goes.
        if (mNotify) LOG.i("Notifying a gesture of type", getGestureType().name());
        return mNotify;
    }

    @Override
    public float scaleValue(float currValue, float minValue, float maxValue) {
        float delta = mFactor; // -1 ... 1

        // ^ This works well if minValue = 0, maxValue = 1.
        // Account for the different range:
        delta *= (maxValue - minValue); // -(max-min) ... (max-min)
        delta *= 2; // Add some sensitivity.

        return GestureLayout.capValue(currValue, currValue + delta, minValue, maxValue);
    }
}
