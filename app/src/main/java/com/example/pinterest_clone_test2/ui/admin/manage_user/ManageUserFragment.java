package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.UserListAdapter;
import com.example.pinterest_clone_test2.models.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManageUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageUserFragment extends Fragment {
    private ManageUserViewModel viewModel;
    private UserListAdapter adapter;
    private ProgressBar progressBar;
    private MaterialButton btnAddUser;

    public ManageUserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(ManageUserViewModel.class);

        // Ánh xạ view
        RecyclerView recyclerView = view.findViewById(R.id.rv_user_list);
        progressBar = view.findViewById(R.id.progressBar);
        btnAddUser = view.findViewById(R.id.btnAddUser);

        // Cấu hình RecyclerView
        adapter = new UserListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ ViewModel
        viewModel.getUserList().observe(getViewLifecycleOwner(), users -> {
            if (users != null && !users.isEmpty()) {
                adapter.setUserList(users);
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        // Sự kiện click nút thêm người dùng
        btnAddUser.setOnClickListener(v -> {
            // TODO: Chuyển sang màn hình thêm người dùng
        });
    }
}