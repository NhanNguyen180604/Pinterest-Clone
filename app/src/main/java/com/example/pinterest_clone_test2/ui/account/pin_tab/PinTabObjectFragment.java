package com.example.pinterest_clone_test2.ui.account.pin_tab;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentPinTabObjectBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.pin.PinFragment;
import com.google.common.collect.Lists;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PinTabObjectFragment extends Fragment {

    FragmentPinTabObjectBinding binding;
    PinListAdapter adapter;
    List<Pin> pins = new ArrayList<>();
    List<String> pinIds = new ArrayList<>();
    PinTabObjectViewModel viewModel;
    Handler handler = new Handler();
    long profileLastUpdated = 0;

    int page = 1;
    final int perPage = 20;
    int totalPage = 0;
    boolean isOnLastPage = false;
    boolean isLoading = false;

    public PinTabObjectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPinTabObjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity().getApplication(), this)).get(PinTabObjectViewModel.class);
    }

    void fetchPinsAsync() {
        Thread thread = new Thread(() -> {
            if (isOnLastPage || isLoading)
                return;

            Log.d("AccountPinTab", "Fetching pins");
            isLoading = true;

            try {
                DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
                assert currentUserDocument != null;
                FirebasePinService.fetchPinsFromIds(pinIds.subList((page - 1) * perPage, Math.min(page * perPage, pinIds.size())), onPinsFetchedFromIdsCallback);
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(requireContext(), getResources().getString(R.string.fetch_pin_failure), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
    }

    final FirebasePinService.OnPinsFetchedFromIdsCallback onPinsFetchedFromIdsCallback = new FirebasePinService.OnPinsFetchedFromIdsCallback() {
        @Override
        public void onSuccess(List<Pin> newPins) {
            page++;
            // janky coding belike
            if (page == totalPage + 1) {
                isOnLastPage = true;
            }

            long lastUpdateTime = FirebaseUserService.getLastUpdateTime();
            handler.post(() -> updateUI(newPins, profileLastUpdated == lastUpdateTime));
            profileLastUpdated = lastUpdateTime;
        }

        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
            isLoading = false;
        }
    };

    void updateUI(List<Pin> newPins, boolean append) {
        if (binding == null) {
            return;
        }

        binding.progressBar.setVisibility(View.GONE);

        if (!append) {
            pins.clear();
        }
        int startPos = pins.size();
        pins.addAll(newPins);
        adapter.notifyItemRangeInserted(startPos, newPins.size());

        isLoading = false;
        restoreScrollState();
    }

    private void restoreScrollState() {
        if (binding == null)
            return;

        Parcelable scroll_state = viewModel.getScrollState();
        if (scroll_state != null && binding.rvPins.getLayoutManager() != null) {
            binding.rvPins.getLayoutManager().onRestoreInstanceState(scroll_state);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding.rvPins.getLayoutManager() != null) {
            viewModel.setScrollState(binding.rvPins.getLayoutManager().onSaveInstanceState());
        }
        viewModel.setPinState(pins);
        viewModel.setOnLastPage(isOnLastPage);
        viewModel.setLastUpdateTime(profileLastUpdated);
        viewModel.setPinIdsState(pinIds);
        viewModel.setPageState(page);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        initRecyclerView();
        binding.progressBar.setVisibility(View.VISIBLE);

        long oldLastUpdateState = viewModel.getLastUpdateTime();
        if (oldLastUpdateState != 0)
            profileLastUpdated = oldLastUpdateState;

        long lastUpdateTime = FirebaseUserService.getLastUpdateTime();
        if (lastUpdateTime != profileLastUpdated) {
            isOnLastPage = false;

            try {
                DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
                assert currentUserDocument != null;
                List<String> userPinIds = (List<String>) currentUserDocument.get("pins");
                if (userPinIds != null) {
                    if (pinIds.isEmpty()) {
                        pinIds.addAll(Lists.reverse(userPinIds));
                    } else {
                        pinIds = Lists.reverse(userPinIds);
                    }
                }
                totalPage = (int) Math.ceil((double) pinIds.size() / perPage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            fetchPinsAsync();
            return;
        }

        int oldPageState = viewModel.getPageState();
        if (oldPageState > 0) {
            page = oldPageState;
        }

        List<String> oldPinIdsState = viewModel.getPinIdsState();
        if (oldPinIdsState != null && !oldPinIdsState.isEmpty()) {
            pinIds = oldPinIdsState;
        } else {
            try {
                DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
                assert currentUserDocument != null;
                List<String> userPinIds = (List<String>) currentUserDocument.get("pins");
                if (userPinIds != null)
                    pinIds.addAll(userPinIds);
                totalPage = (int) Math.ceil((double) pinIds.size() / perPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<Pin> oldPinState = viewModel.getPinState();
        if (oldPinState == null || oldPinState.isEmpty()) {
            fetchPinsAsync();
        } else if (pins.isEmpty()) {
            updateUI(oldPinState, false);
        } else {
            restoreScrollState();
            binding.progressBar.setVisibility(View.GONE);
        }

        isOnLastPage = viewModel.isOnLastPage();
    }

    void initRecyclerView() {
        adapter = new PinListAdapter(requireContext(), pins, pinClickListener);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        binding.rvPins.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvPins.setHasFixedSize(true);
        binding.rvPins.setLayoutManager(layoutManager);

        binding.rvPins.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isOnLastPage || isLoading)
                    return;

                int totalItemCount = layoutManager.getItemCount();
                int[] firstVisibleItems = layoutManager.findFirstVisibleItemPositions(null);
                int firstVisibleItem = firstVisibleItems.length > 0 ? firstVisibleItems[0] : 0;
                int visibleItemCount = layoutManager.getChildCount();
                final int threshold = 4;

                if ((visibleItemCount + firstVisibleItem) >= totalItemCount - threshold) {
                    fetchPinsAsync();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    PinClickListener pinClickListener = (position, v) -> {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_pin_deep_link);
        Bundle args = new Bundle();
        args.putParcelableArrayList("pins", new ArrayList<>(pins));
        args.putInt("position", position);
        args.putString("source", "account");
        PinFragment fragment = new PinFragment();
        fragment.setArguments(args);
        navController.navigate(R.id.action_navigation_account_to_pinFragment3, args, null, null);
    };
}