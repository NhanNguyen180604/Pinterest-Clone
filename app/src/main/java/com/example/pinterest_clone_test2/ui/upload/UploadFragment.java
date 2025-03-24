package com.example.pinterest_clone_test2.ui.upload;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.adapters.ImageAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentUploadBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UploadFragment extends Fragment {
    private int requestingCode;
    private static final int GALLERY_REQUEST_CODE = 100;
    private boolean galleryRequestGranted = false;
    private static final int CAMERA_REQUEST_CODE = 101;
    private boolean cameraRequestGranted = false;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private boolean storagePermissionGranted = false;
    private static final int CAMERA_PERMISSION_CODE = 103;
    private boolean cameraPermissionGranted = false;

    FragmentUploadBinding binding;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageList;
    Uri cameraUri;

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUploadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set up RecyclerView
        binding.recyclerViewImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageList, getContext(), imageSelectedListener);  // Pass the fragment as the listener
        binding.recyclerViewImages.setAdapter(imageAdapter);

        // Set up buttons
        binding.btnNext.setEnabled(false); // Disable until image is selected

        binding.btnExit.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed()); // Back to home

        binding.btnLibrary.setOnClickListener(v -> openGallery()); // Open gallery to pick images

        binding.btnCamera.setOnClickListener(v -> openCamera()); // Open camera to capture images

        binding.btnAddUrl.setOnClickListener(v -> openUrlInput()); // Open URL input dialog

        binding.btnNext.setOnClickListener(v -> proceedToNextStep());

        // Check if permissions for storage are granted when entering the upload fragment
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)) {
            loadAllImagesFromGallery();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                switch (requestingCode) {
                    case GALLERY_REQUEST_CODE:
                        galleryRequestGranted = isGranted;
                        break;
                    case CAMERA_PERMISSION_CODE:
                        cameraPermissionGranted = isGranted;
                        break;
                    case CAMERA_REQUEST_CODE:
                        cameraRequestGranted = isGranted;
                        break;
                    case STORAGE_PERMISSION_CODE:
                        storagePermissionGranted = isGranted;
                        break;
                }
            });

    private boolean checkPermission(@NonNull String permission, int requestCode) {
        requestingCode = requestCode;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            switch (requestingCode) {
                case GALLERY_REQUEST_CODE:
                    galleryRequestGranted = true;
                    break;
                case CAMERA_PERMISSION_CODE:
                    cameraPermissionGranted = true;
                    break;
                case CAMERA_REQUEST_CODE:
                    cameraRequestGranted = true;
                    break;
                case STORAGE_PERMISSION_CODE:
                    storagePermissionGranted = true;
                    break;
            }
            return true;
        }

        requestPermissionLauncher.launch(permission);

        switch (requestingCode) {
            case GALLERY_REQUEST_CODE:
                return galleryRequestGranted;
            case CAMERA_PERMISSION_CODE:
                return cameraPermissionGranted;
            case CAMERA_REQUEST_CODE:
                return cameraRequestGranted;
            case STORAGE_PERMISSION_CODE:
                return storagePermissionGranted;
            default:
                return false;
        }
    }

    private void loadAllImagesFromGallery() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Images.Media._ID};
        try (Cursor cursor = contentResolver.query(imagesUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);

                if (columnIndex != -1) {
                    do {
                        long imageId = cursor.getLong(columnIndex);
                        Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
                        imageList.add(imageUri); // Add the image URI to the list
                        imageAdapter.notifyItemInserted(imageList.size() - 1);
                    } while (cursor.moveToNext());
                } else {
                    Toast.makeText(getActivity(), "Error loading images from gallery", Toast.LENGTH_SHORT).show();
                    Log.d("uploading-error", "Không tìm thấy cột dữ liệu ảnh");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error loading images from gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), uris -> {
        if (!uris.isEmpty()) {
            int startPos = imageList.size();
            imageList.addAll(uris); // Add image to list
            imageAdapter.notifyItemRangeInserted(startPos, uris.size());

            int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            for (Uri uri :
                    uris) {
                requireContext().getContentResolver().takePersistableUriPermission(uri, flag);
            }
        }
    });

    private void openCamera() {
        if (checkPermission(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE)) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                cameraUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.pinterest_clone_test2.fileprovider",
                        photoFile
                );

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri); // Pass the URI here
                cameraActivityLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(getActivity(), "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Camera permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<Intent> cameraActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (cameraUri != null) {  // Use the Uri we created earlier
                        imageList.add(cameraUri);
                        imageAdapter.notifyItemInserted(imageList.size() - 1);
                        onImageSelected(cameraUri);
                    } else {
                        Toast.makeText(requireContext(), "Image capture failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openUrlInput() {
        // Your existing method for handling URL input
        Toast.makeText(getActivity(), "Input URL logic here", Toast.LENGTH_SHORT).show();
    }

    private void proceedToNextStep() {
        // Handle next step (upload or another action)
        Toast.makeText(getActivity(), "Proceed to the next step", Toast.LENGTH_SHORT).show();
    }

    public void onImageSelected(Uri imageUri) {
        // Handle image selection
        binding.selectedImageView.setImageURI(imageUri);
        binding.selectedImageView.setVisibility(View.VISIBLE); // Show selected image
        binding.btnNext.setEnabled(true); // Enable next button when an image is selected
    }

    private final ImageAdapter.OnImageSelectedListener imageSelectedListener = UploadFragment.this::onImageSelected;
}