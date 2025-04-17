package com.example.pinterest_clone_test2.ui.upload;

import android.net.Uri;
import android.widget.ImageView;

public class CollageAction {
    private final CollageActionType type;
    private final ImageView imageView;
    private final Uri imageUri;
    private final float oldX;
    private final float oldY;
    private final float oldScaleX;
    private final float oldScaleY;

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
    }

    public CollageActionType getType() {
        return type;
    }

    public ImageView getImageView() {
        return imageView;
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
}