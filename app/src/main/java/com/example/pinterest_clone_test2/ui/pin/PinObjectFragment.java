package com.example.pinterest_clone_test2.ui.pin;

import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentPinObjectBinding;
import com.example.pinterest_clone_test2.interfaces.ImageClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.pin_comment.CommentModalBottomSheet;

import java.util.Objects;

public class PinObjectFragment extends Fragment {
    PinObjectViewModel viewModel;
    private Pin pin;
    FragmentPinObjectBinding binding;
    String source;

    // need this to prevent crash idk why
    public PinObjectFragment() {
    }

    public PinObjectFragment(Pin pin, String source) {
        this.pin = pin;
        this.source = source;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPinObjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity().getApplication(), this)).get(PinObjectViewModel.class);
        initializeRelevantPins(view);

        binding.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pin != null) {
                    CommentModalBottomSheet modalBottomSheet = new CommentModalBottomSheet(pin.getId());
                    modalBottomSheet.show(requireActivity().getSupportFragmentManager(), CommentModalBottomSheet.TAG);
                } else {
                    Toast.makeText(getContext(), "Pin is null, we are fucked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            }
        });
    }

    private void restoreStates() {
        Parcelable scroll_state = viewModel.getScrollState();
        if (scroll_state != null && binding.rvRelevant.getLayoutManager() != null) {
            binding.rvRelevant.getLayoutManager().onRestoreInstanceState(scroll_state);
        }

        Pin pin_state = viewModel.getPinState();
        if (pin_state != null) {
            pin = pin_state;
        }

        String source_state = viewModel.getSource();
        if (source_state != null) {
            source = source_state;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding.rvRelevant.getLayoutManager() != null) {
            viewModel.setScrollState(binding.rvRelevant.getLayoutManager().onSaveInstanceState());
        }
//        appBarLayout.getTop();
        viewModel.setPinState(pin);
        viewModel.setSourceState(source);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreStates();

        if (pin == null) {
            Toast.makeText(getContext(), "Pin is null, idk why", Toast.LENGTH_SHORT).show();
            Log.d("error", "Pin is null, why is the view model dead??? How come the data are still intact, make no fucking sense");
        } else {
            Glide.with(binding.ivImage.getContext())
                    .load(pin.getMediaURL())
                    .fitCenter()
                    .into(binding.ivImage);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializeRelevantPins(@NonNull View view) {
        PinListAdapter adapter = new PinListAdapter(Pin.testData, relevantImageClickListener);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        binding.rvRelevant.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvRelevant.setLayoutManager(layoutManager);
    }

    private final ImageClickListener relevantImageClickListener = new ImageClickListener() {
        @Override
        public void OnClick(int position, View v) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            bundle.putString("source", source);
//            bundle.putParcelableArrayList("pins", relevant_pins);  // use this when we have real relevant images

            int action = Objects.equals(source, "home") ? R.id.action_pinFragment_self : R.id.action_pinFragment2_self;

            navController.navigate(
                    action,
                    bundle,
                    null,
                    null
            );
        }
    };
}
