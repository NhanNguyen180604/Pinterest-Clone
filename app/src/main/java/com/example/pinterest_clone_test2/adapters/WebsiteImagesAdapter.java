package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.pinterest_clone_test2.R;

import java.util.ArrayList;

public class WebsiteImagesAdapter extends RecyclerView.Adapter<WebsiteImagesAdapter.ImageViewHolder> {

    private final ArrayList<String> imageUrls;
    private final Context context;
    private final OnImageSelectedListener listener;

    public interface OnImageSelectedListener {
        void onImageSelected(String imageUrl);
    }

    public WebsiteImagesAdapter(ArrayList<String> imageUrls, Context context, OnImageSelectedListener listener) {
        this.imageUrls = imageUrls;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.upload_website_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Show loading indicator
        holder.progressBar.setVisibility(View.VISIBLE);
        // Load image with Glide
        Glide.with(context)
                .load(imageUrl)
                // Replace deprecated thumbnail() with a more modern approach
                .sizeMultiplier(0.1f)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(new RequestOptions()
                        .centerCrop()
                        .timeout(10000)) // 10 seconds timeout
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_no_effect)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@NonNull GlideException e, Object model,
                                                @NonNull Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull android.graphics.drawable.Drawable resource, @NonNull Object model,
                                                   @NonNull Target<android.graphics.drawable.Drawable> target,
                                                   @NonNull DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imageView);

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageSelected(imageUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    // Make ImageViewHolder public static to match its usage scope
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
        CardView cardView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_preview);
            progressBar = itemView.findViewById(R.id.image_loading_progress);
            cardView = itemView.findViewById(R.id.image_card);
        }
    }
}