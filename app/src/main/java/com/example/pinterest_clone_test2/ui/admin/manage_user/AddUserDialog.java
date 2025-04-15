package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.app.AlertDialog;
import android.app.Dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pinterest_clone_test2.models.User;
import com.example.pinterest_clone_test2.R;

public class AddUserDialog extends DialogFragment {

    public interface OnUserAddedListener {
        void onUserAdded(User user);
    }

    private final OnUserAddedListener listener;

    public AddUserDialog(OnUserAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        var view = inflater.inflate(R.layout.dialog_add_user, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Thêm tài khoản mới")
                .setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String firstName = ((EditText) view.findViewById(R.id.edit_first_name)).getText().toString().trim();
                    String lastName = ((EditText) view.findViewById(R.id.edit_last_name)).getText().toString().trim();
                    String email = ((EditText) view.findViewById(R.id.edit_email)).getText().toString().trim();
                    String password = ((EditText) view.findViewById(R.id.edit_password)).getText().toString().trim();
                    String birthDate = ((EditText) view.findViewById(R.id.edit_birth_date)).getText().toString().trim();

                    Spinner genderSpinner = view.findViewById(R.id.spinner_gender);
                    Spinner roleSpinner = view.findViewById(R.id.spinner_role);
                    String genderStr = genderSpinner.getSelectedItem().toString();
                    String roleStr = roleSpinner.getSelectedItem().toString();

                    User.Gender gender = User.Gender.valueOf(genderStr.toUpperCase());
                    User.Role role = User.Role.valueOf(roleStr.toUpperCase());

                    if (!email.isEmpty() && !password.isEmpty()) {
                        User newUser = new User(password, email, firstName, lastName, birthDate, gender, role);
                        listener.onUserAdded(newUser);
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .create();
    }
}

