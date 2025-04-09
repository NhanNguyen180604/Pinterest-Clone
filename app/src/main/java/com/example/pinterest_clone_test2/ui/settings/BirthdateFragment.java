package com.example.pinterest_clone_test2.ui.settings;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBirthdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
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
        binding.btnBirthdateUpdate.setOnClickListener(v->{
            String newBirthdate = binding.etBirthdate.getText().toString();
            binding.btnBirthdateUpdate.setEnabled(false);
            binding.btnBirthdateUpdate.setText("Đang cập nhật...");
            FirebaseUserService.updateBirthdate(newBirthdate, new FirebaseUserService.UpdateBirthdateCallback(){
                @Override
                public void OnSuccess() {
                    Toast.makeText(requireContext(), "Cập nhật ngày sinh thành công", Toast.LENGTH_SHORT).show();
                    binding.btnBack.performClick();
                }
                @Override
                public void OnFailure(Exception e) {
                    Toast.makeText(requireContext(), "Cập nhật ngày sinh thất bại", Toast.LENGTH_SHORT).show();
                }
            });
            binding.btnBirthdateUpdate.setEnabled(true);
            binding.btnBirthdateUpdate.setText("Cập nhật");
        });
    }
}