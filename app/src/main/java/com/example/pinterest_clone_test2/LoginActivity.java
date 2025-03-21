package com.example.pinterest_clone_test2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.pinterest_clone_test2.databinding.ActivityMainBinding;
import com.example.pinterest_clone_test2.models.User;
import com.example.pinterest_clone_test2.ui.auth.FragmentLoginEmail;
import com.example.pinterest_clone_test2.ui.auth.FragmentLoginPassword;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterBirthdate;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterGender;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterName;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterPassword;

public class LoginActivity extends AppCompatActivity {
    private User user;
    FragmentManager fragmentManager = getSupportFragmentManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        user = new User();
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentLoginEmail())
                .commit();
    }

    private boolean isExist(String email){
        return false;
    }
    public void updateEmail(String email){
        user.setEmail(email);
        if(!isExist(email)){
            fragmentManager.beginTransaction()
                    .replace(R.id.login_fragment_container, new FragmentRegisterPassword())
                    .commit();
        }
        else{
            fragmentManager.beginTransaction()
                    .replace(R.id.login_fragment_container, new FragmentLoginPassword())
                    .commit();
        }
    }
    public void registerPassword(String password){
        user.setPassword(password);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterName())
                .commit();
    }

    public void registerName(String name){
        user.setFirstName(name);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterBirthdate(name))
                .commit();
    }

    public void registerBirthdate(String birthdate){
        user.setBirthDate(birthdate);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterGender())
                .commit();
    }
    public void registerGender(String gender){
        user.setGender(gender);
    }
}
