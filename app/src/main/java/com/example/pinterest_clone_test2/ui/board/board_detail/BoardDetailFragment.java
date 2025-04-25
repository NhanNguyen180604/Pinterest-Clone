package com.example.pinterest_clone_test2.ui.board.board_detail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.BoardDetailActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinInBoardAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentBoardDetailBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.account.board_tab.AddCollaboratorBottomSheet;
import com.example.pinterest_clone_test2.ui.pin.PinFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class BoardDetailFragment extends Fragment {
    private Board board;
    private String source = "haha";
    private List<Pin> pins;
    FragmentBoardDetailBinding binding;
    private final Handler inactivityHandler = new Handler();
    private Runnable showBarRunnable;
    LinearLayout bottomBar;
    LinearLayout layoutCollaborators;

    public BoardDetailFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBoardDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        board = getArguments() != null ? getArguments().getParcelable("board") : null;
        source = getArguments() != null ? getArguments().getString("source") : null;
        if (board == null) return;
        binding.tvBoardTitle.setText(board.getName());
        if (board.getPins().size() > 1) {
            binding.tvNumberOfPins.setText(String.format(Locale.US, "%d %s", board.getPins().size(), getResources().getString(R.string.pins).toLowerCase()));
        } else {
            binding.tvNumberOfPins.setText(String.format(Locale.US, "%d %s", board.getPins().size(), getResources().getString(R.string.pin).toLowerCase()));
        }
        int overlapMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -10, getResources().getDisplayMetrics());
        layoutCollaborators = view.findViewById(R.id.layout_collaborators);
        layoutCollaborators.removeAllViews();
        List<String> collaboratorIds = board.getCollaborators();
        AtomicInteger avatarsAdded = new AtomicInteger(0);
        int totalAvatars = collaboratorIds.size() + 1;
        DocumentSnapshot currentUserBoardDocument = FirebaseBoardService.getCurrentUserBoardDocument();
        FirebaseUserService.getUserById(currentUserBoardDocument.getString("userId"), task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    ShapeableImageView imageView = new ShapeableImageView(new ContextThemeWrapper(requireContext(), R.style.roundedImageView));
                    int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    imageView.setLayoutParams(params);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_white));
                    imageView.setClickable(true);
                    imageView.setImageResource(R.drawable.ic_account_circle);
                    imageView.setOnClickListener(v -> {
                        showAddCollabBottomSheet();
                    });

                    RequestOptions glideOptions = new RequestOptions()
                            .placeholder(R.drawable.ic_loading)
                            .error(R.drawable.ic_account_circle)
                            .centerCrop();

                    Glide.with(requireContext())
                            .load(document.getString("avatarUrl"))
                            .apply(glideOptions)
                            .into(imageView);

                    layoutCollaborators.addView(imageView);
                    if (avatarsAdded.incrementAndGet() == totalAvatars) {
                        addAddCollaboratorIcon();
                    }
                    for (int i = 0; i < collaboratorIds.size(); i++) {
                        String userId = collaboratorIds.get(i);
                        FirebaseUserService.getUserAvatarUrl(userId, new FirebaseUserService.OnUserAvatarFetchedCallback() {
                            @Override
                            public void onSuccess(String avatarUrl) {
                                ShapeableImageView imageView = new ShapeableImageView(new ContextThemeWrapper(requireContext(), R.style.roundedImageView));
                                int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                                imageView.setLayoutParams(params);
                                params.setMarginStart(overlapMargin);
                                imageView.setLayoutParams(params);
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                imageView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_white));
                                imageView.setClickable(true);
                                imageView.setImageResource(R.drawable.ic_account_circle);
                                imageView.setOnClickListener(v -> {
                                    showAddCollabBottomSheet();
                                });

                                RequestOptions glideOptions = new RequestOptions()
                                        .placeholder(R.drawable.ic_loading)
                                        .error(R.drawable.ic_account_circle)
                                        .centerCrop();

                                Glide.with(requireContext())
                                        .load(avatarUrl)
                                        .apply(glideOptions)
                                        .into(imageView);

                                layoutCollaborators.addView(imageView);
                                if (avatarsAdded.incrementAndGet() == totalAvatars) {
                                    addAddCollaboratorIcon();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("FirebaseUserService", "Failed to fetch user avatar", e);
                                if (avatarsAdded.incrementAndGet() == totalAvatars) {
                                    addAddCollaboratorIcon();
                                }
                            }
                        });
                    }
                } else {
                    Log.d("Firestore", "No such user");
                }
            } else {
                Log.e("Firestore", "Error fetching user", task.getException());
            }
        });
        pins = board.getPinsObj();
        PinInBoardAdapter adapter = getPinListAdapter();
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        RecyclerView rvBoardPins = binding.rvBoardPins;
        rvBoardPins.setHasFixedSize(true);
        rvBoardPins.setLayoutManager(layoutManager);
        rvBoardPins.setAdapter(adapter);
        Log.d("BoardDetailFragment", "Source: " + source);
        binding.btnTune.setOnClickListener(v -> {
            BoardDetailSetViewBottomSheet bottomSheet = BoardDetailSetViewBottomSheet.newInstance(adapter.getViewMode());
            bottomSheet.setOnViewModeSelectedListener(selectedViewMode -> {
                adapter.setViewMode(selectedViewMode);
                applyChanges(selectedViewMode);
            });
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
        binding.progressLoading.setVisibility(View.GONE);
        binding.btnAddCollaborators.setOnClickListener(v -> {
            showAddCollabBottomSheet();
        });
        bottomBar = binding.llEditBoardOptions;
        showBarRunnable = () -> {
            if (bottomBar.getVisibility() != View.VISIBLE) {
                bottomBar.setVisibility(View.VISIBLE);
                bottomBar.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up));
            }
        };
        binding.btnOrganize.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putParcelable("board", board);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_board_detail);
            navController.navigate(R.id.action_boardDetailFragment2_to_boardDetailOrganizeFragment, args, null, null);
        });
        if (!Objects.equals(source, "DeepLink")) {
            rvBoardPins.setOnTouchListener((v, e) -> {
                resetInactivityTimer();
                return false;
            });
        } else {
            binding.llEditBoardOptions.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        inactivityHandler.removeCallbacks(showBarRunnable);
    }

    @NonNull
    private PinInBoardAdapter getPinListAdapter() {
        PinClickListener pinClickListener = (position, clickedView) -> {
            try {
                Bundle args = new Bundle();
                args.putParcelableArrayList("pins", new ArrayList<>(pins));
                args.putInt("position", position);
                args.putString("source", BoardDetailActivity.SOURCE);
                PinFragment fragment = new PinFragment();
                fragment.setArguments(args);
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_board_detail);
                navController.navigate(R.id.action_boardDetailFragment2_to_pinFragment42, args, null, null);
            } catch (Exception e) {
                Log.e("BoardDetailFragment", "Error while opening PinFragment", e);
            }
        };

        return new PinInBoardAdapter(requireContext(), pins, pinClickListener);
    }

    private void resetInactivityTimer() {
        inactivityHandler.removeCallbacks(showBarRunnable);
        inactivityHandler.postDelayed(showBarRunnable, 2000);

        if (bottomBar.getVisibility() == View.VISIBLE) {
            bottomBar.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down));
            ;
            bottomBar.setVisibility(View.GONE);
        }
    }

    private void addAddCollaboratorIcon() {
        ImageView addIcon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics())
        );
        iconParams.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -10, getResources().getDisplayMetrics()));
        addIcon.setLayoutParams(iconParams);
        addIcon.setBackgroundResource(R.drawable.bg_circle_gray);
        addIcon.setImageResource(R.drawable.ic_add_person);
        addIcon.setClickable(true);
        addIcon.setOnClickListener(v -> {
            showAddCollabBottomSheet();
        });
        addIcon.setPadding(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics())
        );

        layoutCollaborators.addView(addIcon);
    }

    private void showAddCollabBottomSheet() {
        AddCollaboratorBottomSheet bottomSheet = AddCollaboratorBottomSheet.newInstance(board.getId());
        Log.d("BoardDetailFragment", "Board ID: " + board.getId());
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    private void applyChanges(int viewMode) {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(viewMode, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        RecyclerView rvBoardPins = binding.rvBoardPins;
        rvBoardPins.setLayoutManager(layoutManager);
        rvBoardPins.getAdapter().notifyDataSetChanged();
    }
}
