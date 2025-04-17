package com.example.pinterest_clone_test2.ui.pin.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.BoardMultipleSelectionAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentEditPinPickBoardsBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditPinPickBoardsFragment extends Fragment {
    FragmentEditPinPickBoardsBinding binding;
    EditPinFragmentViewModel viewModel;
    Map<String, BoardBooleanPair> originalMap;

    public EditPinPickBoardsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(requireActivity().getApplication(), requireActivity())).get(EditPinFragmentViewModel.class);
        cloneOriginalMap();
        binding = FragmentEditPinPickBoardsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnReturn.setOnClickListener(v -> {
            restoreOriginalMap();
            returnToEditPin();
        });
        initRecyclerView();
        binding.btnSave.setOnClickListener(v -> returnToEditPin());
    }

    void initRecyclerView() {
        Map<String, BoardBooleanPair> boardMap = viewModel.getBoardMap().getValue();
        assert boardMap != null;
        List<BoardBooleanPair> pairs = new ArrayList<>();

        for (Map.Entry<String, BoardBooleanPair> entry : boardMap.entrySet()) {
            pairs.add(entry.getValue());
        }
        BoardMultipleSelectionAdapter adapter = new BoardMultipleSelectionAdapter(pairs);
        binding.rvBoards.setAdapter(adapter);
        binding.rvBoards.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
    }

    void returnToEditPin() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_edit_pin);
        navController.navigateUp();
    }

    void cloneOriginalMap() {
        originalMap = new HashMap<>();
        Map<String, BoardBooleanPair> boardMap = viewModel.getBoardMap().getValue();
        assert boardMap != null;
        for (Map.Entry<String, BoardBooleanPair> entry : boardMap.entrySet()) {
            originalMap.put(entry.getKey(), new BoardBooleanPair()
                    .setBoard(entry.getValue().getBoard())
                    .setIncluded(entry.getValue().isIncluded())
            );
        }
    }

    void restoreOriginalMap() {
        viewModel.getBoardMap().setValue(originalMap);
    }
}