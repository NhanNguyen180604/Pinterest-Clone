package com.example.pinterest_clone_test2.ui.home;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.interfaces.ImageClickListener;
import com.example.pinterest_clone_test2.models.Pin;

import java.util.ArrayList;
import java.util.List;

public class TabObjectFragment extends Fragment {
    RecyclerView recycler_view;
    List<Pin> pins;
    TabObjectViewModel view_model;

    // need this to prevent crash idk why
    public TabObjectFragment() {
    }

    public TabObjectFragment(List<Pin> pins) {
        this.pins = pins;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_pager_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view_model = new ViewModelProvider(this).get(TabObjectViewModel.class);

        recycler_view = view.findViewById(R.id.home_pager_recycler_view);
        PinListAdapter adapter = new PinListAdapter(pins, imageClickListener, 0);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        recycler_view.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(layoutManager);

        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int[] pastVisibleItemCount = layoutManager.findFirstVisibleItemPositions(null);

                if (dy > 0) {
                    if (visibleItemCount + pastVisibleItemCount[0] >= totalItemCount) {
                        Log.d("scrolling", "end of scroll reach, get more pins here");
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recycler_view.getLayoutManager() != null) {
            view_model.setScrollState(recycler_view.getLayoutManager().onSaveInstanceState());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Parcelable scroll_state = view_model.getScrollState();
        if (scroll_state != null && recycler_view.getLayoutManager() != null) {
            recycler_view.getLayoutManager().onRestoreInstanceState(scroll_state);
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
                    R.id.action_navigation_home_to_pinFragment,
                    args,
                    null,
                    extras
            );
        }
    };
}