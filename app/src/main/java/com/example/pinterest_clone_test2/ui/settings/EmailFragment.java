package com.example.pinterest_clone_test2.ui.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentEmailBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

public class EmailFragment extends Fragment {

    FragmentEmailBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = Navigation.findNavController(view);
        binding.btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                navController.navigateUp();
            }
        });
        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        String email = currentUserDocument.getString("email");
        binding.etEmail.setText(email);
        binding.btnSave.setOnClickListener(v -> {
            String newEmail = binding.etEmail.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                binding.etEmail.setError("Email không hợp lệ");
                binding.etEmail.requestFocus();
                return;
            }

            // Clear any previous error
            binding.etEmail.setError(null);

            binding.btnSave.setEnabled(false);
            binding.btnSave.setText("Đang cập nhật...");
            FirebaseUserService.updateEmail(newEmail, new FirebaseUserService.UpdateEmailCallback() {
                @Override
                public void OnSuccess() {
                    Toast.makeText(requireContext(), "Email đã được cập nhật", Toast.LENGTH_SHORT).show();
                    binding.btnBack.performClick();
                }

                @Override
                public void OnFailure(Exception e) {
                    Toast.makeText(requireContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            binding.btnSave.setEnabled(true);
            binding.btnSave.setText("Cập nhật");
        });
    }
}