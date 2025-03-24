package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    // Constructor với interface callback
    public ImageAdapter(ArrayList<Uri> imageList, Context context, OnImageSelectedListener onImageSelectedListener) {
        this.imageList = imageList;
        this.context = context;
        this.onImageSelectedListener = onImageSelectedListener;  // Gán listener
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

        // Set an OnClickListener on the image to trigger the callback
        holder.imageItemView.setOnClickListener(v -> onImageSelectedListener.onImageSelected(currentImageUri));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageItemView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageItemView = itemView.findViewById(R.id.imageItemView);
        }
    }

    // Define an interface for image selection callback
    public interface OnImageSelectedListener {
        void onImageSelected(Uri imageUri);
    }
}