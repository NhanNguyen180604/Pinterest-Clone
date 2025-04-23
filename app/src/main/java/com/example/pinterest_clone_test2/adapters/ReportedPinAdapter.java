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
import com.example.pinterest_clone_test2.models.ReportedPin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportedPinAdapter extends RecyclerView.Adapter<ReportedPinAdapter.ReportedPinViewHolder> {

    private final Context context;
    private final List<ReportedPin> reportedPins;
    private final OnReportedPinListener listener;

    public interface OnReportedPinListener {
        void onPinClick(ReportedPin pin);
        void onMarkAsCheckedClick(ReportedPin pin);
        void onDeleteClick(ReportedPin pin);
        void onViewAuthorClick(ReportedPin pin);
    }

    public ReportedPinAdapter(Context context, List<ReportedPin> reportedPins, OnReportedPinListener listener) {
        this.context = context;
        this.reportedPins = reportedPins;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportedPinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reported_pin, parent, false);
        return new ReportedPinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportedPinViewHolder holder, int position) {
        ReportedPin reportedPin = reportedPins.get(position);

        // Hiển thị thông tin pin
        holder.tvPinTitle.setText(reportedPin.getPinTitle() != null && !reportedPin.getPinTitle().isEmpty() ?
                reportedPin.getPinTitle() : "(Không có tiêu đề)");

        String description = reportedPin.getPinDescription();
        if (description != null && !description.isEmpty()) {
            holder.tvPinDescription.setVisibility(View.VISIBLE);
            holder.tvPinDescription.setText(description.length() > 100 ?
                    description.substring(0, 97) + "..." : description);
        } else {
            holder.tvPinDescription.setVisibility(View.GONE);
        }

        holder.tvAuthorName.setText(reportedPin.getPinAuthorName());

        // Định dạng ngày tháng
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvCreatedAt.setText(dateFormat.format(new Date(reportedPin.getPinCreatedAt())));

        // Hiển thị thông tin báo cáo
        holder.tvReportCount.setText(context.getString(R.string.report_count_format, reportedPin.getReportCount()));
        holder.tvReportReason.setText(reportedPin.getMostCommonReasonTitle());
        holder.tvLastReportDate.setText(dateFormat.format(new Date(reportedPin.getLastReportedAt())));

        // Hiển thị trạng thái kiểm tra
        if (reportedPin.isChecked()) {
            holder.tvStatus.setText(R.string.status_checked);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.colorGreen, null));
        } else {
            holder.tvStatus.setText(R.string.status_unchecked);
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.red_pinterest, null));
        }

        // Tải hình ảnh
        if (reportedPin.getThumbnailUrl() != null && !reportedPin.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                    .load(reportedPin.getThumbnailUrl())
                    .placeholder(R.drawable.ic_pin_placeholder)
                    .error(R.drawable.ic_pin_placeholder)
                    .centerCrop()
                    .into(holder.ivPinImage);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_pin_placeholder)
                    .centerCrop()
                    .into(holder.ivPinImage);
        }

        // Tải avatar tác giả
        if (reportedPin.getPinAuthorAvatar() != null && !reportedPin.getPinAuthorAvatar().isEmpty()) {
            Glide.with(context)
                    .load(reportedPin.getPinAuthorAvatar())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.ivAuthorAvatar);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.ivAuthorAvatar);
        }

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onPinClick(reportedPin));
        holder.btnMarkAsChecked.setOnClickListener(v -> listener.onMarkAsCheckedClick(reportedPin));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(reportedPin));
        holder.layoutAuthor.setOnClickListener(v -> listener.onViewAuthorClick(reportedPin));
    }

    @Override
    public int getItemCount() {
        return reportedPins.size();
    }

    public static class ReportedPinViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPinImage;
        TextView tvPinTitle;
        TextView tvPinDescription;
        TextView tvCreatedAt;
        TextView tvReportCount;
        TextView tvReportReason;
        TextView tvLastReportDate;
        TextView tvStatus;
        ImageView ivAuthorAvatar;
        TextView tvAuthorName;
        View layoutAuthor;
        View btnMarkAsChecked;
        View btnDelete;

        public ReportedPinViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPinImage = itemView.findViewById(R.id.iv_pin_image);
            tvPinTitle = itemView.findViewById(R.id.tv_pin_title);
            tvPinDescription = itemView.findViewById(R.id.tv_pin_description);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            tvReportCount = itemView.findViewById(R.id.tv_report_count);
            tvReportReason = itemView.findViewById(R.id.tv_report_reason);
            tvLastReportDate = itemView.findViewById(R.id.tv_last_report_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivAuthorAvatar = itemView.findViewById(R.id.iv_author_avatar);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            layoutAuthor = itemView.findViewById(R.id.layout_author);
            btnMarkAsChecked = itemView.findViewById(R.id.btn_mark_as_checked);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}