package com.example.blackbox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PanningView extends FrameLayout {

    private GestureDetector gestureDetector;

    private float offsetX = 0;
    private float offsetY = 0;

    private int touchSlop;
    private float initialX, initialY;
    private boolean isScrolling = false;
    private boolean initialized = false;

    public PanningView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PanningView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PanningView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setClickable(true);
        setFocusable(true);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                isScrolling = true;

                offsetX -= distanceX;
                offsetY -= distanceY;

                updateChildTranslation();
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            measureChild(child,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        child.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        child.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
    }

    private void updateChildTranslation() {
        if (getChildCount() == 0) return;

        View child = getChildAt(0);

        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        int parentWidth = getWidth();
        int parentHeight = getHeight();

        if (parentWidth == 0 || parentHeight == 0) return;

        // Horizontal bounds
        if (childWidth > parentWidth) {
            offsetX = Math.max(parentWidth - childWidth, Math.min(0, offsetX));
        } else {
            offsetX = (parentWidth - childWidth) / 2f;
        }

        // Vertical bounds
        if (childHeight > parentHeight) {
            offsetY = Math.max(parentHeight - childHeight, Math.min(0, offsetY));
        } else {
            offsetY = (parentHeight - childHeight) / 2f;
        }

        child.setTranslationX(offsetX);
        child.setTranslationY(offsetY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getX();
                initialY = event.getY();
                isScrolling = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(event.getX() - initialX);
                float dy = Math.abs(event.getY() - initialY);

                if (dx > touchSlop || dy > touchSlop) {
                    isScrolling = true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (action == MotionEvent.ACTION_UP && !isScrolling) {
                    performClick(); // keep click behavior
                }
                isScrolling = false;
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() > 0) {
            View child = getChildAt(0);

            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

            // Initial centering
            if (!initialized && child.getMeasuredWidth() > 0) {
                offsetX = (getWidth() - child.getMeasuredWidth()) / 2f;
                offsetY = (getHeight() - child.getMeasuredHeight()) / 2f;

                child.setTranslationX(offsetX);
                child.setTranslationY(offsetY);

                initialized = true;
            }
        }
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);

        if (initialized) {
            updateChildTranslation();
        }
    }
}