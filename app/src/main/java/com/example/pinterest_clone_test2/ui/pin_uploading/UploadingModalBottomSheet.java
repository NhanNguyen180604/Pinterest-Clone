package com.example.pinterest_clone_test2.ui.pin_uploading;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UploadingModalBottomSheet extends BottomSheetDialogFragment {

    ImageButton btn_add_new_pin, btn_add_new_board;
    TextView tv_add_new_pin, tv_add_new_board;
    ImageButton btn_dismiss;

    public static String TAG = "PinUploadingBottomSheet";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.creating_modal_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_add_new_pin = view.findViewById(R.id.btn_add_new_pin);
        btn_add_new_board = view.findViewById(R.id.btn_add_new_board);
        tv_add_new_pin = view.findViewById(R.id.tv_add_new_pin);
        tv_add_new_board = view.findViewById(R.id.tv_add_new_board);
        btn_dismiss = view.findViewById(R.id.btn_dismiss);

        btn_add_new_pin.setOnClickListener(newPinClickListener);
        tv_add_new_pin.setOnClickListener(newPinClickListener);
        btn_add_new_board.setOnClickListener(newBoardClickListener);
        tv_add_new_board.setOnClickListener(newBoardClickListener);
        btn_dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private final View.OnClickListener newPinClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getContext(), "create new pin", Toast.LENGTH_SHORT).show();
        }
    };

    private final View.OnClickListener newBoardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getContext(), "create new board", Toast.LENGTH_SHORT).show();
        }
    };
}
