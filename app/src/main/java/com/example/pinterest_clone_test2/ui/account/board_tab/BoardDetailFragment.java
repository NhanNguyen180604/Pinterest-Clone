package com.example.pinterest_clone_test2.ui.account.board_tab;

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
import com.example.pinterest_clone_test2.databinding.FragmentBoardDetailBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.pin.PinFragment;
import com.example.pinterest_clone_test2.ui.user.UserProfileFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BoardDetailFragment extends Fragment {
    private Board board;
    private List<Pin> pins;
    FragmentBoardDetailBinding binding;
    private String source = UserProfileFragment.SOURCE_ACCOUNT;
    private static final String TAG = "BoardDetailFragment";

    private final Map<String, Integer> navHostResIds = new HashMap<>();
    private final Map<String, Integer> navActionIds = new HashMap<>();

    public BoardDetailFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo navigation mappings
        initNavigationMappings();
    }
    private void initNavigationMappings() {
        navHostResIds.put(UserProfileFragment.SOURCE_HOME, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(UserProfileFragment.SOURCE_SEARCH, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(UserProfileFragment.SOURCE_ACCOUNT, R.id.nav_host_fragment_activity_main);
        navHostResIds.put(UserProfileFragment.SOURCE_PIN_DEEP_LINK, R.id.nav_host_fragment_activity_pin_deep_link);

        navActionIds.put(UserProfileFragment.SOURCE_HOME, R.id.action_boardDetailFragment_to_pinFragment);
        navActionIds.put(UserProfileFragment.SOURCE_SEARCH, R.id.action_boardDetailFragment_to_pinFragment2);
        navActionIds.put(UserProfileFragment.SOURCE_ACCOUNT, R.id.action_boardDetailFragment_to_pinFragment3);
        navActionIds.put(UserProfileFragment.SOURCE_PIN_DEEP_LINK, R.id.action_boardDetailFragmentDeepLink_to_pinFragmentDeepLink);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBoardDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            board = getArguments().getParcelable("board");
            if (getArguments().containsKey("source")) {
                source = getArguments().getString("source", UserProfileFragment.SOURCE_ACCOUNT);
            }
        }
        if (board == null) return;
        binding.tvBoardTitle.setText(board.getName());
        if (board.getPins().size() > 1) {
            binding.tvNumberOfPins.setText(String.format(Locale.US, "%d %s", board.getPins().size(), getResources().getString(R.string.pins).toLowerCase()));
        } else {
            binding.tvNumberOfPins.setText(String.format(Locale.US, "%d %s", board.getPins().size(), getResources().getString(R.string.pin).toLowerCase()));
        }
        binding.btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });
        pins = board.getPinsObj();

        PinListAdapter adapter = getPinListAdapter();
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvBoardPins.setHasFixedSize(true);
        binding.rvBoardPins.setLayoutManager(layoutManager);
        binding.rvBoardPins.setAdapter(adapter);
        binding.progressLoading.setVisibility(View.GONE);
    }

    private NavController getNavController() {
        int navHostId = navHostResIds.getOrDefault(source, R.id.nav_host_fragment_activity_main);
        return Navigation.findNavController(requireActivity(), navHostId);
    }
    @NonNull
    private PinListAdapter getPinListAdapter() {
        PinClickListener pinClickListener = (position, clickedView) -> {
            try {
                NavController navController = getNavController();
                Bundle args = new Bundle();
                args.putParcelableArrayList("pins", new ArrayList<>(pins));
                args.putInt("position", position);
                args.putString("source", source);

                Integer actionId = navActionIds.get(source);

                if (actionId == null) {
                    Log.w(TAG, "Không tìm thấy actionId cho source: " + source + ". Sử dụng cách dự phòng.");
                    try {
                        int pinFragmentId;
                        switch (source) {
                            case UserProfileFragment.SOURCE_HOME:
                                pinFragmentId = R.id.pinFragment;
                                break;
                            case UserProfileFragment.SOURCE_SEARCH:
                                pinFragmentId = R.id.pinFragment2;
                                break;
                            case UserProfileFragment.SOURCE_PIN_DEEP_LINK:
                                pinFragmentId = R.id.pinFragmentDeepLink;
                                break;
                            default:
                                pinFragmentId = R.id.pinFragment3;
                                break;
                        }
                        navController.navigate(pinFragmentId, args);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi điều hướng: " + e.getMessage());
                        Toast.makeText(requireContext(), getString(R.string.error_open_pin_detail), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                navController.navigate(actionId, args);
            } catch (Exception e) {
                Log.e(TAG, "Error while opening PinFragment", e);
                Toast.makeText(requireContext(), getString(R.string.error_open_pin_detail), Toast.LENGTH_SHORT).show();
            }
        };

        return new PinListAdapter(requireContext(), pins, pinClickListener);
    }
}
