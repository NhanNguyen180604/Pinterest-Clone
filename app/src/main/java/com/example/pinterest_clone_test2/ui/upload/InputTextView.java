package com.example.pinterest_clone_test2.ui.upload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.pinterest_clone_test2.R;

public class InputTextView extends AppCompatTextView {
    private float dX, dY;
    private float lastTouchX, lastTouchY;
    private boolean isInActiveState = false;
    private OnTouchActionListener touchActionListener;

    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private int mode = MODE_NONE;

    public interface OnTouchActionListener {
        void onTouchAction(View view, float initialX, float initialY);
        void onSelected(InputTextView view);
    }

    public InputTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setTextColor(Color.BLACK);
        setTextSize(24);
        setPadding(20, 10, 20, 10);
        setBackgroundResource(android.R.color.transparent);
    }

    public void setTouchActionListener(OnTouchActionListener listener) {
        this.touchActionListener = listener;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getParent() == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();
                dX = getX() - lastTouchX;
                dY = getY() - lastTouchY;

                if (touchActionListener != null) {
                    touchActionListener.onSelected(this);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_DRAG) {
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;

                    if (getParent() instanceof FrameLayout) {
                        FrameLayout parent = (FrameLayout) getParent();
                        if (newX < 0) newX = 0;
                        if (newY < 0) newY = 0;
                        if (newX + getWidth() > parent.getWidth()) {
                            newX = parent.getWidth() - getWidth();
                        }
                        if (newY + getHeight() > parent.getHeight()) {
                            newY = parent.getHeight() - getHeight();
                        }
                    }

                    setX(newX);
                    setY(newY);
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (touchActionListener != null) {
                    touchActionListener.onTouchAction(this, lastTouchX, lastTouchY);
                }
                mode = MODE_NONE;
                return true;

            default:
                return false;
        }
    }

    public void setActive(boolean active) {
        isInActiveState = active;
        if (active) {
            setBackgroundResource(R.drawable.text_highlight_border);
        } else {
            setBackgroundResource(android.R.color.transparent);
        }
    }

    public boolean isActive() {
        return isInActiveState;
    }
}