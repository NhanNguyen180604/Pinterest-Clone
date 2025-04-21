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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.FragmentBoardChoosingBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.board.CreateNewBoardFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BoardChoosingFragment extends Fragment {
    public static String NAME = "BoardChoosingFragment";
    FragmentBoardChoosingBinding binding;
    final Pin pin;
    boolean processedImageB64;  // for removing bg case
    List<BaseItem> items = new ArrayList<>();
    Handler handler = new Handler();
    final boolean suggestNewBoard;
    boolean isClicked = false;

    public BoardChoosingFragment() {
        suggestNewBoard = false;
        pin = null;
    }

    public BoardChoosingFragment(Pin pin, boolean suggestNewBoard) {
        this.pin = pin;
        this.suggestNewBoard = suggestNewBoard;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // from remove bg fragment
        if (getArguments() != null) {
            processedImageB64 = getArguments().getBoolean("processedImageB64", false);
        }
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
            if (processedImageB64) {
                NavController navController = getNavController();
                navController.navigateUp();
            } else {
                requireActivity().setResult(Activity.RESULT_CANCELED);
                requireActivity().finish();
            }
        });

        View.OnClickListener createBoardListener = v -> {
            // should have done this earlier, but now im lazy
            if (processedImageB64) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_remove_bg);
                Bundle args = new Bundle();
                args.putBoolean("processedImageB64", true);
                navController.navigate(R.id.action_boardChoosingFragment_to_createNewBoardFragment2, args);
                return;
            }
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.create_board_fragment_container, new CreateNewBoardFragment(pin))
                    .addToBackStack(NAME)
                    .commit();
        };
        binding.fabCreateNewBoard.setOnClickListener(createBoardListener);
        binding.tvCreateBoard.setOnClickListener(createBoardListener);
    }

    @NonNull
    private NavController getNavController() {
        return Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_remove_bg);
    }

    private void initializeBoards() {
        BoardItemAdapter adapter;
        if (pin != null) {
            adapter = new BoardItemAdapter(items, itemClickListener, pin.getId());
        } else {
            adapter = new BoardItemAdapter(items, itemClickListener);
        }
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
                    Board board = document.toObject(Board.class);
                    if (board == null) {
                        continue;
                    }
                    board.setId(document.getId());
                    BaseItem newItem = new BoardItem(board, false);
                    items.add(newItem);
                }

                if (!items.isEmpty()) {
                    items.add(0, new HeaderItem(getResources().getString(R.string.save_to_your_board)));
                }

                DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
                List<String> profilePins = null;
                try {
                    profilePins = (List<String>) currentUserDocument.get("pins");
                } catch (Exception e) {
                    // eat exception
                }
                items.add(0, new BoardItem(
                        new Board()
                                .setId(null)
                                .setName(getResources().getString(R.string.your_profile))
                                .setPins(profilePins),
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
