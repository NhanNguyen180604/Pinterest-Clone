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


public class FragmentRegisterName extends Fragment {
    EditText etName;
    Button btnNext;

    public FragmentRegisterName() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_name, container, false);

        etName = view.findViewById(R.id.et_name);
        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                if(name.isEmpty())
                {
                    etName.setError("Không được để trống tên");
                }
                else
                    ((LoginActivity)requireActivity()).registerName(name);
            }
        });

        return view;
    }
}