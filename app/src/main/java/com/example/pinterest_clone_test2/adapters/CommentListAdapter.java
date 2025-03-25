package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.CommentReplyingViewHolderBinding;
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

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.karyl)
                    .error(R.drawable.turtle_huh);

            if (comment.getAttachmentUrl() != null) {
                Glide.with(_binding.ivAttachment.getContext())
                        .load(comment.getAttachmentUrl())
                        .fitCenter()
                        .apply(options)
                        .into(_binding.ivAttachment);
            } else if (comment.getAttachmentUri() != null) {
                Glide.with(_binding.ivAttachment.getContext())
                        .load(comment.getAttachmentUri())
                        .fitCenter()
                        .apply(options)
                        .into(_binding.ivAttachment);
            } else {
                _binding.ivAttachment.setImageResource(0);
            }
        }
    }

    static class CommentReplyingViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout layout;
        CommentReplyingViewHolderBinding _binding;

        CommentReplyingViewHolder(CommentReplyingViewHolderBinding binding) {
            super(binding.getRoot());
            _binding = binding;
            layout = _binding.commentLayoutContainer;
        }

        void setComment(Comment comment) {
            _binding.setComment(comment);

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.karyl)
                    .error(R.drawable.turtle_huh);

            if (comment.getAttachmentUrl() != null) {
                Glide.with(_binding.ivAttachment.getContext())
                        .load(comment.getAttachmentUrl())
                        .fitCenter()
                        .apply(options)
                        .into(_binding.ivAttachment);
            } else if (comment.getAttachmentUri() != null) {
                Glide.with(_binding.ivAttachment.getContext())
                        .load(comment.getAttachmentUri())
                        .fitCenter()
                        .apply(options)
                        .into(_binding.ivAttachment);
            } else {
                _binding.ivAttachment.setImageResource(0);
            }
        }
    }

    List<Comment> _comments;

    // The minimum amount of items to have below your current scroll position before loading more.
    private ReactionClickListener reactionClickListener;
    private ReplyClickListener replyClickListener;
    private OptionsClickListener optionsClickListener;
    private AttachmentClickListener attachmentClickListener;

    final static int VIEW_NORMAL = 1;
    final static int VIEW_REPLYING = 2;

    public CommentListAdapter(List<Comment> comments, Context context) {
        _comments = comments;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_NORMAL) {
            CommentViewHolderBinding binding = CommentViewHolderBinding.inflate(inflater, parent, false);
            vh = new CommentViewHolder(binding);
        } else {
            CommentReplyingViewHolderBinding binding = CommentReplyingViewHolderBinding.inflate(inflater, parent, false);
            vh = new CommentReplyingViewHolder(binding);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Comment comment = _comments.get(position);
        if (holder instanceof CommentViewHolder) {
            CommentViewHolder vh = (CommentViewHolder) holder;
            vh.setComment(comment);

            vh._binding.tvClickableReact.setOnClickListener(v -> {
                if (reactionClickListener != null) {
                    reactionClickListener.onClick(comment);
                }
            });

            vh._binding.tvClickableReply.setOnClickListener(v -> {
                if (replyClickListener != null) {
                    replyClickListener.onClick(comment);
                }
            });

            vh._binding.tvClickableOptions.setOnClickListener(v -> {
                if (optionsClickListener != null) {
                    optionsClickListener.OnClick(comment);
                }
            });

            vh._binding.ivAttachment.setOnClickListener(v -> {
                if (comment.getAttachmentUrl() != null || comment.getAttachmentUri() != null) {
                    attachmentClickListener.OnClick(comment);
                }
            });
        } else {
            CommentReplyingViewHolder vh = (CommentReplyingViewHolder) holder;
            vh.setComment(comment);

            vh._binding.tvClickableReact.setOnClickListener(v -> {
                if (reactionClickListener != null) {
                    reactionClickListener.onClick(comment);
                }
            });

            vh._binding.tvClickableReply.setOnClickListener(v -> {
                if (replyClickListener != null) {
                    replyClickListener.onClick(comment);
                }
            });

            vh._binding.tvClickableOptions.setOnClickListener(v -> {
                if (optionsClickListener != null) {
                    optionsClickListener.OnClick(comment);
                }
            });

            vh._binding.ivAttachment.setOnClickListener(v -> {
                if (comment.getAttachmentUrl() != null || comment.getAttachmentUri() != null) {
                    attachmentClickListener.OnClick(comment);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return _comments.get(position).getReplyCommentId() != null ? VIEW_REPLYING : VIEW_NORMAL;
    }

    @Override
    public int getItemCount() {
        if (_comments != null)
            return _comments.size();
        return 0;
    }

    public interface ReactionClickListener {
        void onClick(Comment comment);
    }

    public void setReactionClickListener(ReactionClickListener reactionClickListener) {
        this.reactionClickListener = reactionClickListener;
    }

    public interface ReplyClickListener {
        void onClick(Comment comment);
    }

    public void setReplyClickListener(ReplyClickListener replyClickListener) {
        this.replyClickListener = replyClickListener;
    }

    public interface OptionsClickListener {
        void OnClick(Comment comment);
    }

    public void setMoreClickListener(OptionsClickListener optionsClickListener) {
        this.optionsClickListener = optionsClickListener;
    }

    public interface AttachmentClickListener {
        void OnClick(Comment comment);
    }

    public void setAttachmentClickListener(AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }
}
