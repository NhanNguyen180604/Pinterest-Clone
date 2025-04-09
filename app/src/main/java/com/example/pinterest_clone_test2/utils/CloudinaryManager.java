package com.example.pinterest_clone_test2.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

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
    public static void uploadMedia(Uri mediaUri, String mimeType, UploadCallback callback) {
        if (mediaUri != null) {
            Log.d("Cloudinary", "Media URI to upload: " + mediaUri);

            if (mimeType != null) {
                Log.d("Cloudinary", "MIME type: " + mimeType);

                // Handle image or gif upload
                if (mimeType.equals("image/gif")) {
                    MediaManager.get().upload(mediaUri)
                            .unsigned(UPLOAD_PRESET)
                            .callback(callback)
                            .dispatch();
                }
                // Handle video upload
                else if (mimeType.startsWith("video")) {
                    MediaManager.get().upload(mediaUri)
                            .unsigned(UPLOAD_PRESET)
                            .option("resource_type", "video")  // Set resource type to "video"
                            .callback(callback)
                            .dispatch();
                }
                // Handle image upload
                else if (mimeType.startsWith("image")) {
                    MediaManager.get().upload(mediaUri)
                            .unsigned(UPLOAD_PRESET)
                            .callback(callback)
                            .dispatch();
                }
            } else {
                Log.d("Cloudinary", "No MIME type detected for URI: " + mediaUri);
            }
        } else {
            Log.d("Cloudinary", "No media selected");
//            Toast.makeText(context, "No media selected", Toast.LENGTH_SHORT).show();
        }
    }
}
