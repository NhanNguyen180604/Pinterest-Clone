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
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.UploadActivity;
import com.example.pinterest_clone_test2.databinding.FragmentUploadPinDetailsBinding;

public class UploadPinDetailsFragment extends Fragment {

    private FragmentUploadPinDetailsBinding binding;
    private Uri mediaUri;
    boolean isCollage;
    ExoPlayer exoPlayer;

    public UploadPinDetailsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUploadPinDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Receive mediaUri from arguments
        if (getArguments() != null) {
            mediaUri = getArguments().getParcelable("mediaUri");
            if (mediaUri == null) {
                return;
            }

            isCollage = getArguments().getBoolean("isCollage");

            String mimeType = requireContext().getContentResolver().getType(mediaUri);
            if (isCollage)
                mimeType = "image";

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

                exoPlayer = new ExoPlayer.Builder(requireContext()).build();
                binding.selectedVideoView.setPlayer(exoPlayer);
                MediaItem mediaItem = MediaItem.fromUri(mediaUri);
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
            }
        }

        // Handle "Create" button click
        binding.btnCreate.setOnClickListener(v -> {
            if (mediaUri != null) {
                String title = binding.titleEditText.getText().toString().trim();
                String description = binding.descriptionEditText.getText().toString();

                // Check if title is empty
                if (title.isEmpty()) {
                    // Show error message that title is required
                    Toast.makeText(getContext(), R.string.no_title_message, Toast.LENGTH_SHORT).show();

                    binding.titleEditText.setError(getResources().getString(R.string.no_title_message));
                    return;
                }

                if (getActivity() instanceof UploadActivity) {
                    ((UploadActivity) getActivity()).uploadMedia(mediaUri, title, description, isCollage);
                }
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.no_media_selected), Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
