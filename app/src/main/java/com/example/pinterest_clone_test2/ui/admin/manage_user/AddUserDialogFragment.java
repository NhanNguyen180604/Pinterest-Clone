package com.example.pinterest_clone_test2.ui.admin.manage_user;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.User;

import java.util.Arrays;

public class AddUserDialogFragment extends DialogFragment {
    public interface AddUserCallback {
        void onAdd(String password, String email, String name, String birthDate, User.Gender gender, User.Role role);
    }
    private final AddUserCallback callback;

    public AddUserDialogFragment(AddUserCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        var view = inflater.inflate(R.layout.dialog_add_user, null);

        // Khởi tạo các EditText
        EditText nameInput = view.findViewById(R.id.edit_name);
        EditText emailInput = view.findViewById(R.id.edit_email);
        EditText passwordInput = view.findViewById(R.id.edit_password);
        EditText birthDateInput = view.findViewById(R.id.edit_birth_date);

        // Khởi tạo Spinner
        Spinner genderSpinner = view.findViewById(R.id.spinner_gender);
        Spinner roleSpinner = view.findViewById(R.id.spinner_role);

        // Adapter cho Gender Spinner
        ArrayAdapter<CharSequence> genderAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.stream(User.Gender.values()).map(Enum::name).toArray(String[]::new)
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Adapter cho Role Spinner
        ArrayAdapter<CharSequence> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.stream(User.Role.values()).map(Enum::name).toArray(String[]::new)
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Thêm tài khoản")
                .setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    // Lấy dữ liệu từ form
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    String birthDate = birthDateInput.getText().toString().trim();

                    // Kiểm tra dữ liệu
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(getContext(), "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(getContext(), "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(birthDate)) {
                        Toast.makeText(getContext(), "Vui lòng nhập ngày sinh", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Lấy giá trị từ spinner
                    User.Gender gender = User.Gender.valueOf(genderSpinner.getSelectedItem().toString());
                    User.Role role = User.Role.valueOf(roleSpinner.getSelectedItem().toString());

                    // Gọi callback
                    callback.onAdd(password, email, name, birthDate, gender, role);
                })
                .setNegativeButton("Hủy", null)
                .create();
    }
}