package com.example.pinterest_clone_test2.ui.upload;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.UploadActivity;
import com.example.pinterest_clone_test2.databinding.FragmentUploadCollageBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class UploadCollageFragment extends Fragment {

    private FragmentUploadCollageBinding binding;
    private ArrayList<Uri> addedImagesList;

    // Variables for collage handling
    private ImageView activeImageView;
    private float dX, dY;

    public UploadCollageFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUploadCollageBinding.inflate(inflater, container, false);
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

        addedImagesList = new ArrayList<>();

        // Set up the collage area with dotted background
        setupCollageArea();

        // Setup button click listeners
        setupButtonListeners();
    }

    private void setupCollageArea() {
        // Create a white background with dotted grid pattern
        addGridBackground();

        // Make the collage area visible
        binding.collageArea.setVisibility(View.VISIBLE);
    }

    private void addGridBackground() {
        // Create a View for the grid background
        View gridBackground = new View(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        gridBackground.setLayoutParams(params);

        gridBackground.setTag("gridBackground");

        Bitmap gridBitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(gridBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#000000"));
        paint.setStrokeWidth(1);

        for (int i = 0; i < 40; i += 10) {
            for (int j = 0; j < 40; j += 10) {
                canvas.drawCircle(i, j, 1, paint);
            }
        }

        // Create a BitmapDrawable and set it as the background
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), gridBitmap);
        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        gridBackground.setBackground(bitmapDrawable);

        binding.collageArea.setBackgroundColor(Color.WHITE);

        binding.collageArea.addView(gridBackground, 0);
    }

    private void setupButtonListeners() {

        binding.btnExit.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnNext.setOnClickListener(v -> saveAndProceed());

        binding.btnAddImage.setOnClickListener(v -> openGallery());

        binding.btnBrush.setOnClickListener(v -> Toast.makeText(requireContext(), "Brush feature coming soon", Toast.LENGTH_SHORT).show());
        binding.btnText.setOnClickListener(v -> Toast.makeText(requireContext(), "Text feature coming soon", Toast.LENGTH_SHORT).show());
        binding.btnAddItem.setOnClickListener(v -> Toast.makeText(requireContext(), "Add item feature coming soon", Toast.LENGTH_SHORT).show());
        binding.btnGrid.setOnClickListener(v -> Toast.makeText(requireContext(), "Grid feature coming soon", Toast.LENGTH_SHORT).show());
    }

    private void openGallery() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // Add image directly to the collage area
                    addImageToCollage(uri);
                    addedImagesList.add(uri);

                    // Enable Next button once we have at least one image
                    binding.btnNext.setEnabled(true);
                    binding.btnNext.setBackgroundResource(R.drawable.red_button_pinterest);
                }
            });

    private void addImageToCollage(Uri imageUri) {
        // Create ImageView for the selected image
        ImageView imageView = new ImageView(requireContext());

        // Calculate default size (about 1/3 of container width)
        int containerWidth = binding.collageArea.getWidth();
        if (containerWidth <= 0) containerWidth = binding.collageArea.getMeasuredWidth();
        if (containerWidth <= 0) containerWidth = 600; // fallback

        int imageSize = containerWidth / 2;  // Make it bigger to match the screenshot

        // Calculate a position that tries to avoid overlap with existing images
        int childCount = binding.collageArea.getChildCount();
        int offset = childCount * 20; // Stagger images

        // Use FrameLayout params for positioning
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageSize, imageSize);
        params.leftMargin = 40 + (offset % (containerWidth - imageSize - 40));
        params.topMargin = 40 + (offset / 80) * 40;

        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Load the image
        Glide.with(requireContext())
                .load(imageUri)
                .centerCrop()
                .into(imageView);

        // Make draggable
        setupDraggableImage(imageView);

        // Add to collage area
        binding.collageArea.addView(imageView);

        // Set as active image
        setActiveImage(imageView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDraggableImage(ImageView imageView) {
        imageView.setOnTouchListener((v, event) -> {
            if (v != activeImageView) {
                // Set this image as active when touched
                setActiveImage((ImageView) v);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Record initial touch position for drag calculation
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    v.bringToFront(); // Bring to front when dragging
                    return true;

                case MotionEvent.ACTION_MOVE:
                    // Calculate new position
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;

                    // Apply bounds checking to keep within container
                    if (newX < 0) newX = 0;
                    if (newY < 0) newY = 0;
                    if (newX + v.getWidth() > binding.collageArea.getWidth()) {
                        newX = binding.collageArea.getWidth() - v.getWidth();
                    }
                    if (newY + v.getHeight() > binding.collageArea.getHeight()) {
                        newY = binding.collageArea.getHeight() - v.getHeight();
                    }

                    // Move the view
                    v.setX(newX);
                    v.setY(newY);
                    return true;

                default:
                    return false;
            }
        });
    }

    private void setActiveImage(ImageView imageView) {
        // Reset border on previous active image (if any)
        if (activeImageView != null) {
            activeImageView.setBackgroundResource(0);
        }

        // Set new active image
        activeImageView = imageView;
        // Add a border to show it's selected - subtle black border to not distract
        activeImageView.setBackground(createHighlightBorder());
    }

    private android.graphics.drawable.GradientDrawable createHighlightBorder() {
        android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(4, Color.BLACK); // Subtle black border
        return border;
    }

    private Bitmap createBitmapFromCollageArea() {

        if (activeImageView != null) {
            activeImageView.setBackgroundResource(0);
        }

        View gridBackground = binding.collageArea.findViewWithTag("gridBackground");
        int gridVisibility = View.VISIBLE;
        if (gridBackground != null) {
            gridVisibility = gridBackground.getVisibility();
            gridBackground.setVisibility(View.INVISIBLE);
        }

        if (binding.collageArea.getWidth() == 0 || binding.collageArea.getHeight() == 0) {
            binding.collageArea.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            binding.collageArea.layout(0, 0, binding.collageArea.getMeasuredWidth(),
                    binding.collageArea.getMeasuredHeight());
        }

        Bitmap bitmap = Bitmap.createBitmap(
                binding.collageArea.getWidth(),
                binding.collageArea.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        binding.collageArea.draw(canvas);

        if (gridBackground != null) {
            gridBackground.setVisibility(gridVisibility);
        }

        return bitmap;
    }

    private Uri saveBitmapToTempFile(Bitmap bitmap) {
        try {
            // Create a file in the cache directory
            File cacheDir = requireContext().getCacheDir();
            File collageFile = new File(cacheDir, "collage_" + UUID.randomUUID().toString() + ".jpg");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(collageFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            // Return URI
            return Uri.fromFile(collageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveAndProceed() {
        if (addedImagesList.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one image to the collage", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap collageBitmap = createBitmapFromCollageArea();

        Uri collageUri = saveBitmapToTempFile(collageBitmap);

        if (collageUri != null) {
            if (getActivity() instanceof UploadActivity) {
                ((UploadActivity) getActivity()).showDetailFragment(collageUri);
            }
        } else {
            Toast.makeText(requireContext(), "Failed to create collage", Toast.LENGTH_SHORT).show();
        }
    }
}