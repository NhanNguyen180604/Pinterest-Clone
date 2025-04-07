package com.example.pinterest_clone_test2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.pinterest_clone_test2.databinding.ActivityCreateBoardBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.board.board_choosing.BoardChoosingFragment;

public class ChooseBoardActivity extends AppCompatActivity {

    ActivityCreateBoardBinding binding;
    FragmentManager fragmentManager;

    Pin pin;
    boolean suggestNewBoard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCreateBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent.getParcelableExtra("pin") != null) {
            pin = intent.getParcelableExtra("pin");
            suggestNewBoard = intent.getBooleanExtra("suggestNewBoard", false);
        }

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.create_board_fragment_container, new BoardChoosingFragment(pin, suggestNewBoard))
                .commit();
    }
}