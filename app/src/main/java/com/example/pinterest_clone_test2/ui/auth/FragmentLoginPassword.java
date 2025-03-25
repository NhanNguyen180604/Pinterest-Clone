package com.example.pinterest_clone_test2.ui.auth;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.pinterest_clone_test2.R;


public class FragmentLoginPassword extends Fragment {
    ImageButton btnBack;

    public FragmentLoginPassword() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_password, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // Quay về fragment trước đó trong back stack
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        return view;
    }




}