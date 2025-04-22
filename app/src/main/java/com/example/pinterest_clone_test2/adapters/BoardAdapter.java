package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.interfaces.OnBoardClickListener;
import com.example.pinterest_clone_test2.models.Board;

import java.util.List;
import java.util.Locale;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {
    private final Context context;
    private final List<Board> boardList;
    private final OnBoardClickListener onBoardClickListener;

    public BoardAdapter(Context context, List<Board> boardList, OnBoardClickListener listener) {
        this.context = context;
        this.boardList = boardList;
        this.onBoardClickListener = listener;
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFirst, ivSecond, ivThird;
        TextView tvTitle, tvPins;

        public BoardViewHolder(View itemView) {
            super(itemView);
            ivFirst = itemView.findViewById(R.id.iv_firstPic);
            ivSecond = itemView.findViewById(R.id.iv_secondPic);
            ivThird = itemView.findViewById(R.id.iv_thirdPic);
            tvTitle = itemView.findViewById(R.id.tv_board_title);
            tvPins = itemView.findViewById(R.id.tv_number_of_pins);
        }
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.board_item, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BoardViewHolder holder, int position) {
        Board board = boardList.get(position);

        holder.tvTitle.setText(board.getName());

        int pinSize = board.getPinsObj() != null ? board.getPinsObj().size() : 0;

        if (pinSize > 1) {
            holder.tvPins.setText(String.format(Locale.US, "%d %s", board.getPinsObj().size(), context.getResources().getString(R.string.pins).toLowerCase()));
        } else {
            holder.tvPins.setText(String.format(Locale.US, "%d %s", board.getPinsObj().size(), context.getResources().getString(R.string.pin).toLowerCase()));
        }

        if (pinSize > 0) {
            Glide.with(context).load(board.getPinsObj().get(0).getThumbnailUrl()).into(holder.ivFirst);
        }
        if (pinSize > 1) {
            Glide.with(context).load(board.getPinsObj().get(1).getThumbnailUrl()).into(holder.ivSecond);
        }
        if (pinSize > 2) {
            Glide.with(context).load(board.getPinsObj().get(2).getThumbnailUrl()).into(holder.ivThird);
        }
        holder.itemView.setOnClickListener(v -> onBoardClickListener.onBoardClick(board));
    }

    @Override
    public int getItemCount() {
        return boardList != null ? boardList.size() : 0;
    }
}

