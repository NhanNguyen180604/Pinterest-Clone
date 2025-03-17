package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.Comment;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.CommentViewHolder> {
    class CommentViewHolder extends RecyclerView.ViewHolder {
        protected Comment _comment;
        ShapeableImageView ivAvatar;
        TextView tvUsername;
        TextView tvTimestamp;
        ImageView ivAttachment;
        TextView tvContent;
        TextView tvClickableReply;
        TextView tvClickableReact;
        TextView tvClickableMore;
        ConstraintLayout layout;

        CommentViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            ivAttachment = itemView.findViewById(R.id.iv_attachment);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvClickableReply = itemView.findViewById(R.id.tv_clickable_reply);
            tvClickableReact = itemView.findViewById(R.id.tv_clickable_react);
            tvClickableMore = itemView.findViewById(R.id.tv_clickable_more);
            layout = itemView.findViewById(R.id.comment_layout_container);
        }

        public void adjustMarginStart() {
            if (_comment != null && _comment.getReplyCommentId() != null) {
                int startMarginDp = (int) layout.getContext().getResources().getDimension(R.dimen.reply_comment_start_margin);
                layout.setPadding(startMarginDp, 0, 0, 0);
            }
        }

        public void setComment(@NonNull Comment comment) {
            _comment = comment;
        }
    }

    List<Comment> _comments;

    public CommentListAdapter(List<Comment> comments) {
        _comments = comments;
    }

    public void setComments(List<Comment> comments, boolean append) {
        if (append) {
            _comments.addAll(comments);
            notifyItemRangeInserted(_comments.size() - comments.size(), _comments.size());
        } else {
            _comments = comments;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view_holder, parent, false);
        return new CommentViewHolder(itemView, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = _comments.get(position);
        if (comment.getAttachmentUrl() != null) {
            Glide.with(holder.ivAttachment.getContext())
                    .load(Integer.parseInt(comment.getAttachmentUrl()))
                    .fitCenter()
                    .placeholder(R.drawable.karyl)
                    .into(holder.ivAttachment);
        } else {
            holder.ivAttachment.setImageResource(0);
        }

        holder.tvContent.setText(comment.getContent());
        holder.tvUsername.setText(comment.getAuthorName());
    }

    @Override
    public int getItemCount() {
        return _comments != null ? _comments.size() : 0;
    }
}
