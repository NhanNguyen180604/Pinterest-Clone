package com.example.pinterest_clone_test2.ui.pin.btn_save;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinterest_clone_test2.databinding.PinSavingBoardItemViewHolderBinding;
import com.example.pinterest_clone_test2.databinding.PinSavingHeaderViewHolderBinding;
import com.example.pinterest_clone_test2.models.Board;

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

    public BoardItemAdapter(List<BaseItem> items, ItemClickListener listener) {
        this.items = items;
        this.listener = listener;
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
            vh.binding.tvBoardTitle.setText(boardItem.getBoard().getName());
            vh.itemView.setOnClickListener(v -> {
                listener.OnClick(boardItem.getBoard(), boardItem.isNew());
            });
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

    public interface ItemClickListener{
        void OnClick(Board board, boolean isNew);
    }
}
