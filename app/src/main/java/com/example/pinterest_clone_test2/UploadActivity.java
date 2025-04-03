package com.example.pinterest_clone_test2;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.pinterest_clone_test2.ui.upload.UploadFragment;
import com.example.pinterest_clone_test2.ui.upload.UploadImageDetailsFragment;
import com.example.pinterest_clone_test2.databinding.ActivityUploadBinding;

import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private String cloudName = "dyk7cgbch";  // Cloudinary cloud name
    private String uploadPreset = "upload-test"; // Unsigned upload preset
    private String apiKey = "624956292586851";  // Your Cloudinary API key
    private String apiSecret = "P48f-BnE4fLYgbx6DfCkOLGpr08";  // Your Cloudinary API secret

    private ActivityUploadBinding binding; // Activity binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater()); // Inflate view binding
        setContentView(binding.getRoot());

        // Initialize Cloudinary
        initCloudinary();

        // Open the UploadFragment when the activity is created
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_upload_container, new UploadFragment())
                    .commit();
        }
    }

    // Initialize Cloudinary MediaManager
    private void initCloudinary() {
        Log.d("Cloudinary", "Initializing Cloudinary...");
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey); // API key
        config.put("api_secret", apiSecret); // API secret

        // Initialize MediaManager only once in the application
        MediaManager.init(this, config);
        Log.d("Cloudinary", "Cloudinary initialized.");
    }

    // Method to navigate to UploadImageDetailsFragment and pass the imageUri
    public void showDetailFragment(Uri imageUri) {
        Log.d("Cloudinary", "Navigating to UploadImageDetailsFragment with imageUri: " + imageUri);

        UploadImageDetailsFragment detailsFragment = new UploadImageDetailsFragment();

        // Pass only imageUri to UploadImageDetailsFragment (uploadPreset is already in UploadActivity)
        Bundle bundle = new Bundle();
        bundle.putParcelable("imageUri", imageUri);
        detailsFragment.setArguments(bundle);

        // Perform fragment transaction
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_upload_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    // Method for uploading image to Cloudinary (uploadPreset is already declared in the class)
    public void uploadImage(Uri imageUri) {
        Log.d("Cloudinary", "Attempting to upload image"); // Log to confirm upload is starting

        if (imageUri != null) {
            Log.d("Cloudinary", "Image URI to upload: " + imageUri.toString()); // Log imageUri before upload

            MediaManager.get().upload(imageUri).unsigned(uploadPreset).callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    Log.d("Cloudinary Quickstart", "Upload start");
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    Log.d("Cloudinary Quickstart", "Upload progress: " + bytes + "/" + totalBytes);
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    Log.d("Cloudinary Quickstart", "Upload success");
                    String url = (String) resultData.get("secure_url");
                    Log.d("Cloudinary", "Uploaded image URL: " + url); // Log the URL of the uploaded image
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    Log.d("Cloudinary Quickstart", "Upload failed: " + error.getDescription());
                    Toast.makeText(UploadActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    Log.d("Cloudinary Quickstart", "Upload rescheduled");
                }
            }).dispatch();
        } else {
            Log.d("Cloudinary", "No image selected");
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
