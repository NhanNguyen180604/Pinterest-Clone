package com.example.pinterest_clone_test2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.pinterest_clone_test2.models.User;
import com.example.pinterest_clone_test2.ui.auth.FragmentLogin;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterBirthdate;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterEmail;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterGender;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterInterests;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterName;
import com.example.pinterest_clone_test2.ui.auth.FragmentRegisterPassword;
import com.example.pinterest_clone_test2.ui.auth.FragmentStartScreen;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private User user;
    String currentPassword = "";
    FragmentManager fragmentManager;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        user = new User();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentStartScreen())
                .addToBackStack(null)
                .commit();
    }

    public void startRegisterFlow() {
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterEmail())
                .addToBackStack(null)
                .commit();
    }

    public void startLoginFlow() {
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentLogin())
                .addToBackStack(null)
                .commit();
    }

    public void updateEmail(String email) {
        user.setEmail(email);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterPassword())
                .addToBackStack(null)
                .commit();
    }

    public void registerPassword(String password) {
        currentPassword = password;
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterName())
                .addToBackStack(null)
                .commit();
    }

    public void registerName(String name) {
        user.setFirstName(name);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterBirthdate(name))
                .addToBackStack(null)
                .commit();
    }

    public void registerBirthdate(String birthdate) {
        user.setBirthDate(birthdate);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterGender())
                .addToBackStack(null)
                .commit();
    }

    public void registerGender(String gender) {
        user.setGender(gender);
        fragmentManager.beginTransaction()
                .replace(R.id.login_fragment_container, new FragmentRegisterInterests())
                .addToBackStack(null)
                .commit();
    }
    public void registerInterests(List<String> interests) {
        // Convert localized tags back to English for storage
        user.setInterests(interests);
        createUser();
    }

    public void login(String email, String password) {
        user.setEmail(email);
        currentPassword = password;
        loginUserEmailPassword();
    }

    void createUser() {
        auth.createUserWithEmailAndPassword(user.getEmail(), currentPassword)
                .addOnSuccessListener(this, task -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;

                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(user.getFirstName())
                            .build();
                    Task<Void> updateProfileTask = firebaseUser.updateProfile(request);

                    // initialize user info
                    Map<String, Object> userInfos = new HashMap<>();
                    userInfos.put("userId", firebaseUser.getUid());
                    userInfos.put("name", user.getFirstName());
                    userInfos.put("email", firebaseUser.getEmail());
                    userInfos.put("role", "User");
                    userInfos.put("gender", user.getGender().name());
                    userInfos.put("birthdate", user.getBirthDate());
                    userInfos.put("interests", user.getInterests());
                    userInfos.put("pins", new ArrayList<String>());
                    userInfos.put("boards", new ArrayList<String>());
                    userInfos.put("collages", new ArrayList<String>());
                    userInfos.put("followingUsers", new ArrayList<String>());
                    userInfos.put("followers", new ArrayList<String>());
                    userInfos.put("notifications", new ArrayList<String>());
                    userInfos.put("blockedUsers", new ArrayList<String>());
                    userInfos.put("blockedPins", new ArrayList<String>());
                    userInfos.put("blockedCollages", new ArrayList<String>());
                    userInfos.put("website", "");

//                    Task<DocumentReference> updateUserInfoTask = db.collection("users")
//                            .add(userInfos);


//                    Task<Void> updateUserInfoTask = db.collection("users")
//                            .document(firebaseUser.getUid())
//                            .set(userInfos);
//
//                    Tasks.whenAllSuccess(updateProfileTask, updateUserInfoTask)
//                            .addOnSuccessListener(objects -> {
//                                Log.d("firebase-cloud-firestore", "User profile updated & Firestore data added successfully");
//
//                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                startActivity(intent);
//                                finish();
//                            })
//                            .addOnFailureListener(e -> {
//                                Log.e("firebase-cloud-firestore", "Error initializing user data", e);
//                                Toast.makeText(LoginActivity.this, "Lỗi khi khởi tạo thông tin người dùng", Toast.LENGTH_SHORT).show();
//                            });
                    new Handler().postDelayed(() -> db.collection("users")
                            .document(firebaseUser.getUid())
                            .set(userInfos)
                            .addOnSuccessListener(objects -> {
                                Log.d("firebase-cloud-firestore", "User profile updated & Firestore data added successfully");

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("firebase-cloud-firestore", "Error initializing user data", e);
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.user_info_init_failure), Toast.LENGTH_SHORT).show();
                            }), 1000);
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.register_failure), Toast.LENGTH_SHORT).show();
                    Log.e("firebase-auth-singup", "Error signing up", e);
                    fragmentManager.beginTransaction()
                            .replace(R.id.login_fragment_container, new FragmentRegisterEmail())
                            .addToBackStack(null)
                            .commit();
                });
    }

    void loginUserEmailPassword() {
        auth.signInWithEmailAndPassword(user.getEmail(), currentPassword)
                .addOnSuccessListener(authResult -> {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_failure), Toast.LENGTH_SHORT).show();
                    Log.e("firebase-auth-login", "Error logging in", e);
                });
    }
}
