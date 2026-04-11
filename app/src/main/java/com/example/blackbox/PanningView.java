package com.example.blackbox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PanningView extends FrameLayout {

    private float lastX, lastY;
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

        // Clamp ONLY if larger than parent
        if (childWidth > parentWidth) {
            offsetX = Math.max(parentWidth - childWidth, Math.min(0, offsetX));
        }

        if (childHeight > parentHeight) {
            offsetY = Math.max(parentHeight - childHeight, Math.min(0, offsetY));
        }

        child.setTranslationX(offsetX);
        child.setTranslationY(offsetY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                isScrolling = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastX;
                float dy = event.getY() - lastY;

                if (!isScrolling) {
                    float totalDx = Math.abs(event.getX() - initialX);
                    float totalDy = Math.abs(event.getY() - initialY);

                    if (totalDx > touchSlop || totalDy > touchSlop) {
                        isScrolling = true;
                    }
                }

                if (isScrolling) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    offsetX += dx;
                    offsetY += dy;

                    updateChildTranslation();
                }

                lastX = event.getX();
                lastY = event.getY();
                return true;

            case MotionEvent.ACTION_UP:
                if (!isScrolling) {
                    performClick();
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                return true;
        }

        return super.onTouchEvent(event);
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

            if (!initialized && child.getMeasuredWidth() > 0) {
                offsetX = (getWidth() - child.getMeasuredWidth()) / 2f;
                offsetY = (getHeight() - child.getMeasuredHeight()) / 2f;
                initialized = true;
            }

            // ✅ ALWAYS apply translation
            child.setTranslationX(offsetX);
            child.setTranslationY(offsetY);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                initialX = ev.getX();
                initialY = ev.getY();
                isScrolling = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getX() - initialX);
                float dy = Math.abs(ev.getY() - initialY);

                if (dx > touchSlop || dy > touchSlop) {
                    isScrolling = true;

                    // Sync drag starting point
                    lastX = ev.getX();
                    lastY = ev.getY();

                    return true; // start intercepting
                }
                break;
        }
        return false;
    }

    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);

        if (initialized) {
            updateChildTranslation();
        }
    }
}