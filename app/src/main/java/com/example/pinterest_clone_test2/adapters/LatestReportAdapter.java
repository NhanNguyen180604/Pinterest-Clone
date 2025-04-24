package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.LatestReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LatestReportAdapter extends RecyclerView.Adapter<LatestReportAdapter.ReportViewHolder> {

    private final Context context;
    private final List<LatestReport> reports;
    private final OnReportClickListener listener;

    public interface OnReportClickListener {
        void onViewDetailClick(LatestReport report);
        void onHandleReportClick(LatestReport report);
    }

    public LatestReportAdapter(Context context, List<LatestReport> reports, OnReportClickListener listener) {
        this.context = context;
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_latest_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        LatestReport report = reports.get(position);

        // Thiết lập loại báo cáo
        holder.tvReportType.setText(report.getReportType().name());

        // Định dạng thời gian báo cáo
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvReportDate.setText(sdf.format(new Date(report.getReportDate())));

        // Thiết lập thông tin người báo cáo
        holder.tvReporterName.setText(report.getReporterName());

        // Thiết lập avatar người báo cáo
        if (report.getReporterAvatar() != null && !report.getReporterAvatar().isEmpty()) {
            Glide.with(context)
                    .load(report.getReporterAvatar())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.ivReporterAvatar);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(holder.ivReporterAvatar);
        }

        // Thiết lập trạng thái
        if (report.isChecked()) {
            holder.tvStatus.setText("Đã xử lý");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.colorGreen, null));
        } else {
            holder.tvStatus.setText("Chưa xử lý");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.red_pinterest, null));
        }

        // Thiết lập thông tin nội dung bị báo cáo
        holder.tvContentTitle.setText(report.getContentTitle());
        if (report.getContentDescription() != null && !report.getContentDescription().isEmpty()) {
            holder.tvContentDescription.setVisibility(View.VISIBLE);
            holder.tvContentDescription.setText(report.getContentDescription());
        } else {
            holder.tvContentDescription.setVisibility(View.GONE);
        }

        // Thiết lập thumbnail nội dung
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

        // Thiết lập lý do báo cáo
        holder.tvReportReason.setText(report.getReportReason());

        // Thiết lập xử lý sự kiện click
        holder.btnViewDetail.setOnClickListener(v -> listener.onViewDetailClick(report));
        holder.btnHandleReport.setOnClickListener(v -> listener.onHandleReportClick(report));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportType;
        TextView tvReportDate;
        ImageView ivReporterAvatar;
        TextView tvReporterName;
        TextView tvStatus;
        ImageView ivContentThumbnail;
        TextView tvContentTitle;
        TextView tvContentDescription;
        TextView tvReportReasonLabel;
        TextView tvReportReason;
        Button btnViewDetail;
        Button btnHandleReport;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportType = itemView.findViewById(R.id.tv_report_type);
            tvReportDate = itemView.findViewById(R.id.tv_report_date);
            ivReporterAvatar = itemView.findViewById(R.id.iv_reporter_avatar);
            tvReporterName = itemView.findViewById(R.id.tv_reporter_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivContentThumbnail = itemView.findViewById(R.id.iv_content_thumbnail);
            tvContentTitle = itemView.findViewById(R.id.tv_content_title);
            tvContentDescription = itemView.findViewById(R.id.tv_content_description);
            tvReportReasonLabel = itemView.findViewById(R.id.tv_report_reason_label);
            tvReportReason = itemView.findViewById(R.id.tv_report_reason);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
            btnHandleReport = itemView.findViewById(R.id.btn_handle_report);
        }
    }
}