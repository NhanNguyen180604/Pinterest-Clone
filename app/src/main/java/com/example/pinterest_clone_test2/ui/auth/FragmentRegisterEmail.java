package com.example.pinterest_clone_test2.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;


public class FragmentRegisterEmail extends Fragment {
    EditText emailInput;
    Button continueBtn;

    public FragmentRegisterEmail() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up_email, container, false);

        emailInput = view.findViewById(R.id.et_email);
        continueBtn = view.findViewById(R.id.btn_continue);
        continueBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            validateEmail(email, validateCallback);
        });
        return view;
    }

    private void validateEmail(String email, EmailValidateCallback callback) {
        // Kiểm tra định dạng email
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onEmailValidate(email, false, getString(R.string.email_invalid_format));
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.getDocuments().isEmpty()){
                        callback.onEmailValidate(email, false, getString(R.string.email_already_exists));
                    }
                    else {
                        callback.onEmailValidate(email, true, "");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("firebase-firestore", "Could not fetch documents to check if email exists", e);
                    callback.onEmailValidate(email, false, getString(R.string.email_check_error));
                });
    }

    private final EmailValidateCallback validateCallback = new EmailValidateCallback() {
        @Override
        public void onEmailValidate(String email, boolean isValid, String message) {
            if (isValid) {
                ((LoginActivity) requireActivity()).updateEmail(email);
            } else {
                emailInput.setError(message);
            }
        }
    };

    private interface EmailValidateCallback {
        void onEmailValidate(String email, boolean isValid, String message);
    }
}