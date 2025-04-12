package com.example.pinterest_clone_test2.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentSearchResultBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment {

    private FragmentSearchResultBinding binding;
    private String query = "";

    public SearchResultFragment() {
        // Required empty public constructor
    }

    List<Pin> pins = new ArrayList<>();
    SearchResultViewModel view_model;

    PinListAdapter adapter;
    Handler handler = new Handler();
    private final int perPage = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private DocumentSnapshot lastVisible = null;
    long lastSubmitTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query = getArguments().getString("query");
        }

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
        // Thiết lập UI tìm kiếm
        setupSearchUI();

        // Thiết lập RecyclerView
        adapter = new PinListAdapter(requireContext(), pins, pinClickListener);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        binding.rvSearchResult.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvSearchResult.setHasFixedSize(true);
        binding.rvSearchResult.setLayoutManager(layoutManager);

        // Thêm scroll listener cho phân trang
        binding.rvSearchResult.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isLastPage || isLoading)
                    return;

                int totalItemCount = layoutManager.getItemCount();
                int[] firstVisibleItems = layoutManager.findFirstVisibleItemPositions(null);
                int firstVisibleItem = firstVisibleItems.length > 0 ? firstVisibleItems[0] : 0;
                int visibleItemCount = layoutManager.getChildCount();
                final int threshold = 4;

                if ((visibleItemCount + firstVisibleItem) >= totalItemCount - threshold) {
                    performSearch();
                }
            }
        });

        // Bắt đầu tìm kiếm nếu có query
        if (query != null && !query.isEmpty()) {
            resetSearchState();
            performSearch();
        }
    }

    private void setupSearchUI() {
        binding.svSearchBar.post(() -> {
            binding.svSearchBar.setQuery("", false);
            binding.svSearchBar.setQuery(query, false);
        });

        binding.svSearchBar.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            binding.btnSearchCancel.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });

        binding.btnSearchCancel.setOnClickListener(v -> {
            binding.svSearchBar.setQuery("", false);
            binding.svSearchBar.clearFocus();
        });

        binding.svSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchResultFragment.this.query = query;
                resetSearchState();
                performSearch();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.backBtn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_pin_deep_link);
            navController.navigateUp();
        });
    }

    private void resetSearchState() {
        pins.clear();
        adapter.notifyDataSetChanged();
        isLastPage = false;
        isLoading = false;
        lastVisible = null;
    }

    private void performSearch() {
        long now = System.currentTimeMillis();
        if (now - lastSubmitTime < 1000)
            return;

        if (query == null || query.isEmpty() || isLoading || isLastPage) {
            return;
        }

        lastSubmitTime = now;
        isLoading = true;

        // Hiển thị progress bar
        binding.progressBar.setVisibility(View.VISIBLE);

        Thread thread = new Thread(() -> {
            try {
                FirebasePinService.searchPins(query, lastVisible, perPage, new FirebasePinService.SearchPinServiceCallback() {
                    @Override
                    public void onSearchSuccess(List<Pin> results, DocumentSnapshot lastDoc) {
                        lastVisible = lastDoc;

                        if (results.isEmpty()) {
                            isLastPage = true;

                            handler.post(() -> {
                                isLoading = false;

                                // Ẩn progress bar
                                binding.progressBar.setVisibility(View.GONE);

                                // Hiển thị thông báo không có kết quả nếu không có pin nào
                                if (pins.isEmpty()) {
                                    binding.tvEmptyResults.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            handler.post(() -> {
                                // Ẩn thông báo không có kết quả
                                binding.tvEmptyResults.setVisibility(View.GONE);

                                int startPos = pins.size();
                                pins.addAll(results);
                                adapter.notifyItemRangeInserted(startPos, results.size());

                                isLoading = false;

                                // Ẩn progress bar
                                binding.progressBar.setVisibility(View.GONE);
                            });
                        }
                    }

                    @Override
                    public void onSearchFailure(Exception e) {
                        Log.e("SearchResultFragment", "Search failed", e);

                        handler.post(() -> {
                            isLoading = false;

                            // Ẩn progress bar
                            binding.progressBar.setVisibility(View.GONE);

                            // Hiển thị thông báo lỗi
                            Toast.makeText(requireContext(), "Không thể tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } catch (Exception e) {
                Log.e("SearchResultFragment", "Error while searching", e);

                handler.post(() -> {
                    isLoading = false;

                    // Ẩn progress bar
                    binding.progressBar.setVisibility(View.GONE);

                    // Hiển thị thông báo lỗi
                    Toast.makeText(requireContext(), "Lỗi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });

        thread.start();
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

    private final PinClickListener pinClickListener = (position, v) -> {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_pin_deep_link);

        Bundle args = new Bundle();
        args.putParcelableArrayList("pins", new ArrayList<>(pins));
        args.putInt("position", position);
        args.putString("source", "search");

        navController.navigate(
                R.id.action_navigation_search_result_to_pinFragment2,
                args,
                null,
                null
        );
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}