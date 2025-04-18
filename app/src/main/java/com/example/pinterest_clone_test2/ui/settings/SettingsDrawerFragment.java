package com.example.pinterest_clone_test2.ui.settings;

import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.AdminActivity;
import com.example.pinterest_clone_test2.LoginActivity;
import com.example.pinterest_clone_test2.MainActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentSettingsDrawerBinding;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

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

        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        if (currentUserDocument != null) {
            binding.tvUsername.setText(currentUserDocument.getString("name"));

            RequestOptions glideOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.turtle_huh)
                    .centerCrop();

            Glide.with(binding.ivAvatar.getContext())
                    .load(currentUserDocument.getString("avatarUrl"))
                    .apply(glideOptions)
                    .into(binding.ivAvatar);

            if (Objects.equals(currentUserDocument.get("role"), "Admin")) {
                binding.btnGoToAdmin.setOnClickListener(v -> {
                    Intent intent = new Intent(requireActivity(), AdminActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                });
                binding.btnGoToAdmin.setVisibility(View.VISIBLE);
            }

            binding.btnLogout.setOnClickListener(v -> {
                auth.signOut();
                MainActivity activity = (MainActivity) requireActivity();
                Intent intent = new Intent(activity, LoginActivity.class);
                startActivity(intent);
                activity.finish();
            });

            binding.viewProfileBtn.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("userId", currentUserDocument.getId());
                args.putString("source", "account");
                navController.navigate(R.id.action_settingsDrawerFragment_to_userProfileFragment3, args);
            });
        } else {
            Toast.makeText(requireContext(), getResources().getString(R.string.fetch_user_failure), Toast.LENGTH_SHORT).show();
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}