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
import com.example.pinterest_clone_test2.databinding.ItemReportedPinBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportedPin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportedPinAdapter extends RecyclerView.Adapter<ReportedPinAdapter.ReportedPinViewHolder> {

    private final List<ReportedPin> pinList;
    private final Context context;
    private final OnReportedPinListener listener;

    public interface OnReportedPinListener {
        void onPinClick(ReportedPin pin);
        void onAuthorClick(ReportedPin pin);
        void onProcessClick(ReportedPin pin);
        void onReportInfoClick(ReportedPin pin);
    }

    public ReportedPinAdapter(Context context, List<ReportedPin> pinList, OnReportedPinListener listener) {
        this.context = context;
        this.pinList = new ArrayList<>(pinList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportedPinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportedPinBinding binding = ItemReportedPinBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReportedPinViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportedPinViewHolder holder, int position) {
        ReportedPin pin = pinList.get(position);
        holder.bind(pin);
    }

    @Override
    public int getItemCount() {
        return pinList.size();
    }

    public void updateData(List<ReportedPin> newPins) {
        pinList.clear();
        pinList.addAll(newPins);
        notifyDataSetChanged();
    }

    class ReportedPinViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportedPinBinding binding;

        public ReportedPinViewHolder(ItemReportedPinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ReportedPin pin) {
            // Hiển thị thông tin cơ bản của pin
            binding.tvPinTitle.setText(pin.getPinTitle() != null && !pin.getPinTitle().isEmpty() ?
                    pin.getPinTitle() : "(Không có tiêu đề)");

            String description = pin.getPinDescription();
            if (description != null && !description.isEmpty()) {
                binding.tvPinDescription.setVisibility(View.VISIBLE);
                binding.tvPinDescription.setText(description.length() > 100 ?
                        description.substring(0, 97) + "..." : description);
            } else {
                binding.tvPinDescription.setVisibility(View.GONE);
            }

            binding.tvAuthorName.setText(pin.getPinAuthorName());

            // Định dạng ngày tháng
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(pin.getPinCreatedAt());
            String dateFormatted = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
            binding.tvCreatedAt.setText(dateFormatted);

            // Hiển thị loại pin
            binding.tvPinType.setText(getPinTypeText(pin.getPinType()));
            binding.tvPinType.setBackgroundTintList(ContextCompat.getColorStateList(context,
                    getPinTypeColor(pin.getPinType())));

            // Hiển thị thông tin báo cáo
            binding.tvReportCount.setText(context.getString(R.string.report_count_format, pin.getReportCount()));
            binding.tvReportReason.setText(context.getString(R.string.reason_prefix) + " " + pin.getMostCommonReasonTitle());

            // Hiển thị thời gian báo cáo gần nhất
            Calendar reportCalendar = Calendar.getInstance();
            reportCalendar.setTimeInMillis(pin.getLastReportedAt());
            String reportDate = DateFormat.format("dd/MM/yyyy HH:mm", reportCalendar).toString();
            binding.tvLastReportDate.setText(context.getString(R.string.last_report_time_label) + ": " + reportDate);

            // Hiển thị trạng thái kiểm tra
            if (pin.isChecked()) {
                binding.chipStatus.setText(R.string.status_checked);
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(context, R.color.colorGreen));
                binding.tvStatus.setText(R.string.status_checked);
                binding.tvStatus.setTextColor(context.getResources().getColor(R.color.colorGreen, null));
            } else {
                binding.chipStatus.setText(R.string.status_unchecked);
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(context, R.color.colorRed));
                binding.tvStatus.setText(R.string.status_unchecked);
                binding.tvStatus.setTextColor(context.getResources().getColor(R.color.colorRed, null));
            }

            // Tải hình ảnh pin
            if (pin.getThumbnailUrl() != null && !pin.getThumbnailUrl().isEmpty()) {
                Glide.with(context)
                        .load(pin.getThumbnailUrl())
                        .placeholder(R.drawable.ic_pin_placeholder)
                        .error(R.drawable.ic_pin_placeholder)
                        .centerCrop()
                        .into(binding.ivPinImage);
            } else if (pin.getMediaUrl() != null && !pin.getMediaUrl().isEmpty()) {
                Glide.with(context)
                        .load(pin.getMediaUrl())
                        .placeholder(R.drawable.ic_pin_placeholder)
                        .error(R.drawable.ic_pin_placeholder)
                        .centerCrop()
                        .into(binding.ivPinImage);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_pin_placeholder)
                        .centerCrop()
                        .into(binding.ivPinImage);
            }

            // Tải avatar tác giả
            if (pin.getPinAuthorAvatar() != null && !pin.getPinAuthorAvatar().isEmpty()) {
                Glide.with(context)
                        .load(pin.getPinAuthorAvatar())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_account_circle)
                                .error(R.drawable.ic_account_circle)
                                .circleCrop())
                        .into(binding.ivAuthorAvatar);
            } else {
                binding.ivAuthorAvatar.setImageResource(R.drawable.ic_account_circle);
            }

            // Set click listeners
            binding.cardPinImage.setOnClickListener(v -> listener.onPinClick(pin));
            binding.tvPinTitle.setOnClickListener(v -> listener.onPinClick(pin));
            binding.tvPinDescription.setOnClickListener(v -> listener.onPinClick(pin));

            binding.layoutAuthor.setOnClickListener(v -> listener.onAuthorClick(pin));
            binding.ivAuthorAvatar.setOnClickListener(v -> listener.onAuthorClick(pin));
            binding.tvAuthorName.setOnClickListener(v -> listener.onAuthorClick(pin));

            binding.layoutReportInfo.setOnClickListener(v -> listener.onReportInfoClick(pin));
            binding.btnViewDetails.setOnClickListener(v -> listener.onPinClick(pin));
            binding.btnProcess.setOnClickListener(v -> listener.onProcessClick(pin));
        }

        private String getPinTypeText(Pin.PinType type) {
            if (type == null) {
                return context.getString(R.string.pin_type_unknown);
            }

            switch (type) {
                case IMAGE:
                    return context.getString(R.string.pin_type_image);
                case GIF:
                    return context.getString(R.string.pin_type_gif);
                case VIDEO:
                    return context.getString(R.string.pin_type_video);
                default:
                    return context.getString(R.string.pin_type_unknown);
            }
        }

        private int getPinTypeColor(Pin.PinType type) {
            if (type == null) {
                return R.color.grey;
            }

            switch (type) {
                case IMAGE:
                    return R.color.colorPrimary;
                case GIF:
                    return R.color.colorAccent;
                case VIDEO:
                    return R.color.colorSecondary;
                default:
                    return R.color.grey;
            }
        }
    }
}