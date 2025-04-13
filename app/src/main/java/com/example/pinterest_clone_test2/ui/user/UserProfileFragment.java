package com.example.pinterest_clone_test2.ui.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentUserProfileBinding;
import com.example.pinterest_clone_test2.models.User;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {
    private FragmentUserProfileBinding binding;
    private String userId;
    private User user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (userId != null) {
            loadUserInfo();
            loadUserPins();
        }

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigateUp();
        });
    }

    private void loadUserInfo() {
        FirebaseUserService.getUserInfos(userId, new FirebaseUserService.GetUserInfoCallback() {
            @Override
            public void OnSuccess(DocumentSnapshot documentSnapshot) {
                String name = documentSnapshot.getString("name");
                String avatarUrl = documentSnapshot.getString("avatarUrl");


                binding.tvName.setText(name);

                if (avatarUrl != null) {
                    Glide.with(binding.ivUserAvatar.getContext())
                            .load(avatarUrl)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_loading)
                                    .error(R.drawable.turtle_huh))
                            .into(binding.ivUserAvatar);
                }
                List<String> followers = (List<String>) documentSnapshot.get("followers");
                int followersCount = followers != null ? followers.size() : 0;
                String followerString = binding.tvFollowers.getText().toString();
                binding.tvFollowers.setText(followersCount + " " + followerString);

                List<String> following = (List<String>) documentSnapshot.get("following");
                int followingCount = following != null ? following.size() : 0;
                String followingString = binding.tvFollowing.getText().toString();
                binding.tvFollowing.setText(followingCount+ " " + followingString);

                
            }

            @Override
            public void OnFailure(Exception e) {
                Toast.makeText(requireContext(), "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserPins() {
        // Tương tự như trong BoardTabObjectFragment, tải danh sách Pins của người dùng
    }
}