package com.example.pinterest_clone_test2.ui.pin.btn_save;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentPinSavingChooseBoardBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.board.CreateNewBoardFragment;

import java.util.ArrayList;
import java.util.List;

public class PinSavingFragment extends Fragment {
    public static String NAME = "PinSavingFragment";
    FragmentPinSavingChooseBoardBinding binding;
    Pin pin;

    public PinSavingFragment() {

    }

    public PinSavingFragment(Pin pin) {
        this.pin = pin;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPinSavingChooseBoardBinding.inflate(inflater, container, false);
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

        binding.btnClose.setOnClickListener(v -> {
            requireActivity().setResult(Activity.RESULT_CANCELED);
            requireActivity().finish();
        });

        View.OnClickListener createBoardListener = v -> {
            if (pin != null) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.create_board_fragment_container, new CreateNewBoardFragment(pin))
                        .addToBackStack(NAME)
                        .commit();
            } else {
                Toast.makeText(requireContext(), "Pin is null, life is not good", Toast.LENGTH_SHORT).show();
            }

        };
        binding.fabCreateNewBoard.setOnClickListener(createBoardListener);
        binding.tvCreateBoard.setOnClickListener(createBoardListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeBoards();
    }

    private void initializeBoards() {
        List<BaseItem> items = new ArrayList<>();
        items.add(new BoardItem(
                new Board()
                        .setId(null)
                        .setName(getResources().getString(R.string.your_profile)),
                false
        ));

        items.add(new HeaderItem(getResources().getString(R.string.save_to_your_board)));
        for (Board board :
                Board.seedData) {
            items.add(new BoardItem(board, false));
        }

        items.add(new HeaderItem(getResources().getString(R.string.select_to_create_a_new_board)));
        for (Board board :
                Board.ideaBoardSeedData) {
            items.add(new BoardItem(board, true));
        }

        BoardItemAdapter adapter = new BoardItemAdapter(items, itemClickListener);
        binding.rvBoards.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvBoards.setLayoutManager(layoutManager);
    }

    BoardItemAdapter.ItemClickListener itemClickListener = (board, isNew) -> {
        Intent data = new Intent();
        data.putExtra("added", true);

        if (board.getId() == null) {
            data.putExtra("profile", true);
        } else {
            data.putExtra("boardName", board.getName());
            data.putExtra("isNew", isNew);
            // this is stupid, i should refactor this, but i'm lazy now
            if (!isNew){
                data.putExtra("boardId", board.getId());
            }
        }

        requireActivity().setResult(Activity.RESULT_OK, data);
        requireActivity().finish();
    };
}
