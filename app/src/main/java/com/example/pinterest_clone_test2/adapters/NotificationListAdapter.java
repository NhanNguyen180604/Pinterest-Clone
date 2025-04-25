package com.example.pinterest_clone_test2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.NotificationViewHolderBinding;
import com.example.pinterest_clone_test2.models.Notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder> {
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        NotificationViewHolderBinding binding;

        public NotificationViewHolder(@NonNull NotificationViewHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    List<Notification> notifications;

    public NotificationListAdapter(Context context) {
        notifications = new ArrayList<>(Arrays.asList(
                new Notification(context.getResources().getString(R.string.notification_title_1), R.drawable.serious_cat, R.string.notification_time_stamp_1),
                new Notification(context.getResources().getString(R.string.notification_title_1), R.drawable.high_gojo, R.string.notification_time_stamp_1),
                new Notification(context.getResources().getString(R.string.notification_title_2), R.drawable.karyl_cat, R.string.notification_time_stamp_1),
                new Notification(context.getResources().getString(R.string.notification_title_5), R.drawable.sandalphon_burst_chain, R.string.notification_time_stamp_2),
                new Notification(context.getResources().getString(R.string.notification_title_3), R.drawable.siegfried, R.string.notification_time_stamp_2),
                new Notification(context.getResources().getString(R.string.notification_title_4), R.drawable.absolute_nothing_lucilius, R.string.notification_time_stamp_2),
                new Notification(context.getResources().getString(R.string.notification_title_4), R.drawable.araragi, R.string.notification_time_stamp_2),
                new Notification(context.getResources().getString(R.string.notification_title_4), R.drawable.paradise_lost, R.string.notification_time_stamp_3),
                new Notification(context.getResources().getString(R.string.notification_title_2), R.drawable.paradise_losto, R.string.notification_time_stamp_3),
                new Notification(context.getResources().getString(R.string.notification_title_1), R.drawable.cow, R.string.notification_time_stamp_3),
                new Notification(context.getResources().getString(R.string.notification_title_1), R.drawable.cat_on_sofa, R.string.notification_time_stamp_3)
        ));
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        NotificationViewHolderBinding binding = NotificationViewHolderBinding.inflate(inflater, parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.binding.tvNotificationTitle.setText(notification.getTitle());
        holder.binding.tvTimestamp.setText(notification.getTimestamp());
        Glide.with(holder.binding.ivNotificationImage.getContext())
                .load(notification.getImageSource())
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_loading)
                        .error(R.drawable.ic_loading))
                .into(holder.binding.ivNotificationImage);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}
