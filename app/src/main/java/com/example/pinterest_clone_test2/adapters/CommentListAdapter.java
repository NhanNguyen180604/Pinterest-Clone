package com.example.pinterest_clone_test2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.CommentViewHolderBinding;
import com.example.pinterest_clone_test2.models.Comment;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout layout;
        CommentViewHolderBinding _binding;

        CommentViewHolder(CommentViewHolderBinding binding) {
            super(binding.getRoot());
            _binding = binding;
            layout = _binding.commentLayoutContainer;
        }

        void setComment(Comment comment) {
            _binding.setComment(comment);
            if (comment.getAttachmentUrl() != null) {
                Glide.with(_binding.ivAttachment.getContext())
                        .load(Integer.parseInt(comment.getAttachmentUrl()))
                        .fitCenter()
                        .placeholder(R.drawable.karyl)
                        .into(_binding.ivAttachment);
            } else {
                _binding.ivAttachment.setImageResource(0);
            }
        }

        public void adjustMarginStart() {
            int startMarginDp = (int) layout.getContext().getResources().getDimension(R.dimen.reply_comment_start_margin);
            layout.setPadding(startMarginDp, 0, 0, 0);
        }
    }

    static class SpinnerViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        SpinnerViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.spinner);
        }
    }

    final List<Comment> _comments;

    // The minimum amount of items to have below your current scroll position before loading more.
    private final int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    final static int VIEW_ITEM = 1;
    final static int VIEW_PROGRESS = 2;

    public CommentListAdapter(List<Comment> comments, RecyclerView recyclerView) {
        _comments = comments;

        if (recyclerView.getLayoutManager() != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = layoutManager.getItemCount();
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
//            vh = new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view_holder, parent, false));
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            CommentViewHolderBinding binding = CommentViewHolderBinding.inflate(inflater, parent, false);
            vh = new CommentViewHolder(binding);
        } else {
            vh = new SpinnerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner, parent, false));
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Comment comment = _comments.get(position);
        if (holder instanceof CommentViewHolder) {
            CommentViewHolder vh = (CommentViewHolder) holder;
            if (comment.getReplyCommentId() != null) {
                vh.adjustMarginStart();
            }
            vh.setComment(comment);
        } else {
            ((SpinnerViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemViewType(int position) {
        return _comments.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public int getItemCount() {
        return _comments.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
