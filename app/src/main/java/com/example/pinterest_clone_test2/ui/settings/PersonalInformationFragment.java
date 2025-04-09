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
import com.example.pinterest_clone_test2.databinding.FragmentPersonalInformationBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

public class PersonalInformationFragment extends Fragment {
    FragmentPersonalInformationBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPersonalInformationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        String birthdate = currentUserDocument.getString("birthdate");
        String gender = currentUserDocument.getString("gender");
        binding.btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
        });
        binding.btnBirthdate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_personal_information_to_birthdate);
            }
        });
        binding.tvYourBirthdate.setText(birthdate);
        binding.btnGender.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_personal_information_to_gender);
            }
        });
        binding.tvYourGender.setText(gender);
    }
}