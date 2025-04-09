package com.example.pinterest_clone_test2.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentGenderBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

public class GenderFragment extends Fragment {
    FragmentGenderBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
                    binding.btnSave.setText(getString(R.string.updating));
                    FirebaseUserService.updateGender(selectedGender, new FirebaseUserService.UpdateGenderCallback() {
                        @Override
                        public void OnSuccess() {
                            Toast.makeText(requireContext(), getString(R.string.gender_update_success), Toast.LENGTH_LONG).show();
                            binding.btnBack.performClick();
                            binding.btnSave.setEnabled(true);
                            binding.btnSave.setText(getString(R.string.update));
                        }

                        @Override
                        public void OnFailure(Exception e) {
                            Toast.makeText(requireContext(), getString(R.string.gender_update_failure), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            binding.btnSave.setEnabled(true);
                            binding.btnSave.setText(getString(R.string.update));
                        }
                    });
                }
            }
        });
    }
}