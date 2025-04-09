package com.example.pinterest_clone_test2.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class PinListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static class PinImageViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;

        public PinImageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.image_view_holder);
        }
    }

    public static class PinVideoViewHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        ImageView iv;

        public PinVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.video_view);
            iv = itemView.findViewById(R.id.image_view_holder);
        }
    }

    List<Pin> pins;
    PinClickListener listener;
    DocumentSnapshot currentUserDocument;
    List<String> blockedPins = null;
    List<String> blockedUsers = null;
    final int VIEW_TYPE_IMAGE = 1;
    final int VIEW_TYPE_VIDEO = 2;

    public PinListAdapter(List<Pin> pins, PinClickListener listener) {
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
                Log.d("PinListAdapter", "blocked");
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
            // TODO: fix this stupidity
            if (isBlocked(pin)) {
                Log.d("PinListAdapter", "blocked");
                vh.iv.setVisibility(View.VISIBLE);
                vh.videoView.setVisibility(View.GONE);
                Glide.with(vh.itemView.getContext())
                        .load(R.drawable.hidden_image)
                        .fitCenter()
                        .apply(options)
                        .into(vh.iv);
                return;
            }

            //TODO: load video
        }

        holder.itemView.setOnClickListener(v -> listener.OnClick(holder.getBindingAdapterPosition(), v));
    }

    @Override
    public int getItemCount() {
        if (pins != null)
            return pins.size();
        return 0;
    }
}
