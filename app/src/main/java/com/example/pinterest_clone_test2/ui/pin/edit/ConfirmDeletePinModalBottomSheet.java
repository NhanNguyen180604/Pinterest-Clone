package com.example.pinterest_clone_test2.ui.pin.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.databinding.ConfirmDeletePinModalBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ConfirmDeletePinModalBottomSheet extends BottomSheetDialogFragment {
    ConfirmDeletePinModalBottomSheetBinding binding;
    ConfirmDeletePinCallback callback;
    public static String TAG = "ConfirmDeletePin";

    public ConfirmDeletePinModalBottomSheet(ConfirmDeletePinCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ConfirmDeletePinModalBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            callback.Delete();
            dismiss();
        });
    }

    public interface ConfirmDeletePinCallback {
        void Delete();
    }
}
