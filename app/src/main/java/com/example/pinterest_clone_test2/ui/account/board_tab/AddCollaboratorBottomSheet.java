package com.example.pinterest_clone_test2.ui.account.board_tab;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.EditCollaboratorActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.AddCollaboratorsBottomSheetBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;

public class AddCollaboratorBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_BOARD_ID = "board_id";
    private String boardId;

    public static AddCollaboratorBottomSheet newInstance(String boardId) {
        AddCollaboratorBottomSheet fragment = new AddCollaboratorBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_BOARD_ID, boardId);
        fragment.setArguments(args);
        return fragment;
    }

    public AddCollaboratorBottomSheet() {

    }

    AddCollaboratorsBottomSheetBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boardId = getArguments().getString(ARG_BOARD_ID);
            Log.d("AddCollaboratorBottomSheet", "Board ID: " + boardId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = AddCollaboratorsBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        String userId = currentUserDocument.getString("userId");
        binding.clEditBoardPermission.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditCollaboratorActivity.class);
            startActivity(intent);
        });
        binding.btnClose.setOnClickListener(
                v -> {
                    dismiss();
                }
        );
        binding.clCopyLink.setOnClickListener(v -> {
            if (boardId != null) {
                String inviteLink = "https://open.my.pinterest-clone/add-collaborators?boardId=" + boardId + "\\&userId=" + userId;

                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Invite Link", inviteLink);
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);

                Toast.makeText(requireContext(), "Invite link copied!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Board ID not available", Toast.LENGTH_SHORT).show();
            }

        });
    }
}
