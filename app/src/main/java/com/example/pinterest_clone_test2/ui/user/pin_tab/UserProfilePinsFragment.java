package com.example.pinterest_clone_test2.ui.user.pin_tab;

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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentUserProfilePinsBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.user.UserProfileFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfilePinsFragment extends Fragment {
    private static final String TAG = "UserProfilePinsFragment";
    private FragmentUserProfilePinsBinding binding;
    private String userId;
    private String source;
    private final ArrayList<Pin> userPins = new ArrayList<>();
    private PinListAdapter pinAdapter;

    // Maps to store navigation controllers and actions by source
    private final Map<String, Integer> navActionIds = new HashMap<>();

    public UserProfilePinsFragment() {
        // Required empty public constructor
    }

    public static UserProfilePinsFragment newInstance(String userId, String source) {
        UserProfilePinsFragment fragment = new UserProfilePinsFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("source", source);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            source = getArguments().getString("source", UserProfileFragment.SOURCE_HOME);
        }

        // Initialize navigation mappings
        initNavigationMappings();
    }

    // Initialize navigation action IDs based on source
    private void initNavigationMappings() {
        // Navigation action IDs for pins
        navActionIds.put(UserProfileFragment.SOURCE_HOME, R.id.action_userProfileFragment_to_pinFragment);
        navActionIds.put(UserProfileFragment.SOURCE_SEARCH, R.id.action_userProfileFragment2_to_pinFragment2);
        navActionIds.put(UserProfileFragment.SOURCE_ACCOUNT, R.id.action_userProfileFragment3_to_pinFragment3);
        navActionIds.put(UserProfileFragment.SOURCE_PIN_DEEP_LINK, R.id.action_userProfileFragmentDeepLink_to_pinFragmentDeepLink);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfilePinsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadUserPins();
    }

    private void setupRecyclerView() {
        pinAdapter = new PinListAdapter(requireContext(), userPins, new PinClickListener() {
            @Override
            public void OnClick(int position, View itemView) {
                navigateToPin(userPins, position);
            }
        });

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.rvUserPins.setLayoutManager(layoutManager);
        binding.rvUserPins.setAdapter(pinAdapter);
    }

    private void navigateToPin( ArrayList<Pin> pins, int position) {
        NavController navController = getNavController();

        Bundle bundle = new Bundle();
        bundle.putString("source", source);
        bundle.putInt("position",position);
        bundle.putParcelableArrayList("pins", pins);

        Integer actionId = navActionIds.get(source);

        if (actionId == null) {
            Log.w(TAG, "Không tìm thấy actionId cho source: " + source + ". Sử dụng cách dự phòng.");
            try {
                switch (source) {
                    case UserProfileFragment.SOURCE_HOME:
                        navController.navigate(R.id.action_userProfileFragment_to_pinFragment, bundle);
                        break;
                    case UserProfileFragment.SOURCE_SEARCH:
                        navController.navigate(R.id.action_userProfileFragment2_to_pinFragment2, bundle);
                        break;
                    case UserProfileFragment.SOURCE_ACCOUNT:
                        navController.navigate(R.id.action_userProfileFragment3_to_pinFragment3, bundle);
                        break;
                    case UserProfileFragment.SOURCE_PIN_DEEP_LINK:
                        navController.navigate(R.id.action_userProfileFragmentDeepLink_to_pinFragmentDeepLink, bundle);
                        break;
                    default:
                        navController.navigate(R.id.pinFragment, bundle);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi điều hướng: " + e.getMessage());
                Toast.makeText(requireContext(), getString(R.string.fetch_pin_failure), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        try {
            navController.navigate(actionId, bundle);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi điều hướng với action ID " + actionId + ": " + e.getMessage());
            Toast.makeText(requireContext(), getString(R.string.fetch_pin_failure), Toast.LENGTH_SHORT).show();
        }
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

    private void loadUserPins() {
        binding.progressLoading.setVisibility(View.VISIBLE);
        binding.tvNoPins.setVisibility(View.GONE);

        FirebaseUserService.getUserCreatedPins(userId, new FirebaseUserService.GetUserPinsCallback() {
            @Override
            public void OnSuccess(List<Pin> pins) {
                binding.progressLoading.setVisibility(View.GONE);

                if (pins.isEmpty()) {
                    binding.tvNoPins.setVisibility(View.VISIBLE);
                    return;
                }

                userPins.clear();
                userPins.addAll(pins);
                pinAdapter.notifyDataSetChanged();
            }

            @Override
            public void OnFailure(Exception e) {
                binding.progressLoading.setVisibility(View.GONE);
                binding.tvNoPins.setVisibility(View.VISIBLE);
                binding.tvNoPins.setText(R.string.fetch_pin_failure);
                Log.e(TAG, "Error loading pins", e);
                Toast.makeText(requireContext(), getString(R.string.fetch_pin_failure) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}