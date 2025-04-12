package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Locale;

public class PinListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static class PinImageViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;

        public PinImageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.image_view_holder);
        }
    }

    public static class PinVideoViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        ExoPlayer player;
        ImageView iv;
        TextView tvDuration;

        public PinVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.video_view);
            iv = itemView.findViewById(R.id.image_view_holder);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }
    }

    Context context;
    List<Pin> pins;
    PinClickListener listener;
    DocumentSnapshot currentUserDocument;
    List<String> blockedPins = null;
    List<String> blockedUsers = null;
    final int VIEW_TYPE_IMAGE = 1;
    final int VIEW_TYPE_VIDEO = 2;

    public PinListAdapter(Context context, List<Pin> pins, PinClickListener listener) {
        this.context = context;
        this.pins = pins;
        this.listener = listener;
        currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        try {
            blockedPins = (List<String>) currentUserDocument.get("blockedPins");
            blockedUsers = (List<String>) currentUserDocument.get("blockedUsers");
        } catch (Exception e) {
            //eat exception
        }
    }

    boolean isBlocked(Pin pin) {
        if (blockedPins != null) {
            final List<String> finalBlockedPins = blockedPins;
            if (finalBlockedPins.contains(pin.getId()))
                return true;
        }
        if (blockedUsers != null) {
            final List<String> finalBlockedUsers = blockedUsers;
            return finalBlockedUsers.contains(pin.getAuthorId());
        }
        return false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType != VIEW_TYPE_VIDEO) {
            return new PinImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pin_image_view_holder, parent, false));
        }

        return new PinVideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.pin_video_view_holder, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        Pin pin = pins.get(position);

        if (pin.getType() == Pin.PinType.VIDEO)
            return VIEW_TYPE_VIDEO;

        return VIEW_TYPE_IMAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Pin pin = pins.get(position);

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_loading)
                .fitCenter()
                .error(R.drawable.turtle_huh);

        if (holder instanceof PinImageViewHolder) {
            PinImageViewHolder vh = (PinImageViewHolder) holder;
            if (isBlocked(pin)) {
                Glide.with(vh.itemView.getContext())
                        .load(R.drawable.hidden_image)
                        .fitCenter()
                        .apply(options)
                        .into(vh.iv);
                return;
            }

            if (pin.getType() == Pin.PinType.IMAGE) {
                Glide.with(vh.itemView.getContext())
                        .load(pins.get(position).getThumbnailUrl())
                        .fitCenter()
                        .apply(options)
                        .into(vh.iv);
            } else {
                Glide.with(vh.itemView.getContext())
                        .asGif()
                        .load(pins.get(position).getThumbnailUrl())
                        .fitCenter()
                        .apply(options)
                        .into(vh.iv);
            }
        } else {
            PinVideoViewHolder vh = (PinVideoViewHolder) holder;
            if (isBlocked(pin)) {
                vh.playerView.setVisibility(View.GONE);
                Glide.with(vh.itemView.getContext())
                        .load(R.drawable.hidden_image)
                        .fitCenter()
                        .apply(options)
                        .into(vh.iv);
                return;
            }

            Glide.with(vh.itemView.getContext())
                    .load(R.drawable.video_placeholder)
                    .fitCenter()
                    .apply(options)
                    .into(vh.iv);

            if (vh.player == null) {
                vh.player = new ExoPlayer.Builder(context).build();
                vh.playerView.setPlayer(vh.player);
            }

            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(pin.getThumbnailUrl()));
            vh.player.setMediaItem(mediaItem);
            vh.player.prepare();
            vh.player.setPlayWhenReady(false);

            vh.player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    if (playbackState == Player.STATE_READY) {
                        vh.iv.setVisibility(View.GONE);
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

        holder.itemView.setOnClickListener(v -> listener.OnClick(holder.getBindingAdapterPosition(), v));
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof PinVideoViewHolder) {
            PinVideoViewHolder vh = (PinVideoViewHolder) holder;
            vh.player.stop();
            vh.player.release();
            vh.player = null;
        }
    }

    @Override
    public int getItemCount() {
        if (pins != null)
            return pins.size();
        return 0;
    }
}
