package com.example.pinterest_clone_test2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.pinterest_clone_test2.databinding.ActivityMainBinding;
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
//        auth.signOut();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Toast.makeText(this, "Xin chào " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}