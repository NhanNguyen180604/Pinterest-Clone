package com.example.pinterest_clone_test2.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {
    private List<User> userList = new ArrayList<>();
    private final UserActionListener actionListener;

    public interface UserActionListener {
        void onBlockClick(User user);
        void onDeleteClick(User user);
        void onItemClick(User user);
    }

    @SuppressLint("NotifyDataSetChanged")
    public UserListAdapter(List<User> userList, UserActionListener listener) {
        this.userList = userList;
        this.actionListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setUserList(List<User> newUsers) {
        this.userList.clear();
        this.userList.addAll(newUsers);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_view_holder, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getFirstName());
        holder.email.setText(user.getEmail());
        holder.blockBtn.setOnClickListener(v -> actionListener.onBlockClick(user));
        holder.deleteBtn.setOnClickListener(v -> actionListener.onDeleteClick(user));
        holder.itemView.setOnClickListener(v -> actionListener.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        Button blockBtn, deleteBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_username);
            email = itemView.findViewById(R.id.tv_user_email);
            blockBtn = itemView.findViewById(R.id.btn_block);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
        }
    }
}