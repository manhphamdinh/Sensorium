package com.example.blackbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class FluidView extends View {

    // Direction of gravity (normalized)
    private float gravityDirX = 0f;
    private float gravityDirY = 1f;

    private final Paint fluidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path fluidShape = new Path();

    private static final float FLUID_FILL_RATE = 0.6f;
    private static final float SEARCH_PRECISION = 0.5f;

    public FluidView(Context context, AttributeSet attrs) {
        super(context, attrs);
        fluidPaint.setColor(ContextCompat.getColor(getContext(), R.color.puzzle1translucent));
    }

    // 1. Update gravity direction
    public void setGravity(float rawGX, float rawGY) {
        float magnitude = (float) Math.sqrt(rawGX * rawGX + rawGY * rawGY);
        if (magnitude == 0) return;

        gravityDirX = rawGX / magnitude;
        gravityDirY = -(rawGY / magnitude); // flip for screen coords

        invalidate();
    }

    // 2. Find fluid surface level (how "full" it is along gravity direction)
    private float findSurfaceLevel(float width, float height) {
        float targetFluidArea = width * height * FLUID_FILL_RATE;

        float minLevel = -width - height;
        float maxLevel = width + height;

        while (maxLevel - minLevel > SEARCH_PRECISION) {
            float midLevel = (minLevel + maxLevel) * 0.5f;
            float currentArea = computeFluidArea(midLevel, width, height);

            if (currentArea > targetFluidArea) {
                maxLevel = midLevel;
            } else {
                minLevel = midLevel;
            }
        }

        return (minLevel + maxLevel) * 0.5f;
    }

    // 3. Calculate how much fluid area exists for a given surface level
    private float computeFluidArea(float surfaceLevel, float width, float height) {
        return calculatePolygonArea(
                getFluidPolygon(surfaceLevel, width, height)
        );
    }

    // 4. Clip container rectangle with fluid surface
    private List<float[]> getFluidPolygon(float surfaceLevel, float width, float height) {
        List<float[]> containerCorners = new ArrayList<>();
        containerCorners.add(new float[]{0, 0});
        containerCorners.add(new float[]{width, 0});
        containerCorners.add(new float[]{width, height});
        containerCorners.add(new float[]{0, height});

        List<float[]> fluidPoints = new ArrayList<>();

        for (int i = 0; i < containerCorners.size(); i++) {
            float[] start = containerCorners.get(i);
            float[] end = containerCorners.get((i + 1) % containerCorners.size());

            boolean startInside = isInsideFluid(start, surfaceLevel);
            boolean endInside = isInsideFluid(end, surfaceLevel);

            if (startInside && endInside) {
                fluidPoints.add(end);
            }
            else if (startInside) {
                fluidPoints.add(getLineIntersection(start, end, surfaceLevel));
            }
            else if (endInside) {
                fluidPoints.add(getLineIntersection(start, end, surfaceLevel));
                fluidPoints.add(end);
            }
        }

        return fluidPoints;
    }

    // Check if a point is below the fluid surface
    private boolean isInsideFluid(float[] point, float surfaceLevel) {
        return gravityDirX * point[0] + gravityDirY * point[1] <= surfaceLevel;
    }

    // Find intersection between edge and fluid surface
    private float[] getLineIntersection(float[] start, float[] end, float surfaceLevel) {
        float x1 = start[0], y1 = start[1];
        float x2 = end[0], y2 = end[1];

        float distStart = gravityDirX * x1 + gravityDirY * y1 - surfaceLevel;
        float distEnd = gravityDirX * x2 + gravityDirY * y2 - surfaceLevel;

        float t = distStart / (distStart - distEnd);

        return new float[]{
                x1 + t * (x2 - x1),
                y1 + t * (y2 - y1)
        };
    }

    // 5. Compute polygon area
    private float calculatePolygonArea(List<float[]> polygonPoints) {
        float area = 0f;

        for (int i = 0; i < polygonPoints.size(); i++) {
            float[] current = polygonPoints.get(i);
            float[] next = polygonPoints.get((i + 1) % polygonPoints.size());

            area += current[0] * next[1] - next[0] * current[1];
        }

        return Math.abs(area) * 0.5f;
    }

    // 6. Build drawable shape
    private void buildFluidShape(List<float[]> polygonPoints) {
        fluidShape.reset();

        if (polygonPoints.isEmpty()) return;

        fluidShape.moveTo(polygonPoints.get(0)[0], polygonPoints.get(0)[1]);

        for (int i = 1; i < polygonPoints.size(); i++) {
            fluidShape.lineTo(polygonPoints.get(i)[0], polygonPoints.get(i)[1]);
        }

        fluidShape.close();
    }

    // 7. Render
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float surfaceLevel = findSurfaceLevel(width, height);

        List<float[]> fluidPolygon = getFluidPolygon(surfaceLevel, width, height);
        buildFluidShape(fluidPolygon);

        canvas.drawPath(fluidShape, fluidPaint);
    }
}