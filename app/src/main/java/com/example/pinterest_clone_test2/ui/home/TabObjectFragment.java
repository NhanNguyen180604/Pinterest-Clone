package com.example.pinterest_clone_test2.ui.home;

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
import com.example.pinterest_clone_test2.databinding.FragmentHomePagerItemBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TabObjectFragment extends Fragment {
    List<Pin> pins = new ArrayList<>();
    PinListAdapter adapter;
    Board board;
    TabObjectViewModel viewModel;
    Handler handler = new Handler();
    FragmentHomePagerItemBinding binding;
    final int perPage = 20;
    boolean isOnLastPage = false;
    boolean isLoading = false;
    DocumentSnapshot lastVisible;  // for pagination

    // need this to prevent crash idk why
    public TabObjectFragment() {
    }

    public TabObjectFragment(Board board) {
        this.board = board;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomePagerItemBinding.inflate(inflater, container, false);
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
        viewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity().getApplication(), this)).get(TabObjectViewModel.class);
    }

    void fetchPinsAsync() {
        Thread thread = new Thread(() -> {
            if (isOnLastPage || isLoading)
                return;

            Log.d("HomeTab", "Fetching pins");
            isLoading = true;

            // pretend to have an algorithm that fetch pins based on this board's content
            // no way we can do this
            try {
                FirebasePinService.getPins(lastVisible, perPage, null, callback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    final FirebasePinService.GetPinServiceCallback callback = new FirebasePinService.GetPinServiceCallback() {
        @Override
        public void OnSuccess(QuerySnapshot querySnapshot) {
            List<Pin> newPins = new ArrayList<>();
            List<DocumentSnapshot> documents = querySnapshot.getDocuments();

            // exclude blocked pins, authors...
            DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
            if (currentUserDocument != null) {
                List<String> blockedPins = null;
                List<String> blockedUsers = null;

                try {
                    blockedPins = (List<String>) currentUserDocument.get("blockedPins");
                    blockedUsers = (List<String>) currentUserDocument.get("blockedUsers");
                } catch (Exception e) {
                    // eat exception
                }

                if (blockedPins != null) {
                    List<String> finalBlockedPins = blockedPins;
                    documents.removeIf(doc -> finalBlockedPins.contains(doc.getId()));
                }
                if (blockedUsers != null) {
                    List<String> finalBlockedUsers = blockedUsers;
                    documents.removeIf(doc -> finalBlockedUsers.contains(doc.getString("authorId")));
                }
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.pin_filter_failure), Toast.LENGTH_SHORT).show();
            }

            if (documents.isEmpty()) {
                isOnLastPage = true;
                isLoading = false;
                return;
            }

            lastVisible = documents.get(documents.size() - 1);
            Log.d("HomeTabLastVisible", lastVisible.getId());

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
            handler.post(() -> updateUI(newPins, true));
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

    @Override
    public void onPause() {
        super.onPause();
        if (binding.homePagerRecyclerView.getLayoutManager() != null) {
            viewModel.setScrollState(binding.homePagerRecyclerView.getLayoutManager().onSaveInstanceState());
        }
        viewModel.setPinState(pins);
        viewModel.setOnLastPage(isOnLastPage);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        initRecyclerView();
        binding.progressBar.setVisibility(View.VISIBLE);

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

    private void initRecyclerView() {
        Log.d("HomeTab", "Init recyclerview");
        adapter = new PinListAdapter(requireContext(), pins, pinClickListener);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        binding.homePagerRecyclerView.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.homePagerRecyclerView.setHasFixedSize(true);
        binding.homePagerRecyclerView.setLayoutManager(layoutManager);

        binding.homePagerRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void restoreScrollState() {
        if (binding == null)
            return;

        Parcelable scroll_state = viewModel.getScrollState();
        if (scroll_state != null && binding.homePagerRecyclerView.getLayoutManager() != null) {
            binding.homePagerRecyclerView.getLayoutManager().onRestoreInstanceState(scroll_state);
        }
    }

    private final PinClickListener pinClickListener = (position, v) -> {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_pin_deep_link);

        Bundle args = new Bundle();
        args.putParcelableArrayList("pins", new ArrayList<>(pins));
        args.putInt("position", position);
        args.putString("source", "home");

        navController.navigate(
                R.id.action_navigation_home_to_pinFragment,
                args,
                null,
                null
        );
    };
}