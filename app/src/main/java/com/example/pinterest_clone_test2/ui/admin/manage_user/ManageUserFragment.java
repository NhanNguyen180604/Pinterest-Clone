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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.UserListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentManageUserBinding;
import com.example.pinterest_clone_test2.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

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

    // CHƯA XONG
    private void setupRecyclerView() {
        adapter = new UserListAdapter(new ArrayList<>(), new UserListAdapter.UserActionListener() {
            @Override
            public void onBanClick(User user) {
                if (!isBannedSelected) {
                    // Nếu đang ở tab người thường → chặn user
                    viewModel.banUser(user.getUserId(),
                            () -> Toast.makeText(getContext(), "Đã chặn " + user.getEmail(), Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                } else {
                    // Nếu đang ở tab bị chặn → bỏ chặn user
                    viewModel.unbanUser(user.getUserId(),
                            () -> Toast.makeText(getContext(), "Đã bỏ chặn " + user.getEmail(), Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onItemClick(User user) {
//                UserDetailFragment detailFragment = UserDetailFragment.newInstance(user);
//                getParentFragmentManager().beginTransaction()
//                        .replace(R.id.nav_host_fragment, detailFragment)
//                        .addToBackStack(null)
//                        .commit();
//
//                Toast.makeText(getContext(), "Xem chi tiết: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    // CHƯA XONG
    private void setupObservers() {
        viewModel.getNormalUsers().observe(getViewLifecycleOwner(), users -> {
            if (!isBannedSelected) {
                adapter.updateData(users);
            }
        });

        viewModel.getBannedUsers().observe(getViewLifecycleOwner(), users -> {
            if (isBannedSelected) {
                adapter.updateData(users);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            // Nếu có ProgressBar trong layout, bạn có thể bỏ comment dòng này
            // binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
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
                        User newUser = new User(password, email, name, birthDate, gender, role);
                        viewModel.addUser(newUser,
                                () -> Toast.makeText(getContext(), "Đã thêm " + email, Toast.LENGTH_SHORT).show(),
                                e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
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

        btnApply.setOnClickListener(v -> {
            int selectedId = radioGroupRole.getCheckedRadioButtonId();
            User.Role role = null;

            if (selectedId == R.id.radio_admin) {
                role = User.Role.Admin;
            } else if (selectedId == R.id.radio_user) {
                role = User.Role.User;
            }

            viewModel.setRoleFilter(role);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}