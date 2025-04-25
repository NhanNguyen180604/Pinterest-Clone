package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.ItemReportedCommentBinding;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportedComment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportedCommentAdapter extends RecyclerView.Adapter<ReportedCommentAdapter.ReportedCommentViewHolder> {

    private final List<ReportedComment> commentList;
    private final Context context;
    private final OnReportedCommentListener listener;

    public interface OnReportedCommentListener {
        void onCommentClick(ReportedComment comment);
        void onAuthorClick(ReportedComment comment);
        void onProcessClick(ReportedComment comment);
        void onViewPinClick(ReportedComment comment);
    }

    public ReportedCommentAdapter(Context context, List<ReportedComment> commentList, OnReportedCommentListener listener) {
        this.context = context;
        this.commentList = new ArrayList<>(commentList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportedCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportedCommentBinding binding = ItemReportedCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReportedCommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportedCommentViewHolder holder, int position) {
        ReportedComment comment = commentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateData(List<ReportedComment> newComments) {
        commentList.clear();
        commentList.addAll(newComments);
        notifyDataSetChanged();
    }

    class ReportedCommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportedCommentBinding binding;

        public ReportedCommentViewHolder(ItemReportedCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ReportedComment comment) {
            // Hiển thị thông tin cơ bản
            binding.tvCommentContent.setText(comment.getCommentContent());
            binding.tvAuthorName.setText(comment.getCommentAuthorName());
            binding.tvReportCount.setText(context.getString(R.string.report_count_format, comment.getReportCount()));

            // Hiển thị lý do báo cáo phổ biến nhất với prefix theo ngôn ngữ
            if (comment.getMostCommonReasonTitle() != null && !comment.getMostCommonReasonTitle().isEmpty()) {
                String reasonPrefix = ReportReason.getReasonPrefix(context);
                binding.tvReportReason.setText(reasonPrefix + " " + comment.getMostCommonReasonTitle());
                binding.tvReportReason.setVisibility(View.VISIBLE);
            } else {
                binding.tvReportReason.setVisibility(View.GONE);
            }

            // Hiển thị thời gian báo cáo
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(comment.getLastReportedAt());
            String dateFormatted = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
            binding.tvReportDate.setText(dateFormatted);

            // Hiển thị ảnh đại diện
            if (comment.getCommentAuthorAvatar() != null && !comment.getCommentAuthorAvatar().isEmpty()) {
                Glide.with(context)
                        .load(comment.getCommentAuthorAvatar())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_account_circle)
                                .error(R.drawable.ic_account_circle)
                                .circleCrop())
                        .into(binding.ivAuthorAvatar);
            } else {
                binding.ivAuthorAvatar.setImageResource(R.drawable.ic_account_circle);
            }

            // Hiển thị ảnh đính kèm nếu có
            if (comment.getAttachmentUrl() != null && !comment.getAttachmentUrl().isEmpty()) {
                binding.ivCommentAttachment.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(comment.getAttachmentUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_loading)
                                .error(R.drawable.ic_loading))
                        .into(binding.ivCommentAttachment);
            } else {
                binding.ivCommentAttachment.setVisibility(View.GONE);
            }

            // Hiển thị trạng thái đã kiểm tra hay chưa
            if (comment.isChecked()) {
                binding.chipStatus.setText(R.string.status_checked);
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(context, R.color.colorGreen));
            } else {
                binding.chipStatus.setText(R.string.status_unchecked);
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(context, R.color.colorRed));
            }

            // Set click listeners
            binding.cardComment.setOnClickListener(v -> listener.onCommentClick(comment));
            binding.btnProcess.setOnClickListener(v -> listener.onProcessClick(comment));
            binding.btnViewPin.setOnClickListener(v -> listener.onViewPinClick(comment));

            // Add click listeners for author avatar and name
            binding.ivAuthorAvatar.setOnClickListener(v -> listener.onAuthorClick(comment));
            binding.tvAuthorName.setOnClickListener(v -> listener.onAuthorClick(comment));
        }
    }
}