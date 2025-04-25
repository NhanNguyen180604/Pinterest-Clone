package com.example.pinterest_clone_test2.ui.auth;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;


public class FragmentLogin extends Fragment {
    ImageButton btnBack;
    MaterialButton btnLogin;
    private String email;
    TextInputEditText etEmail;
    TextInputEditText etPassword;

    public FragmentLogin() {
        // Required empty public constructor
    }

    public FragmentLogin(String email) {
        // Required empty public constructor
        this.email = email;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        btnLogin = view.findViewById(R.id.btn_login);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);

        etEmail.setText(this.email);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnLogin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString();
            String password = Objects.requireNonNull(etPassword.getText()).toString();
            // TODO: validate email, password here
            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError(getString(R.string.email_invalid_format));
                return;
            }else if(password.isEmpty()){
                etPassword.setError(getString(R.string.empty_error));
            }else{
                ((LoginActivity) requireActivity()).login(email, password);
            }
        });
        return view;
    }
}