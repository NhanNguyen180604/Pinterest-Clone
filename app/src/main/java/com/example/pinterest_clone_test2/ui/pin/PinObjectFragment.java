package com.example.pinterest_clone_test2.ui.pin;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.TransitionSet;
import android.util.Log;
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
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.interfaces.ImageClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PinObjectFragment extends Fragment {
    RecyclerView rv_relevant;
    ImageView iv_image;
    PinObjectViewModel view_model;
    FloatingActionButton fab_back;
    private Pin pin;
    private int position;
    private int depth;
    private boolean isInitial;

    // need this to prevent crash idk why
    public PinObjectFragment() {
    }

    public PinObjectFragment(Pin pin, int position, int depth, boolean isInitial) {
        this.pin = pin;
        this.position = position;
        this.depth = depth;
        this.isInitial = isInitial;
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

        if (isInitial && !view_model.getTransitionFinishedState()) {
            fab_back.setVisibility(View.INVISIBLE);
        }

        restoreStates();

        iv_image.setTransitionName(Integer.toString(pin.getImageSource()) + depth + position);
        Log.d("transitionName2", iv_image.getTransitionName());

        fab_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
        });

        if (getParentFragment() != null) {
            TransitionSet transitionSet = (TransitionSet) getParentFragment().getSharedElementEnterTransition();
            if (transitionSet != null) {
                transitionSet.addListener(new android.transition.Transition.TransitionListener() {
                    @Override
                    public void onTransitionEnd(android.transition.Transition transition) {
                        fab_back.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onTransitionStart(android.transition.Transition transition) {
                    }

                    @Override
                    public void onTransitionCancel(android.transition.Transition transition) {
                    }

                    @Override
                    public void onTransitionPause(android.transition.Transition transition) {
                    }

                    @Override
                    public void onTransitionResume(android.transition.Transition transition) {
                    }
                });
            }
        }
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

        int is_initial_state = view_model.getInitialState();
        if (is_initial_state != -1) {
            isInitial = is_initial_state != 0;
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
        // saving state of this for the animation to start after navigating back to home
        view_model.setInitialState(isInitial ? 1 : 0);
        view_model.setTransitionFinishedState(true);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        Glide.with(iv_image.getContext())
                .load(pin.getImageSource())
                .fitCenter()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        if ((isInitial || view_model.getTransitionFinishedState()) && getParentFragment() != null) {
                            getParentFragment().startPostponedEnterTransition();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        if ((isInitial || view_model.getTransitionFinishedState()) && getParentFragment() != null) {
                            getParentFragment().startPostponedEnterTransition();
                        }
                        return false;
                    }
                })
                .into(iv_image);
    }

    private void initializeRelevantPins(@NonNull View view) {
        rv_relevant = view.findViewById(R.id.rv_relevant);
        PinListAdapter adapter = new PinListAdapter(Pin.testData, relevantImageClickListener, depth + 1);
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
            bundle.putInt("depth", depth + 1);
//            bundle.putParcelableArrayList("pins", relevant_pins);  // use this when we have real relevant images

            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(v, v.getTransitionName())
                    .build();

            navController.navigate(
                    R.id.action_pinFragment_self,
                    bundle,
                    null,
                    extras
            );
        }
    };
}
