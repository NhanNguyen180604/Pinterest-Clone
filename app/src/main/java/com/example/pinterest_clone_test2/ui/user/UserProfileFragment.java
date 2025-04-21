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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.BoardAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentUserProfileBinding;
import com.example.pinterest_clone_test2.interfaces.OnBoardClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
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
    private final List<Board> userBoards = new ArrayList<>();
    private BoardAdapter boardAdapter;
    private boolean isSelf = false;
    private static final String TAG = "UserProfileFragment";

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

    // Initialize navigation host resources and action IDs based on source
    private void initNavigationMappings() {
        // Navigation host resource IDs
        navHostResIds.put(SOURCE_HOME, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(SOURCE_SEARCH, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(SOURCE_ACCOUNT, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(SOURCE_PIN_DEEP_LINK, R.id.nav_host_fragment_activity_pin_deep_link);

        // Navigation action IDs - cập nhật với action mới cho board detail
        navActionIds.put(SOURCE_HOME, R.id.action_userProfileFragment_to_boardDetailFragment);
        navActionIds.put(SOURCE_SEARCH, R.id.action_userProfileFragment2_to_boardDetailFragment);
        navActionIds.put(SOURCE_ACCOUNT, R.id.action_userProfileFragment3_to_boardDetailFragment);
        navActionIds.put(SOURCE_PIN_DEEP_LINK, R.id.action_userProfileFragmentDeepLink_to_boardDetailFragmentDeepLink);
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

        // Update label text to show "Bảng" instead of "Pins"
        binding.tvPinsLabel.setText(R.string.boards);

        // Check if this is the current user's profile
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && viewModel.getUserId() != null && viewModel.getUserId().equals(currentUser.getUid())) {
            isSelf = true;
            binding.btnFollow.setVisibility(View.GONE);
        }

        // Set up the RecyclerView for boards
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

        // Load user boards
        loadUserBoards();
    }

    private NavController getNavController() {
        String source = viewModel.getSource();
        int navHostId = navHostResIds.getOrDefault(source, R.id.nav_host_fragment_activity_main);
        return Navigation.findNavController(requireActivity(), navHostId);
    }

    private void setupRecyclerView() {
        // Initialize the board adapter
        boardAdapter = new BoardAdapter(requireContext(), userBoards, new OnBoardClickListener() {
            @Override
            public void onBoardClick(Board board) {
                navigateToBoardDetail(board);
            }
        });

        // Set up the RecyclerView with a GridLayoutManager (2 columns)
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        binding.rvUserPins.setLayoutManager(layoutManager);
        binding.rvUserPins.setHasFixedSize(true);
        binding.rvUserPins.setAdapter(boardAdapter);
    }

    private void navigateToBoardDetail(Board board) {
        NavController navController = getNavController();

        Bundle bundle = new Bundle();

        bundle.putParcelable("board", board);

        bundle.putString("source", viewModel.getSource());

        String source = viewModel.getSource();
        Integer actionId = navActionIds.get(source);

        if (actionId == null) {
            Log.w(TAG, "Không tìm thấy actionId cho source: " + source + ". Sử dụng cách dự phòng.");
            try {
                navController.navigate(R.id.boardDetailFragment, bundle);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi điều hướng: " + e.getMessage());
                Toast.makeText(requireContext(), "Không thể mở board chi tiết", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        try {
            navController.navigate(actionId, bundle);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi điều hướng với action ID " + actionId + ": " + e.getMessage());
            Toast.makeText(requireContext(), "Không thể mở board chi tiết", Toast.LENGTH_SHORT).show();
        }
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

    private void loadUserBoards() {
        binding.progressLoading.setVisibility(View.VISIBLE);
        binding.tvNoPins.setVisibility(View.GONE);

        // Xóa boards hiện tại
        userBoards.clear();
        boardAdapter.notifyDataSetChanged();

        // Tạo board "Tất cả Ghim"
        FirebaseBoardService.createAllPinsBoard(viewModel.getUserId(), new FirebaseBoardService.CreateAllPinsBoardCallback() {
            @Override
            public void OnSuccess(Board allPinsBoard) {
                // Thêm board tất cả Ghim vào đầu danh sách
                userBoards.add(allPinsBoard);
                boardAdapter.notifyItemInserted(0);

                // Tải các board thông thường
                loadRegularBoards();
            }

            @Override
            public void OnEmpty() {
                // Không có pins nào, tải các board thông thường
                loadRegularBoards();
            }

            @Override
            public void OnFailure(Exception e) {
                Log.e(TAG, "Error creating All Pins board", e);
                // Vẫn tiếp tục tải các board thông thường
                loadRegularBoards();
            }
        });
    }

    private void loadRegularBoards() {
        // Lấy boards của người dùng, lọc theo isPublic nếu không phải profile của chính mình
        FirebaseBoardService.getUserBoardsByUserId(
                viewModel.getUserId(),
                !isSelf, // chỉ lấy public boards nếu không phải profile của mình
                new FirebaseBoardService.GetUserBoardsCallback() {
                    @Override
                    public void OnSuccess(List<Board> boards) {
                        if (boards.isEmpty() && userBoards.isEmpty()) {
                            // Không có boards nào
                            binding.progressLoading.setVisibility(View.GONE);
                            binding.tvNoPins.setText(R.string.no_board_message);
                            binding.tvNoPins.setVisibility(View.VISIBLE);
                            return;
                        }

                        // Tải pins cho từng board
                        for (Board board : boards) {
                            FirebaseBoardService.fetchPinsForBoard(board, new FirebaseBoardService.FetchPinsForBoardCallback() {
                                @Override
                                public void OnSuccess(Board updatedBoard) {
                                    userBoards.add(updatedBoard);
                                    boardAdapter.notifyItemInserted(userBoards.size() - 1);
                                }

                                @Override
                                public void OnFailure(Exception e) {
                                    Log.e(TAG, "Error loading pins for board: " + board.getId(), e);
                                    // Thêm board ngay cả khi không tải được pins
                                    userBoards.add(board);
                                    boardAdapter.notifyItemInserted(userBoards.size() - 1);
                                }
                            });
                        }

                        binding.progressLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void OnFailure(Exception e) {
                        Log.e(TAG, "Error loading boards", e);
                        binding.progressLoading.setVisibility(View.GONE);

                        if (userBoards.isEmpty()) {
                            binding.tvNoPins.setText(R.string.fetch_boards_failure);
                            binding.tvNoPins.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}