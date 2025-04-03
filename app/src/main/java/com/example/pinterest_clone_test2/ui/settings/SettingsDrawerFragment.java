package com.example.pinterest_clone_test2.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pinterest_clone_test2.AdminActivity;
import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.MainActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentSettingsDrawerBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Objects;

public class SettingsDrawerFragment extends Fragment {

    FragmentSettingsDrawerBinding binding;
    FirebaseAuth auth;

    public SettingsDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsDrawerBinding.inflate(inflater, container, false);

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(binding.getRoot());
            navController.navigateUp();
        });
        binding.btnAccountManagement.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(binding.getRoot());
            navController.navigate(R.id.action_settingsDrawerFragment_to_account_management);
        });

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            binding.tvUsername.setText(user.getDisplayName());
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("userId", user.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);
                        if (Objects.equals(userDocument.get("role"), "Admin")) {
                            binding.btnGoToAdmin.setOnClickListener(v -> {
                                Intent intent = new Intent(requireActivity(), AdminActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            });
                            binding.btnGoToAdmin.setVisibility(View.VISIBLE);
                        }

                    })
                    .addOnFailureListener(e -> Log.e("firebase-firestore", "Failed to fetch user info", e));

            binding.btnLogout.setOnClickListener(v -> {
                auth.signOut();
                MainActivity activity = (MainActivity) requireActivity();
                Intent intent = new Intent(activity, LoginActivity.class);
                startActivity(intent);
                activity.finish();
            });
        } else {
            Log.e("firebase-auth", "What in the actual fuck, how is the user null here");
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}