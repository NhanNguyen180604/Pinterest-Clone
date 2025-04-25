package com.example.pinterest_clone_test2.ui.board.board_detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.BoardDetailSetViewBottomSheetBinding;
import com.example.pinterest_clone_test2.interfaces.OnViewModeSelectedListener;
import com.example.pinterest_clone_test2.ui.account.board_tab.AddCollaboratorBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BoardDetailSetViewBottomSheet extends BottomSheetDialogFragment {
    public BoardDetailSetViewBottomSheet(){

    }
    private static final String VIEW_MODE_ID = "view_mode";
    private int viewMode;
    OnViewModeSelectedListener listener;
    public void setOnViewModeSelectedListener(OnViewModeSelectedListener listener) {
        this.listener = listener;
    }

    BoardDetailSetViewBottomSheetBinding binding;
    public static BoardDetailSetViewBottomSheet newInstance(int viewMode) {
        BoardDetailSetViewBottomSheet fragment = new BoardDetailSetViewBottomSheet();
        Bundle args = new Bundle();
        args.putInt(VIEW_MODE_ID, viewMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            viewMode = getArguments().getInt(VIEW_MODE_ID, 2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BoardDetailSetViewBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnClose.setOnClickListener(v->{
            dismiss();
        });
        Log.d("viewmode", String.valueOf(viewMode));
        if(viewMode == 1){
            binding.ivTickWide.setVisibility(View.VISIBLE);
        }
        else if(viewMode == 2){
            binding.ivTickDefault.setVisibility(View.VISIBLE);
        }
        else if(viewMode == 3){
            binding.ivTickCompact.setVisibility(View.VISIBLE);
        }
        binding.tvClickableWide.setOnClickListener(v -> {
            if (listener != null) listener.onViewModeSelected(1);
            dismiss();
        });

        binding.tvClickableDefault.setOnClickListener(v -> {
            if (listener != null) listener.onViewModeSelected(2);
            dismiss();
        });

        binding.tvClickableCompact.setOnClickListener(v -> {
            if (listener != null) listener.onViewModeSelected(3);
            dismiss();
        });
    }
}
