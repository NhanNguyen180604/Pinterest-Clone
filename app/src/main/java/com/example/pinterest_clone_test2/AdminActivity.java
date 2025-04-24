package com.example.pinterest_clone_test2;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.pinterest_clone_test2.databinding.ActivityAdminBinding;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private ImageButton btnBackToUser;
    private TextView tvAdminTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập header
        setupHeader();

        // Thiết lập navigation
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_admin);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Lắng nghe sự thay đổi điểm đến để cập nhật tiêu đề
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Cập nhật tiêu đề dựa trên ID của fragment đích
            updateTitle(destination.getId());
        });
    }

    private void setupHeader() {
        // Lấy tham chiếu đến các thành phần trong header
        // Sửa lỗi ở đây - lấy thành phần từ layout của admin header
        btnBackToUser = binding.adminHeaderLayout.btnBackToUser;
        tvAdminTitle = binding.adminHeaderLayout.tvAdminTitle;

        // Thiết lập sự kiện click cho nút Back
        btnBackToUser.setOnClickListener(v -> {
            finish(); // Kết thúc AdminActivity để quay về MainActivity
        });
    }

    private void updateTitle(int destinationId) {
        // Cập nhật tiêu đề dựa trên ID của destination
        if (destinationId == R.id.manageUserFragment) {
            tvAdminTitle.setText(R.string.tab_manage_user);
        } else if (destinationId == R.id.manageCommentFragment) {
            tvAdminTitle.setText(R.string.tab_manage_comment);
        } else if (destinationId == R.id.managePinFragment) {
            tvAdminTitle.setText(R.string.tab_manage_pin);
        } else if (destinationId == R.id.manageReportFragment) {
            tvAdminTitle.setText(R.string.tab_manage_report);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}