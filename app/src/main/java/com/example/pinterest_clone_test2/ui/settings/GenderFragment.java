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
import android.widget.RadioButton;
import android.widget.Toast;

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
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = binding.rgGender.getCheckedRadioButtonId();

                if (selectedId != -1) {
                    RadioButton selectedRadioButton = binding.getRoot().findViewById(selectedId);
                    String selectedGender = selectedRadioButton.getText().toString();
                    binding.btnSave.setEnabled(false);
                    binding.btnSave.setText("Đang cập nhật...");
                    FirebaseUserService.updateGender(selectedGender, new FirebaseUserService.UpdateGenderCallback() {
                        @Override
                        public void OnSuccess() {
                            Toast.makeText(requireContext(), "Giới tính đã được cập nhật", Toast.LENGTH_LONG).show();
                            binding.btnBack.performClick();
                        }

                        @Override
                        public void OnFailure(Exception e) {
                            Toast.makeText(requireContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Cập nhật");
                }
            }
        });
    }
}