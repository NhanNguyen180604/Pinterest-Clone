package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final ArrayList<Uri> mediaList;
    private final Context context;
    private final OnMediaSelectedListener onMediaSelectedListener;

    // Constructor with OnMediaSelectedListener
    public ImageAdapter(ArrayList<Uri> mediaList, Context context, OnMediaSelectedListener onMediaSelectedListener) {
        this.mediaList = mediaList;
        this.context = context;
        this.onMediaSelectedListener = onMediaSelectedListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each media item
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_upload_item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Uri currentUri = mediaList.get(position);

        // Get MIME type of the current media (image, gif, or video)
        String mimeType = context.getContentResolver().getType(currentUri);

        if (mimeType != null) {
            if (mimeType.startsWith("image") || mimeType.contains("gif")) {
                // For images or GIFs, load them into the ImageView
                Glide.with(holder.mediaItemView.getContext())
                        .load(currentUri)
                        .centerCrop()
                        .into(holder.mediaItemView);  // Display the image or gif

                holder.videoView.setVisibility(View.GONE);  // Hide VideoView
                holder.mediaItemView.setVisibility(View.VISIBLE);  // Show ImageView
            } else if (mimeType.startsWith("video")) {
                // For videos, hide the ImageView and display VideoView
                holder.mediaItemView.setVisibility(View.GONE);  // Hide ImageView
                holder.videoView.setVisibility(View.VISIBLE);  // Show VideoView

                // Set the video URI to the VideoView and start playing it
                holder.videoView.setVideoURI(currentUri);
                holder.videoView.setOnPreparedListener(mp -> holder.videoView.start());  // Start the video once it's ready

                holder.videoView.setOnCompletionListener(mp -> {
                    // Hide VideoView after completion
                    holder.videoView.setVisibility(View.GONE);
                    // Optionally, show a thumbnail or reset other UI elements after the video completes
                    holder.mediaItemView.setVisibility(View.VISIBLE);  // Show ImageView again if needed
                });
            }
        }

        // Set click listener to handle media selection
        holder.itemView.setOnClickListener(v -> onMediaSelectedListener.onMediaSelected(currentUri));
    }



    @Override
    public int getItemCount() {
        return mediaList.size();  // Return the size of the media list
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaItemView; // To display image or gif
        VideoView videoView; // To display video

        public ImageViewHolder(View itemView) {
            super(itemView);
            mediaItemView = itemView.findViewById(R.id.mediaItemView);
            videoView = itemView.findViewById(R.id.videoView);
        }
    }

    // Interface for media selection callback
    public interface OnMediaSelectedListener {
        void onMediaSelected(Uri mediaUri);
    }
}
