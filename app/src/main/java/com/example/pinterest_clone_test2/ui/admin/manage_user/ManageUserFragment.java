package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.app.AlertDialog;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

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

import java.util.ArrayList;

public class ManageUserFragment extends Fragment {
    private FragmentManageUserBinding binding;
    private ManageUserViewModel viewModel;
    private UserListAdapter adapter;

    // UI
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

        // Set default tab button style
        setupTabButtonsStyle();

        viewModel.fetchNormalUsers(); // Load default
    }

    private void setupUI() {
        binding.layoutTabs.check(R.id.btn_normal); // Select normal tab by default
    }

    private void setupTabButtonsStyle() {
        // Select NORMAL button first
        Button normalButton = binding.btnNormal;
        normalButton.setBackgroundTintList(getResources().getColorStateList(R.color.tab_selected_background, null));
        normalButton.setTextColor(getResources().getColor(R.color.tab_selected_text, null));
    }

    private void setupRecyclerView() {
        adapter = new UserListAdapter(new ArrayList<>(), new UserListAdapter.UserActionListener() {
            @Override
            public void onBanClick(User user) {
                showLoading(true);
                if (!isBannedSelected) {
                    // If on NORMAL tab → ban user
                    viewModel.banUser(user.getUserId(),
                            () -> {
                                showLoading(false);
                                Toast.makeText(getContext(),
                                        getString(R.string.banned_user_success, user.getEmail()),
                                        Toast.LENGTH_SHORT).show();
                            },
                            e -> {
                                showLoading(false);
                                Toast.makeText(getContext(),
                                        getString(R.string.error_message, e.getMessage()),
                                        Toast.LENGTH_SHORT).show();
                            }
                    );
                } else {
                    // If on BANNED tab → unban user
                    viewModel.unbanUser(user.getUserId(),
                            () -> {
                                showLoading(false);
                                Toast.makeText(getContext(),
                                        getString(R.string.unbanned_user_success, user.getEmail()),
                                        Toast.LENGTH_SHORT).show();
                            },
                            e -> {
                                showLoading(false);
                                Toast.makeText(getContext(),
                                        getString(R.string.error_message, e.getMessage()),
                                        Toast.LENGTH_SHORT).show();
                            }
                    );
                }
            }

            @Override
            public void onItemClick(User user) {
                // Navigate to user profile
                navigateToUserProfile(user);
            }

            @Override
            public void onRoleChanged(User user, User.Role newRole) {
                // Refresh data after role change
                if (isBannedSelected) {
                    viewModel.fetchBannedUsers();
                } else {
                    viewModel.fetchNormalUsers();
                }
            }
        });

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    private void navigateToUserProfile(User user) {
        // Create Bundle to pass information
        Bundle args = new Bundle();
        args.putString("userId", user.getUserId());
        args.putString("source", "admin"); // Mark source as admin

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin);
        navController.navigate(R.id.action_manageUserFragment_to_userProfileFragment4, args);
    }

    private void setupObservers() {
        viewModel.getNormalUsers().observe(getViewLifecycleOwner(), users -> {
            if (!isBannedSelected) {
                adapter.updateData(users);
                adapter.setBannedList(false);

                // Show notification if no data
                if (users.isEmpty()) {
                    binding.tvNoData.setVisibility(View.VISIBLE);
                    binding.tvNoData.setText(getString(R.string.no_users_found));
                } else {
                    binding.tvNoData.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getBannedUsers().observe(getViewLifecycleOwner(), users -> {
            if (isBannedSelected) {
                adapter.updateData(users);
                adapter.setBannedList(true);

                // Show notification if no data
                if (users.isEmpty()) {
                    binding.tvNoData.setVisibility(View.VISIBLE);
                    binding.tvNoData.setText(getString(R.string.no_banned_users));
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
        // Search event listener
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }
        });

        // Tab change event listener
        binding.layoutTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_banned) {
                    // Change color for selected tab and unselected tab
                    binding.btnBanned.setBackgroundTintList(getResources().getColorStateList(R.color.tab_selected_background, null));
                    binding.btnBanned.setTextColor(getResources().getColor(R.color.tab_selected_text, null));
                    binding.btnNormal.setBackgroundTintList(getResources().getColorStateList(R.color.tab_unselected_background, null));
                    binding.btnNormal.setTextColor(getResources().getColor(R.color.tab_unselected_text, null));

                    isBannedSelected = true;
                    viewModel.setIsBannedSelected(true);
                    viewModel.fetchBannedUsers();
                } else if (checkedId == R.id.btn_normal) {
                    // Change color for selected tab and unselected tab
                    binding.btnNormal.setBackgroundTintList(getResources().getColorStateList(R.color.tab_selected_background, null));
                    binding.btnNormal.setTextColor(getResources().getColor(R.color.tab_selected_text, null));
                    binding.btnBanned.setBackgroundTintList(getResources().getColorStateList(R.color.tab_unselected_background, null));
                    binding.btnBanned.setTextColor(getResources().getColor(R.color.tab_unselected_text, null));

                    isBannedSelected = false;
                    viewModel.setIsBannedSelected(false);
                    viewModel.fetchNormalUsers();
                }
            }
        });

        // Filter button click listener
        binding.btnFilter.setOnClickListener(v -> {
            showFilterDialog();
        });
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_user, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.filter_users))
                .setView(dialogView)
                .create();

        RadioGroup radioGroupRole = dialogView.findViewById(R.id.radioGroupRole);
        Button btnApply = dialogView.findViewById(R.id.btn_apply_filter);

        // Set initial checked state based on current filter
        User.Role currentFilter = viewModel.getRoleFilter();
        if (currentFilter == null) {
            radioGroupRole.check(R.id.radio_all);
        } else if (currentFilter == User.Role.Admin) {
            radioGroupRole.check(R.id.radio_admin);
        } else if (currentFilter == User.Role.User) {
            radioGroupRole.check(R.id.radio_user);
        }

        // Apply filter
        btnApply.setOnClickListener(v -> {
            int selectedId = radioGroupRole.getCheckedRadioButtonId();
            User.Role role = null;

            if (selectedId == R.id.radio_admin) {
                role = User.Role.Admin;
            } else if (selectedId == R.id.radio_user) {
                role = User.Role.User;
            }
            // If "All" is selected, role will be null

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