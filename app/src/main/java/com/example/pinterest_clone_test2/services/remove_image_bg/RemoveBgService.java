package com.example.pinterest_clone_test2.services.remove_image_bg;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.BuildConfig;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class RemoveBgService {
    private static String processedImageB64;

    public static String getProcessedImageB64() {
        return processedImageB64;
    }

    public static void clearProcessedImageB64() {
        processedImageB64 = null;
    }

    private static final String REMOVE_BG_API_URL = "https://api.remove.bg/v1.0/removebg";

    public static void removeBackground(@NonNull String imageUrl, RemoveBgCallback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("image_url", imageUrl)
                .add("size", "auto")
                .add("format", "auto")
                .build();

        Request request = new Request.Builder()
                .url(REMOVE_BG_API_URL)
                .addHeader("X-Api-Key", BuildConfig.REMOVE_BG_API_KEY)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (e.getMessage() != null) {
                    Log.e("RemoveBgService", e.getMessage());
                }
                callback.OnFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] imageData = response.body().bytes();
                        processedImageB64 = Base64.encodeToString(imageData, Base64.DEFAULT);
                        callback.OnSuccess();
                    } catch (IOException e) {
                        if (e.getMessage() != null) {
                            Log.e("RemoveBgFragment", e.getMessage());
                        }
                        callback.OnFailure(new Exception("Failed to read image data from response"));
                    }
                } else {
                    Log.e("RemoveBgService", "Response failed");
                    callback.OnFailure(new Exception("Response failed with status " + response.code()));
                }
            }
        });
    }

    public interface RemoveBgCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }
}
