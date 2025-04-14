package com.example.pinterest_clone_test2.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.SearchIdeaAdapter;
import com.example.pinterest_clone_test2.adapters.ViewPagerSearchIdeaAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentSearchBinding;
import com.example.pinterest_clone_test2.interfaces.SearchIdeaClickListener;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    long lastSubmitTime = 0;

    public SearchFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPagerSearchIdeaAdapter pager_adapter = new ViewPagerSearchIdeaAdapter();
        binding.searchIdeaPager.setAdapter(pager_adapter);

        SearchIdeaAdapter rv_adapter = new SearchIdeaAdapter(searchIdeaClickListener);
        binding.rvIdeas.setAdapter(rv_adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
        binding.rvIdeas.setLayoutManager(layoutManager);

        binding.btnSearchCancel.setOnClickListener(v -> {
            binding.svSearchBar.setQuery("", false);
            binding.svSearchBar.clearFocus();
        });

        binding.svSearchBar.setOnQueryTextFocusChangeListener((v, hasFocus) -> binding.btnSearchCancel.setVisibility(hasFocus ? View.VISIBLE : View.GONE));

        binding.svSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                long now = System.currentTimeMillis();
                if (now - lastSubmitTime < 1000){
                    return false;
                }

                navigateToSearchResult(query);
                lastSubmitTime = now;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("test", newText);
                return false;
            }
        });
    }

    private final SearchIdeaClickListener searchIdeaClickListener = this::navigateToSearchResult;

    private void navigateToSearchResult(String query) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        Bundle args = new Bundle();
        args.putString("query", query);

        navController.navigate(
                R.id.action_navigation_search_to_searchResultFragment,
                args,
                null,
                null
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}