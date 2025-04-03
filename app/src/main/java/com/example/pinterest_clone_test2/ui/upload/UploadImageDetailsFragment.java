package com.example.pinterest_clone_test2.ui.upload;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pinterest_clone_test2.UploadActivity;
import com.example.pinterest_clone_test2.databinding.FragmentUploadImageDetailsBinding;

public class UploadImageDetailsFragment extends Fragment {

    private FragmentUploadImageDetailsBinding binding;
    private Uri imageUri;

    public UploadImageDetailsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUploadImageDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Receive imageUri from arguments
        if (getArguments() != null) {
            imageUri = getArguments().getParcelable("imageUri");
            if (imageUri != null) {
                binding.selectedImageView.setImageURI(imageUri);  // Display selected image
            } else {
                Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }
        }

        // Handle "Create" button click
        binding.btnCreate.setOnClickListener(v -> {
            if (imageUri != null) {
                // Call uploadImage method in UploadActivity to upload image
                if (getActivity() instanceof UploadActivity) {
                    ((UploadActivity) getActivity()).uploadImage(imageUri); // Upload image from Activity
                }
            } else {
                Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
