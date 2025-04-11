package com.example.pinterest_clone_test2.ui.board.board_choosing;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.PinSavingBoardItemViewHolderBinding;
import com.example.pinterest_clone_test2.databinding.PinSavingHeaderViewHolderBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class BoardItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        PinSavingHeaderViewHolderBinding binding;

        public HeaderViewHolder(PinSavingHeaderViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        PinSavingBoardItemViewHolderBinding binding;

        public BoardViewHolder(PinSavingBoardItemViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private final List<BaseItem> items;
    public static int VIEW_TYPE_HEADER = 0;
    public static int VIEW_TYPE_BOARD = 1;
    private final ItemClickListener listener;
    private final String pinId;

    public BoardItemAdapter(List<BaseItem> items, ItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        pinId = null;
    }

    public BoardItemAdapter(List<BaseItem> items, ItemClickListener listener, String pinId) {
        this.items = items;
        this.listener = listener;
        this.pinId = pinId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_HEADER) {
            PinSavingHeaderViewHolderBinding binding = PinSavingHeaderViewHolderBinding.inflate(inflater, parent, false);
            vh = new HeaderViewHolder(binding);
        } else {
            PinSavingBoardItemViewHolderBinding binding = PinSavingBoardItemViewHolderBinding.inflate(inflater, parent, false);
            vh = new BoardViewHolder(binding);
        }

        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseItem item = items.get(position);
        if (holder instanceof BoardItemAdapter.BoardViewHolder) {
            BoardViewHolder vh = (BoardViewHolder) holder;
            BoardItem boardItem = (BoardItem) item;
            Board board = boardItem.getBoard();

            vh.binding.tvBoardTitle.setText(boardItem.getBoard().getName());
            vh.itemView.setOnClickListener(v -> listener.OnClick(boardItem.getBoard(), boardItem.isNew()));

            // null id is for profile
            if (board.getId() == null) {
                DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
                if (currentUserDocument != null) {
                    RequestOptions glideOptions = new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .fitCenter();

                    Glide.with(vh.binding.ivBoardImage.getContext())
                            .load(currentUserDocument.getString("avatarUrl"))
                            .apply(glideOptions)
                            .into(vh.binding.ivBoardImage);
                }

                vh.binding.ivBoardImage.setBackgroundColor(Color.TRANSPARENT);
            } else {
                vh.binding.ivBoardImage.setImageResource(0);
                vh.binding.ivBoardImage.setBackgroundColor(ContextCompat.getColor(vh.itemView.getContext(), R.color.dark_grey));
            }

            // toggle saved icon if pin is already in the board
            if (pinId != null && board.getPins() != null) {
                List<String> pinIds = board.getPins();
                if (pinIds.contains(pinId)) {
                    vh.binding.ivSavedIcon.setVisibility(View.VISIBLE);
                    vh.itemView.setOnClickListener(null);
                } else {
                    vh.binding.ivSavedIcon.setVisibility(View.GONE);
                }

                // fetch pin image because why  not
                if (!pinIds.isEmpty()) {
                    FirebasePinService.fetchPinsFromIds(pinIds.subList(0, 1), new FirebasePinService.OnPinsFetchedFromIdsCallback() {
                        @Override
                        public void onSuccess(List<Pin> pins) {
                            Pin theOnlyPin = pins.get(0);
                            RequestOptions glideOptions = new RequestOptions()
                                    .placeholder(R.drawable.ic_loading)
                                    .centerCrop();

                            Glide.with(vh.binding.ivBoardImage.getContext())
                                    .load(theOnlyPin.getThumbnailUrl())
                                    .apply(glideOptions)
                                    .into(vh.binding.ivBoardImage);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("BoardItemAdapter", "Could not fetch pins from board");
                            e.printStackTrace();
                        }
                    });
                }
            } else {
                vh.binding.ivSavedIcon.setVisibility(View.GONE);
            }
        } else {
            HeaderViewHolder vh = (HeaderViewHolder) holder;
            HeaderItem headerItem = (HeaderItem) item;
            vh.binding.tvHeader.setText(headerItem.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        if (items != null)
            return items.size();
        return 0;
    }

    public interface ItemClickListener {
        void OnClick(Board board, boolean isNew);
    }
}
