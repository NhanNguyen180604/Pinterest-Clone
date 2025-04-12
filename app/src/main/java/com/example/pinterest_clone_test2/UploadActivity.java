package com.example.pinterest_clone_test2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.pinterest_clone_test2.databinding.ActivityUploadBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.ui.upload.UploadFragment;
import com.example.pinterest_clone_test2.ui.upload.UploadPinDetailsFragment;
import com.example.pinterest_clone_test2.utils.CloudinaryManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

public class UploadActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private ActivityUploadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_upload_container, new UploadFragment())
                    .commit();
        }
    }

    public void showDetailFragment(Uri mediaUri) {
        Log.d("Cloudinary", "Navigating to UploadImageDetailsFragment with mediaUri: " + mediaUri);

        UploadPinDetailsFragment detailsFragment = new UploadPinDetailsFragment();

        // Pass only mediaUri to UploadImageDetailsFragment
        Bundle bundle = new Bundle();
        bundle.putParcelable("mediaUri", mediaUri);
        detailsFragment.setArguments(bundle);

        // Perform fragment transaction
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_upload_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    public void uploadMedia(Uri mediaUri, String title, String description) {
        Log.d("Cloudinary", "Attempting to upload media");

        if (mediaUri != null) {
            Log.d("Cloudinary", "Media URI to upload: " + mediaUri);

            // Kiểm tra MIME type và gọi hàm upload từ CloudinaryManager
            String mimeType = getContentResolver().getType(mediaUri);
            String mediaType;

            // Xác định loại media: "image", "video", "gif"
            if (mimeType != null) {
                if (mimeType.startsWith("image") && mimeType.contains("gif")) {
                    mediaType = "gif";  // Xử lý GIF
                } else if (mimeType.startsWith("video")) {
                    mediaType = "video";  // Xử lý video
                } else {
                    mediaType = "image";  // Xử lý ảnh
                }

                // Call CloudinaryManager to upload the media
                CloudinaryManager.uploadMedia(mediaUri, mimeType, new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Upload start");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d("Cloudinary", "Upload progress: " + bytes + "/" + totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        if (url == null) {
                            if (mimeType.startsWith("image")) {
                                Toast.makeText(UploadActivity.this, getResources().getString(R.string.upload_image_failure), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UploadActivity.this, getResources().getString(R.string.upload_video_failure), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            savePinToFirestore(url, title, description, mediaType);  // Lưu vào Firestore
                            navigateBackToHome();  // Điều hướng về trang chủ sau khi upload thành công
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(UploadActivity.this, "", Toast.LENGTH_SHORT).show();
                        Log.e("Cloudinary", "Error: " + error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d("Cloudinary", "Rescheduled");
                    }
                });
            } else {
                Log.d("Cloudinary", "No media selected");
                Toast.makeText(this, getResources().getString(R.string.no_media_selected), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savePinToFirestore(String mediaUrl, String title, String description, String mediaType) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        String thumbnailUrl = mediaUrl.replace("/upload/", "/upload/w_200/");

        Pin.PinType pinType;
        if ("video".equals(mediaType)) {
            pinType = Pin.PinType.VIDEO;
        } else if ("gif".equals(mediaType)) {
            pinType = Pin.PinType.GIF;
        } else {
            pinType = Pin.PinType.IMAGE;
        }

        // Tạo đối tượng Pin
        Pin pin = new Pin()
                .setAuthorId(userId)
                .setType(pinType)
                .setMediaUrl(mediaUrl)
                .setThumbnailUrl(thumbnailUrl)
                .setName(title)
                .setDescription(description)
                .setAllowComment(true)
                .setCreatedAt(System.currentTimeMillis());

        FirebasePinService.uploadPin(pin, new FirebasePinService.UploadPinServiceCallback() {
            @Override
            public void OnSuccess(DocumentReference documentReference) {
                Log.d("Firestore", "Pin added with ID: " + documentReference.getId());
            }

            @Override
            public void OnFailure(Exception e) {
                Log.e("Firestore", "Error adding Pin: " + e.getMessage());
                Toast.makeText(UploadActivity.this, getResources().getString(R.string.upload_pin_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navigate back to the home screen after successful upload
    private void navigateBackToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Optional: Finish the current activity to prevent user from going back to upload screen
    }
}