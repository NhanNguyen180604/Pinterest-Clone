package com.example.pinterest_clone_test2.ui.account;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.BoardAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentBoardTabObjectBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BoardTabObjectFragment extends Fragment {
    private FragmentBoardTabObjectBinding binding;
    private BoardAdapter boardAdapter;
    private List<Board> boardList;

    public BoardTabObjectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBoardTabObjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = binding.rvBoards;
        recyclerView.setLayoutManager((new GridLayoutManager(requireContext(), 2)));
        FirebaseBoardService.getUserBoards(new FirebaseBoardService.GetBoardServiceCallback() {
            @Override
            public void OnSuccess(QuerySnapshot querySnapshot) {
                boardList = new ArrayList<>();
                for (var doc : querySnapshot.getDocuments()) {
                    Board board = doc.toObject(Board.class);
                    if (board == null)
                        continue;

                    if (board.getPins() != null && !board.getPins().isEmpty()) {
                        FirebaseBoardService.fetchPinsFromIds(board.getPins(), new FirebaseBoardService.OnPinsFetchedCallback() {
                            @Override
                            public void onSuccess(List<Pin> pins) {
                                board.setPinsObj(pins); // this is your new field
                                boardList.add(board);
                                boardAdapter.notifyDataSetChanged(); // or use submitList for diffing
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("BoardFragment", "Failed to fetch pins for board: " + board.getId(), e);
                            }
                        });
                    } else {
                        board.setPinsObj(new ArrayList<>());
                        boardList.add(board);
                        boardAdapter.notifyDataSetChanged();
                    }
                    binding.progressLoading.setVisibility(View.GONE);
                }

                boardAdapter = new BoardAdapter(requireContext(), boardList, board -> {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("board", board); // Make sure Board implements Parcelable
                    navController.navigate(R.id.action_navigation_account_to_boardDetailFragment, bundle);
                });

                recyclerView.setAdapter(boardAdapter);
            }

            @Override
            public void OnFailure(Exception e) {
                Log.e("Firebase", "Error fetching boards", e);
                Toast.makeText(requireContext(), "Failed to load boards", Toast.LENGTH_SHORT).show();
                binding.progressLoading.setVisibility(View.GONE);
            }
        });

    }
}