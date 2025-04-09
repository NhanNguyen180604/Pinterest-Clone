package com.example.pinterest_clone_test2.ui.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentGenderBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

public class GenderFragment extends Fragment {
    FragmentGenderBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGenderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
        });
        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        String gender = currentUserDocument.getString("gender");
        assert gender != null;
        if (gender.equals("Nam")) {
            binding.rbGenderMale.setChecked(true);
        } else {
            binding.rbGenderFemale.setChecked(true);
        }
    }
}