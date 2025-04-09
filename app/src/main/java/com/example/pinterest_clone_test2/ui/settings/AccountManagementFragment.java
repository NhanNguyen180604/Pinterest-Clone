package com.example.pinterest_clone_test2.ui.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentAccountManagementBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

public class AccountManagementFragment extends Fragment {
    FragmentAccountManagementBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
        });
        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        String email = currentUserDocument.getString("email");
        binding.btnPersonalInformation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_account_management_to_personal_information);
            }
        });
        binding.btnEmail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_account_management_to_email);
            }
        });
        binding.tvYourEmail.setText(email);
        binding.btnPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_account_management_to_password);
            }
        });
    }
}