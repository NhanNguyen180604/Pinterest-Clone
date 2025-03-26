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

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.UploadActivity;
import com.example.pinterest_clone_test2.databinding.FragmentUploadImageDetailsBinding;
import com.example.pinterest_clone_test2.ui.home.HomeFragment;

public class UploadImageDetailsFragment extends Fragment {

    private FragmentUploadImageDetailsBinding binding;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUploadImageDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nhận dữ liệu từ bundle
        if (getArguments() != null) {
            imageUri = getArguments().getParcelable("imageUri");
            if (imageUri != null) {
                binding.selectedImageView.setImageURI(imageUri);
            } else {
                Toast.makeText(getContext(), "Không có ảnh nào được chọn", Toast.LENGTH_SHORT).show();
            }
        }

        // Back button
        binding.btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Chọn bảng
        binding.chooseBoardContainer.setOnClickListener(v ->
                Toast.makeText(getContext(), "Chức năng chọn bảng", Toast.LENGTH_SHORT).show()
        );

        // Gắn thẻ chủ đề
        binding.tagTopicContainer.setOnClickListener(v ->
                Toast.makeText(getContext(), "Chức năng gắn thẻ chủ đề", Toast.LENGTH_SHORT).show()
        );

        // Cài đặt nâng cao
        binding.advancedSettingContainer.setOnClickListener(v ->
                Toast.makeText(getContext(), "Chức năng cài đặt nâng cao", Toast.LENGTH_SHORT).show()
        );

        binding.btnCreate.setOnClickListener(v -> onCreatePin());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onCreatePin() {
        String title = binding.titleEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();
        String link = binding.linkEditText.getText().toString().trim();

        // Gọi phương thức createPin trong Activity để xử lý logic tạo ghim
        if (getActivity() instanceof UploadActivity) {
            ((UploadActivity) getActivity()).createPin(title, description, link);
        }

    }

}
