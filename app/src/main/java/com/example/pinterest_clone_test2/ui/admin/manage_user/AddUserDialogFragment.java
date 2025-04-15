package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.User;

public class AddUserDialogFragment extends DialogFragment {
    public interface AddUserCallback {
        void onAdd(String password, String email, String firstName, String lastName, String birthDate, User.Gender gender, User.Role role);
    }

    private final AddUserCallback callback;

    public AddUserDialogFragment(AddUserCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        var view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) var nameInput = view.findViewById(R.id.edit_first_name);
        var emailInput = view.findViewById(R.id.edit_email);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Thêm tài khoản")
                .setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String firstName = ((EditText) view.findViewById(R.id.edit_first_name)).getText().toString().trim();
                    String lastName = ((EditText) view.findViewById(R.id.edit_last_name)).getText().toString().trim();
                    String email = ((EditText) view.findViewById(R.id.edit_email)).getText().toString().trim();
                    String password = ((EditText) view.findViewById(R.id.edit_password)).getText().toString().trim();
                    String birthDate = ((EditText) view.findViewById(R.id.edit_birth_date)).getText().toString().trim();

                    Spinner genderSpinner = view.findViewById(R.id.spinner_gender);
                    Spinner roleSpinner = view.findViewById(R.id.spinner_role);

                    User.Gender gender = User.Gender.valueOf(genderSpinner.getSelectedItem().toString());
                    User.Role role = User.Role.valueOf(roleSpinner.getSelectedItem().toString());

                    if (!email.isEmpty() && !password.isEmpty()) {
                        callback.onAdd(password, email, firstName, lastName, birthDate, gender, role);
                    }
                })
                .setNegativeButton("Hủy", null)
                .create();

    }

}