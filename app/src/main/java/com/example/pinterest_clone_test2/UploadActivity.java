package com.example.pinterest_clone_test2;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pinterest_clone_test2.ui.upload.UploadFragment;
import com.example.pinterest_clone_test2.ui.upload.UploadImageDetailsFragment;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_upload_container, new UploadFragment())
                    .commit();
        }
    }

    public void showDetailFragment(Uri imageUri) {
        UploadImageDetailsFragment detailsFragment = new UploadImageDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("imageUri", imageUri);
        detailsFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_upload_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }
    // Phương thức xử lý tạo Ghim
    public void createPin(String title, String description, String link) {
        // Xử lý việc tạo ghim (validate, lưu vào database, hoặc chuyển sang màn hình tiếp theo)
        if (title.isEmpty() ) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        } else {
            // Giả sử chúng ta chỉ hiển thị Toast, bạn có thể thay thế bằng logic lưu vào database hoặc gì đó.
            Toast.makeText(this, "Tạo Ghim với tiêu đề: " + title, Toast.LENGTH_SHORT).show();
        }
    }

}
