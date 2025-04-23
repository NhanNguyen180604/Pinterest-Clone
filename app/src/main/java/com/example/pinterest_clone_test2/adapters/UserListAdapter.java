package com.example.pinterest_clone_test2.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {
    private List<User> userList = new ArrayList<>();
    private final UserActionListener actionListener;
    private boolean isBannedList = false;

    // Interface for handling action events
    public interface UserActionListener {
        void onBanClick(User user);
        void onItemClick(User user);
        default void onRoleChanged(User user, User.Role newRole) {}
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

    // ViewHolder class
    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        ImageView avatar;
        Button banBtn;
        ImageButton moreOptionsBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_user_name);
            email = itemView.findViewById(R.id.tv_user_email);
            avatar = itemView.findViewById(R.id.iv_user_avatar);
            banBtn = itemView.findViewById(R.id.btn_ban);
            moreOptionsBtn = itemView.findViewById(R.id.btn_more_options);
        }

        void showRoleOptions(User user) {
            Context context = itemView.getContext();
            PopupMenu popupMenu = new PopupMenu(context, moreOptionsBtn);

            // Add role change options based on current role
            if (user.getRole() == User.Role.User) {
                popupMenu.getMenu().add(Menu.NONE, 1, Menu.NONE, context.getString(R.string.make_admin));
            } else if (user.getRole() == User.Role.Admin) {
                popupMenu.getMenu().add(Menu.NONE, 2, Menu.NONE, context.getString(R.string.make_user));
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    // Make user an admin
                    changeUserRole(user, User.Role.Admin);
                    return true;
                } else if (item.getItemId() == 2) {
                    // Make admin a regular user
                    changeUserRole(user, User.Role.User);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }

        private void changeUserRole(User user, User.Role newRole) {
            // Show loading dialog
            Context context = itemView.getContext();
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.updating_role));
            progressDialog.show();

            // Update user role in Firestore
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUserId())
                    .update("role", newRole.toString())
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(context,
                                context.getString(R.string.role_updated_successfully),
                                Toast.LENGTH_SHORT).show();

                        // Notify listener for UI refresh
                        actionListener.onRoleChanged(user, newRole);
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(context,
                                context.getString(R.string.failed_to_update_role) + ": " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());

        // Load avatar if available
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

        // Set up ban/unban button
        if (isBannedList) {
            holder.banBtn.setText(holder.itemView.getContext().getString(R.string.unban_button));
            holder.banBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.grey)));
            holder.banBtn.setVisibility(View.VISIBLE);
        } else {
            holder.banBtn.setText(holder.itemView.getContext().getString(R.string.ban_button));
            holder.banBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red_pinterest)));

            // Hide ban button for Admin users
            if (user.getRole() == User.Role.Admin) {
                holder.banBtn.setVisibility(View.GONE);
            } else {
                holder.banBtn.setVisibility(View.VISIBLE);
            }
        }

        // Set click listeners
        holder.banBtn.setOnClickListener(v -> actionListener.onBanClick(user));
        holder.itemView.setOnClickListener(v -> actionListener.onItemClick(user));
        holder.moreOptionsBtn.setOnClickListener(v -> holder.showRoleOptions(user));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }
}