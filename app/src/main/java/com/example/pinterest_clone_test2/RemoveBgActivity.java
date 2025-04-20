package com.example.pinterest_clone_test2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pinterest_clone_test2.databinding.ActivityRemoveBgBinding;

public class RemoveBgActivity extends AppCompatActivity {
    ActivityRemoveBgBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemoveBgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}