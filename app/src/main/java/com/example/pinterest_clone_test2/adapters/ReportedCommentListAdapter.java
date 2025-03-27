package com.example.pinterest_clone_test2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.CommentReport;
import java.util.List;

public class ReportedCommentListAdapter extends RecyclerView.Adapter<ReportedCommentListAdapter.ReportedCommentViewHolder> {
    private List<CommentReport> commentReportList;

    public ReportedCommentListAdapter(List<CommentReport> commentReportList) {
        this.commentReportList = commentReportList;
    }

    @NonNull
    @Override
    public ReportedCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reported_comment, parent, false);
        return new ReportedCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportedCommentViewHolder holder, int position) {
        CommentReport comment = commentReportList.get(position);
        holder.tv_reported_reason.setText(new StringBuilder("Reason(s): ").append(comment.toString()).toString());
    }

    @Override
    public int getItemCount() {
        return commentReportList.size();
    }

    public static class ReportedCommentViewHolder extends RecyclerView.ViewHolder {
        TextView tv_reported_email,tv_reported_comment_content,tv_reported_reason, tv_reported_comment_id;

        public ReportedCommentViewHolder(@NonNull View itemView) {
            super(itemView);
//            tv_reported_comment_content = itemView.findViewById(R.id.tv_reported_comment_content);
            tv_reported_reason= itemView.findViewById(R.id.tv_reported_reason);
//            tv_reported_email = itemView.findViewById(R.id.tv_reported_email);
        }
    }
}