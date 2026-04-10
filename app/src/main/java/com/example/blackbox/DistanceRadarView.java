package com.example.blackbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DistanceRadarView extends View {

    private Paint paintBlue;
    private Paint paintWhite;
    private Paint paintSquare;
    private float progress = 0f;

    private boolean[] solvedStages = new boolean[4];

    public DistanceRadarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paintBlue = new Paint();
        paintBlue.setColor(Color.parseColor("#037dff"));
        paintBlue.setStyle(Paint.Style.STROKE);
        paintBlue.setStrokeWidth(5f);
        paintBlue.setAntiAlias(true);

        paintSquare = new Paint();
        paintSquare.setAntiAlias(true);

        paintWhite = new Paint();
        paintWhite.setColor(Color.WHITE);
        paintWhite.setStyle(Paint.Style.STROKE);
        paintWhite.setStrokeWidth(8f);
        paintWhite.setAntiAlias(true);
    }

    public void setDistance(double distanceInMeters) {
        if (distanceInMeters <= 1) {
            progress = 0;
        } else {
            progress = (float) Math.log10(distanceInMeters);
            if (progress > 4f) progress = 4f;
        }
        invalidate();
    }

    public void setSolved(int index) {
        if (index >= 0 && index < 4) {
            solvedStages[index] = true;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() * 0.8f;
        float maxRadius = centerY - 150;
        float step = maxRadius / 4f;

        for (int i = 1; i <= 4; i++) {
            paintBlue.setAlpha(255 - (i * 40));
            float currentRadius = step * i;

            canvas.drawCircle(centerX, centerY, currentRadius, paintBlue);

            float boxTopY = centerY - currentRadius;

            if (solvedStages[i - 1]) {
                paintSquare.setStyle(Paint.Style.FILL);
                paintSquare.setColor(Color.parseColor("#037dff"));
            } else {
                paintSquare.setStyle(Paint.Style.STROKE);
                paintSquare.setStrokeWidth(8f);
                paintSquare.setColor(Color.parseColor("#037dff"));
            }

            canvas.drawRoundRect(centerX - 40, boxTopY - 40, centerX + 40, boxTopY + 40, 15, 15, paintSquare);


            if (!solvedStages[i - 1]) {
                canvas.drawCircle(centerX, boxTopY, 6, paintBlue);
            }
        }


        float whiteRadius = (progress / 4f) * maxRadius;
        if (whiteRadius > 0) {
            canvas.drawCircle(centerX, centerY, whiteRadius, paintWhite);
        }
    }
}