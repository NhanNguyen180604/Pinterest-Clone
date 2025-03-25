package com.example.pinterest_clone_test2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import  android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import androidx.navigation.ui.NavigationUI;

import com.example.pinterest_clone_test2.databinding.ActivityMainBinding;
import com.example.pinterest_clone_test2.models.User;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    User.UserInfo userInfo = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = getIntent().getStringExtra("user_token");
        if (token == null || (userInfo = User.getUserByToken(token)) == null) {
            SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("user_token");
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            Toast toast = Toast.makeText(this, "xin chào " + userInfo.getFirstName(), Toast.LENGTH_SHORT);
            toast.show();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            NavigationUI.setupWithNavController(binding.navView, navController);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}