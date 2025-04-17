package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.User;

import java.io.Serializable;

public class UserDetailFragment extends Fragment {
    private static final String ARG_USER = "user_data";
    private User user;
    private ManageUserViewModel viewModel;

    public static UserDetailFragment newInstance(User user) {
        UserDetailFragment fragment = new UserDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, (Serializable) user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ManageUserViewModel.class);

        if (user != null) {
            ((TextView) view.findViewById(R.id.text_name)).setText(user.getName());
            ((TextView) view.findViewById(R.id.text_email)).setText(user.getEmail());
            ((TextView) view.findViewById(R.id.text_birth_date)).setText(user.getBirthDate());
            ((TextView) view.findViewById(R.id.text_gender)).setText(user.getGender().toString());
            ((TextView) view.findViewById(R.id.text_role)).setText(user.getRole().toString());

            Button btnBan = view.findViewById(R.id.btn_ban_user);
            Button btnChangeRole = view.findViewById(R.id.btn_change_role);
            Button btnBack = view.findViewById(R.id.btn_back);

            boolean isBanned = user.isBanned(); // Thêm phương thức này vào model User

            btnBan.setText(isBanned ? "BỎ CẤM NGƯỜI DÙNG" : "CẤM NGƯỜI DÙNG");
            btnBan.setOnClickListener(v -> {
                if (isBanned) {
                    viewModel.unbanUser(user.getUserId(),
                            () -> Toast.makeText(getContext(), "Đã bỏ cấm người dùng", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                } else {
                    viewModel.banUser(user.getUserId(),
                            () -> Toast.makeText(getContext(), "Đã cấm người dùng", Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
                requireActivity().onBackPressed();
            });

            btnChangeRole.setOnClickListener(v -> {
                String newRole = user.getRole() == User.Role.Admin ? "User" : "Admin";
                viewModel.changeUserRole(user.getUserId(), newRole,
                        () -> Toast.makeText(getContext(), "Đã thay đổi vai trò người dùng", Toast.LENGTH_SHORT).show(),
                        e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
                requireActivity().onBackPressed();
            });

            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }
}