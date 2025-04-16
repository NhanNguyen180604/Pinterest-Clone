package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final ArrayList<Uri> imageList;
    private final Context context;
    private final OnImageSelectedListener onImageSelectedListener;
    private final boolean isSelectedImageList; // Flag to indicate if this adapter is for selected images

    // Constructor with callback interface and flag for selected images
    public ImageAdapter(ArrayList<Uri> imageList, Context context, OnImageSelectedListener onImageSelectedListener) {
        this(imageList, context, onImageSelectedListener, false);
    }

    // New constructor with isSelectedImageList flag
    public ImageAdapter(ArrayList<Uri> imageList, Context context, OnImageSelectedListener onImageSelectedListener, boolean isSelectedImageList) {
        this.imageList = imageList;
        this.context = context;
        this.onImageSelectedListener = onImageSelectedListener;
        this.isSelectedImageList = isSelectedImageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_upload_item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Uri currentImageUri = imageList.get(position);

        // Use Glide to load the image into ImageView in RecyclerView
        Glide.with(holder.imageItemView.getContext())
                .load(currentImageUri)
                .centerCrop()
                .into(holder.imageItemView);

        // Show remove button only for selected images
        if (isSelectedImageList && holder.btnRemoveSelectedImage != null) {
            holder.btnRemoveSelectedImage.setVisibility(View.VISIBLE);
            holder.btnRemoveSelectedImage.setOnClickListener(v -> {
                if (onImageSelectedListener != null) {
                    onImageSelectedListener.onImageDeselected(currentImageUri);
                }
            });
        } else if (holder.btnRemoveSelectedImage != null) {
            holder.btnRemoveSelectedImage.setVisibility(View.GONE);
        }

        // Set onClickListener to select image
        holder.imageItemView.setOnClickListener(v -> {
            if (onImageSelectedListener != null) {
                if (isSelectedImageList) {
                    // If this is a selected image, deselect it when clicked
                    onImageSelectedListener.onImageDeselected(currentImageUri);
                } else {
                    // Otherwise, select it
                    onImageSelectedListener.onImageSelected(currentImageUri);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageItemView;
        ImageButton btnRemoveSelectedImage;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageItemView = itemView.findViewById(R.id.imageItemView);
            btnRemoveSelectedImage = itemView.findViewById(R.id.btnRemoveSelectedImage);
        }
    }

    // Define an interface for image selection callback
    public interface OnImageSelectedListener {
        void onImageSelected(Uri imageUri);
        void onImageDeselected(Uri imageUri);
    }
}