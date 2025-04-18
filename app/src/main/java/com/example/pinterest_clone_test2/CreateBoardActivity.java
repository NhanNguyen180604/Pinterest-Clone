package com.example.pinterest_clone_test2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.pinterest_clone_test2.databinding.ActivityCreateBoardBinding;
import com.example.pinterest_clone_test2.ui.board.CreateNewBoardFragment;

public class CreateBoardActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    ActivityCreateBoardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.create_board_fragment_container, new CreateNewBoardFragment())
                .commit();
    }
}