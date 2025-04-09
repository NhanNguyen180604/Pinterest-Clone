package com.example.pinterest_clone_test2.ui.account.board_tab;

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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentBoardDetailBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.pin.PinFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BoardDetailFragment extends Fragment {
    private Board board;
    private List<Pin> pins;
    FragmentBoardDetailBinding binding;

    public BoardDetailFragment() {
        // Required empty constructor
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
        // Get board object
        board = getArguments() != null ? getArguments().getParcelable("board") : null;
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

    @NonNull
    private PinListAdapter getPinListAdapter() {
        PinClickListener pinClickListener = (position, clickedView) -> {
            try {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putParcelableArrayList("pins", new ArrayList<>(pins));
                args.putInt("position", position);
                args.putString("source", "account");
                PinFragment fragment = new PinFragment();
                fragment.setArguments(args);
                navController.navigate(R.id.action_boardDetailFragment_to_pinFragment3, args, null, null);
            } catch (Exception e) {
                Log.e("BoardDetailFragment", "Error while opening PinFragment", e);
            }
        };

        return new PinListAdapter(pins, pinClickListener);
    }
}
