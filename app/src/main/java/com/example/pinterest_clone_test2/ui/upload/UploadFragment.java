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
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.pinterest_clone_test2.UploadActivity;
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
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;

    FragmentUploadBinding binding;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageList;
    Uri cameraUri;
    Uri selectImageUri;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public UploadFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            switch (requestingCode) {
                case GALLERY_REQUEST_CODE:
                    break;
                case CAMERA_REQUEST_CODE:
                    if (isGranted) {
                        openCamera();
                    }
                    break;
                case STORAGE_PERMISSION_CODE:
                    if (isGranted) {
                        loadAllImagesFromGallery();
                    }
                    break;
            }
        });
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

        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageList, getContext(), imageSelectedListener);
        binding.recyclerViewImages.setLayoutManager(new GridLayoutManager(getContext(), 4));
        binding.recyclerViewImages.setAdapter(imageAdapter);

        binding.btnNext.setEnabled(false);
        binding.selectedImageContainer.setVisibility(View.GONE);

        binding.btnExit.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnLibrary.setOnClickListener(v -> openGallery());
        binding.btnCamera.setOnClickListener(v -> requestPermissionIfNeeded(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE));
        binding.btnAddUrl.setOnClickListener(v -> openUrlInput());
        binding.btnNext.setOnClickListener(v -> proceedToNextStep());
        binding.btnRemoveSelectedImage.setOnClickListener(v -> resetSelectedImage());

        requestPermissionIfNeeded(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        if (savedInstanceState != null) {
            selectImageUri = savedInstanceState.getParcelable("selectedImageUri");
            if (selectImageUri != null) {
                onImageSelected(selectImageUri);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectImageUri != null) {
            outState.putParcelable("selectedImageUri", selectImageUri);
        }
    }

    private void requestPermissionIfNeeded(@NonNull String permission, int requestCode) {
        requestingCode = requestCode;
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            switch (requestingCode) {
                case GALLERY_REQUEST_CODE:
                    break;
                case STORAGE_PERMISSION_CODE:
                    loadAllImagesFromGallery();
                    break;
                case CAMERA_REQUEST_CODE:
                    openCamera();
                    break;
            }
        } else {
            requestPermissionLauncher.launch(permission);
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
                        imageList.add(imageUri);
                        imageAdapter.notifyItemInserted(imageList.size() - 1);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGallery() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), uris -> {
                if (!uris.isEmpty()) {
                    int startPos = imageList.size();
                    imageList.addAll(uris);
                    imageAdapter.notifyItemRangeInserted(startPos, uris.size());
                    for (Uri uri : uris) {
                        requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
            });

    private void openCamera() {
        File photoFile = createImageFile();
        if (photoFile != null) {
            cameraUri = FileProvider.getUriForFile(requireContext(), "com.example.pinterest_clone_test2.fileprovider", photoFile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            cameraActivityLauncher.launch(cameraIntent);
        }
    }

    private final ActivityResultLauncher<Intent> cameraActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && cameraUri != null) {
                    imageList.add(cameraUri);
                    imageAdapter.notifyItemInserted(imageList.size() - 1);
                    onImageSelected(cameraUri);
                }
            });

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + "_";
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openUrlInput() {
        Toast.makeText(getActivity(), "Input URL logic here", Toast.LENGTH_SHORT).show();
    }

    private void proceedToNextStep() {
        if (getActivity() instanceof UploadActivity) {
            ((UploadActivity) getActivity()).showDetailFragment(selectImageUri);
        }
    }

    public void onImageSelected(Uri imageUri) {
        this.selectImageUri = imageUri;
        binding.selectedImageView.setImageURI(imageUri);
        binding.selectedImageContainer.setVisibility(View.VISIBLE);
        binding.buttonContainer.setVisibility(View.GONE);
        binding.btnNext.setEnabled(true);
    }

    private void resetSelectedImage() {
        selectImageUri = null;
        binding.selectedImageContainer.setVisibility(View.GONE);
        binding.buttonContainer.setVisibility(View.VISIBLE);
        binding.btnNext.setEnabled(false);
    }

    private final ImageAdapter.OnImageSelectedListener imageSelectedListener = new ImageAdapter.OnImageSelectedListener() {
        @Override
        public void onImageSelected(Uri imageUri) {
            selectImageUri = imageUri;
            binding.selectedImageView.setImageURI(imageUri);
            binding.selectedImageContainer.setVisibility(View.VISIBLE);
            binding.buttonContainer.setVisibility(View.GONE);
            binding.btnNext.setEnabled(true);
        }

        @Override
        public void onImageDeselected(Uri imageUri) {
            selectImageUri = null;
            binding.selectedImageContainer.setVisibility(View.GONE);
            binding.buttonContainer.setVisibility(View.VISIBLE);
            binding.btnNext.setEnabled(false);
        }
    };
}
