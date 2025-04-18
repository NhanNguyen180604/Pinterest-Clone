package com.example.pinterest_clone_test2.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.BoardCheckboxItemViewHolderBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.ui.pin.edit.BoardBooleanPair;

import java.util.List;

public class BoardMultipleSelectionAdapter extends RecyclerView.Adapter<BoardMultipleSelectionAdapter.BoardViewHolder> {
    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        BoardCheckboxItemViewHolderBinding binding;
        ExoPlayer exoPlayer;

        public BoardViewHolder(BoardCheckboxItemViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    List<BoardBooleanPair> pairs;

    public BoardMultipleSelectionAdapter(List<BoardBooleanPair> pairs) {
        this.pairs = pairs;
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BoardViewHolder(BoardCheckboxItemViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        BoardBooleanPair pair = pairs.get(position);
        Board board = pair.getBoard();

        holder.binding.tvBoardTitle.setText(board.getName());
        holder.binding.checkbox.setChecked(pair.isIncluded());
        holder.binding.tvBoardTitle.setOnClickListener(v -> {
            pair.setIncluded(!pair.isIncluded());
            holder.binding.checkbox.setChecked(pair.isIncluded());
        });
        holder.binding.checkbox.setOnClickListener(v -> {
            pair.setIncluded(!pair.isIncluded());
            holder.binding.checkbox.setChecked(pair.isIncluded());
        });
        holder.binding.ivBoardImage.setOnClickListener(v -> {
            pair.setIncluded(!pair.isIncluded());
            holder.binding.checkbox.setChecked(pair.isIncluded());
        });

        if (board.getPins() == null || board.getPins().isEmpty()) {
            holder.binding.playerView.setVisibility(View.GONE);
            holder.binding.ivBoardImage.setVisibility(View.VISIBLE);
            Glide.with(holder.binding.ivBoardImage.getContext())
                    .load(R.drawable.ic_loading)
                    .into(holder.binding.ivBoardImage);
            return;
        }

        FirebasePinService.fetchPinsFromIds(board.getPins().subList(0, 1), new FirebasePinService.OnPinsFetchedFromIdsCallback() {
            @Override
            public void onSuccess(List<Pin> pins) {
                Pin theOnlyPin = pins.get(0);
                if (theOnlyPin.getType() == Pin.PinType.VIDEO) {
                    holder.binding.playerView.setVisibility(View.VISIBLE);
                    holder.binding.ivBoardImage.setVisibility(View.GONE);
                } else {
                    holder.binding.playerView.setVisibility(View.GONE);
                    holder.binding.ivBoardImage.setVisibility(View.VISIBLE);
                }

                if (theOnlyPin.getType() == Pin.PinType.IMAGE) {
                    Glide.with(holder.binding.ivBoardImage.getContext())
                            .load(theOnlyPin.getThumbnailUrl())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_loading)
                                    .error(R.drawable.turtle_huh)
                                    .centerCrop())
                            .into(holder.binding.ivBoardImage);
                } else if (theOnlyPin.getType() == Pin.PinType.GIF) {
                    Glide.with(holder.binding.ivBoardImage.getContext())
                            .asGif()
                            .load(theOnlyPin.getThumbnailUrl())
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_loading)
                                    .error(R.drawable.turtle_huh)
                                    .centerCrop())
                            .into(holder.binding.ivBoardImage);
                } else {
                    holder.exoPlayer = new ExoPlayer.Builder(holder.itemView.getContext()).build();
                    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(theOnlyPin.getThumbnailUrl()));
                    holder.exoPlayer.setMediaItem(mediaItem);
                    holder.binding.playerView.setPlayer(holder.exoPlayer);
                    holder.exoPlayer.prepare();
                    holder.exoPlayer.setPlayWhenReady(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EditPinBoardPicking", "Adapter could not fetch pin from board");
                e.printStackTrace();
                holder.binding.playerView.setVisibility(View.GONE);
                holder.binding.ivBoardImage.setVisibility(View.VISIBLE);
                Glide.with(holder.binding.ivBoardImage.getContext())
                        .load(R.drawable.ic_loading)
                        .into(holder.binding.ivBoardImage);
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull BoardViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.exoPlayer != null) {
            holder.exoPlayer.stop();
            holder.exoPlayer.release();
            holder.exoPlayer = null;
        }
    }

    @Override
    public int getItemCount() {
        if (pairs == null)
            return 0;
        return pairs.size();
    }
}
