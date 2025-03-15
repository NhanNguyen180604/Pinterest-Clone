package com.example.pinterest_clone_test2.ui.pin;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.interfaces.ImageClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PinObjectFragment extends Fragment {
    RecyclerView rv_relevant;
    ImageView iv_image;
    PinObjectViewModel view_model;
    FloatingActionButton fab_back;
    private Pin pin;

    // need this to prevent crash idk why
    public PinObjectFragment() {
    }

    public PinObjectFragment(Pin pin) {
        this.pin = pin;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pin_pager_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_model = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity().getApplication(), this)).get(PinObjectViewModel.class);
        iv_image = view.findViewById(R.id.iv_image);
        fab_back = view.findViewById(R.id.btn_back);
        initializeRelevantPins(view);

        fab_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
        });
    }

    private void restoreStates() {
        Parcelable scroll_state = view_model.getScrollState();
        if (scroll_state != null && rv_relevant.getLayoutManager() != null) {
            rv_relevant.getLayoutManager().onRestoreInstanceState(scroll_state);
        }

        Pin pin_state = view_model.getPinState();
        if (pin_state != null) {
            pin = pin_state;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (rv_relevant.getLayoutManager() != null) {
            view_model.setScrollState(rv_relevant.getLayoutManager().onSaveInstanceState());
        }
//        appBarLayout.getTop();
        view_model.setPinState(pin);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreStates();

        Glide.with(iv_image.getContext())
                .load(pin.getMediaURL())
                .fitCenter()
                .into(iv_image);
    }

    private void initializeRelevantPins(@NonNull View view) {
        rv_relevant = view.findViewById(R.id.rv_relevant);
        PinListAdapter adapter = new PinListAdapter(Pin.testData, relevantImageClickListener);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        rv_relevant.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rv_relevant.setLayoutManager(layoutManager);
    }

    private final ImageClickListener relevantImageClickListener = new ImageClickListener() {
        @Override
        public void OnClick(int position, View v) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
//            bundle.putParcelableArrayList("pins", relevant_pins);  // use this when we have real relevant images

            navController.navigate(
                    R.id.action_pinFragment_self,
                    bundle,
                    null,
                    null
            );
        }
    };
}
