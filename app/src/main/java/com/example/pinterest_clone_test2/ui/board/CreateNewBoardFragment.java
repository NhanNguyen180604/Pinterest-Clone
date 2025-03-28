package com.example.pinterest_clone_test2.ui.board;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentCreateNewBoardBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CreateNewBoardFragment extends BottomSheetDialogFragment {
    FragmentCreateNewBoardBinding binding;
    BoardViewModelObservable viewModel;

    public CreateNewBoardFragment() {
        viewModel = new BoardViewModelObservable();
    }

    public CreateNewBoardFragment(@NonNull Pin pin) {
        viewModel = new BoardViewModelObservable(pin);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateNewBoardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setViewModel(viewModel);

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.karyl)
                .error(R.drawable.turtle_huh)
                .centerCrop();

        Glide.with(binding.ivPreviewImage.getContext())
                .load(viewModel.getPin().getThumbnailUrl())
                .apply(options)
                .into(binding.ivPreviewImage);

        binding.btnClose.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        binding.btnCreate.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("added", true);
            data.putExtra("boardName", viewModel.getBoardName());
            data.putExtra("isPrivate", viewModel.getIsPrivate());
            data.putExtra("isNew", true);
            requireActivity().setResult(Activity.RESULT_OK, data);
            requireActivity().finish();
        });
    }
}
