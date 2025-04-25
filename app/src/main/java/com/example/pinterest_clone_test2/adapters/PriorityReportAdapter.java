package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.PriorityReport;
import com.example.pinterest_clone_test2.models.ReportSeverity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PriorityReportAdapter extends RecyclerView.Adapter<PriorityReportAdapter.PriorityReportViewHolder> {

    private final Context context;
    private final List<PriorityReport> reports;
    private final OnPriorityReportClickListener listener;

    public interface OnPriorityReportClickListener {
        void onReportClick(PriorityReport report);
        void onTakeActionClick(PriorityReport report);
    }

    public PriorityReportAdapter(Context context, List<PriorityReport> reports, OnPriorityReportClickListener listener) {
        this.context = context;
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PriorityReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_priority_report, parent, false);
        return new PriorityReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PriorityReportViewHolder holder, int position) {
        PriorityReport report = reports.get(position);

        // Thiết lập màu card theo mức độ nghiêm trọng
        setupCardBorder(holder.cardContainer, report.getSeverity());

        // Thiết lập chip mức độ nghiêm trọng
        setupSeverityChip(holder.chipSeverity, report.getSeverity());

        // Loại báo cáo (Pin hay Comment)
        holder.tvReportType.setText(report.getReportType() == PriorityReport.ReportType.PIN ? "PIN" : "COMMENT");

        // Thông tin nội dung báo cáo
        holder.tvContentTitle.setText(report.getContentTitle());

        // Số lượng báo cáo
        holder.tvReportCount.setText(String.format(Locale.getDefault(), "%d reports", report.getReportCount()));

        // Lý do báo cáo chính
        holder.tvReportReason.setText(report.getMainReasonTitle());

        // Thời gian chờ
        setupWaitingTime(holder.tvWaitingTime, report.getWaitingTime());

        // Hình ảnh nội dung
        if (report.getContentThumbnail() != null && !report.getContentThumbnail().isEmpty()) {
            Glide.with(context)
                    .load(report.getContentThumbnail())
                    .placeholder(R.drawable.ic_pin_placeholder)
                    .error(R.drawable.ic_pin_placeholder)
                    .centerCrop()
                    .into(holder.ivContentThumbnail);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_pin_placeholder)
                    .centerCrop()
                    .into(holder.ivContentThumbnail);
        }

        // Thông tin tác giả
        holder.tvAuthorName.setText(report.getAuthorName());
        if (report.getAuthorAvatar() != null && !report.getAuthorAvatar().isEmpty()) {
            Glide.with(context)
                    .load(report.getAuthorAvatar())
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

        // Thiết lập sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onReportClick(report));
        holder.btnTakeAction.setOnClickListener(v -> listener.onTakeActionClick(report));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    /**
     * Thiết lập màu viền card theo mức độ nghiêm trọng
     */
    private void setupCardBorder(MaterialCardView cardView, ReportSeverity severity) {
        int borderWidth = 0;
        int borderColor = 0;

        switch (severity) {
            case URGENT:
                borderWidth = 2; // dp
                borderColor = R.color.severity_urgent;
                break;
            case HIGH:
                borderWidth = 2; // dp
                borderColor = R.color.severity_high;
                break;
            case MEDIUM:
                borderWidth = 1; // dp
                borderColor = R.color.severity_medium;
                break;
            case LOW:
            default:
                borderWidth = 0; // Không có viền
                break;
        }

        if (borderWidth > 0) {
            cardView.setStrokeWidth(dpToPx(borderWidth));
            cardView.setStrokeColor(ContextCompat.getColor(context, borderColor));
        } else {
            cardView.setStrokeWidth(0);
        }
    }

    /**
     * Thiết lập chip mức độ nghiêm trọng
     */
    private void setupSeverityChip(Chip chip, ReportSeverity severity) {
        int bgColor;
        int textColor = R.color.white;
        String text;

        switch (severity) {
            case URGENT:
                text = "URGENT";
                bgColor = R.color.severity_urgent;
                break;
            case HIGH:
                text = "HIGH";
                bgColor = R.color.severity_high;
                break;
            case MEDIUM:
                text = "MEDIUM";
                bgColor = R.color.severity_medium;
                break;
            case LOW:
            default:
                text = "LOW";
                bgColor = R.color.severity_low;
                textColor = R.color.dark_grey;
                break;
        }

        chip.setText(text);
        chip.setChipBackgroundColor(ContextCompat.getColorStateList(context, bgColor));
        chip.setTextColor(ContextCompat.getColor(context, textColor));
    }

    /**
     * Chuyển đổi thời gian chờ (ms) thành chuỗi hiển thị
     */
    private void setupWaitingTime(TextView textView, long waitingTimeMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(waitingTimeMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(waitingTimeMillis) % 24;

        String waitingText;
        if (days > 0) {
            waitingText = String.format(Locale.getDefault(), "%dd %dh waiting", days, hours);
        } else {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(waitingTimeMillis) % 60;
            if (hours > 0) {
                waitingText = String.format(Locale.getDefault(), "%dh %dm waiting", hours, minutes);
            } else {
                waitingText = String.format(Locale.getDefault(), "%dm waiting", minutes);
            }
        }

        textView.setText(waitingText);

        // Thiết lập màu sắc dựa trên thời gian chờ
        int textColor;
        if (days >= 3) {
            textColor = R.color.severity_urgent;
        } else if (days >= 1) {
            textColor = R.color.severity_high;
        } else if (hours >= 12) {
            textColor = R.color.severity_medium;
        } else {
            textColor = R.color.dark_grey;
        }

        textView.setTextColor(ContextCompat.getColor(context, textColor));
    }

    /**
     * Chuyển đổi dp sang px
     */
    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    static class PriorityReportViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardContainer;
        Chip chipSeverity;
        TextView tvReportType;
        TextView tvContentTitle;
        TextView tvReportCount;
        TextView tvReportReason;
        TextView tvWaitingTime;
        ImageView ivContentThumbnail;
        ImageView ivAuthorAvatar;
        TextView tvAuthorName;
        MaterialButton btnTakeAction;

        PriorityReportViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_container);
            chipSeverity = itemView.findViewById(R.id.chip_severity);
            tvReportType = itemView.findViewById(R.id.tv_report_type);
            tvContentTitle = itemView.findViewById(R.id.tv_content_title);
            tvReportCount = itemView.findViewById(R.id.tv_report_count);
            tvReportReason = itemView.findViewById(R.id.tv_report_reason);
            tvWaitingTime = itemView.findViewById(R.id.tv_waiting_time);
            ivContentThumbnail = itemView.findViewById(R.id.iv_content_thumbnail);
            ivAuthorAvatar = itemView.findViewById(R.id.iv_author_avatar);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            btnTakeAction = itemView.findViewById(R.id.btn_take_action);
        }
    }
}