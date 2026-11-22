package com.example.ghostradar.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Jauge de bruit verticale dessinée en Canvas.
 *
 * <p>Le niveau et la couleur sont calculés par {@link NoiseGaugeFormatter} ;
 * cette vue ne fait que convertir les dp en pixels et dessiner les segments
 * de bas en haut, puis l'icône micro.</p>
 */
public final class NoiseGaugeView extends View {

    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int filledSegments;
    private double currentDbfs;
    private double thresholdDbfs;

    public NoiseGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        iconPaint.setColor(NoiseGaugeStyle.MIC_ICON_COLOR_ARGB);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(dpToPx(NoiseGaugeStyle.SEGMENT_GAP_DP));
        setWillNotDraw(false);
    }

    /** Met à jour la jauge depuis le `NoiseMonitor` du collecteur. */
    public void update(double level, double dbfs, double threshold) {
        filledSegments = NoiseGaugeFormatter.segmentCount(level);
        currentDbfs = dbfs;
        thresholdDbfs = threshold;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) dpToPx(NoiseGaugeStyle.SEGMENT_WIDTH_DP);
        float gaugeHeightDp = (NoiseGaugeStyle.SEGMENT_COUNT
                * NoiseGaugeStyle.SEGMENT_HEIGHT_DP)
                + ((NoiseGaugeStyle.SEGMENT_COUNT - 1) * NoiseGaugeStyle.SEGMENT_GAP_DP);
        int height = (int) dpToPx(gaugeHeightDp
                + NoiseGaugeStyle.ICON_GAP_DP
                + NoiseGaugeStyle.ICON_SIZE_DP);
        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float segmentWidth = dpToPx(NoiseGaugeStyle.SEGMENT_WIDTH_DP);
        float segmentHeight = dpToPx(NoiseGaugeStyle.SEGMENT_HEIGHT_DP);
        float gap = dpToPx(NoiseGaugeStyle.SEGMENT_GAP_DP);
        float gaugeHeight = (NoiseGaugeStyle.SEGMENT_COUNT * segmentHeight)
                + ((NoiseGaugeStyle.SEGMENT_COUNT - 1) * gap);
        float left = (getWidth() - segmentWidth) / 2f;

        for (int i = 0; i < NoiseGaugeStyle.SEGMENT_COUNT; i++) {
            float bottom = gaugeHeight - i * (segmentHeight + gap);
            float top = bottom - segmentHeight;
            segmentPaint.setColor(NoiseGaugeFormatter.colorForSegment(
                    i, filledSegments, currentDbfs, thresholdDbfs));
            canvas.drawRect(left, top, left + segmentWidth, bottom, segmentPaint);
        }

        drawMicrophone(canvas, gaugeHeight + dpToPx(NoiseGaugeStyle.ICON_GAP_DP));
    }

    private void drawMicrophone(Canvas canvas, float iconTop) {
        float iconSize = dpToPx(NoiseGaugeStyle.ICON_SIZE_DP);
        float centerX = getWidth() / 2f;
        float capsuleHalfWidth = iconSize / 5f;
        float capsuleBottom = iconTop + iconSize * 0.62f;
        float stemBottom = iconTop + iconSize * 0.84f;
        float baseHalfWidth = iconSize * 0.22f;

        canvas.drawRoundRect(
                centerX - capsuleHalfWidth, iconTop,
                centerX + capsuleHalfWidth, capsuleBottom,
                capsuleHalfWidth, capsuleHalfWidth, iconPaint);
        canvas.drawLine(centerX, capsuleBottom, centerX, stemBottom, iconPaint);
        canvas.drawLine(
                centerX - baseHalfWidth, stemBottom,
                centerX + baseHalfWidth, stemBottom, iconPaint);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
