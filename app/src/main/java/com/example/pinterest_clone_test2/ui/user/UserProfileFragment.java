package com.example.pinterest_clone_test2.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentUserProfileBinding;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UserProfileFragment extends Fragment {
    private FragmentUserProfileBinding binding;
    private String userId;
    private String source;
    private final List<Pin> userPins = new ArrayList<>();
    private PinListAdapter pinAdapter;
    private boolean isFollowing = false;
    private boolean isSelf = false;

    private int followersCount;
    private int followingCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            source = getArguments().getString("source");
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

        // Check if this is the current user's profile
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && userId != null && userId.equals(currentUser.getUid())) {
            isSelf = true;
            binding.btnFollow.setVisibility(View.GONE);
        }

        // Set up the RecyclerView for pins
        setupRecyclerView();

        if (userId != null) {
            loadUserInfo();
            checkFollowStatus();
            loadUserPins();
        }

        binding.btnBack.setOnClickListener(v -> {
            NavController navController;
            if (Objects.equals(source, "pinDeepLink")) {
                navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_pin_deep_link);
            } else {
                navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            }
            navController.navigateUp();
        });

        binding.btnFollow.setOnClickListener(v -> toggleFollow());
    }

    private void setupRecyclerView() {
        // Initialize the pin adapter
        pinAdapter = new PinListAdapter(requireContext(), userPins, (position, v) -> {
            // Navigate to PinFragment (similar to how it's done in BoardDetailFragment)
            NavController navController;
            if (Objects.equals(source, "pinDeepLink")) {
                navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_pin_deep_link);
            } else {
                navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            }

            Bundle args = new Bundle();
            args.putParcelableArrayList("pins", new ArrayList<>(userPins));
            args.putInt("position", position);
            args.putString("source", source);

            // Navigate to the appropriate pin fragment based on which tab is active
            int action = R.id.action_userProfileFragment_to_pinFragment;
            if (Objects.equals(source, "search")) {
                action = R.id.action_userProfileFragment2_to_pinFragment2;
            } else if (Objects.equals(source, "account")) {
                action = R.id.action_userProfileFragment3_to_pinFragment3;
            } else if (Objects.equals(source, "pinDeepLink")) {
                action = R.id.action_userProfileFragmentDeepLink_to_pinFragmentDeepLink;
            }
            navController.navigate(action, args);
        });

        // Set up the RecyclerView with a StaggeredGridLayoutManager
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvUserPins.setLayoutManager(layoutManager);
        binding.rvUserPins.setHasFixedSize(true);
        binding.rvUserPins.setAdapter(pinAdapter);
    }

    private void checkFollowStatus() {
        if (isSelf) return;

        FirebaseUserService.checkIfFollowing(userId, new FirebaseUserService.CheckFollowStatusCallback() {
            @Override
            public void OnSuccess(boolean following) {
                isFollowing = following;
                updateFollowButton();
            }

            @Override
            public void OnFailure(Exception e) {
                Toast.makeText(requireContext(), R.string.check_status_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFollowButton() {
        if (isSelf) {
            binding.btnFollow.setVisibility(View.GONE);
            return;
        }

        if (isFollowing) {
            binding.btnFollow.setText(R.string.following);
            binding.btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.black));
        } else {
            binding.btnFollow.setText(R.string.follow);
        }
    }

    private void toggleFollow() {
        if (isSelf) return;

        binding.btnFollow.setEnabled(false);

        if (isFollowing) {
            // Unfollow
            FirebaseUserService.unfollowUser(userId, new FirebaseUserService.FollowUserCallback() {
                @Override
                public void OnSuccess() {
                    isFollowing = false;
                    updateFollowButton();
                    updateFollowerCount(-1);
                    binding.btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.colorRed));
                    binding.btnFollow.setEnabled(true);
                }

                @Override
                public void OnFailure(Exception e) {
                    Toast.makeText(requireContext(), R.string.unfollow_failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnFollow.setEnabled(true);
                }
            });
        } else {
            // Follow
            FirebaseUserService.followUser(userId, new FirebaseUserService.FollowUserCallback() {
                @Override
                public void OnSuccess() {
                    isFollowing = true;
                    updateFollowButton();
                    updateFollowerCount(1);
                    binding.btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.black));
                    binding.btnFollow.setEnabled(true);
                }

                @Override
                public void OnFailure(Exception e) {
                    Toast.makeText(requireContext(), R.string.follow_failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnFollow.setEnabled(true);
                }
            });
        }
    }

    private void updateFollowerCount(int delta) {
        followersCount += delta;
        binding.tvFollowers.setText((String.format(Locale.US, "%d %s", followersCount, getString(R.string.followers))));
    }

    private void loadUserInfo() {
        binding.progressLoading.setVisibility(View.VISIBLE);

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
                followersCount = followers != null ? followers.size() : 0;
                binding.tvFollowers.setText(String.format(Locale.US, "%d %s", followersCount, getString(R.string.followers)));

                List<String> following = (List<String>) documentSnapshot.get("following");
                followingCount = following != null ? following.size() : 0;
                binding.tvFollowing.setText(String.format(Locale.US, "%d %s", followingCount, getString(R.string.following)));
            }

            @Override
            public void OnFailure(Exception e) {
                binding.progressLoading.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.loading_user_info_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserPins() {
        binding.progressLoading.setVisibility(View.VISIBLE);
        binding.tvNoPins.setVisibility(View.GONE);

        FirebaseUserService.getUserPins(userId, new FirebaseUserService.GetUserPinsCallback() {
            @Override
            public void OnSuccess(List<Pin> pins) {
                binding.progressLoading.setVisibility(View.GONE);

                if (pins.isEmpty()) {
                    binding.tvNoPins.setVisibility(View.VISIBLE);
                    return;
                }

                int oldSize = userPins.size();
                userPins.clear();
                pinAdapter.notifyItemRangeRemoved(0, oldSize);
                userPins.addAll(pins);
                pinAdapter.notifyItemRangeInserted(0, userPins.size());
            }

            @Override
            public void OnFailure(Exception e) {
                binding.progressLoading.setVisibility(View.GONE);
                binding.tvNoPins.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), R.string.loading_user_info_failed + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}