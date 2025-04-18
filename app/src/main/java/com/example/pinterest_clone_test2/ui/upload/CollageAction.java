package com.example.pinterest_clone_test2.ui.upload;

import android.net.Uri;
import android.widget.ImageView;

public class CollageAction {
    private final CollageActionType type;
    private final ImageView imageView;
    private InputTextView textView;
    private final Uri imageUri;
    private final float oldX;
    private final float oldY;
    private final float oldScaleX;
    private final float oldScaleY;
    private final DrawingPathView.DrawnPath drawnPath;

    CollageAction(
            CollageActionType type,
            ImageView imageView,
            Uri imageUri,
            float oldX,
            float oldY,
            float oldScaleX,
            float oldScaleY) {
        this.type = type;
        this.imageView = imageView;
        this.imageUri = imageUri;
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldScaleX = oldScaleX;
        this.oldScaleY = oldScaleY;
        this.drawnPath = null;
        this.textView = null;
    }

    CollageAction(
            CollageActionType type,
            DrawingPathView.DrawnPath drawnPath) {
        this.type = type;
        this.imageView = null;
        this.imageUri = null;
        this.oldX = 0;
        this.oldY = 0;
        this.oldScaleX = 0;
        this.oldScaleY = 0;
        this.drawnPath = drawnPath;
        this.textView = null;
    }

    CollageAction(
            CollageActionType type,
            InputTextView textView,
            Uri imageUri,
            float oldX,
            float oldY,
            float oldScaleX,
            float oldScaleY) {
        this.type = type;
        this.textView = textView;
        this.imageView = null;
        this.imageUri = imageUri;
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldScaleX = oldScaleX;
        this.oldScaleY = oldScaleY;
        this.drawnPath = null;
    }

    CollageAction(
            CollageActionType type,
            InputTextView textView,
            float oldX,
            float oldY,
            float oldScaleX,
            float oldScaleY) {
        this.type = type;
        this.textView = textView;
        this.imageView = null;
        this.imageUri = null;
        this.oldX = oldX;
        this.oldY = oldY;
        this.oldScaleX = oldScaleX;
        this.oldScaleY = oldScaleY;
        this.drawnPath = null;
    }

    public CollageActionType getType() {
        return type;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public InputTextView getTextView() {
        return textView;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public float getOldX() {
        return oldX;
    }

    public float getOldY() {
        return oldY;
    }

    public float getOldScaleX() {
        return oldScaleX;
    }

    public float getOldScaleY() {
        return oldScaleY;
    }

    public DrawingPathView.DrawnPath getDrawnPath() {
        return drawnPath;
    }

    public void setTextView(InputTextView textView) {
        this.textView = textView;
    }
}