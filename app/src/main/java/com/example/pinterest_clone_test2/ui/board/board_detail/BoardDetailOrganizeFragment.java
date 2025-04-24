package com.example.pinterest_clone_test2.ui.board.board_detail;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinInBoardAdapter;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentBoardDetailOrganizeBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.ui.pin.edit.ConfirmDeletePinModalBottomSheet;

import java.util.ArrayList;
import java.util.List;

public class BoardDetailOrganizeFragment extends Fragment {
    FragmentBoardDetailOrganizeBinding binding;
    private Board board;
    private List<Pin> pins;
    private Handler inactivityHandler = new Handler();
    private Runnable showBarRunnable;
    private boolean allSelected = false;
    LinearLayout bottomBar;
    public BoardDetailOrganizeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBoardDetailOrganizeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setBottomBarEnabled(false);
        board = getArguments() != null ? getArguments().getParcelable("board") : null;
        if (board == null) return;
        pins = board.getPinsObj();
        PinInBoardAdapter adapter = getPinListAdapter();
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        RecyclerView rvBoardPins = binding.rvBoardPins;
        rvBoardPins.setHasFixedSize(true);
        rvBoardPins.setLayoutManager(layoutManager);
        rvBoardPins.setAdapter(adapter);
        rvBoardPins.setOnTouchListener((v, e)->{
            resetInactivityTimer();
            return false;
        });
        binding.progressLoading.setVisibility(View.GONE);
        bottomBar = binding.llOrganizeBoardOptions;
        showBarRunnable = () -> {
            if (bottomBar.getVisibility() != View.VISIBLE) {
                bottomBar.setVisibility(View.VISIBLE);
                bottomBar.startAnimation(AnimationUtils.loadAnimation(requireContext() ,R.anim.slide_up));
            }
        };
        binding.btnDelete.setOnClickListener(v->{
            ConfirmDeletePinModalBottomSheet sheet = new ConfirmDeletePinModalBottomSheet(this::deletePin);
            sheet.show(requireActivity().getSupportFragmentManager(), ConfirmDeletePinModalBottomSheet.TAG);
        });
        binding.btnSelectAll.setOnClickListener(v -> {
            allSelected = !allSelected;
            for (Pin pin : pins) {
                pin.setSelected(allSelected);
            }
            binding.rvBoardPins.getAdapter().notifyDataSetChanged();
            binding.btnSelectAll.setText(allSelected ? R.string.deselect_all : R.string.select_all);
            updateButtonsState();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        inactivityHandler.removeCallbacks(showBarRunnable);
    }

    private void setBottomBarEnabled(boolean enabled) {
        float alpha = enabled ? 1f : 0.3f;

        binding.btnMoveTo.setEnabled(enabled);
        binding.btnSection.setEnabled(enabled);
        binding.btnDelete.setEnabled(enabled);

        binding.btnMoveTo.setAlpha(alpha);
        binding.btnSection.setAlpha(alpha);
        binding.btnDelete.setAlpha(alpha);
    }
    @NonNull
    private PinInBoardAdapter getPinListAdapter() {
        PinClickListener pinClickListener = (position, clickedView) -> {
            pins.get(position).setSelected(!pins.get(position).isSelected());
            binding.rvBoardPins.getAdapter().notifyItemChanged(position);
            updateButtonsState();
        };

        return new PinInBoardAdapter(requireContext(), pins, pinClickListener);
    }


    private void resetInactivityTimer() {
        inactivityHandler.removeCallbacks(showBarRunnable);
        inactivityHandler.postDelayed(showBarRunnable, 2000);

        if (bottomBar.getVisibility() == View.VISIBLE) {
            bottomBar.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down));;
            bottomBar.setVisibility(View.GONE);
        }
    }

    private List<String> getSelectedPinIds() {
        List<String> selectedIds = new ArrayList<>();
        for (Pin pin : pins) {
            if (pin.isSelected()) {
                selectedIds.add(pin.getId());
            }
        }
        return selectedIds;
    }

    private void updateButtonsState() {
        boolean anySelected = false;
        for (Pin pin : pins) {
            if (pin.isSelected()) {
                anySelected = true;
                break;
            }
        }
        setBottomBarEnabled(anySelected);
        binding.btnSelectAll.setEnabled(!pins.isEmpty());
        boolean allSelectedNow = true;
        for (Pin pin : pins) {
            if (!pin.isSelected()) {
                allSelectedNow = false;
                break;
            }
        }
        binding.btnSelectAll.setText(allSelectedNow ? R.string.deselect_all : R.string.select_all);
    }

    private void deletePin(){
        List<String> selectedPinsId = getSelectedPinIds();

        if (selectedPinsId.isEmpty()) {
            Log.d("DeletePins", "No pins selected to delete.");
            return;
        }
        FirebaseBoardService.deletePinsFromBoard(board.getId(), selectedPinsId, new FirebaseBoardService.DeletePinsCallback() {
            @Override
            public void OnSuccess() {
                Log.d("DeletePin", "Pin successfully removed from board.");
                pins.removeIf(pin -> selectedPinsId.contains(pin.getId()));
                binding.rvBoardPins.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void OnFailure(Exception e) {
                Log.e("DeletePin", "Failed to remove pin from board", e);
            }
        });
    }
}