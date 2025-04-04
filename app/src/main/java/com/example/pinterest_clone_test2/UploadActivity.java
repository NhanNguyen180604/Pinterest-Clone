package com.example.pinterest_clone_test2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.pinterest_clone_test2.databinding.ActivityUploadBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.upload.UploadFragment;
import com.example.pinterest_clone_test2.ui.upload.UploadImageDetailsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UploadActivity extends AppCompatActivity {

    // TODO: hide these information
    final String cloudName = "dyk7cgbch";
    final String uploadPreset = "upload-test";
    final String apiKey = "624956292586851";
    final String apiSecret = "P48f-BnE4fLYgbx6DfCkOLGpr08";
    private FirebaseFirestore firestore; // Firestore instance to save Pin data
    private ActivityUploadBinding binding; // Activity binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater()); // Inflate view binding
        setContentView(binding.getRoot());
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
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

    private boolean isMediaManagerInitialized() {
        try {
            MediaManager.get();
            return true;
        } catch (Exception e) {
            return false;
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
        if (!isMediaManagerInitialized()) {
            MediaManager.init(this, config);
            Log.d("Cloudinary", "Cloudinary initialized.");
        }
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

    public void uploadImage(Uri imageUri, String title, String description) {
        Log.d("Cloudinary", "Attempting to upload image");

        if (imageUri != null) {
            Log.d("Cloudinary", "Image URI to upload: " + imageUri);

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
                    Log.d("Cloudinary", "Uploaded image URL: " + url);

                    // Save Pin information to Firestore
                    savePinToFirestore(url, title, description);

                    // Display success message to the user
                    Toast.makeText(UploadActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate back to MainActivity after successful upload
                    Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
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

    private void savePinToFirestore(String imageUrl, String title, String description) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        String thumbnailUrl = imageUrl.replace("/upload/", "/upload/c_thumb,w_200/");

        // Create a Pin object
        Pin pin = new Pin()
                .setAuthorId(userId)
                .setType(Pin.PinType.IMAGE)  // Assuming it's an image for now
                .setMediaUrl(imageUrl)
                .setThumbnailUrl(thumbnailUrl)
                .setName(title)
                .setDescription(description)
                .setIsLiked(false)
                .setAllowComment(true)
                .setLikeCount(0)
                .setCreatedAt(System.currentTimeMillis());

        // Firestore will auto-generate the ID for the pin
        firestore.collection("pins")
                .add(pin)
                .addOnSuccessListener(documentReference ->
                        Log.d("Firestore", "Pin added with ID: " + documentReference.getId())
                )
                .addOnFailureListener(e -> {
                    Log.d("Firestore", "Error adding Pin: " + e.getMessage());
                    Toast.makeText(UploadActivity.this, "Failed to save Pin.", Toast.LENGTH_SHORT).show();
                });
    }
}
