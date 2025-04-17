package com.example.pinterest_clone_test2.ui.settings;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentEmailBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

public class EmailFragment extends Fragment {

    FragmentEmailBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = Navigation.findNavController(view);
        binding.btnBack.setOnClickListener(v -> navController.navigateUp());
        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        String email = currentUserDocument.getString("email");
        binding.etEmail.setText(email);
        binding.btnSave.setOnClickListener(v -> {
            String newEmail = binding.etEmail.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                binding.etEmail.setError(getString(R.string.invalid_email_error));
                binding.etEmail.requestFocus();
                return;
            }

            // Clear any previous error
            binding.etEmail.setError(null);

            binding.btnSave.setEnabled(false);
            binding.btnSave.setText(getString(R.string.updating));
            FirebaseUserService.updateEmail(newEmail, new FirebaseUserService.UpdateEmailCallback() {
                @Override
                public void OnSuccess() {
                    Toast.makeText(requireContext(), getString(R.string.email_updated_success), Toast.LENGTH_SHORT).show();
                    binding.btnBack.performClick();
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText(getString(R.string.update));
                }

                @Override
                public void OnFailure(Exception e) {
                    Toast.makeText(requireContext(), getString(R.string.email_updated_failure), Toast.LENGTH_SHORT).show();
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText(getString(R.string.update));
                    e.printStackTrace();
                }
            });
        });
    }
}