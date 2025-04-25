package com.example.pinterest_clone_test2.ui.user.board_tab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.pinterest_clone_test2.BoardDetailActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.BoardAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentUserProfileBoardsBinding;
import com.example.pinterest_clone_test2.interfaces.OnBoardClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.ui.user.UserProfileFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserProfileBoardsFragment extends Fragment {
    private static final String TAG = "UserProfileBoardsFragment";
    private FragmentUserProfileBoardsBinding binding;
    private String userId;
    private String source;
    private boolean isSelf;
    private final List<Board> userBoards = new ArrayList<>();
    private BoardAdapter boardAdapter;

    // Maps to store navigation action IDs by source
    private final Map<String, Integer> navActionIds = new HashMap<>();

    public UserProfileBoardsFragment() {
        // Required empty public constructor
    }

    public static UserProfileBoardsFragment newInstance(String userId, String source, boolean isSelf) {
        UserProfileBoardsFragment fragment = new UserProfileBoardsFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("source", source);
        args.putBoolean("isSelf", isSelf);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            source = getArguments().getString("source", UserProfileFragment.SOURCE_HOME);
            isSelf = getArguments().getBoolean("isSelf", false);
        }

        // Initialize navigation mappings
        initNavigationMappings();
    }

    // Initialize navigation action IDs based on source
    private void initNavigationMappings() {
        // Navigation action IDs for boards
        navActionIds.put(UserProfileFragment.SOURCE_HOME, R.id.action_userProfileFragment_to_boardDetailFragment);
        navActionIds.put(UserProfileFragment.SOURCE_SEARCH, R.id.action_userProfileFragment2_to_boardDetailFragment);
        navActionIds.put(UserProfileFragment.SOURCE_ACCOUNT, R.id.action_userProfileFragment3_to_boardDetailFragment);
        navActionIds.put(UserProfileFragment.SOURCE_PIN_DEEP_LINK, R.id.action_userProfileFragmentDeepLink_to_boardDetailFragmentDeepLink);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBoardsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadUserBoards();
    }

    private void setupRecyclerView() {
        boardAdapter = new BoardAdapter(requireContext(), userBoards, this::navigateToBoardDetail);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        binding.rvUserBoards.setLayoutManager(layoutManager);
        binding.rvUserBoards.setAdapter(boardAdapter);
    }

    private void navigateToBoardDetail(Board board) {
        Intent intent = new Intent(requireContext(), BoardDetailActivity.class);
        intent.putExtra("boardId", board.getId());
        if (Objects.equals(board.getId(), "allPins")) {
            intent.putExtra("userId", userId);
            intent.putExtra("allPins", true);
        }
        startActivity(intent);
    }

    private NavController getNavController() {
        int navHostId;
        if (source.equals(UserProfileFragment.SOURCE_PIN_DEEP_LINK)) {
            navHostId = R.id.nav_host_fragment_activity_pin_deep_link;
        } else {
            navHostId = R.id.nav_host_fragment_activity_main;
        }
        return Navigation.findNavController(requireActivity(), navHostId);
    }

    private void loadUserBoards() {
        binding.progressLoading.setVisibility(View.VISIBLE);
        binding.tvNoBoards.setVisibility(View.GONE);

        // Clear current boards
        userBoards.clear();
        boardAdapter.notifyDataSetChanged();

        // Create "All Pins" board
        FirebaseBoardService.createAllPinsBoard(getContext(), userId, new FirebaseBoardService.CreateAllPinsBoardCallback() {
            @Override
            public void OnSuccess(Board allPinsBoard) {
                // Add "All Pins" board to the start of the list
                allPinsBoard.setId("allPins");
                userBoards.add(allPinsBoard);
                boardAdapter.notifyItemInserted(0);

                // Load regular boards
                loadRegularBoards();
            }

            @Override
            public void OnEmpty() {
                // No pins available, load regular boards
                loadRegularBoards();
            }

            @Override
            public void OnFailure(Exception e) {
                Log.e(TAG, "Error creating All Pins board", e);
                // Continue loading regular boards
                loadRegularBoards();
            }
        });
    }

    private void loadRegularBoards() {
        // Retrieve user's boards, filter by isPublic if not the user's own profile
        FirebaseBoardService.getUserBoardsByUserId(
                userId,
                !isSelf, // Only fetch public boards if not the user's own profile
                new FirebaseBoardService.GetUserBoardsCallback() {
                    @Override
                    public void OnSuccess(List<Board> boards) {
                        binding.progressLoading.setVisibility(View.GONE);

                        if (boards.isEmpty() && userBoards.isEmpty()) {
                            // No boards available
                            binding.tvNoBoards.setVisibility(View.VISIBLE);
                            return;
                        }

                        // Load pins for each board
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
                                    // Add board even if pins could not be loaded
                                    userBoards.add(board);
                                    boardAdapter.notifyItemInserted(userBoards.size() - 1);
                                }
                            });
                        }
                    }

                    @Override
                    public void OnFailure(Exception e) {
                        Log.e(TAG, "Error loading boards", e);
                        binding.progressLoading.setVisibility(View.GONE);

                        if (userBoards.isEmpty()) {
                            binding.tvNoBoards.setText(R.string.fetch_boards_failure);
                            binding.tvNoBoards.setVisibility(View.VISIBLE);
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