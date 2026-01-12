package com.gym.fitconnectpro.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.gym.fitconnectpro.models.WeightLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleLineChart extends View {

    private Paint linePaint;
    private Paint pointPaint;
    private Paint textPaint;
    private Paint gridPaint;
    
    private List<WeightLog> dataPoints = new ArrayList<>();
    private float minWeight = 0;
    private float maxWeight = 0;

    public SimpleLineChart(Context context) {
        super(context);
        init();
    }

    public SimpleLineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#3498DB")); // Blue
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        pointPaint = new Paint();
        pointPaint.setColor(Color.parseColor("#E74C3C")); // Red points
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);
        
        textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(30f);
        textPaint.setAntiAlias(true);
        
        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(2f);
    }

    public void setData(List<WeightLog> data) {
        this.dataPoints = data;
        if (data != null && !data.isEmpty()) {
            calculateRange();
        }
        invalidate(); // Redraw
    }

    private void calculateRange() {
        minWeight = Float.MAX_VALUE;
        maxWeight = Float.MIN_VALUE;

        for (WeightLog log : dataPoints) {
            float w = (float) log.getWeight();
            if (w < minWeight) minWeight = w;
            if (w > maxWeight) maxWeight = w;
        }
        
        // Add padding
        minWeight -= 2;
        maxWeight += 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataPoints == null || dataPoints.isEmpty()) {
            canvas.drawText("No Data Available", getWidth() / 2f - 100, getHeight() / 2f, textPaint);
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float padding = 80f; // Left padding for y-axis labels
        float usableWidth = width - (padding * 2);
        float usableHeight = height - (padding * 2);

        // Draw Axes
        canvas.drawLine(padding, height - padding, width - padding, height - padding, gridPaint); // X-axis
        canvas.drawLine(padding, padding, padding, height - padding, gridPaint); // Y-axis

        // Draw Y-Axis Labels (Min, Mid, Max)
        canvas.drawText(String.format("%.1f", maxWeight), 10, padding + 10, textPaint);
        canvas.drawText(String.format("%.1f", minWeight), 10, height - padding, textPaint);

        if (dataPoints.size() < 2) {
            // Just draw one point
             float x = padding + (usableWidth / 2);
             float y = height - padding - (( (float)dataPoints.get(0).getWeight() - minWeight) / (maxWeight - minWeight) * usableHeight);
             canvas.drawCircle(x, y, 10f, pointPaint);
             return;
        }

        Path path = new Path();
        float stepX = usableWidth / (dataPoints.size() - 1);
        
        List<Float> xCoords = new ArrayList<>();
        List<Float> yCoords = new ArrayList<>();

        for (int i = 0; i < dataPoints.size(); i++) {
            float weight = (float) dataPoints.get(i).getWeight();
            float x = padding + (i * stepX);
            float y = height - padding - ((weight - minWeight) / (maxWeight - minWeight) * usableHeight);
            
            xCoords.add(x);
            yCoords.add(y);
            
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        canvas.drawPath(path, linePaint);

        // Draw points and labels
        for (int i = 0; i < xCoords.size(); i++) {
            canvas.drawCircle(xCoords.get(i), yCoords.get(i), 8f, pointPaint);
            // Draw date label for first and last? Or just date indices.
            // Let's simple draw date substring for each point if enough space, or just first/last
        }
    }
}
