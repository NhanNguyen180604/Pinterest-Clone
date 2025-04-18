package com.example.pinterest_clone_test2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pinterest_clone_test2.databinding.ActivityEditPinBinding;

public class EditPinActivity extends AppCompatActivity {

    ActivityEditPinBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}