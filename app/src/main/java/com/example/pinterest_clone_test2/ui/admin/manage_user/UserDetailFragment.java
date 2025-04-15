package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.User;

import java.io.Serializable;

public class UserDetailFragment extends Fragment {
    private static final String ARG_USER = "user_data";

    public static UserDetailFragment newInstance(User user) {
        var fragment = new UserDetailFragment();
        var args = new Bundle();
        args.putSerializable(ARG_USER, (Serializable) user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        User user = (User) getArguments().getSerializable(ARG_USER);
        assert user != null;
        ((TextView) view.findViewById(R.id.text_name)).setText(user.getFirstName());
        ((TextView) view.findViewById(R.id.text_email)).setText(user.getEmail());
        // Thêm các info khác nếu cần
    }
}