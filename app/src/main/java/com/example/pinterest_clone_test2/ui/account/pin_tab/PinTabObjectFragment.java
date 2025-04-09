package com.example.pinterest_clone_test2.ui.account.pin_tab;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentPinTabObjectBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PinTabObjectFragment extends Fragment {

    FragmentPinTabObjectBinding binding;
    PinListAdapter adapter;
    List<Pin> pins = new ArrayList<>();
    PinTabObjectViewModel viewModel;
    Handler handler = new Handler();
    long profileLastUpdated = 0;

    final int perPage = 20;
    boolean isOnLastPage = false;
    boolean isLoading = false;
    boolean fetchSavedPins = true;
    DocumentSnapshot lastVisible;  // for pagination

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
                FirebasePinService.getUserProfilePins(currentUserDocument.getId(), lastVisible, perPage, callback, fetchSavedPins);
                fetchSavedPins = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    final FirebasePinService.GetProfilePinServiceCallback callback = new FirebasePinService.GetProfilePinServiceCallback() {
        @Override
        public void OnSuccess(List<DocumentSnapshot> documents, DocumentSnapshot returnLastVisible) {
            List<Pin> newPins = new ArrayList<>();

            if (documents.isEmpty()) {
                isOnLastPage = true;
                isLoading = false;
                return;
            }

            lastVisible = returnLastVisible;

            // create pins from documents
            for (DocumentSnapshot document :
                    documents) {
                Pin pin = new Pin()
                        .setId(document.getId())
                        .setAllowComment(Boolean.TRUE.equals(document.getBoolean("allowComment")))
                        .setAuthorId(document.getString("authorId"))
                        .setMediaUrl(document.getString("mediaUrl"))
                        .setThumbnailUrl(document.getString("thumbnailUrl"))
                        .setType(document.get("type", Pin.PinType.class));

                String description = document.getString("description");
                String name = document.getString("name");
                pin.setDescription(description != null ? description : "")
                        .setName(name != null ? name : "");

                Long createdAt = document.getLong("createdAt");
                Integer likeCount = document.get("likeCount", Integer.class);
                pin.setCreatedAt(createdAt != null ? createdAt : 0);
                pin.setLikeCount(likeCount != null ? likeCount : 0);

                newPins.add(pin);
            }
            long lastUpdateTime = FirebaseUserService.getLastUpdateTime();
            handler.post(() -> updateUI(newPins, profileLastUpdated == lastUpdateTime));
            profileLastUpdated = lastUpdateTime;
        }

        @Override
        public void OnFailure(Exception e) {
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
            fetchSavedPins = true;
            isOnLastPage = false;
            lastVisible = null;
            fetchPinsAsync();
            return;
        }

        // restore states
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
        adapter = new PinListAdapter(pins, pinClickListener);
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

    PinClickListener pinClickListener = new PinClickListener() {
        @Override
        public void OnClick(int position, View v) {

        }
    };
}