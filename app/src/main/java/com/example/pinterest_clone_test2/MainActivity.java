package com.example.pinterest_clone_test2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.pinterest_clone_test2.broadcast_receivers.DownloadMediaBroadcastReceiver;
import com.example.pinterest_clone_test2.databinding.ActivityMainBinding;
import com.example.pinterest_clone_test2.services.download.PinMediaDownloader;
import com.example.pinterest_clone_test2.services.firebase.FirebaseTagService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.upload.UploadDialogFragment;
import com.example.pinterest_clone_test2.utils.CloudinaryManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    DownloadMediaBroadcastReceiver downloadMediaBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseTagService.initFixedTags(this);
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        FirebaseUserService.initUserDocument();

        // Initialize Cloudinary
        CloudinaryManager.initCloudinary(this);

        Toast.makeText(
                this,
                String.format(Locale.US, getResources().getString(R.string.hello_user_string_template), user.getDisplayName()),
                Toast.LENGTH_SHORT
        ).show();

        // delay to fetch current user and init cloudinary, pray that this works
        new Handler().postDelayed(() -> {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
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
    protected void onStart() {
        super.onStart();
        downloadMediaBroadcastReceiver = new DownloadMediaBroadcastReceiver();
        IntentFilter filter = new IntentFilter(PinMediaDownloader.ACTION_PIN_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadMediaBroadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // my IDE is highlighting this as an error, but it still works
            registerReceiver(downloadMediaBroadcastReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(downloadMediaBroadcastReceiver);
        } catch (Exception e) {
            //eat exception
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}