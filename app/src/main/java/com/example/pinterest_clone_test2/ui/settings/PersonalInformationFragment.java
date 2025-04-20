package com.example.pinterest_clone_test2.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentPersonalInformationBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class PersonalInformationFragment extends Fragment {
    FragmentPersonalInformationBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPersonalInformationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        String gender = currentUserDocument.getString("gender");
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });
        binding.btnBirthdate.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_personal_information_to_birthdate);
        });

        Locale locale = getResources().getConfiguration().getLocales().get(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.calendar_picker_date_format), locale);
        // fuck
        SimpleDateFormat viFormat = new SimpleDateFormat("dd 'thg' M, yyyy", new Locale("vi", "VN"));
        SimpleDateFormat enFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String birthdate = currentUserDocument.getString("birthdate");

        if (birthdate == null) {
            birthdate = getString(R.string.birthdate_placeholder);
        }
        try {
            String formattedDate = dateFormat.format(viFormat.parse(birthdate));
            binding.tvYourBirthdate.setText(formattedDate);
        } catch (ParseException e) {
            try {
                String formattedDate = dateFormat.format(enFormat.parse(birthdate));
                binding.tvYourBirthdate.setText(formattedDate);
            } catch (ParseException e2) {
                // eat shit
                // this better not happen
            }
        }

        binding.btnGender.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_personal_information_to_gender);
        });
        if (Objects.equals(gender, "Nam")) {
            binding.tvYourGender.setText(getResources().getString(R.string.male_text));
        } else {
            binding.tvYourGender.setText(Objects.equals(gender, "Nữ") ? getResources().getString(R.string.female_text) : getResources().getString(R.string.other_gender_text));
        }
    }
}