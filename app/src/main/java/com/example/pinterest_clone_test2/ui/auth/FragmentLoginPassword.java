package com.example.pinterest_clone_test2.ui.auth;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentLoginPassword#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentLoginPassword extends Fragment {

    public FragmentLoginPassword() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_password, container, false);

        return view;
    }




}