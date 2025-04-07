package com.example.pinterest_clone_test2.ui.board.board_choosing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.pinterest_clone_test2.databinding.FragmentBoardChoosingBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.ui.board.CreateNewBoardFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BoardChoosingFragment extends Fragment {
    public static String NAME = "BoardChoosingFragment";
    FragmentBoardChoosingBinding binding;
    Pin pin;
    List<BaseItem> items = new ArrayList<>();
    Handler handler = new Handler();
    final boolean suggestNewBoard;
    boolean isClicked = false;

    public BoardChoosingFragment() {
        suggestNewBoard = false;
    }

    public BoardChoosingFragment(@Nullable Pin pin, boolean suggestNewBoard) {
        this.pin = pin;
        this.suggestNewBoard = suggestNewBoard;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBoardChoosingBinding.inflate(inflater, container, false);
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

        fetchUserBoardAsync();

        binding.btnClose.setOnClickListener(v -> {
            requireActivity().setResult(Activity.RESULT_CANCELED);
            requireActivity().finish();
        });

        View.OnClickListener createBoardListener = v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.create_board_fragment_container, new CreateNewBoardFragment(pin))
                    .addToBackStack(NAME)
                    .commit();
        };
        binding.fabCreateNewBoard.setOnClickListener(createBoardListener);
        binding.tvCreateBoard.setOnClickListener(createBoardListener);
    }

    private void initializeBoards() {
        BoardItemAdapter adapter = new BoardItemAdapter(items, itemClickListener);
        binding.rvBoards.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvBoards.setLayoutManager(layoutManager);
    }

    BoardItemAdapter.ItemClickListener itemClickListener = (board, isNew) -> {
        if (isClicked)
            return;

        isClicked = true;
        binding.fabCreateNewBoard.setEnabled(false);
        binding.tvCreateBoard.setEnabled(false);

        Intent data = new Intent();

        if (board.getId() == null) {
            data.putExtra("profile", true);
            requireActivity().setResult(Activity.RESULT_OK, data);
            requireActivity().finish();
            return;
        }

        if (isNew) {
            board.setPins(new ArrayList<>())
                    .setCollaborators(new ArrayList<>())
                    .setPublic(true);

            FirebaseBoardService.createNewBoard(board, new FirebaseBoardService.CreateBoardServiceCallback() {
                @Override
                public void OnSuccess(DocumentReference documentReference) {
                    data.putExtra("boardId", documentReference.getId());
                    data.putExtra("boardName", board.getName());
                    requireActivity().setResult(Activity.RESULT_OK, data);
                    requireActivity().finish();
                }

                @Override
                public void OnFailure(Exception e) {
                    Toast.makeText(requireContext(), getResources().getString(R.string.create_board_failure), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    isClicked = false;
                    binding.fabCreateNewBoard.setEnabled(true);
                    binding.tvCreateBoard.setEnabled(true);
                }
            });
        } else {
            data.putExtra("boardId", board.getId());
            data.putExtra("boardName", board.getName());
            requireActivity().setResult(Activity.RESULT_OK, data);
            requireActivity().finish();
        }
    };

    void fetchUserBoardAsync() {
        if (!items.isEmpty()) {
            handler.post(this::initializeBoards);
            return;
        }

        Thread thread = new Thread(() -> FirebaseBoardService.getUserBoards(new FirebaseBoardService.GetBoardServiceCallback() {
            @Override
            public void OnSuccess(QuerySnapshot querySnapshot) {
                List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                for (DocumentSnapshot document :
                        documentSnapshots) {
                    Board board = new Board()
                            .setId(document.getId())
                            .setName(document.getString("name"))
                            .setDescription(document.getString("description"))
                            .setAuthorId(document.getString("authorId"))
                            .setPublic(Boolean.TRUE.equals(document.getBoolean("isPublic")));
                    BaseItem newItem = new BoardItem(board, false);
                    items.add(newItem);
                }

                if (!items.isEmpty()) {
                    items.add(0, new HeaderItem(getResources().getString(R.string.save_to_your_board)));
                }

                items.add(0, new BoardItem(
                        new Board()
                                .setId(null)
                                .setName(getResources().getString(R.string.your_profile)),
                        false
                ));

                if (suggestNewBoard) {
                    items.add(new HeaderItem(getResources().getString(R.string.select_to_create_a_new_board)));
                    for (Board board :
                            Board.ideaBoardSeedData) {
                        items.add(new BoardItem(board, true));
                    }
                }

                handler.post(() -> initializeBoards());
            }

            @Override
            public void OnFailure(Exception e) {
                e.printStackTrace();
                handler.post(() -> initializeBoards());
            }
        }));
        thread.start();
    }
}
