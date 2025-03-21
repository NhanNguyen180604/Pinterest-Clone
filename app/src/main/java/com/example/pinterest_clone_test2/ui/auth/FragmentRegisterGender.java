package com.example.pinterest_clone_test2.ui.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.R;


public class FragmentRegisterGender extends Fragment {
    Button female;
    Button male;
    Button other;

    public FragmentRegisterGender() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_gender, container, false);
        female = view.findViewById(R.id.btn_female);
        male = view.findViewById(R.id.btn_male);
        other = view.findViewById(R.id.btn_other);
        // Gán listener cho từng button
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)requireActivity()).registerGender("Nữ");
            }
        });
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)requireActivity()).registerGender("Nam");
            }
        });
        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)requireActivity()).registerGender("Other");
            }
        });


        return view;
    }
}