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

public class CollageImageAdapter extends RecyclerView.Adapter<CollageImageAdapter.CollageImageViewHolder> {

    private final ArrayList<Uri> imageList;
    private final Context context;
    private final OnImageClickListener onImageClickListener;

    public CollageImageAdapter(ArrayList<Uri> imageList, Context context, OnImageClickListener onImageClickListener) {
        this.imageList = imageList;
        this.context = context;
        this.onImageClickListener = onImageClickListener;
    }

    @NonNull
    @Override
    public CollageImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_upload_item_image, parent, false);
        return new CollageImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollageImageViewHolder holder, int position) {
        Uri currentImageUri = imageList.get(position);

        // Load image with Glide
        Glide.with(holder.imageView.getContext())
                .load(currentImageUri)
                .centerCrop()
                .into(holder.imageView);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onImageClickListener != null) {
                onImageClickListener.onImageClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class CollageImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public CollageImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageItemView);
        }
    }

    public interface OnImageClickListener {
        void onImageClick(int position);
    }
}