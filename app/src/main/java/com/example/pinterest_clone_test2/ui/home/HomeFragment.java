package com.example.pinterest_clone_test2.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.ViewPagerHomeAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentHomeBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    ViewPagerHomeAdapter adapter;
    List<Board> boards = new ArrayList<>();
    Handler handler = new Handler();
    HomeViewModel viewModel;
    boolean addOnlyNewBoards = false;

    public HomeFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity().getApplication(), this)).get(HomeViewModel.class);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        List<Board> oldBoardState = viewModel.getBoards();
        if (oldBoardState == null) {
            fetchBoardsAsync();
        } else if (FirebaseBoardService.isCurrentUserBoardListUpdated()) {
            addOnlyNewBoards = true;
            fetchBoardsAsync();
        } else {
            boards = oldBoardState;
            updateTabUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.setBoardState(boards);
    }

    void fetchBoardsAsync() {
        Thread thread = new Thread(() -> {
            if (addOnlyNewBoards) {
                FirebaseBoardService.getUserBoards(callback);
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            QuerySnapshot currentUserBoardSnapshot = FirebaseBoardService.getCurrentUserBoardSnapshot();
            if (currentUserBoardSnapshot == null) {
                FirebaseBoardService.getUserBoards(callback);
            } else {
                callback.OnSuccess(currentUserBoardSnapshot);
            }
        });
        thread.start();
    }

    void updateTabUI() {
        // edge case: user navigate to other tabs before UI is updated
        if (binding == null)
            return;

        binding.progressBar.setVisibility(View.GONE);
        adapter = new ViewPagerHomeAdapter(this, boards);
        binding.homePager.setAdapter(adapter);

        new TabLayoutMediator(binding.homeTabPager, binding.homePager,
                (tab, position) -> tab.setText(boards.get(position).getName())).attach();
    }

    private final FirebaseBoardService.GetBoardServiceCallback callback = new FirebaseBoardService.GetBoardServiceCallback() {
        @Override
        public void OnSuccess(QuerySnapshot querySnapshot) {
            if (!addOnlyNewBoards) {
                boards.add(new Board().setName(getResources().getString(R.string.all)));
                List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                for (DocumentSnapshot document :
                        documentSnapshots) {
                    boards.add(new Board()
                            .setId(document.getId())
                            .setName(document.getString("name"))
                            .setDescription(document.getString("description"))
                            .setAuthorId(document.getString("authorId"))
                            .setPublic(Boolean.TRUE.equals(document.getBoolean("isPublic")))
                    );
                }
            } else {
                List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                for (DocumentSnapshot document :
                        documentSnapshots) {
                    if (boards.stream().noneMatch(b -> Objects.equals(b.getId(), document.getId()))) {
                        boards.add(new Board()
                                .setId(document.getId())
                                .setName(document.getString("name"))
                                .setDescription(document.getString("description"))
                                .setAuthorId(document.getString("authorId"))
                                .setPublic(Boolean.TRUE.equals(document.getBoolean("isPublic")))
                        );
                    }
                }
                addOnlyNewBoards = false;
            }
            handler.post(() -> updateTabUI());
        }

        @Override
        public void OnFailure(Exception e) {
            Log.d("HomeFragment", "Failed to fetch user boards");
            if (e.getMessage() != null) {
                Log.e("HomeFragment", e.getMessage());
            }
            boards.add(new Board().setName(getResources().getString(R.string.all)));
            handler.post(() -> updateTabUI());
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}