package com.example.pinterest_clone_test2.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.pinterest_clone_test2.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {

    private static final String CLOUD_NAME = BuildConfig.CLOUD_NAME;
    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String API_SECRET = BuildConfig.API_SECRET;
    private static final String UPLOAD_PRESET = BuildConfig.UPLOAD_PRESET;

    // Initialize Cloudinary
    public static void initCloudinary(Context context) {
        Log.d("Cloudinary", "Initializing Cloudinary...");
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME);
        config.put("api_key", API_KEY);
        config.put("api_secret", API_SECRET);

        if (!isMediaManagerInitialized()) {
            MediaManager.init(context, config);
            Log.d("Cloudinary", "Cloudinary initialized.");
        }
    }

    // Check if MediaManager is initialized
    private static boolean isMediaManagerInitialized() {
        try {
            MediaManager.get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Upload image to Cloudinary
    public static void uploadImage(@NonNull Uri imageUri, UploadCallback callback) {
        Log.d("Cloudinary", "Image URI to upload: " + imageUri);

        MediaManager.get().upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .callback(callback)
                .dispatch();
    }
}
