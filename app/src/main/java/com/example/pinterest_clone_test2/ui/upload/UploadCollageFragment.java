package com.example.pinterest_clone_test2.ui.upload;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.UploadActivity;
import com.example.pinterest_clone_test2.databinding.FragmentUploadCollageBinding;
import com.example.pinterest_clone_test2.interfaces.ScaleListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;
import java.util.UUID;

public class UploadCollageFragment extends Fragment implements ScaleListener {
    private FragmentUploadCollageBinding binding;
    private ArrayList<Uri> addedImagesList;
    private ImageView activeImageView;
    private float dX, dY;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 3.0f;

    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;
    private int mode = MODE_NONE;

    private final PointF startPoint = new PointF();
    private final PointF mid = new PointF();
    private float oldDist = 1f;

    private final Stack<CollageAction> undoStack = new Stack<>();
    private final Stack<CollageAction> redoStack = new Stack<>();
    private static final int MAX_STACK_SIZE = 20;

    private float initialX, initialY, initialScaleX, initialScaleY;

    // Drawing related variables
    private DrawingPathView drawingPathView;

    private boolean isDrawingMode = false;
    private final HashMap<View, Integer> viewZIndexMap = new HashMap<>();
    private int zIndexCounter = 0;
    private int currentColor = Color.BLACK;
    private float currentStrokeWidth = 8f;
    private Uri currentPhotoUri;

    public UploadCollageFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        scaleGestureDetector = new ScaleGestureDetector(requireContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                return UploadCollageFragment.this.onScale(detector, activeImageView, MIN_SCALE, MAX_SCALE);
            }
        });
        setupCollageArea();
        setupButtonListeners();
        setupDrawingLayer();
        updateUndoRedoButtonStates();
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector, ImageView targetView, float minScale, float maxScale) {
        if (targetView != null) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));

            targetView.setScaleX(scaleFactor);
            targetView.setScaleY(scaleFactor);
            return true;
        }
        return false;
    }

    private void setupCollageArea() {
        addGridBackground();
        binding.collageArea.setVisibility(View.VISIBLE);
    }

    private void addGridBackground() {
        View gridBackground = new View(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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

        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), gridBitmap);
        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        gridBackground.setBackground(bitmapDrawable);
        binding.collageArea.setBackgroundColor(Color.WHITE);
        binding.collageArea.addView(gridBackground, 0);
    }

    private void setupDrawingLayer() {
        drawingPathView = new DrawingPathView(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        drawingPathView.setLayoutParams(params);
        drawingPathView.setTag("drawingLayer");
        drawingPathView.setDrawingEnabled(false);

        // Remove any existing drawing layer
        View existingDrawingLayer = binding.collageArea.findViewWithTag("drawingLayer");
        if (existingDrawingLayer != null) {
            binding.collageArea.removeView(existingDrawingLayer);
            // Remove from z-index map if it exists
            viewZIndexMap.remove(existingDrawingLayer);
        }

        viewZIndexMap.put(drawingPathView, zIndexCounter++);

        binding.collageArea.addView(drawingPathView);
    }
    private void setupButtonListeners() {
        binding.btnExit.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnNext.setOnClickListener(v -> saveAndProceed());

        binding.btnAddImage.setOnClickListener(v -> openGallery());

        binding.btnBrush.setOnClickListener(v -> toggleDrawingMode());
        binding.btnText.setOnClickListener(v -> Toast.makeText(requireContext(), "Text feature coming soon", Toast.LENGTH_SHORT).show());
        binding.btnAddItem.setOnClickListener(v -> Toast.makeText(requireContext(), "Add item feature coming soon", Toast.LENGTH_SHORT).show());
        binding.btnGrid.setOnClickListener(v -> showMediaOptionsDialog());

        binding.btnUndo.setOnClickListener(v -> performUndo());
        binding.btnRedo.setOnClickListener(v -> performRedo());

        binding.btnUndo.setAlpha(0.5f);
        binding.btnUndo.setEnabled(false);
        binding.btnRedo.setAlpha(0.5f);
        binding.btnRedo.setEnabled(false);
    }

    private void toggleDrawingMode() {
        isDrawingMode = !isDrawingMode;
        drawingPathView.setDrawingEnabled(isDrawingMode);

        if (isDrawingMode) {
            binding.btnBrush.setBackgroundResource(R.drawable.red_button_pinterest);
            showBrushOptionsDialog();
            disableImageSelection();

            if (drawingPathView != null) {
                // When drawing mode is enabled, ensure drawing layer is on top
                binding.collageArea.removeView(drawingPathView);
                // Assign the highest z-index
                viewZIndexMap.put(drawingPathView, zIndexCounter++);
                binding.collageArea.addView(drawingPathView);
            }
        } else {
            binding.btnBrush.setBackground(null);
            TypedValue outValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            binding.btnBrush.setBackgroundResource(outValue.resourceId);
            enableImageSelection();
        }
    }

    private void disableImageSelection() {
        // Hide the active image selection if any
        if (activeImageView != null) {
            activeImageView.setBackgroundResource(0);
            activeImageView = null;
        }

        // Disable touch events on images
        for (int i = 0; i < binding.collageArea.getChildCount(); i++) {
            View child = binding.collageArea.getChildAt(i);
            if (child instanceof ImageView && child.getTag() instanceof Uri) {
                child.setOnTouchListener(null);
            }
        }
    }

    private void enableImageSelection() {
        // Re-enable touch events on images
        for (int i = 0; i < binding.collageArea.getChildCount(); i++) {
            View child = binding.collageArea.getChildAt(i);
            if (child instanceof ImageView && child.getTag() instanceof Uri) {
                setupDraggableZoomableImage((ImageView) child);
            }
        }
    }

    private void showBrushOptionsDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Create the dialog view programmatically
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.WHITE);

        // Add title
        TextView titleText = new TextView(requireContext());
        titleText.setText(R.string.brush_options);
        titleText.setTextSize(18);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setPadding(0, 0, 0, 20);
        layout.addView(titleText);

        // Create basic colors section
        TextView basicColorsTitle = new TextView(requireContext());
        basicColorsTitle.setText(R.string.basic_colors);
        basicColorsTitle.setTextSize(14);
        layout.addView(basicColorsTitle);

        // Basic color grid in horizontal scrollview
        HorizontalScrollView basicColorsScroll = new HorizontalScrollView(requireContext());
        LinearLayout basicColorsLayout = new LinearLayout(requireContext());
        basicColorsLayout.setOrientation(LinearLayout.HORIZONTAL);
        basicColorsScroll.addView(basicColorsLayout);
        layout.addView(basicColorsScroll);

        // Add standard colors
        int[] basicColors = new int[]{
                Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.WHITE,
                Color.RED, Color.rgb(255, 128, 0), Color.YELLOW,
                Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA,
                Color.rgb(128, 0, 0), Color.rgb(128, 64, 0), Color.rgb(128, 128, 0),
                Color.rgb(0, 128, 0), Color.rgb(0, 128, 128), Color.rgb(0, 0, 128), Color.rgb(128, 0, 128)
        };

        for (int color : basicColors) {
            ImageButton colorButton = createColorButton(color, dialog);
            basicColorsLayout.addView(colorButton);
        }

        // Add custom color picker section
        TextView customColorTitle = new TextView(requireContext());
        customColorTitle.setText(R.string.custom_color);
        customColorTitle.setTextSize(14);
        customColorTitle.setPadding(0, 20, 0, 10);
        layout.addView(customColorTitle);

        // RGB sliders for custom color
        LinearLayout customColorLayout = new LinearLayout(requireContext());
        customColorLayout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(customColorLayout);

        final int[] rgb = new int[]{255, 0, 0}; // Initial red color

        final View colorPreview = new View(requireContext());
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(80, 80);
        previewParams.gravity = Gravity.CENTER_HORIZONTAL;
        previewParams.bottomMargin = 20;
        colorPreview.setLayoutParams(previewParams);
        GradientDrawable previewShape = new GradientDrawable();
        previewShape.setShape(GradientDrawable.OVAL);
        previewShape.setColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
        previewShape.setStroke(2, Color.BLACK);
        colorPreview.setBackground(previewShape);
        customColorLayout.addView(colorPreview);

        // RGB sliders
        String[] rgbLabels = {"Red", "Green", "Blue"};
        for (int i = 0; i < 3; i++) {
            final int index = i;
            LinearLayout sliderRow = new LinearLayout(requireContext());
            sliderRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = new TextView(requireContext());
            label.setText(rgbLabels[i]);
            label.setMinWidth(60);
            sliderRow.addView(label);

            SeekBar rgbSeekBar = new SeekBar(requireContext());
            rgbSeekBar.setMax(255);
            rgbSeekBar.setProgress(rgb[i]);
            rgbSeekBar.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            final TextView valueText = new TextView(requireContext());
            valueText.setText(String.valueOf(rgb[i]));
            valueText.setMinWidth(40);

            rgbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    rgb[index] = progress;
                    valueText.setText(String.valueOf(progress));
                    int newColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
                    ((GradientDrawable) colorPreview.getBackground()).setColor(newColor);
                    colorPreview.invalidate();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            sliderRow.addView(rgbSeekBar);
            sliderRow.addView(valueText);
            customColorLayout.addView(sliderRow);
        }


        // Create stroke width section
        TextView strokeWidthTitle = new TextView(requireContext());
        strokeWidthTitle.setText(R.string.stroke_width);
        strokeWidthTitle.setTextSize(14);
        strokeWidthTitle.setPadding(0, 20, 0, 10);
        layout.addView(strokeWidthTitle);

        // Add preview of current stroke width
        final View strokePreview = new View(requireContext());
        LinearLayout.LayoutParams strokePreviewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 40);
        strokePreview.setLayoutParams(strokePreviewParams);
        GradientDrawable strokeShape = new GradientDrawable();
        strokeShape.setShape(GradientDrawable.RECTANGLE);
        strokeShape.setColor(Color.BLACK);
        strokePreview.setBackground(strokeShape);
        layout.addView(strokePreview);

        // Create stroke width slider
        SeekBar strokeWidthSeekBar = new SeekBar(requireContext());
        strokeWidthSeekBar.setMax(50);
        strokeWidthSeekBar.setProgress((int) currentStrokeWidth);
        strokeWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentStrokeWidth = Math.max(1, progress);
                drawingPathView.setStrokeWidth(currentStrokeWidth);

                // Update preview
                ViewGroup.LayoutParams params = strokePreview.getLayoutParams();
                params.height = (int) (currentStrokeWidth * 1.5);
                strokePreview.setLayoutParams(params);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(strokeWidthSeekBar);

        // Done button
        Button doneButton = new Button(requireContext());
        doneButton.setText(R.string.done);
        LinearLayout.LayoutParams doneButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        doneButtonParams.topMargin = 20;
        doneButton.setLayoutParams(doneButtonParams);
        doneButton.setOnClickListener(v ->{
                int customColor = Color.rgb(rgb[0], rgb[1], rgb[2]);
                currentColor = customColor;
                drawingPathView.setColor(customColor);
                dialog.dismiss();
                });
        layout.addView(doneButton);

        dialog.setContentView(layout);
        dialog.show();
    }

    // Helper method to create color selection buttons
    private ImageButton createColorButton(int color, final Dialog dialog) {
        ImageButton colorButton = new ImageButton(requireContext());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(80, 80);
        buttonParams.setMargins(10, 10, 10, 10);
        colorButton.setLayoutParams(buttonParams);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        shape.setStroke(2, Color.BLACK);
        colorButton.setBackground(shape);

        colorButton.setOnClickListener(v -> {
            currentColor = color;
            drawingPathView.setColor(color);
            dialog.dismiss();
        });

        return colorButton;
    }
    private void showMediaOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialog);
        dialog.setContentView(R.layout.dialog_media_options);

        ImageButton btnCamera = dialog.findViewById(R.id.btnCamera);
        if (btnCamera != null) btnCamera.setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });

        dialog.show();
    }


    // Cần check quyền camera
    private void openCamera() {
        File photoFile;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            photoFile = File.createTempFile(
                    "JPEG_" + timeStamp + "_",
                    ".jpg",
                    storageDir
            );

            currentPhotoUri = Uri.fromFile(photoFile);
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                photoFile);

        takePictureLauncher.launch(photoURI);
    }
    // Add this launcher to the class
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    // Photo was taken successfully
                    ImageView addedImage = addImageToCollage(currentPhotoUri);
                    addedImagesList.add(currentPhotoUri);

                    recordAddImageAction(addedImage, currentPhotoUri);

                    binding.btnNext.setEnabled(true);
                    redoStack.clear();
                    updateUndoRedoButtonStates();
                }
            }
    );
    private void openGallery() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        if (uri != null) {
            ImageView addedImage = addImageToCollage(uri);
            addedImagesList.add(uri);

            recordAddImageAction(addedImage, uri);

            binding.btnNext.setEnabled(true);
            redoStack.clear();
            updateUndoRedoButtonStates();
        }
    });

    private ImageView addImageToCollage(Uri imageUri) {
        ImageView imageView = new ImageView(requireContext());
        int containerWidth = binding.collageArea.getWidth();
        if (containerWidth <= 0) containerWidth = binding.collageArea.getMeasuredWidth();
        if (containerWidth <= 0) containerWidth = 600;

        int imageSize = containerWidth / 2;

        // Count only image views instead of all children
        int imageCount = 0;
        for (int i = 0; i < binding.collageArea.getChildCount(); i++) {
            View child = binding.collageArea.getChildAt(i);
            if (child instanceof ImageView && child.getTag() instanceof Uri) {
                imageCount++;
            }
        }
        int offset = imageCount * 20;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageSize, imageSize);
        params.leftMargin = 40 + (offset % (containerWidth - imageSize - 40));
        params.topMargin = 40 + (offset / 80) * 40;

        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setTag(imageUri);

        // Assign the highest z-index to this new image
        int newZIndex = zIndexCounter++;
        viewZIndexMap.put(imageView, newZIndex);

        // Simply add the view at the top - no need to reorder
        binding.collageArea.addView(imageView);

        Glide.with(requireContext()).load(imageUri).centerCrop().into(imageView);
        setupDraggableZoomableImage(imageView);
        setActiveImage(imageView);

        return imageView;
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupDraggableZoomableImage(ImageView imageView) {
        imageView.setOnTouchListener((v, event) -> {
            if (isDrawingMode) {
                return false;
            }

            if (v != activeImageView) {
                setActiveImage((ImageView) v);
            }
            scaleGestureDetector.onTouchEvent(event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_DRAG;
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    startPoint.set(event.getX(), event.getY());

                    initialX = v.getX();
                    initialY = v.getY();
                    initialScaleX = v.getScaleX();
                    initialScaleY = v.getScaleY();

                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = MODE_ZOOM;
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        midPoint(mid, event);
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (mode == MODE_DRAG) {
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        if (newX < 0) newX = 0;
                        if (newY < 0) newY = 0;
                        if (newX + v.getWidth() > binding.collageArea.getWidth()) {
                            newX = binding.collageArea.getWidth() - v.getWidth();
                        }
                        if (newY + v.getHeight() > binding.collageArea.getHeight()) {
                            newY = binding.collageArea.getHeight() - v.getHeight();
                        }

                        v.setX(newX);
                        v.setY(newY);
                    } else if (mode == MODE_ZOOM && event.getPointerCount() >= 2) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            float scale = newDist / oldDist;
                            float currentScale = v.getScaleX();
                            float newScale = currentScale * scale;

                            if (newScale > MIN_SCALE && newScale < MAX_SCALE) {
                                v.setScaleX(newScale);
                                v.setScaleY(newScale);
                            }

                            oldDist = newDist;
                        }
                    }
                    return true;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = MODE_NONE;
                    return true;

                case MotionEvent.ACTION_UP:
                    if (initialX != v.getX() || initialY != v.getY() || initialScaleX != v.getScaleX() || initialScaleY != v.getScaleY()) {

                        recordTransformAction((ImageView) v, initialX, initialY, initialScaleX, initialScaleY);

                        redoStack.clear();
                        updateUndoRedoButtonStates();
                    }

                    mode = MODE_NONE;
                    return true;

                default:
                    return false;
            }
        });
    }

    private void recordAddImageAction(ImageView imageView, Uri imageUri) {
        CollageAction action = new CollageAction(CollageActionType.ADD_IMAGE, imageView, imageUri, 0, 0, 0, 0);

        addToUndoStack(action);
    }

    private void recordTransformAction(ImageView imageView, float oldX, float oldY, float oldScaleX, float oldScaleY) {
        CollageAction action = new CollageAction(CollageActionType.TRANSFORM, imageView, (Uri) imageView.getTag(), oldX, oldY, oldScaleX, oldScaleY);

        addToUndoStack(action);
    }

    private void recordDrawPathAction(DrawingPathView.DrawnPath drawnPath) {
        CollageAction action = new CollageAction(CollageActionType.DRAW_PATH, drawnPath);
        addToUndoStack(action);
    }

    private void addToUndoStack(CollageAction action) {
        if (undoStack.size() >= MAX_STACK_SIZE) {
            undoStack.remove(0);
        }

        undoStack.push(action);
        updateUndoRedoButtonStates();
    }

    private void performUndo() {
        if (undoStack.isEmpty()) {
            return;
        }

        CollageAction action = undoStack.pop();

        switch (action.getType()) {
            case ADD_IMAGE:
                binding.collageArea.removeView(action.getImageView());
                addedImagesList.remove(action.getImageUri());

                if (activeImageView == action.getImageView()) {
                    activeImageView = null;
                }

                redoStack.push(action);
                break;

            case TRANSFORM:
                CollageAction redoAction = new CollageAction(CollageActionType.TRANSFORM, action.getImageView(), action.getImageUri(), action.getImageView().getX(), action.getImageView().getY(), action.getImageView().getScaleX(), action.getImageView().getScaleY());

                action.getImageView().setX(action.getOldX());
                action.getImageView().setY(action.getOldY());
                action.getImageView().setScaleX(action.getOldScaleX());
                action.getImageView().setScaleY(action.getOldScaleY());

                redoStack.push(redoAction);
                break;

            case DRAW_PATH:
                drawingPathView.undoLastPath();
                redoStack.push(action);
                break;
        }

        // Ensure z-index ordering is maintained
        sortViewsByZIndex();

        updateUndoRedoButtonStates();

        if (addedImagesList.isEmpty() && drawingPathView.isDrawingEnabled()) {
            binding.btnNext.setEnabled(true);
        } else if (addedImagesList.isEmpty()) {
            binding.btnNext.setEnabled(false);
        }
    }

    private void performRedo() {
        if (redoStack.isEmpty()) {
            return;
        }

        CollageAction action = redoStack.pop();

        switch (action.getType()) {
            case ADD_IMAGE:
                // Add with the original z-index
                int zIndex = viewZIndexMap.getOrDefault(action.getImageView(), zIndexCounter++);
                viewZIndexMap.put(action.getImageView(), zIndex);
                binding.collageArea.addView(action.getImageView());
                addedImagesList.add(action.getImageUri());

                binding.btnNext.setEnabled(true);

                undoStack.push(action);
                break;

            case TRANSFORM:
                CollageAction undoAction = new CollageAction(CollageActionType.TRANSFORM, action.getImageView(), action.getImageUri(), action.getImageView().getX(), action.getImageView().getY(), action.getImageView().getScaleX(), action.getImageView().getScaleY());

                action.getImageView().setX(action.getOldX());
                action.getImageView().setY(action.getOldY());
                action.getImageView().setScaleX(action.getOldScaleX());
                action.getImageView().setScaleY(action.getOldScaleY());

                undoStack.push(undoAction);
                break;

            case DRAW_PATH:
                drawingPathView.redoPath(action.getDrawnPath());
                undoStack.push(action);
                break;
        }

        // Ensure z-index ordering is maintained
        sortViewsByZIndex();

        updateUndoRedoButtonStates();
    }

    private void updateUndoRedoButtonStates() {
        if (undoStack.isEmpty()) {
            binding.btnUndo.setAlpha(0.5f);
            binding.btnUndo.setEnabled(false);
        } else {
            binding.btnUndo.setAlpha(1.0f);
            binding.btnUndo.setEnabled(true);
        }

        if (redoStack.isEmpty()) {
            binding.btnRedo.setAlpha(0.5f);
            binding.btnRedo.setEnabled(false);
        } else {
            binding.btnRedo.setAlpha(1.0f);
            binding.btnRedo.setEnabled(true);
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void setActiveImage(ImageView imageView) {
        if (activeImageView != null) {
            activeImageView.setBackgroundResource(0);
        }

        activeImageView = imageView;
        scaleFactor = activeImageView.getScaleX();
        activeImageView.setBackground(createHighlightBorder());
    }

    private GradientDrawable createHighlightBorder() {
        android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(4, Color.BLACK);
        return border;
    }

    private Bitmap createBitmapFromCollageArea() {
        // Temporarily disable drawing mode for the export
        boolean wasDrawingEnabled = drawingPathView.isDrawingEnabled();
        drawingPathView.setDrawingEnabled(false);

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
            binding.collageArea.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            binding.collageArea.layout(0, 0, binding.collageArea.getMeasuredWidth(), binding.collageArea.getMeasuredHeight());
        }

        Bitmap bitmap = Bitmap.createBitmap(binding.collageArea.getWidth(), binding.collageArea.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        binding.collageArea.draw(canvas);

        if (gridBackground != null) {
            gridBackground.setVisibility(gridVisibility);
        }

        // Restore drawing mode
        drawingPathView.setDrawingEnabled(wasDrawingEnabled);

        return bitmap;
    }

    private Uri saveBitmapToTempFile(Bitmap bitmap) {
        try {
            File cacheDir = requireContext().getCacheDir();
            File collageFile = new File(cacheDir, "collage_" + UUID.randomUUID().toString() + ".jpg");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(collageFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            return Uri.fromFile(collageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveAndProceed() {
        if (addedImagesList.isEmpty() && !hasDrawings()) {
            Toast.makeText(requireContext(), "Please add at least one image or drawing to the collage", Toast.LENGTH_SHORT).show();
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

    // Method to check if there are any drawings
    private boolean hasDrawings() {
        return drawingPathView != null && drawingPathView.getLastPath() != null;
    }
    @SuppressLint("ClickableViewAccessibility")
    // Override onResume to handle touch events properly when switching between fragments
    @Override
    public void onResume() {
        super.onResume();

        // Sắp xếp lại views theo z-index
        sortViewsByZIndex();

        // Cài đặt listener cho drawing paths để ghi lại các hành động undo
        if (drawingPathView != null) {
            drawingPathView.setOnTouchListener((v, event) -> {
                if (!isDrawingMode) {
                    return false;
                }

                boolean handled = drawingPathView.onTouchEvent(event);

                // Ghi lại hành động vẽ khi ngón tay được nhấc lên
                if (handled && event.getAction() == MotionEvent.ACTION_UP) {
                    DrawingPathView.DrawnPath lastPath = drawingPathView.getLastPath();
                    if (lastPath != null) {
                        recordDrawPathAction(lastPath);
                        redoStack.clear();
                        updateUndoRedoButtonStates();

                        // Enable next button since we have content
                        binding.btnNext.setEnabled(true);
                    }
                }

                return handled;
            });
        }
    }
    private void sortViewsByZIndex() {
        int childCount = binding.collageArea.getChildCount();
        ArrayList<View> sortedViews = new ArrayList<>();

        // Collect all views
        for (int i = 0; i < childCount; i++) {
            sortedViews.add(binding.collageArea.getChildAt(i));
        }

        // Sort views by z-index
        Collections.sort(sortedViews, (v1, v2) -> {
            Integer z1 = viewZIndexMap.getOrDefault(v1, 0);
            Integer z2 = viewZIndexMap.getOrDefault(v2, 0);
            return z1.compareTo(z2);
        });

        // Clear the container
        binding.collageArea.removeAllViews();

        // Add views back in the sorted order
        for (View view : sortedViews) {
            binding.collageArea.addView(view);
        }
    }
}