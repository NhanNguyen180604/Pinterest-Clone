package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.UserListAdapter;
import com.example.pinterest_clone_test2.models.User;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManageUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageUserFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ManageUserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ManageUserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManageUserFragment newInstance(String param1, String param2) {
        ManageUserFragment fragment = new ManageUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_user, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rv_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ManageUserViewModel viewModel = new ViewModelProvider(this).get(ManageUserViewModel.class);

        UserListAdapter adapter = new UserListAdapter(new ArrayList<>(), new UserListAdapter.UserActionListener() {
            @Override
            public void onBlockClick(User user) {
                if (user.isBlocked()) {
                    viewModel.unblockUser(user);
                } else {
                    viewModel.blockUser(user);
                }
            }

            @Override
            public void onDeleteClick(User user) {
                viewModel.deleteUser(user);
            }

            @Override
            public void onItemClick(User user) {
                // TODO: Hiển thị chi tiết hoặc mở dialog
            }
        });

        recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu LiveData
        viewModel.getUsers().observe(getViewLifecycleOwner(), adapter::setUserList);

        viewModel.loadUsersFromFirebase();

        return view;
    }
}