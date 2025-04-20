package com.example.pinterest_clone_test2.interfaces;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public interface ScaleListener {
    boolean onScale(ScaleGestureDetector detector, ImageView targetView, float minScale, float maxScale);
}