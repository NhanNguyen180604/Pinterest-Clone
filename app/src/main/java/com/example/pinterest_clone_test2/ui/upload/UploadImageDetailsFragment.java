package com.example.pinterest_clone_test2.ui.upload;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.MainActivity;
import com.example.pinterest_clone_test2.UploadActivity;
import com.example.pinterest_clone_test2.databinding.FragmentUploadImageDetailsBinding;

public class UploadImageDetailsFragment extends Fragment {

    private FragmentUploadImageDetailsBinding binding;
    private Uri mediaUri;

    public UploadImageDetailsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUploadImageDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Receive mediaUri from arguments
        if (getArguments() != null) {
            mediaUri = getArguments().getParcelable("mediaUri");
            if (mediaUri != null) {
                // Determine media type and load accordingly
                String mimeType = requireContext().getContentResolver().getType(mediaUri); // Changed to requireContext()

                if (mimeType != null && (mimeType.startsWith("image") || mimeType.contains("gif"))) {
                    // For images or GIFs, use Glide to load into ImageView
                    Glide.with(binding.selectedImageView.getContext())
                            .load(mediaUri)
                            .into(binding.selectedImageView);

                    binding.selectedImageView.setVisibility(View.VISIBLE);
                    binding.selectedVideoView.setVisibility(View.GONE);  // Ensure video view is hidden for images/GIFs
                } else if (mimeType != null && mimeType.startsWith("video")) {
                    // For videos, use VideoView to play the video
                    binding.selectedImageView.setVisibility(View.GONE);  // Hide ImageView for video
                    binding.selectedVideoView.setVisibility(View.VISIBLE);  // Show VideoView for video

                    VideoView videoView = binding.selectedVideoView;
                    videoView.setVideoURI(mediaUri);

                    // Set an event listener to handle when video is prepared and ready to start
                    videoView.setOnPreparedListener(mp -> {
                        videoView.start();  // Start playing the video once it's ready
                    });

                    // Optionally, add an onCompletionListener to handle video completion
                    videoView.setOnCompletionListener(mp -> Toast.makeText(getContext(), "Video completed", Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(getContext(), "No media selected", Toast.LENGTH_SHORT).show();
            }
        }

        // Handle "Create" button click
        binding.btnCreate.setOnClickListener(v -> {
            if (mediaUri != null) {
                if (getActivity() instanceof UploadActivity) {
                    String title = binding.titleEditText.getText().toString();
                    String description = binding.descriptionEditText.getText().toString();
                    ((UploadActivity) getActivity()).uploadMedia(mediaUri, title, description);
                }
            } else {
                Toast.makeText(getContext(), "No media selected", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
