package com.example.pinterest_clone_test2.adapters;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {
    private List<User> userList = new ArrayList<>();
    private final UserActionListener actionListener;
    private boolean isBannedList = false;

    // Thiết lập các hàm xử lý sự kiện cho các nút trong ViewHolder
    public interface UserActionListener {
        void onBanClick(User user);
        void onItemClick(User user);
    }

    // Constructor
    @SuppressLint("NotifyDataSetChanged")
    public UserListAdapter(List<User> userList, UserActionListener listener) {
        this.userList = userList;
        this.actionListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setBannedList(boolean isBanned) {
        this.isBannedList = isBanned;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_view_holder, parent, false);
        return new UserViewHolder(view);
    }

    // ViewHolder cho mỗi item trong RecyclerView
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        ImageView avatar;
        Button banBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_user_name);
            email = itemView.findViewById(R.id.tv_user_email);
            avatar = itemView.findViewById(R.id.iv_user_avatar);
            banBtn = itemView.findViewById(R.id.btn_ban);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());

        // Load avatar nếu có
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getAvatarUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_account_circle)
                            .error(R.drawable.ic_account_circle)
                            .centerCrop())
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_account_circle);
        }

        if (isBannedList) {
            holder.banBtn.setText(holder.itemView.getContext().getString(R.string.unban_button));
            holder.banBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.grey)));
            holder.banBtn.setVisibility(View.VISIBLE);
        } else {
            holder.banBtn.setText(holder.itemView.getContext().getString(R.string.ban_button));
            holder.banBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red_pinterest)));

            // Ẩn nút cấm nếu user là Admin
            if (user.getRole() == User.Role.Admin) {
                holder.banBtn.setVisibility(View.GONE);
            } else {
                holder.banBtn.setVisibility(View.VISIBLE);
            }
        }

        holder.banBtn.setOnClickListener(v -> actionListener.onBanClick(user));
        holder.itemView.setOnClickListener(v -> actionListener.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }
}