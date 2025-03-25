package com.example.pinterest_clone_test2.ui.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.R;


public class FragmentLoginEmail extends Fragment {
    EditText emailInput;
    Button continueBtn;

    public FragmentLoginEmail() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_email, container, false);

        emailInput = view.findViewById(R.id.et_email);
        continueBtn = view.findViewById(R.id.btn_continue);
        continueBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (isValidEmail(email)) {
                ((LoginActivity) requireActivity()).updateEmail(email);
            } else {
                emailInput.setError("Email không hợp lệ");
            }
        });
        return view;
    }

    private boolean isValidEmail(String email) {
        // Kiểm tra định dạng email
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}