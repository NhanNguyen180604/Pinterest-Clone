package com.example.pinterest_clone_test2.ui.upload;

import android.Manifest;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.example.pinterest_clone_test2.adapters.WebsiteImagesAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentUploadBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.os.Handler;
import android.os.Looper;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.pinterest_clone_test2.utils.LoadingDialog;

import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        ArrayList<Uri> newMediaList = new ArrayList<>();

        // Sử dụng MediaStore.Files để truy vấn cả ảnh và video
        Uri allMediaUri = MediaStore.Files.getContentUri("external");

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
        };

        // Lấy cả ảnh và video
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        // Sắp xếp theo thời gian mới nhất
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        try {
            Cursor cursor = contentResolver.query(
                    allMediaUri,
                    projection,
                    selection,
                    null,
                    sortOrder
            );

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    int mediaType = cursor.getInt(mediaTypeColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);

                    Uri contentUri;
                    if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                        contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        // Thêm vào nếu là ảnh
                        if (mimeType.startsWith("image")) {
                            newMediaList.add(contentUri);
                        }
                    } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                        // Thêm vào nếu là video
                        if (mimeType.startsWith("video")) {
                            newMediaList.add(contentUri);
                        }
                    }
                }
                cursor.close();
            }

            // Sau khi thu thập tất cả URIs, cập nhật danh sách và thông báo adapter
            if (!newMediaList.isEmpty()) {
                mediaList.clear(); // Xóa danh sách cũ nếu cần
                mediaList.addAll(newMediaList);
                mediaAdapter.notifyDataSetChanged();
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
        // Inflate custom layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_url_input, null);
        EditText urlInput = dialogView.findViewById(R.id.url_input);

        // Create AlertDialog with the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Set button click listeners
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_add).setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (!url.isEmpty()) {
                validateAndLoadMediaFromUrl(url);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

        // Show keyboard after dialog appears
        urlInput.postDelayed(() -> {
            urlInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(urlInput, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private void validateAndLoadMediaFromUrl(String url) {
        // Show loading dialog
        LoadingDialog loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("Validating media...");
        loadingDialog.show();

        // Check if URL is a direct media file or a webpage
        String lowerCaseUrl = url.toLowerCase();
        boolean hasImageExtension = lowerCaseUrl.endsWith(".jpg") || lowerCaseUrl.endsWith(".jpeg") ||
                lowerCaseUrl.endsWith(".png") || lowerCaseUrl.endsWith(".gif") ||
                lowerCaseUrl.endsWith(".webp");
        boolean hasVideoExtension = lowerCaseUrl.endsWith(".mp4") || lowerCaseUrl.endsWith(".mov") ||
                lowerCaseUrl.endsWith(".webm") || lowerCaseUrl.endsWith(".avi");

        if (hasImageExtension) {
            processImageUrl(url, loadingDialog);
        } else if (hasVideoExtension) {
            processVideoUrl(url, loadingDialog);
        } else {
            // Likely a webpage URL, extract images
            extractImagesFromWebpage(url, loadingDialog);
        }
    }

    private void extractImagesFromWebpage(String urlParam, LoadingDialog loadingDialog) {
        loadingDialog.setMessage("Extracting images from webpage...");

        new Thread(() -> {
            try {
                // Create a new local variable to hold the potentially modified URL
                String finalUrl = urlParam;

                // Add http:// prefix if missing
                if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                    finalUrl = "https://" + finalUrl;
                }


                // Connect to the website with more browser-like headers
                Connection.Response response = Jsoup.connect(finalUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .referrer("https://www.google.com/")
                        .timeout(15000)
                        .followRedirects(true)
                        .execute();

                if (response.statusCode() != 200) {
                    requireActivity().runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        if (response.statusCode() == 403) {
                            Toast.makeText(getContext(), "This website has blocked access to its images. Please try a different website or download images manually.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to access webpage: " + response.statusCode(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                Document doc = response.parse();
                String baseUri = doc.baseUri();

                // Extract image URLs from the page
                HashMap<String, String> imageUrls = new HashMap<>(); // Using HashMap to avoid duplicates while keeping track of dimensions

                // 1. Get images from <img> tags
                Elements imgElements = doc.select("img");
                for (Element img : imgElements) {
                    String imgUrl = img.absUrl("src");
                    // Skip data URIs, tiny images, and empty URLs
                    if (!imgUrl.isEmpty() && !imgUrl.startsWith("data:") && isValidImageUrl(imgUrl)) {
                        // Get image dimensions from attributes if available
                        String width = img.attr("width");
                        String height = img.attr("height");

                        // Skip very small icons
                        if (!width.isEmpty() && !height.isEmpty()) {
                            try {
                                int w = Integer.parseInt(width);
                                int h = Integer.parseInt(height);
                                if (w < 100 || h < 100) continue; // Skip small icons
                            } catch (NumberFormatException e) {
                                // Ignore parsing errors and include the image
                                Log.d("WebImageExtractor", "Error parsing image dimensions", e);
                            }
                        }

                        imageUrls.put(imgUrl, width + "x" + height);
                    }
                }

                // 2. Get Open Graph image tags (used by social media)
                Elements metaOgImage = doc.select("meta[property=og:image]");
                for (Element meta : metaOgImage) {
                    String imgUrl = meta.attr("content");
                    if (!imgUrl.isEmpty() && isValidImageUrl(imgUrl)) {
                        if (!imgUrl.startsWith("http")) {
                            imgUrl = new URL(new URL(baseUri), imgUrl).toString();
                        }
                        imageUrls.put(imgUrl, "og"); // Priority for OG images
                    }
                }

                // 3. Get Twitter card images
                Elements metaTwitterImage = doc.select("meta[name=twitter:image]");
                for (Element meta : metaTwitterImage) {
                    String imgUrl = meta.attr("content");
                    if (!imgUrl.isEmpty() && isValidImageUrl(imgUrl)) {
                        if (!imgUrl.startsWith("http")) {
                            imgUrl = new URL(new URL(baseUri), imgUrl).toString();
                        }
                        imageUrls.put(imgUrl, "twitter"); // Priority for Twitter images
                    }
                }

                // 4. Get background images from style attributes
                Elements elementsWithStyle = doc.select("[style]");
                for (Element element : elementsWithStyle) {
                    String style = element.attr("style");
                    if (style.contains("background-image")) {
                        // Extract URL from background-image: url('...')
                        extractBackgroundImageUrl(style, baseUri, imageUrls);
                    }
                }

                // 5. Get CSS background images
                Elements styleElements = doc.select("style");
                for (Element style : styleElements) {
                    String cssContent = style.html();
                    // Find all background-image: url patterns in CSS
                    extractBackgroundImagesFromCss(cssContent, baseUri, imageUrls);
                }

                // 6. Get external CSS files and parse them
                Elements linkElements = doc.select("link[rel=stylesheet]");
                for (Element link : linkElements) {
                    String cssUrl = link.absUrl("href");
                    if (!cssUrl.isEmpty()) {
                        try {
                            Connection.Response cssResponse = Jsoup.connect(cssUrl)
                                    .userAgent("Mozilla/5.0")
                                    .timeout(5000)
                                    .ignoreContentType(true)
                                    .execute();

                            if (cssResponse.statusCode() == 200) {
                                String cssContent = cssResponse.body();
                                extractBackgroundImagesFromCss(cssContent, baseUri, imageUrls);
                            }
                        } catch (Exception e) {
                            // Skip this CSS file if there's an error
                            Log.e("WebImageExtractor", "Error loading CSS: " + e.getMessage(), e);
                        }
                    }
                }

                // If no images found
                final ArrayList<String> finalImageUrls = new ArrayList<>(imageUrls.keySet());
                if (finalImageUrls.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(getContext(), "No images found on this webpage", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Show image selection dialog
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    showImageSelectionDialog(finalImageUrls);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (e instanceof org.jsoup.HttpStatusException &&
                            ((org.jsoup.HttpStatusException)e).getStatusCode() == 403) {
                        Toast.makeText(getContext(), "This website restricts automated access to its images. Please try another website or method.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Error extracting images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e("WebImageExtractor", "Error extracting images", e);
                });
            }
        }).start();
    }

    private void extractBackgroundImageUrl(String style, String baseUri, HashMap<String, String> imageUrls) {
        try {
            int startIndex = style.indexOf("url(");
            if (startIndex >= 0) {
                int endIndex = style.indexOf(")", startIndex);
                if (endIndex > startIndex) {
                    String imgUrl = style.substring(startIndex + 4, endIndex).trim();
                    // Remove quotes if present
                    if ((imgUrl.startsWith("'") && imgUrl.endsWith("'")) ||
                            (imgUrl.startsWith("\"") && imgUrl.endsWith("\""))) {
                        imgUrl = imgUrl.substring(1, imgUrl.length() - 1);
                    }

                    // Convert to absolute URL if needed
                    if (!imgUrl.startsWith("http")) {
                        try {
                            imgUrl = new URL(new URL(baseUri), imgUrl).toString();
                        } catch (Exception e) {
                            Log.d("WebImageExtractor", "Malformed URL", e);
                            return; // Skip if URL is malformed
                        }
                    }

                    if (isValidImageUrl(imgUrl) && !imgUrl.contains("data:")) {
                        imageUrls.put(imgUrl, "bg"); // Add with a background tag
                    }
                }
            }
        } catch (Exception e) {
            Log.e("WebImageExtractor", "Error extracting background image", e);
        }
    }

    private void extractBackgroundImagesFromCss(String cssContent, String baseUri, HashMap<String, String> imageUrls) {
        try {
            // Simple regex pattern to find background-image: url(...) in CSS
            Pattern pattern = Pattern.compile("background(-image)?\\s*:\\s*url\\(['\"]?(.*?)['\"]?\\)");
            Matcher matcher = pattern.matcher(cssContent);

            while (matcher.find()) {
                String imgUrl = matcher.group(2);
                if (imgUrl != null && !imgUrl.isEmpty() && !imgUrl.startsWith("data:")) {
                    // Convert to absolute URL if needed
                    if (!imgUrl.startsWith("http")) {
                        try {
                            imgUrl = new URL(new URL(baseUri), imgUrl).toString();
                        } catch (Exception e) {
                            Log.d("WebImageExtractor", "Malformed URL in CSS", e);
                            continue; // Skip if URL is malformed
                        }
                    }

                    if (isValidImageUrl(imgUrl)) {
                        imageUrls.put(imgUrl, "css"); // Add with a CSS tag
                    }
                }
            }
        } catch (Exception e) {
            Log.e("WebImageExtractor", "Error extracting CSS background images", e);
        }
    }

    private boolean isValidImageUrl(String url) {
        if (url == null) return false;

        String lowerCaseUrl = url.toLowerCase();
        return !url.isEmpty() &&
                (lowerCaseUrl.contains(".jpg") ||
                        lowerCaseUrl.contains(".jpeg") ||
                        lowerCaseUrl.contains(".png") ||
                        lowerCaseUrl.contains(".gif") ||
                        lowerCaseUrl.contains(".webp"));
    }

    private void showImageSelectionDialog(ArrayList<String> imageUrls) {
        // Create a custom dialog to show images
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_website_images, null);
        builder.setView(dialogView);

        RecyclerView imageRecyclerView = dialogView.findViewById(R.id.website_images_recyclerview);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_selection);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);

        dialogTitle.setText(String.format(Locale.getDefault(), "Found %d images", imageUrls.size()));

        // Set up RecyclerView with grid layout
        int spanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 2;
        imageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        // Create the dialog before referencing it in the adapter
        final AlertDialog dialog = builder.create();

        // Create adapter for website images - now the dialog variable is defined before being used
        WebsiteImagesAdapter adapter = new WebsiteImagesAdapter(imageUrls, getContext(), url -> {
            // Handle image selection
            loadSelectedWebImage(url);
            dialog.dismiss();
        });

        imageRecyclerView.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadSelectedWebImage(String url) {
        // Show loading dialog
        LoadingDialog loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("Loading selected image...");
        loadingDialog.show();

        // Using Glide to fetch and process the image
        Glide.with(requireContext())
                .asBitmap()
                .load(url)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        loadingDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to load image: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Bitmap resource, @NonNull Object model, @NonNull Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        // Save bitmap to file in background
                        new Thread(() -> {
                            try {
                                File imageFile = createImageFileFromBitmap(resource);
                                Uri localUri = FileProvider.getUriForFile(
                                        requireContext(),
                                        "com.example.pinterest_clone_test2.fileprovider",
                                        imageFile);

                                // Update UI on main thread
                                requireActivity().runOnUiThread(() -> {
                                    loadingDialog.dismiss();
                                    onMediaSelected(localUri);
                                });
                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() -> {
                                    loadingDialog.dismiss();
                                    Toast.makeText(getContext(), "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("WebImageExtractor", "Error saving image", e);
                                });
                            }
                        }).start();
                        return false;
                    }
                })
                .submit();
    }
    private void processImageUrl(String url, LoadingDialog loadingDialog) {
        loadingDialog.setMessage("Loading image...");

        // Using Glide to fetch and process the image
        Glide.with(requireContext())
                .asBitmap()
                .load(url)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        loadingDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to load image: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                        Log.e("WebImageExtractor", "Image load failed", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Bitmap resource, @NonNull Object model, @NonNull Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        // Save bitmap to file in background
                        new Thread(() -> {
                            try {
                                File imageFile = createImageFileFromBitmap(resource);
                                Uri localUri = FileProvider.getUriForFile(
                                        requireContext(),
                                        "com.example.pinterest_clone_test2.fileprovider",
                                        imageFile);

                                // Update UI on main thread
                                requireActivity().runOnUiThread(() -> {
                                    loadingDialog.dismiss();
                                    onMediaSelected(localUri);
                                });
                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() -> {
                                    loadingDialog.dismiss();
                                    Toast.makeText(getContext(), "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("WebImageExtractor", "Error saving image file", e);
                                });
                            }
                        }).start();
                        return false;
                    }
                })
                .submit();
    }
    private void processVideoUrl(String url, LoadingDialog loadingDialog) {
        loadingDialog.setMessage("Validating video...");

        // Create a handler to manage timeout
        Handler handler = new Handler(Looper.getMainLooper());

        // Try with resources for MediaMetadataRetriever
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            // Create a runnable to handle timeout
            Runnable timeoutRunnable = () -> {
                Toast.makeText(getContext(), "Video validation timed out. URL may not be valid.", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                try {
                    retriever.release();
                } catch (Exception e) {
                    Log.d("WebImageExtractor", "Error releasing retriever", e);
                }
            };

            handler.postDelayed(timeoutRunnable, 5000);

            // Run validation in background
            new Thread(() -> {
                try {
                    // Try to set data source and retrieve metadata
                    retriever.setDataSource(url, new HashMap<>());

                    // Try to get a frame to confirm it's a valid video
                    Bitmap frame = retriever.getFrameAtTime();

                    // Remove the timeout handler
                    handler.removeCallbacks(timeoutRunnable);

                    if (frame != null) {
                        // Video is valid, create URI and continue
                        Uri videoUri = Uri.parse(url);
                        requireActivity().runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            onMediaSelected(videoUri);
                        });
                    } else {
                        // No frames could be retrieved, likely not a valid video
                        handler.removeCallbacks(timeoutRunnable);
                        requireActivity().runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            Toast.makeText(getContext(), "URL does not contain valid video content", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    // Remove the timeout handler
                    handler.removeCallbacks(timeoutRunnable);
                    requireActivity().runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to validate video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("WebImageExtractor", "Video validation failed", e);
                    });
                }
            }).start();
        } catch (Exception e) {
            loadingDialog.dismiss();
            Toast.makeText(getContext(), "Error initializing video validator", Toast.LENGTH_SHORT).show();
            Log.e("WebImageExtractor", "Error with media retriever", e);
        }
    }

    private File createImageFileFromBitmap(Bitmap bitmap) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "URL_IMG_" + timeStamp + ".jpg";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, fileName);

        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
            outputStream.flush();
        }

        return imageFile;
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
        binding.cvBtnCamera.setVisibility(View.GONE);
        binding.cvBtnAddUrl.setVisibility(View.GONE);
        binding.cvBtnLibrary.setVisibility(View.GONE);

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
        binding.cvBtnCamera.setVisibility(View.GONE);
        binding.cvBtnAddUrl.setVisibility(View.GONE);
        binding.cvBtnLibrary.setVisibility(View.GONE);
        binding.btnNext.setEnabled(true);
    }

    private void resetSelectedMedia() {
        selectMediaUri = null;
        binding.selectedMediaContainer.setVisibility(View.GONE);
        binding.selectedVideoView.setVisibility(View.GONE);
        binding.cvBtnCamera.setVisibility(View.VISIBLE);
        binding.cvBtnAddUrl.setVisibility(View.VISIBLE);
        binding.cvBtnLibrary.setVisibility(View.VISIBLE);
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