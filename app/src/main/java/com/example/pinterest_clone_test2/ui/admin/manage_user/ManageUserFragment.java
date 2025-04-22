package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.UserListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentManageUserBinding;
import com.example.pinterest_clone_test2.models.User;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class ManageUserFragment extends Fragment {

    private FragmentManageUserBinding binding;
    private ManageUserViewModel viewModel;
    private UserListAdapter adapter;

    // UI
    private TextInputEditText et_search;
    private boolean isBannedSelected = false;

    public ManageUserFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentManageUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ManageUserViewModel.class);
        setupUI();
        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchNormalUsers(); // Load mặc định
    }

    private void setupUI() {
        et_search = binding.etSearch;
        binding.layoutTabs.check(R.id.btn_normal); // Chọn tab bình thường mặc định
    }

    private void setupRecyclerView() {
        adapter = new UserListAdapter(new ArrayList<>(), new UserListAdapter.UserActionListener() {
            @Override
            public void onBanClick(User user) {
                showLoading(true);
                if (!isBannedSelected) {
                    // Nếu đang ở tab người thường → chặn user
                    viewModel.banUser(user.getUserId(),
                            () -> {
                                showLoading(false);
                                Toast.makeText(getContext(), "Đã chặn " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            },
                            e -> {
                                showLoading(false);
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
                } else {
                    // Nếu đang ở tab bị chặn → bỏ chặn user
                    viewModel.unbanUser(user.getUserId(),
                            () -> {
                                showLoading(false);
                                Toast.makeText(getContext(), "Đã bỏ chặn " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            },
                            e -> {
                                showLoading(false);
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    );
                }
            }

            @Override
            public void onItemClick(User user) {
                // Chuyển đến fragment chi tiết người dùng
                UserDetailFragment detailFragment = UserDetailFragment.newInstance(user);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_admin, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getNormalUsers().observe(getViewLifecycleOwner(), users -> {
            if (!isBannedSelected) {
                adapter.updateData(users);
                adapter.setBannedList(false);

                // Hiển thị thông báo nếu không có dữ liệu
                if (users.isEmpty()) {
                    binding.tvNoData.setVisibility(View.VISIBLE);
                    binding.tvNoData.setText("Không có người dùng nào");
                } else {
                    binding.tvNoData.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getBannedUsers().observe(getViewLifecycleOwner(), users -> {
            if (isBannedSelected) {
                adapter.updateData(users);
                adapter.setBannedList(true);

                // Hiển thị thông báo nếu không có dữ liệu
                if (users.isEmpty()) {
                    binding.tvNoData.setVisibility(View.VISIBLE);
                    binding.tvNoData.setText("Không có người dùng nào bị cấm");
                } else {
                    binding.tvNoData.setVisibility(View.GONE);
                }
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), this::showLoading);

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Nút quay lại User Mode
        binding.btnBackToUser.setOnClickListener(v -> {
            requireActivity().finish();
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }
        });

        binding.layoutTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_banned) {
                    isBannedSelected = true;
                    viewModel.setIsBannedSelected(true);
                    viewModel.fetchBannedUsers();
                } else if (checkedId == R.id.btn_normal) {
                    isBannedSelected = false;
                    viewModel.setIsBannedSelected(false);
                    viewModel.fetchNormalUsers();
                }
            }
        });

        binding.btnAddUser.setOnClickListener(v -> {
            // Hiển thị dialog thêm người dùng
            AddUserDialogFragment dialog = new AddUserDialogFragment(
                    (password, email, name, birthDate, gender, role) -> {
                        // Tạo user mới
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setBirthDate(birthDate);
                        newUser.setGender(gender.name());
                        newUser.setRole(role);

                        showLoading(true);
                        viewModel.addUser(newUser,
                                () -> {
                                    showLoading(false);
                                    Toast.makeText(getContext(), "Đã thêm " + email, Toast.LENGTH_SHORT).show();
                                },
                                e -> {
                                    showLoading(false);
                                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        );
                    }
            );
            dialog.show(getParentFragmentManager(), "add_user_dialog");
        });

        binding.btnFilter.setOnClickListener(v -> {
            showFilterDialog();
        });
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_user, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Lọc người dùng")
                .setView(dialogView)
                .create();

        RadioGroup radioGroupRole = dialogView.findViewById(R.id.radioGroupRole);
        Button btnApply = dialogView.findViewById(R.id.btn_apply_filter);

        // Đặt trạng thái checked ban đầu dựa vào filter hiện tại
        User.Role currentFilter = viewModel.getRoleFilter();
        if (currentFilter == null) {
            radioGroupRole.check(R.id.radio_all);
        } else if (currentFilter == User.Role.Admin) {
            radioGroupRole.check(R.id.radio_admin);
        } else if (currentFilter == User.Role.User) {
            radioGroupRole.check(R.id.radio_user);
        }

        btnApply.setOnClickListener(v -> {
            int selectedId = radioGroupRole.getCheckedRadioButtonId();
            User.Role role = null;

            if (selectedId == R.id.radio_admin) {
                role = User.Role.Admin;
            } else if (selectedId == R.id.radio_user) {
                role = User.Role.User;
            }
            // Nếu chọn "Tất cả", role sẽ là null

            viewModel.setRoleFilter(role);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showLoading(boolean show) {
        if (binding != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.rvUsers.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}