package com.example.pinterest_clone_test2.ui.settings;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentBirthdateBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BirthdateFragment extends Fragment {

    FragmentBirthdateBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBirthdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });
        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        String birthdate = currentUserDocument.getString("birthdate");
        binding.etBirthdate.setText(birthdate);
        binding.etBirthdate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        // 🔥 Set the calendar to the selected date
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Format and set the selected date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'thg' M, yyyy", new Locale("vi", "VN"));
                        String formattedDate = dateFormat.format(calendar.getTime());
                        binding.etBirthdate.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        binding.btnBirthdateUpdate.setOnClickListener(v -> {
            String newBirthdate = binding.etBirthdate.getText().toString();

            // Parse selected date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'thg' M, yyyy", new Locale("vi", "VN"));
            try {
                Calendar birthCal = Calendar.getInstance();
                birthCal.setTime(dateFormat.parse(newBirthdate));

                Calendar today = Calendar.getInstance();
                today.add(Calendar.YEAR, -6); // subtract 18 years from today

                if (birthCal.after(today)) {
                    binding.tvBirthdateWarning.setVisibility(View.VISIBLE);
                    binding.tvBirthdateWarning.setText(getString(R.string.birthdate_warning));
                    return;
                }
                binding.tvBirthdateWarning.setVisibility(View.GONE);
                binding.btnBirthdateUpdate.setEnabled(false);
                binding.btnBirthdateUpdate.setText(getString(R.string.updating));

                FirebaseUserService.updateBirthdate(newBirthdate, new FirebaseUserService.UpdateBirthdateCallback() {
                    @Override
                    public void OnSuccess() {
                        Toast.makeText(requireContext(), getString(R.string.birthdate_update_success), Toast.LENGTH_SHORT).show();
                        binding.btnBack.performClick();
                        binding.btnBirthdateUpdate.setEnabled(true);
                        binding.btnBirthdateUpdate.setText(getText(R.string.update));
                    }

                    @Override
                    public void OnFailure(Exception e) {
                        Toast.makeText(requireContext(), getString(R.string.birthdate_update_failure), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        binding.btnBirthdateUpdate.setEnabled(true);
                        binding.btnBirthdateUpdate.setText(getText(R.string.update));
                    }
                });

            } catch (Exception e) {
                Toast.makeText(requireContext(), R.string.birthdate_not_valid, Toast.LENGTH_SHORT).show();
            }
        });
    }
}