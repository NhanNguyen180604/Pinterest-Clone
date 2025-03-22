package com.example.pinterest_clone_test2.ui.upload;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.ImageAdapter;

import java.util.ArrayList;

public class UploadFragment extends Fragment {

    private static final int GALLERY_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private static final int CAMERA_PERMISSION_CODE = 103;

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageList;
    private Button btnNext;
    private ImageButton btnExit, btnLibrary, btnCamera, btnAddUrl;
    private ImageView selectedImageView;

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageList, getContext(), this::onImageSelected);  // Pass the fragment as the listener
        recyclerView.setAdapter(imageAdapter);

        selectedImageView = view.findViewById(R.id.selectedImageView);

        // Set up buttons
        btnNext = view.findViewById(R.id.btnNext);
        btnNext.setEnabled(false); // Disable until image is selected

        btnExit = view.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> getActivity().onBackPressed()); // Back to home

        btnLibrary = view.findViewById(R.id.btnLibrary);
        btnLibrary.setOnClickListener(v -> openGallery()); // Open gallery to pick images

        btnCamera = view.findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(v -> openCamera()); // Open camera to capture images

        btnAddUrl = view.findViewById(R.id.btnAddUrl);
        btnAddUrl.setOnClickListener(v -> openUrlInput()); // Open URL input dialog

        // Check if permissions for storage are granted when entering the upload fragment
        checkStoragePermission();

        return view;
    }


    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            loadAllImagesFromGallery();
        }
    }

    private void loadAllImagesFromGallery() {
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = contentResolver.query(imagesUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                if (columnIndex != -1) {
                    do {
                        String imagePath = cursor.getString(columnIndex);
                        Uri imageUri = Uri.parse(imagePath);
                        imageList.add(imageUri); // Add the image URI to the list
                    } while (cursor.moveToNext());

                    imageAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed
                } else {
                    Toast.makeText(getActivity(), "Không tìm thấy cột dữ liệu ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error loading images from gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
        } else {
            Toast.makeText(getContext(), "Permission denied to access gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(getActivity(), "Camera không khả dụng", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Request camera permission if not granted
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
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
        recyclerView.setVisibility(View.GONE);
        selectedImageView.setImageURI(imageUri);
        selectedImageView.setVisibility(View.VISIBLE); // Show selected image
        btnNext.setEnabled(true); // Enable next button when an image is selected
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                Uri selectedImageUri = data.getData();
                imageList.add(selectedImageUri); // Add image to list
                imageAdapter.notifyDataSetChanged();

                // When an image is selected from the RecyclerView, show it
                onImageSelected(selectedImageUri);
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Uri capturedImageUri = data.getData();
                imageList.add(capturedImageUri); // Add captured image to list
                imageAdapter.notifyDataSetChanged();

                // When an image is captured, show it
                onImageSelected(capturedImageUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
                loadAllImagesFromGallery();
            } else {
                Toast.makeText(getActivity(), "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted for camera
                openCamera();
            } else {
                Toast.makeText(getActivity(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}