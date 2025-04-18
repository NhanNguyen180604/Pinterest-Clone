package com.example.pinterest_clone_test2.ui.account.board_tab;

import android.os.Bundle;
import android.os.Handler;
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
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BoardTabObjectFragment extends Fragment {
    private FragmentBoardTabObjectBinding binding;
    private BoardAdapter boardAdapter;
    private final List<Board> boardList = new ArrayList<>();
    Handler handler = new Handler();

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
        boardAdapter = new BoardAdapter(requireContext(), boardList, board -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putParcelable("board", board);
            navController.navigate(R.id.action_navigation_account_to_boardDetailFragment, bundle);
        });
        recyclerView.setAdapter(boardAdapter);
        recyclerView.setLayoutManager((new GridLayoutManager(requireContext(), 2)));

        if (!boardList.isEmpty()) {
            binding.progressLoading.setVisibility(View.GONE);
            return;
        }

        FirebaseBoardService.getUserBoards(new FirebaseBoardService.GetBoardServiceCallback() {
            @Override
            public void OnSuccess(QuerySnapshot querySnapshot) {
                for (var doc : querySnapshot.getDocuments()) {
                    Board board = doc.toObject(Board.class);
                    if (board == null)
                        continue;

                    if (board.getPins() != null && !board.getPins().isEmpty()) {
                        FirebasePinService.fetchPinsFromIds(board.getPins(), new FirebasePinService.OnPinsFetchedFromIdsCallback() {
                            @Override
                            public void onSuccess(List<Pin> pins) {
                                handler.post(() -> {
                                    board.setPinsObj(pins);
                                    boardList.add(board);
                                    boardAdapter.notifyItemInserted(boardList.size() - 1);
                                    binding.progressLoading.setVisibility(View.GONE);
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("BoardFragment", "Failed to fetch pins for board: " + board.getId(), e);
                                binding.progressLoading.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        handler.post(() -> {
                            board.setPinsObj(new ArrayList<>());
                            boardList.add(board);
                            boardAdapter.notifyItemInserted(boardList.size() - 1);
                            binding.progressLoading.setVisibility(View.GONE);
                        });
                    }
                }
            }

            @Override
            public void OnFailure(Exception e) {
                Log.e("Firebase", "Error fetching boards", e);
                Toast.makeText(requireContext(), getResources().getString(R.string.fetch_boards_failure), Toast.LENGTH_SHORT).show();
                binding.progressLoading.setVisibility(View.GONE);
            }
        });

    }
}