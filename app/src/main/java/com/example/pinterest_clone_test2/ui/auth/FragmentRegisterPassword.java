package com.example.pinterest_clone_test2.ui.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.R;

public class FragmentRegisterPassword extends Fragment {

    EditText etPassWord;
    Button btnNext;
    public FragmentRegisterPassword() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_password, container, false);
        etPassWord = view.findViewById(R.id.et_password);
        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassWord.getText().toString();
                if(password.isEmpty()){
                    etPassWord.setError("Không nên bỏ trống mật khẩu!");
                }else
                    ((LoginActivity)requireActivity()).registerPassword(password);
            }
        });
        return view;
    }
}