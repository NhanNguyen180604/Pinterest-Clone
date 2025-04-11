package com.example.pinterest_clone_test2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.pinterest_clone_test2.databinding.ActivityMainBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.upload.UploadDialogFragment;
import com.example.pinterest_clone_test2.utils.CloudinaryManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        FirebaseUserService.initUserDocument();

        // Initialize Cloudinary
        CloudinaryManager.initCloudinary(this);

        Toast.makeText(this, "Xin chào " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

        // delay to fetch current user and init cloudinary, pray that this works
        new Handler().postDelayed(() -> {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_pin_deep_link);
            binding.navView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_upload_tab) {
                    UploadDialogFragment dialog = new UploadDialogFragment();
                    dialog.show(getSupportFragmentManager(), "UploadDialog");
                    return false;
                } else {
                    NavigationUI.onNavDestinationSelected(item, navController);
                    return true;
                }
            });
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}