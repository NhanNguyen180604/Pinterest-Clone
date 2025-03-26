package com.example.pinterest_clone_test2.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.pinterest_clone_test2.AdminActivity;
import com.example.pinterest_clone_test2.MainActivity;
import com.example.pinterest_clone_test2.R;

public class SettingsDrawerFragment extends Fragment {

    public SettingsDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        Button btnAccountManagement = view.findViewById(R.id.btn_account_management);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
        });
        btnAccountManagement.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_settingsDrawerFragment_to_account_management);
            }
        });

        Button goToAdminBtn = view.findViewById(R.id.btn_go_to_admin);
        goToAdminBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AdminActivity.class);
            startActivity(intent);
        });
    }
}