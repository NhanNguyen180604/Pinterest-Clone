package com.example.pinterest_clone_test2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.example.pinterest_clone_test2.models.User;
import com.example.pinterest_clone_test2.ui.auth.FragmentLoginEmail;
import com.example.pinterest_clone_test2.ui.auth.FragmentLoginPassword;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterBirthdate;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterGender;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterName;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterPassword;

public class LoginActivity extends AppCompatActivity {
    private User user;
    User.UserInfo userInfo = null;
    FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("user_token", null);
        String email = sharedPreferences.getString("user_email", null);
        User.initializeToken(token, email);
        if(token != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user_token",token);
            startActivity(intent);
            finish();
        }else{
            setContentView(R.layout.activity_login);
            user = new User();
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.login_fragment_container, new FragmentLoginEmail())
                    .addToBackStack(null)
                    .commit();
        }

    }


    public void updateEmail(String email){
        user.setEmail(email);
        if(User.isEmailExists(email)){
            fragmentManager.beginTransaction()
                    .replace(R.id.login_fragment_container, new FragmentLoginPassword(email))
                    .addToBackStack(null)
                    .commit();
        }
        else{
            fragmentManager.beginTransaction()
                    .replace(R.id.login_fragment_container, new FragmentRegisterPassword())
                    .addToBackStack(null)
                    .commit();
        }
    }
    public void registerPassword(String password){
        user.setPassword(password);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterName())
                .addToBackStack(null)
                .commit();
    }

    public void registerName(String name){
        user.setFirstName(name);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterBirthdate(name))
                .addToBackStack(null)
                .commit();
    }

    public void registerBirthdate(String birthdate){
        user.setBirthDate(birthdate);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterGender())
                .addToBackStack(null)
                .commit();
    }
    public void registerGender(String gender){
        user.setGender(gender);
        String token = User.register(user);
        transferToMain(token, user.getEmail());
    }

    public void loginEmail(String password) {
        user.setPassword(password);
        String token = User.login(user.getEmail(), user.getPassword());
        if(token == null)
        {
            fragmentManager.beginTransaction()
                    .replace(R.id.login_fragment_container, new FragmentLoginEmail())
                    .addToBackStack(null)
                    .commit();
            Toast toast = Toast.makeText(this, "failed to login", Toast.LENGTH_LONG);
        }else
            transferToMain(token, user.getEmail());
    }

    private void transferToMain(String token, String email){
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_token", token);
        editor.putString("user_email", email);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user_token", token);
        startActivity(intent);
        finish();
    }
}
