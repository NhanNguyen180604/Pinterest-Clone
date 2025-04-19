package com.example.pinterest_clone_test2.ui.pin.remove_bg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentRemoveBgBinding;
import com.example.pinterest_clone_test2.services.remove_image_bg.RemoveBgService;

import java.util.Objects;

public class RemoveBgFragment extends Fragment {
    FragmentRemoveBgBinding binding;
    String originalImageUrl;
    RemoveBgFragmentViewModel viewModel;
    Handler handler = new Handler();

    public RemoveBgFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = requireActivity().getIntent();
        if (intent.getStringExtra("imageUrl") != null) {
            originalImageUrl = intent.getStringExtra("imageUrl");
        }
    }

    void removeBgAsync() {
        Thread thread = new Thread(() -> RemoveBgService.removeBackground(originalImageUrl, new RemoveBgService.RemoveBgCallback() {
            @Override
            public void OnSuccess() {
                handler.post(() -> {
                    loadRemovedBgImage();
                    initButtonInteractions();
                    binding.progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void OnFailure(Exception e) {
                Log.e("RemoveBgFragment", Objects.requireNonNull(e.getMessage()));
                handler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), getResources().getString(R.string.bg_removal_failure), Toast.LENGTH_SHORT).show();
                });
            }
        }));
        thread.start();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(requireActivity().getApplication(), requireActivity())).get(RemoveBgFragmentViewModel.class);
        binding = FragmentRemoveBgBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        restoreStates();

        String processedImageB64 = RemoveBgService.getProcessedImageB64();

        binding.btnClose.setOnClickListener(v -> {
            requireActivity().setResult(Activity.RESULT_CANCELED);
            requireActivity().finish();
        });

        if (originalImageUrl == null && processedImageB64 == null) {
            Toast.makeText(requireContext(), getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (processedImageB64 == null) {
            RequestOptions options = new RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.turtle_huh);
            Glide.with(binding.ivRemovedBg.getContext())
                    .load(originalImageUrl)
                    .apply(options)
                    .into(binding.ivRemovedBg);

            removeBgAsync();
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            loadRemovedBgImage();
            initButtonInteractions();
        }
    }

    private void initButtonInteractions() {
        binding.btnSave.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_remove_bg);
            Bundle args = new Bundle();
            args.putBoolean("processedImageB64", true);
            navController.navigate(R.id.action_removeBgFragment_to_boardChoosingFragment, args);
        });
        binding.progressBar.setVisibility(View.GONE);
    }

    private void loadRemovedBgImage() {
        RequestOptions options = new RequestOptions()
                .fitCenter()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.turtle_huh);
        Glide.with(binding.ivRemovedBg.getContext())
                .asBitmap()
                .load(Base64.decode(RemoveBgService.getProcessedImageB64(), Base64.DEFAULT))
                .apply(options)
                .into(binding.ivRemovedBg);
    }

    void restoreStates() {
        String oldOriginalImageUrl = viewModel.getOriginalImageUrl();
        if (oldOriginalImageUrl != null) {
            originalImageUrl = oldOriginalImageUrl;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.setOriginalImageUrl(originalImageUrl);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}