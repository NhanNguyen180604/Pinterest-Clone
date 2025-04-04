package com.example.pinterest_clone_test2.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.ViewPagerHomeAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentHomeBinding;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    ViewPager2 view_pager;
    TabLayout tab_layout;
    List<Board> boards = new ArrayList<>();
    Handler handler = new Handler();
    HomeViewModel viewModel;

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
            FirebaseBoardService.getUserBoards(callback);
        });
        thread.start();
    }

    void updateTabUI() {
        // edge case: user navigate to other tabs before UI is updated
        if (binding == null)
            return;

        view_pager = binding.homePager;
        ViewPagerHomeAdapter adapter = new ViewPagerHomeAdapter(this, boards);
        view_pager.setAdapter(adapter);

        tab_layout = binding.homeTabPager;
        new TabLayoutMediator(tab_layout, view_pager,
                (tab, position) -> {
                    tab.setText(String.format(Locale.US, boards.get(position).getName(), position + 1));
                }).attach();
    }

    private final FirebaseBoardService.GetBoardServiceCallback callback = new FirebaseBoardService.GetBoardServiceCallback() {
        @Override
        public void OnSuccess(QuerySnapshot queryDocumentSnapshots) {
            boards.add(new Board().setName(getResources().getString(R.string.all)));
            List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
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
            handler.post(() -> updateTabUI());
        }

        @Override
        public void OnFailure(Exception e) {
            e.printStackTrace();
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