package com.example.pinterest_clone_test2.ui.board;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.CreateBoardActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentCreateNewBoardBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class CreateNewBoardFragment extends BottomSheetDialogFragment {
    FragmentCreateNewBoardBinding binding;
    BoardViewModelObservable viewModel;

    public CreateNewBoardFragment() {
        viewModel = new BoardViewModelObservable();
    }

    public CreateNewBoardFragment(@Nullable Pin pin) {
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
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.turtle_huh)
                .centerCrop();

        if (viewModel.getPin() != null) {
            binding.cvPreviewImage.setVisibility(View.VISIBLE);
            Glide.with(binding.ivPreviewImage.getContext())
                    .load(viewModel.getPin().getThumbnailUrl())
                    .apply(options)
                    .into(binding.ivPreviewImage);
        } else {
            binding.cvPreviewImage.setVisibility(View.GONE);
        }

        binding.btnClose.setOnClickListener(v -> {
            Activity activity = requireActivity();
            if (activity instanceof CreateBoardActivity) {
                activity.finish();
            } else {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnCreate.setOnClickListener(v -> {
            binding.btnCreate.setEnabled(false);

            Intent data = new Intent();

            // TODO: add real collaborators
            Board board = new Board()
                    .setName(viewModel.getBoardName())
                    .setCollaborators(new ArrayList<>())
                    .setPublic(!viewModel.getIsPrivate());

            if (viewModel.getPin() != null) {
                List<String> pins = new ArrayList<>();
                pins.add(viewModel.getPin().getId());
                board.setPins(pins);
            }

            FirebaseBoardService.createNewBoard(board, new FirebaseBoardService.CreateBoardServiceCallback() {
                @Override
                public void OnSuccess(DocumentReference documentReference) {
                    if (viewModel.getPin() == null) {
                        Toast.makeText(requireContext(), getResources().getString(R.string.create_board_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                requireContext(),
                                String.format(getResources().getString(R.string.pin_save_to_board_template), viewModel.getBoardName()),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    data.putExtra("boardId", documentReference.getId());
                    data.putExtra("boardName", viewModel.getBoardName());
                    requireActivity().setResult(Activity.RESULT_OK, data);
                    requireActivity().finish();
                }

                @Override
                public void OnFailure(Exception e) {
                    Toast.makeText(requireContext(), getResources().getString(R.string.create_board_failure), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    // reset button state
                    binding.setViewModel(viewModel);
                }
            });
        });
    }
}
