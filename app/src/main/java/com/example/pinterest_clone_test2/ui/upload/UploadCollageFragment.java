package com.example.pinterest_clone_test2.ui.upload;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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
import com.example.pinterest_clone_test2.UploadActivity;
import com.example.pinterest_clone_test2.databinding.FragmentUploadCollageBinding;
import com.example.pinterest_clone_test2.interfaces.ScaleListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    private PointF startPoint = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private Stack<CollageAction> undoStack = new Stack<>();
    private Stack<CollageAction> redoStack = new Stack<>();
    private static final int MAX_STACK_SIZE = 20;

    private float initialX, initialY, initialScaleX, initialScaleY;

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
            public boolean onScale(ScaleGestureDetector detector) {
                return UploadCollageFragment.this.onScale(detector, activeImageView, MIN_SCALE, MAX_SCALE);
            }
        });
        setupCollageArea();
        setupButtonListeners();
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

    private void setupButtonListeners() {
        binding.btnExit.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.btnNext.setOnClickListener(v -> saveAndProceed());

        binding.btnAddImage.setOnClickListener(v -> openGallery());

        binding.btnBrush.setOnClickListener(v -> Toast.makeText(requireContext(), "Brush feature coming soon", Toast.LENGTH_SHORT).show());
        binding.btnText.setOnClickListener(v -> Toast.makeText(requireContext(), "Text feature coming soon", Toast.LENGTH_SHORT).show());
        binding.btnAddItem.setOnClickListener(v -> Toast.makeText(requireContext(), "Add item feature coming soon", Toast.LENGTH_SHORT).show());
        binding.btnGrid.setOnClickListener(v -> Toast.makeText(requireContext(), "Grid feature coming soon", Toast.LENGTH_SHORT).show());

        binding.btnUndo.setOnClickListener(v -> performUndo());
        binding.btnRedo.setOnClickListener(v -> performRedo());

        binding.btnUndo.setAlpha(0.5f);
        binding.btnUndo.setEnabled(false);
        binding.btnRedo.setAlpha(0.5f);
        binding.btnRedo.setEnabled(false);
    }

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

        int childCount = binding.collageArea.getChildCount();
        int offset = childCount * 20;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageSize, imageSize);
        params.leftMargin = 40 + (offset % (containerWidth - imageSize - 40));
        params.topMargin = 40 + (offset / 80) * 40;

        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setTag(imageUri);

        Glide.with(requireContext()).load(imageUri).centerCrop().into(imageView);

        setupDraggableZoomableImage(imageView);
        binding.collageArea.addView(imageView);
        setActiveImage(imageView);

        return imageView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDraggableZoomableImage(ImageView imageView) {
        imageView.setOnTouchListener((v, event) -> {
            if (v != activeImageView) {
                setActiveImage((ImageView) v);
            }
            scaleGestureDetector.onTouchEvent(event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_DRAG;
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    v.bringToFront();
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
                            Matrix matrix = new Matrix();
                            float currentScale = ((ImageView) v).getScaleX();
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
        }

        updateUndoRedoButtonStates();

        if (addedImagesList.isEmpty()) {
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
        }

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