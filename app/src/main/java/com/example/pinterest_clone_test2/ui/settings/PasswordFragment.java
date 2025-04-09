package com.example.pinterest_clone_test2.ui.settings;

import android.os.Bundle;
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
import com.example.pinterest_clone_test2.databinding.FragmentPasswordBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;

public class PasswordFragment extends Fragment {
    FragmentPasswordBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordBinding.inflate(inflater, container, false);
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
        binding.btnSave.setOnClickListener(v -> {
            String oldPassword = "";
            if (binding.etOldPassword.getText() != null) {
                oldPassword = binding.etOldPassword.getText().toString().trim();
            }
            String newPassword = "";
            if (binding.etNewPassword.getText() != null) {
                newPassword = binding.etNewPassword.getText().toString().trim();
            }

            // Validate inputs
            if (oldPassword.isEmpty()) {
                binding.tvOldPasswordWarning.setText(getString(R.string.please_enter_old_password));
                binding.tvOldPasswordWarning.setVisibility(View.VISIBLE);
                binding.etOldPassword.requestFocus();
                return;
            }

            if (newPassword.isEmpty()) {
                binding.tvNewPasswordWarning.setText(getString(R.string.please_enter_new_password));
                binding.tvNewPasswordWarning.setVisibility(View.VISIBLE);
                binding.tilNewPassword.requestFocus();
                return;
            }
            binding.btnSave.setEnabled(false);
            binding.btnSave.setText(getString(R.string.updating));
            FirebaseUserService.updatePassword(oldPassword, newPassword, new FirebaseUserService.UpdatePasswordCallback() {
                @Override
                public void OnSuccess() {
                    Toast.makeText(requireContext(), getString(R.string.password_update_success), Toast.LENGTH_SHORT).show();
                    binding.tvOldPasswordWarning.setVisibility(View.GONE);
                    binding.tvNewPasswordWarning.setVisibility(View.GONE);
                    binding.etNewPassword.setText("");
                    binding.etOldPassword.setText("");
                    binding.btnBack.performClick();
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText(getString(R.string.update));
                }

                @Override
                public void OnFailure(Exception e) {
                    binding.tvOldPasswordWarning.setText(getString(R.string.old_password_error));
                    binding.tvOldPasswordWarning.setVisibility(View.VISIBLE);
                    binding.etOldPassword.requestFocus();
                    e.printStackTrace();
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText(getString(R.string.update));
                }
            });
        });
    }
}