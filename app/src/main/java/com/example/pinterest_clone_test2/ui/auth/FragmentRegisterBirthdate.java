package com.example.pinterest_clone_test2.ui.auth;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FragmentRegisterBirthdate extends Fragment {
    private Calendar calendar;
    TextView dateDisplay;
    Button btnNext;

    ImageButton btnBack;
    TextView tvTitle;
    private String name;
    public FragmentRegisterBirthdate() {
        // Required empty public constructor
    }
    public FragmentRegisterBirthdate(String name) {
        // Required empty public constructor
        this.name = name;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_birthdate, container, false);
        dateDisplay = view.findViewById(R.id.tv_date_display);
        btnNext = view.findViewById(R.id.btn_next);
        tvTitle = view.findViewById(R.id.tv_title);
        calendar = Calendar.getInstance();
        btnBack = view.findViewById(R.id.btn_back);

        String title = "Xin chào "+ name + "! Nhập ngày sinh của bạn";
        tvTitle.setText(title);
        dateDisplay.setOnClickListener(v -> showCustomDatePicker());

        btnNext.setOnClickListener(v -> {
            String birthdate = dateDisplay.getText().toString();
            ((LoginActivity) requireActivity()).registerBirthdate(birthdate);
        });

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    // Java implementation
    private void showCustomDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                new ContextThemeWrapper(requireContext(), R.style.CustomDatePickerDialog),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set Vietnamese button text
        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "HỦY", datePickerDialog);
        datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", datePickerDialog);

        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        // Format like "21 thg 3, 2025"
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'thg' M, yyyy", new Locale("vi", "VN"));
        String formattedDate = dateFormat.format(calendar.getTime());
        dateDisplay.setText(formattedDate);
    }
}

