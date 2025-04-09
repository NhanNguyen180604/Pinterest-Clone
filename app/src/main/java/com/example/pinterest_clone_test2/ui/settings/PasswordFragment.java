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
import android.widget.Toast;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentPasswordBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;

public class PasswordFragment extends Fragment {
    FragmentPasswordBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordBinding.inflate(inflater, container, false);
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
        binding.btnSave.setOnClickListener(v -> {
            String oldPassword = binding.etOldPassword.getText().toString().trim();
            String newPassword = binding.etNewPassword.getText().toString().trim();

            // Validate inputs
            if (oldPassword.isEmpty()) {
                binding.tvOldPasswordWarning.setText("Vui lòng nhập mật khẩu cũ");
                binding.tvOldPasswordWarning.setVisibility(View.VISIBLE);
                binding.etOldPassword.requestFocus();
                return;
            }

            if (newPassword.isEmpty()) {
                binding.tvNewPasswordWarning.setText("Vui lòng nhập mật khẩu mới");
                binding.tvNewPasswordWarning.setVisibility(View.VISIBLE);
                binding.tilNewPassword.requestFocus();
                return;
            }
            binding.btnSave.setEnabled(false);
            binding.btnSave.setText("Đang cập nhật...");
            FirebaseUserService.updatePassword(oldPassword, newPassword, new FirebaseUserService.UpdatePasswordCallback() {
                @Override
                public void OnSuccess() {
                    Toast.makeText(requireContext(), "Mật khẩu đã được cập nhật", Toast.LENGTH_SHORT).show();
                    binding.tvOldPasswordWarning.setVisibility(View.GONE);
                    binding.tvNewPasswordWarning.setVisibility(View.GONE);
                    binding.etNewPassword.setText("");
                    binding.etOldPassword.setText("");
                    binding.btnBack.performClick();
                }

                @Override
                public void OnFailure(Exception e) {
//                    binding.tvOldPasswordWarning.setError("Mật khẩu cũ không đúng hoặc lỗi: " + e.getMessage());
                    binding.tvOldPasswordWarning.setText("Mật khẩu cũ không đúng hoặc lỗi");
                    binding.tvOldPasswordWarning.setVisibility(View.VISIBLE);
                    binding.etOldPassword.requestFocus();
                }
            });
            binding.btnSave.setEnabled(true);
            binding.btnSave.setText("Cập nhật");

        });
    }
}