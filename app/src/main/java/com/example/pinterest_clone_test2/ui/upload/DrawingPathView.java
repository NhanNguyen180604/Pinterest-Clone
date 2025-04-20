package com.example.pinterest_clone_test2.ui.upload;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingPathView extends View {

    private Path currentPath;
    private Paint paint;
    private List<DrawnPath> paths;
    private float currentX, currentY;
    private static final float TOUCH_TOLERANCE = 4;
    private boolean isDrawingEnabled = false;

    public DrawingPathView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paths = new ArrayList<>();
        currentPath = new Path();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(8);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (DrawnPath drawnPath : paths) {
            canvas.drawPath(drawnPath.path, drawnPath.paint);
        }

        if (isDrawingEnabled) {
            canvas.drawPath(currentPath, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawingEnabled) {
            return false;
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                return true;
            default:
                return false;
        }
    }

    private void touchStart(float x, float y) {
        currentPath.reset();
        currentPath.moveTo(x, y);
        currentX = x;
        currentY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - currentX);
        float dy = Math.abs(y - currentY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            currentPath.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2);
            currentX = x;
            currentY = y;
        }
    }

    private void touchUp() {
        currentPath.lineTo(currentX, currentY);
        DrawnPath drawnPath = new DrawnPath(new Path(currentPath), new Paint(paint));
        paths.add(drawnPath);
        currentPath.reset();
    }

    public DrawnPath getLastPath() {
        if (!paths.isEmpty()) {
            return paths.get(paths.size() - 1);
        }
        return null;
    }

    public void undoLastPath() {
        if (!paths.isEmpty()) {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    public void redoPath(DrawnPath path) {
        paths.add(path);
        invalidate();
    }

    public void setDrawingEnabled(boolean enabled) {
        this.isDrawingEnabled = enabled;
    }

    public boolean isDrawingEnabled() {
        return isDrawingEnabled;
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setStrokeWidth(float width) {
        paint.setStrokeWidth(width);
    }

    public static class DrawnPath {
        public Path path;
        public Paint paint;

        public DrawnPath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }
}