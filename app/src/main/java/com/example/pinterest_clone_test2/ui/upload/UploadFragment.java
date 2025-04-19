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
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.UploadActivity;
import com.example.pinterest_clone_test2.adapters.MediaAdapter;
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
    private MediaAdapter mediaAdapter;
    private ArrayList<Uri> mediaList;
    Uri cameraUri;
    Uri selectMediaUri;
    ExoPlayer exoPlayer;
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
                        loadAllMediaFromGallery();
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

        mediaList = new ArrayList<>();
        mediaAdapter = new MediaAdapter(mediaList, getContext(), mediaSelectedListener);
        binding.recyclerViewImages.setLayoutManager(new GridLayoutManager(getContext(), 4));
        binding.recyclerViewImages.setAdapter(mediaAdapter);

        binding.btnNext.setEnabled(false);
        binding.selectedMediaContainer.setVisibility(View.GONE);

        binding.btnExit.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnLibrary.setOnClickListener(v -> openGallery());
        binding.btnCamera.setOnClickListener(v -> requestPermissionIfNeeded(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE));
        binding.btnAddUrl.setOnClickListener(v -> openUrlInput());
        binding.btnNext.setOnClickListener(v -> proceedToNextStep());
        binding.btnRemoveSelectedMedia.setOnClickListener(v -> resetSelectedMedia());

        requestPermissionIfNeeded(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);

        if (savedInstanceState != null) {
            selectMediaUri = savedInstanceState.getParcelable("selectedMediaUri");
            if (selectMediaUri != null) {
                onMediaSelected(selectMediaUri);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectMediaUri != null) {
            outState.putParcelable("selectedMediaUri", selectMediaUri);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseExoPlayer();
    }

    private void requestPermissionIfNeeded(@NonNull String permission, int requestCode) {
        requestingCode = requestCode;
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            switch (requestingCode) {
                case GALLERY_REQUEST_CODE:
                    break;
                case STORAGE_PERMISSION_CODE:
                    loadAllMediaFromGallery();
                    break;
                case CAMERA_REQUEST_CODE:
                    openCamera();
                    break;
            }
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void loadAllMediaFromGallery() {
        ContentResolver contentResolver = requireContext().getContentResolver();

        // Query images and GIFs
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] imageProjection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.MIME_TYPE};

        // Query videos
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] videoProjection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.MIME_TYPE};

        try {
            ArrayList<Uri> newMediaList = new ArrayList<>();

            // Query for images and GIFs
            try (Cursor cursor = contentResolver.query(imageUri, imageProjection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int mimeTypeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
                    if (columnIndex != -1 && mimeTypeIndex != -1) {
                        do {
                            long mediaId = cursor.getLong(columnIndex);
                            String mimeType = cursor.getString(mimeTypeIndex);
                            Uri mediaUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(mediaId));

                            // Add image or gif
                            if (mimeType.startsWith("image") || mimeType.equals("image/gif")) {
                                newMediaList.add(mediaUri);
                            }
                        } while (cursor.moveToNext());
                    }
                }
            }

            // Query for videos
            try (Cursor cursor = contentResolver.query(videoUri, videoProjection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                    int mimeTypeIndex = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);
                    if (columnIndex != -1 && mimeTypeIndex != -1) {
                        do {
                            long mediaId = cursor.getLong(columnIndex);
                            String mimeType = cursor.getString(mimeTypeIndex);
                            Uri mediaUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(mediaId));

                            // Add video
                            if (mimeType.startsWith("video")) {
                                newMediaList.add(mediaUri);
                            }
                        } while (cursor.moveToNext());
                    }
                }
            }

            // After collecting all the media URIs, update the media list and notify the adapter
            if (!newMediaList.isEmpty()) {
                mediaList.addAll(newMediaList); // Add the new media to the list
                mediaAdapter.notifyItemRangeInserted(mediaList.size() - newMediaList.size(), newMediaList.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openGallery() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), uris -> {
                if (!uris.isEmpty()) {
                    int startPos = mediaList.size();
                    mediaList.addAll(uris);
                    mediaAdapter.notifyItemRangeInserted(startPos, uris.size());
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
                    mediaList.add(cameraUri);
                    mediaAdapter.notifyItemInserted(mediaList.size() - 1);
                    onMediaSelected(cameraUri);
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
            ((UploadActivity) getActivity()).showDetailFragment(selectMediaUri);
        }
    }

    public void onMediaSelected(Uri mediaUri) {
        this.selectMediaUri = mediaUri;
        String mimeType = requireContext().getContentResolver().getType(mediaUri);

        releaseExoPlayer();
        binding.selectedMediaContainer.setVisibility(View.VISIBLE);

        if (mimeType != null && (mimeType.startsWith("image") || mimeType.contains("gif"))) {
            Glide.with(binding.selectedImageView.getContext())
                    .load(mediaUri)
                    .placeholder(R.drawable.ic_loading)
                    .into(binding.selectedImageView);  // Hiển thị ảnh hoặc GIF

            binding.selectedImageView.setVisibility(View.VISIBLE);
            binding.selectedVideoView.setVisibility(View.GONE);

        } else if (mimeType != null && mimeType.startsWith("video")) {
            binding.selectedImageView.setVisibility(View.GONE);
            binding.selectedVideoView.setVisibility(View.VISIBLE);

            exoPlayer = new ExoPlayer.Builder(requireContext()).build();
            binding.selectedVideoView.setPlayer(exoPlayer);
            MediaItem mediaItem = MediaItem.fromUri(mediaUri);
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.prepare();
        }

        // Ẩn button container và kích hoạt nút Next khi media được chọn
        binding.btnCamera.setVisibility(View.GONE);
        binding.btnLibrary.setVisibility(View.GONE);
        binding.btnAddUrl.setVisibility(View.GONE);
        binding.btnNext.setEnabled(true);
    }


    private void resetSelectedMedia() {
        selectMediaUri = null;
        binding.selectedMediaContainer.setVisibility(View.GONE);
        binding.selectedVideoView.setVisibility(View.GONE);
        binding.btnCamera.setVisibility(View.VISIBLE);
        binding.btnLibrary.setVisibility(View.VISIBLE);
        binding.btnAddUrl.setVisibility(View.VISIBLE);
        binding.btnNext.setEnabled(false);
        releaseExoPlayer();
    }

    void releaseExoPlayer() {
        if (exoPlayer == null)
            return;

        exoPlayer.stop();
        exoPlayer.release();
        exoPlayer = null;
    }

    private final MediaAdapter.OnMediaSelectedListener mediaSelectedListener = this::onMediaSelected;
}