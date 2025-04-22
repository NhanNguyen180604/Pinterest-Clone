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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentUserDetailBinding;
import com.example.pinterest_clone_test2.models.User;

public class UserDetailFragment extends Fragment {
    private static final String ARG_USER = "user_data";
    private User user;
    private ManageUserViewModel viewModel;
    private FragmentUserDetailBinding binding;

    public static UserDetailFragment newInstance(User user) {
        UserDetailFragment fragment = new UserDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ManageUserViewModel.class);

        if (user != null) {
            // Hiển thị thông tin người dùng
            binding.textName.setText(user.getName());
            binding.textEmail.setText(user.getEmail());
            binding.textBirthDate.setText(user.getBirthDate());
            binding.textGender.setText(user.getGender().toString());
            binding.textRole.setText(user.getRole().toString());

            // Cập nhật giao diện nút Cấm/Bỏ cấm
            boolean isBanned = user.isBanned();
            binding.btnBanUser.setText(isBanned ? "BỎ CẤM NGƯỜI DÙNG" : "CẤM NGƯỜI DÙNG");

            // Ẩn nút cấm nếu user là Admin
            if (user.getRole() == User.Role.Admin && !isBanned) {
                binding.btnBanUser.setVisibility(View.GONE);
            }

            // Xử lý sự kiện nút Cấm/Bỏ cấm
            binding.btnBanUser.setOnClickListener(v -> {
                showBanConfirmDialog(isBanned);
            });

            // Xử lý sự kiện nút Thay đổi vai trò
            binding.btnChangeRole.setOnClickListener(v -> {
                showChangeRoleConfirmDialog();
            });

            // Xử lý sự kiện nút Quay lại
            binding.btnBack.setOnClickListener(v -> {
                requireActivity().onBackPressed();
            });
        }
    }

    private void showBanConfirmDialog(boolean isBanned) {
        String title = isBanned ? "Bỏ cấm người dùng" : "Cấm người dùng";
        String message = isBanned ?
                "Bạn có chắc muốn bỏ cấm người dùng này?" :
                "Bạn có chắc muốn cấm người dùng này?";

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Thực hiện cấm hoặc bỏ cấm
                    if (isBanned) {
                        viewModel.unbanUser(user.getUserId(),
                                () -> {
                                    Toast.makeText(getContext(), "Đã bỏ cấm người dùng", Toast.LENGTH_SHORT).show();
                                    requireActivity().onBackPressed();
                                },
                                e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    } else {
                        viewModel.banUser(user.getUserId(),
                                () -> {
                                    Toast.makeText(getContext(), "Đã cấm người dùng", Toast.LENGTH_SHORT).show();
                                    requireActivity().onBackPressed();
                                },
                                e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangeRoleConfirmDialog() {
        String newRole = user.getRole() == User.Role.Admin ? User.Role.User.name() : User.Role.Admin.name();

        new AlertDialog.Builder(requireContext())
                .setTitle("Thay đổi vai trò")
                .setMessage("Bạn có chắc muốn thay đổi vai trò của người dùng này từ " +
                        user.getRole().name() + " thành " + newRole + "?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    viewModel.changeUserRole(user.getUserId(), newRole,
                            () -> {
                                Toast.makeText(getContext(), "Đã thay đổi vai trò người dùng", Toast.LENGTH_SHORT).show();
                                binding.textRole.setText(newRole);

                                // Cập nhật trạng thái nút Cấm nếu thay đổi từ User thành Admin
                                if (newRole.equals(User.Role.Admin.name())) {
                                    binding.btnBanUser.setVisibility(View.GONE);
                                } else {
                                    binding.btnBanUser.setVisibility(View.VISIBLE);
                                }
                            },
                            e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}