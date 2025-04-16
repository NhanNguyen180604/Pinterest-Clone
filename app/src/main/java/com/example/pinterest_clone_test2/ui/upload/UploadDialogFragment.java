package com.example.pinterest_clone_test2.ui.upload;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.UploadActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UploadDialogFragment extends BottomSheetDialogFragment {

    public UploadDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_upload_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bấm nút Ghép
        view.findViewById(R.id.btnCollage).setOnClickListener(v -> {
            dismiss();
            // Truyền thông tin vào UploadActivity để mở CollageFragment
            Intent intent = new Intent(requireContext(), UploadActivity.class);
            intent.putExtra("isCollage", true);  // Truyền thông tin chọn chế độ ghép ảnh
            startActivity(intent);
        });

        // Bấm nút Pin
        view.findViewById(R.id.btnPin).setOnClickListener(v -> {
            dismiss();
            // Truyền thông tin vào UploadActivity để mở UploadFragment
            Intent intent = new Intent(requireContext(), UploadActivity.class);
            intent.putExtra("isPin", true);  // Truyền thông tin chọn chế độ pin ảnh
            startActivity(intent);
        });

        // Bấm nút Bảng
        view.findViewById(R.id.btnBoard).setOnClickListener(v -> {
            dismiss();
            // Xử lý sau nếu cần
        });

        // Bấm nút đóng
        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }
}
