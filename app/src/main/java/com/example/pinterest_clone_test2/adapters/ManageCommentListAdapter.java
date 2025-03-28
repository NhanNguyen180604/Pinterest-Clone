package com.example.pinterest_clone_test2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.Comment;

import java.util.List;

public class ManageCommentListAdapter extends RecyclerView.Adapter<ManageCommentListAdapter.ReportedCommentViewHolder> {
    private List<Comment> commentList;

    public ManageCommentListAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ReportedCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reported_comment, parent, false);
        return new ReportedCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportedCommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tv_reported_comment_content.setText(comment.getContent());
        holder.tv_reported_email.setText(comment.getAuthorName());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ReportedCommentViewHolder extends RecyclerView.ViewHolder {
        TextView tv_reported_email,tv_reported_comment_content;

        public ReportedCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_reported_comment_content = itemView.findViewById(R.id.tv_reported_comment_content);
            tv_reported_email = itemView.findViewById(R.id.tv_reported_email);
        }
    }
}