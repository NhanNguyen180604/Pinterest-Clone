package com.example.pinterest_clone_test2.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserProfileFragment extends Fragment {
    private FragmentUserProfileBinding binding;
    private UserProfileViewModel viewModel;
    private final List<Pin> userPins = new ArrayList<>();
    private PinListAdapter pinAdapter;
    private boolean isSelf = false;

    // Constants for source values
    public static final String SOURCE_HOME = "home";
    public static final String SOURCE_SEARCH = "search";
    public static final String SOURCE_ACCOUNT = "account";
    public static final String SOURCE_PIN_DEEP_LINK = "pinDeepLink";

    // Maps to store navigation controllers and actions by source
    private final Map<String, Integer> navHostResIds = new HashMap<>();
    private final Map<String, Integer> navActionIds = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        // Process arguments and restore state
        if (getArguments() != null) {
            // Only set userId from arguments if it's not already in ViewModel
            if (viewModel.getUserId() == null) {
                viewModel.setUserId(getArguments().getString("userId"));
            }

            // Only set source from arguments if it's not already in ViewModel
            if (viewModel.getSource() == null) {
                viewModel.setSource(getArguments().getString("source", SOURCE_HOME));
            }
        }

        // Initialize navigation mappings
        initNavigationMappings();
    }


     //Initialize navigation host resources and action IDs based on source

    private void initNavigationMappings() {
        // Navigation host resource IDs
        navHostResIds.put(SOURCE_HOME, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(SOURCE_SEARCH, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(SOURCE_ACCOUNT, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(SOURCE_PIN_DEEP_LINK, R.id.nav_host_fragment_activity_pin_deep_link);

        // Navigation action IDs
        navActionIds.put(SOURCE_HOME, R.id.action_userProfileFragment_to_pinFragment);
        navActionIds.put(SOURCE_SEARCH, R.id.action_userProfileFragment2_to_pinFragment2);
        navActionIds.put(SOURCE_ACCOUNT, R.id.action_userProfileFragment3_to_pinFragment3);
        navActionIds.put(SOURCE_PIN_DEEP_LINK, R.id.action_userProfileFragmentDeepLink_to_pinFragmentDeepLink);
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
        if (currentUser != null && viewModel.getUserId() != null && viewModel.getUserId().equals(currentUser.getUid())) {
            isSelf = true;
            binding.btnFollow.setVisibility(View.GONE);
        }

        // Set up the RecyclerView for pins
        setupRecyclerView();

        if (viewModel.getUserId() != null) {
            // Restore UI state from ViewModel if available
            restoreUiState();

            // Load data if needed
            loadUserData();
        }

        binding.btnBack.setOnClickListener(v -> {
            NavController navController = getNavController();
            navController.navigateUp();
        });

        binding.btnFollow.setOnClickListener(v -> toggleFollow());
    }

    //Restore UI state from ViewModel
    private void restoreUiState() {
        // Restore name if available
        String name = viewModel.getName();
        if(name != null){
            binding.tvName.setText(name);
        }

        // Restore avatar url if available
        String avatarUrl = viewModel.getAvatarUrl();
        if(avatarUrl !=null ){
            Glide.with(binding.ivUserAvatar.getContext())
                    .load(avatarUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .error(R.drawable.turtle_huh))
                    .into(binding.ivUserAvatar);
        }

        // Restore following status if available
        Boolean isFollowing = viewModel.getIsFollowing();
        if (isFollowing != null) {
            updateFollowButton(isFollowing);
        }

        // Restore follower count if available
        Integer followersCount = viewModel.getFollowersCount();
        if (followersCount != null) {
            binding.tvFollowers.setText(String.format(Locale.US,
                    "%d %s", followersCount, getString(R.string.followers)));
        }

        // Restore following count if available
        Integer followingCount = viewModel.getFollowingCount();
        if (followingCount != null) {
            binding.tvFollowing.setText(String.format(Locale.US,
                    "%d %s", followingCount, getString(R.string.following)));
        }
    }

    //Load user data from Firebase
    private void loadUserData() {
        // Load user info if not already loaded or refresh needed
        if (viewModel.getFollowersCount() == null || viewModel.getFollowingCount() == null) {
            loadUserInfo();
        }

        // Check follow status if not already checked
        if (viewModel.getIsFollowing() == null && !isSelf) {
            checkFollowStatus();
        }

        // Always load pins (they could change)
        loadUserPins();
    }

    /**
     * Get the appropriate NavController based on current source
     * @return NavController for the current source
     */
    private NavController getNavController() {
        String source = viewModel.getSource();
        int navHostId = navHostResIds.getOrDefault(source, R.id.nav_host_fragment_activity_main);
        return Navigation.findNavController(requireActivity(), navHostId);
    }

    /**
     * Get the appropriate navigation action ID based on current source
     * @return action ID for navigation
     */
    private int getNavigationActionId() {
        String source = viewModel.getSource();
        return navActionIds.getOrDefault(source, R.id.action_userProfileFragment_to_pinFragment);
    }

    private void setupRecyclerView() {
        // Initialize the pin adapter
        pinAdapter = new PinListAdapter(requireContext(), userPins, (position, v) -> {
            // Navigate to PinFragment using source-specific navigation
            NavController navController = getNavController();

            Bundle args = new Bundle();
            args.putParcelableArrayList("pins", new ArrayList<>(userPins));
            args.putInt("position", position);
            args.putString("source", viewModel.getSource());

            // Navigate to the appropriate pin fragment based on source
            int action = getNavigationActionId();
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

        FirebaseUserService.checkIfFollowing(viewModel.getUserId(), new FirebaseUserService.CheckFollowStatusCallback() {
            @Override
            public void OnSuccess(boolean following) {
                viewModel.setIsFollowing(following);
                updateFollowButton(following);
            }

            @Override
            public void OnFailure(Exception e) {
                Toast.makeText(requireContext(), R.string.check_status_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFollowButton(boolean isFollowing) {
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
        boolean isFollowing = viewModel.getIsFollowing() != null && viewModel.getIsFollowing();

        if (isFollowing) {
            // Unfollow
            FirebaseUserService.unfollowUser(viewModel.getUserId(), new FirebaseUserService.FollowUserCallback() {
                @Override
                public void OnSuccess() {
                    viewModel.setIsFollowing(false);
                    updateFollowButton(false);

                    // Update follower count in ViewModel and UI
                    int newCount = viewModel.getFollowersCount() - 1;
                    viewModel.setFollowersCount(newCount);
                    binding.tvFollowers.setText(String.format(Locale.US, "%d %s", newCount, getString(R.string.followers)));

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
            FirebaseUserService.followUser(viewModel.getUserId(), new FirebaseUserService.FollowUserCallback() {
                @Override
                public void OnSuccess() {
                    viewModel.setIsFollowing(true);
                    updateFollowButton(true);

                    // Update follower count in ViewModel and UI
                    int newCount = viewModel.getFollowersCount() + 1;
                    viewModel.setFollowersCount(newCount);
                    binding.tvFollowers.setText(String.format(Locale.US, "%d %s", newCount, getString(R.string.followers)));

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

    private void loadUserInfo() {
        binding.progressLoading.setVisibility(View.VISIBLE);

        FirebaseUserService.getUserInfos(viewModel.getUserId(), new FirebaseUserService.GetUserInfoCallback() {
            @Override
            public void OnSuccess(DocumentSnapshot documentSnapshot) {
                String name = documentSnapshot.getString("name");
                viewModel.setName(name);

                String avatarUrl = documentSnapshot.getString("avatarUrl");
                viewModel.setAvatarUrl(avatarUrl);

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
                viewModel.setFollowersCount(followersCount);
                binding.tvFollowers.setText(String.format(Locale.US, "%d %s", followersCount, getString(R.string.followers)));

                List<String> following = (List<String>) documentSnapshot.get("following");
                int followingCount = following != null ? following.size() : 0;
                viewModel.setFollowingCount(followingCount);
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

        FirebaseUserService.getUserPins(viewModel.getUserId(), new FirebaseUserService.GetUserPinsCallback() {
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