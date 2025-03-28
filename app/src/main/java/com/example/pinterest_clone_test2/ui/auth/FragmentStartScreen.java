package com.example.pinterest_clone_test2.ui.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.databinding.FragmentStartScreenBinding;

public class FragmentStartScreen extends Fragment {
    FragmentStartScreenBinding binding;
    public FragmentStartScreen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStartScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LoginActivity activity = (LoginActivity) requireActivity();
        binding.btnSignup.setOnClickListener(v -> {
            activity.startRegisterFlow();
        });
        binding.btnSignin.setOnClickListener(v -> {
            activity.startLoginFlow();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}