package com.example.pinterest_clone_test2.ui.search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentSearchResultBinding;
import com.example.pinterest_clone_test2.interfaces.ImageClickListener;
import com.example.pinterest_clone_test2.models.Pin;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment {

    private FragmentSearchResultBinding binding;
    private String query = "";

    public SearchResultFragment() {
        // Required empty public constructor
    }

    List<Pin> pins;
    SearchResultViewModel view_model;

    public static SearchResultFragment newInstance() {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query = getArguments().getString("query");
        }

        pins = Pin.testData;
        view_model = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity().getApplication(), this)).get(SearchResultViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchResultBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.svSearchBar.post(() -> {
            binding.svSearchBar.setQuery("", false);
            binding.svSearchBar.setQuery(query, false);
        });

        binding.svSearchBar.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                binding.btnSearchCancel.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            }
        });

        binding.btnSearchCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.svSearchBar.setQuery("", false);
                binding.svSearchBar.clearFocus();
            }
        });

        binding.svSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("query", query);
                navController.navigate(
                        R.id.action_searchResultFragment_self,
                        args,
                        null,
                        null
                );
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("test", newText);
                return false;
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigateUp();
            }
        });

        PinListAdapter adapter = new PinListAdapter(pins, imageClickListener, 0);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        binding.rvSearchResult.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvSearchResult.setHasFixedSize(true);
        binding.rvSearchResult.setLayoutManager(layoutManager);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding.rvSearchResult.getLayoutManager() != null) {
            view_model.setScrollState(binding.rvSearchResult.getLayoutManager().onSaveInstanceState());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        Parcelable scroll_state = view_model.getScrollState();
        if (scroll_state != null && binding.rvSearchResult.getLayoutManager() != null) {
            binding.rvSearchResult.getLayoutManager().onRestoreInstanceState(scroll_state);
        }
    }

    private final ImageClickListener imageClickListener = new ImageClickListener() {
        @Override
        public void OnClick(int position, View v) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

            Bundle args = new Bundle();
            args.putParcelableArrayList("pins", new ArrayList<>(pins));
            args.putInt("position", position);
            args.putInt("depth", 0);

            Log.d("transitionName", v.getTransitionName());

            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(v, v.getTransitionName())
                    .build();

            navController.navigate(
                    R.id.action_navigation_search_result_to_pinFragment,
                    args,
                    null,
                    extras
            );
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}