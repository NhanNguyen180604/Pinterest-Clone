package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;

import java.util.ArrayList;
import java.util.Locale;

public class MediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<Uri> mediaList;
    private final Context context;
    private final OnMediaSelectedListener onMediaSelectedListener;
    public static int IMAGE_VIEW_HOLDER = 1;
    public static int VIDEO_VIEW_HOLDER = 2;

    // Constructor with OnMediaSelectedListener
    public MediaAdapter(ArrayList<Uri> mediaList, Context context, OnMediaSelectedListener onMediaSelectedListener) {
        this.mediaList = mediaList;
        this.context = context;
        this.onMediaSelectedListener = onMediaSelectedListener;
    }

    @Override
    public int getItemViewType(int position) {
        Uri currentUri = mediaList.get(position);
        String mimeType = context.getContentResolver().getType(currentUri);

        // yolo
        if (mimeType == null) {
            return IMAGE_VIEW_HOLDER;
        }

        if (mimeType.startsWith("image") || mimeType.contains("gif"))
            return IMAGE_VIEW_HOLDER;
        else return VIDEO_VIEW_HOLDER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each media item
        View view;
        if (viewType == IMAGE_VIEW_HOLDER) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_upload_item_image, parent, false);
            return new ImageViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_upload_item_video, parent, false);
            return new VideoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Uri currentUri = mediaList.get(position);

        if (holder instanceof ImageViewHolder) {
            ImageViewHolder vh = (ImageViewHolder) holder;
            // For images or GIFs, load them into the ImageView
            Glide.with(vh.mediaItemView.getContext())
                    .load(currentUri)
                    .centerCrop()
                    .into(vh.mediaItemView);  // Display the image or gif
        } else {
            VideoViewHolder vh = (VideoViewHolder) holder;
            vh.player = new ExoPlayer.Builder(context).build();
            vh.playerView.setPlayer(vh.player);
            vh.playerView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

            MediaItem mediaItem = MediaItem.fromUri(currentUri);
            vh.player.setMediaItem(mediaItem);
            vh.player.prepare();
            vh.player.setPlayWhenReady(false);
            vh.player.pause();

            vh.player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    if (playbackState == Player.STATE_READY) {
                        long durationMs = vh.player.getDuration();
                        if (durationMs > 0) {
                            long durationSeconds = durationMs / 1000;
                            long minutes = (durationSeconds % 3600) / 60;
                            long seconds = durationSeconds % 60;
                            vh.player.removeListener(this);
                            String durationString = String.format(Locale.US, "%02d:%02d", minutes, seconds);
                            vh.tvDuration.setText(durationString);
                        }
                    }
                }
            });
        }

        holder.itemView.setOnClickListener(v -> onMediaSelectedListener.onMediaSelected(currentUri));
    }


    @Override
    public int getItemCount() {
        return mediaList.size();  // Return the size of the media list
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VideoViewHolder) {
            VideoViewHolder vh = (VideoViewHolder) holder;
            vh.player.stop();
            vh.player.release();
            vh.player = null;
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaItemView; // To display image or gif

        public ImageViewHolder(View itemView) {
            super(itemView);
            mediaItemView = itemView.findViewById(R.id.mediaItemView);
        }
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        ExoPlayer player;
        CardView cardView;
        TextView tvDuration;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            cardView = itemView.findViewById(R.id.card_view);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }
    }

    // Interface for media selection callback
    public interface OnMediaSelectedListener {
        void onMediaSelected(Uri mediaUri);
    }
}
