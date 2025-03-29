package com.example.pinterest_clone_test2.ui.pin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.CreateBoardActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentPinObjectBinding;
import com.example.pinterest_clone_test2.interfaces.ImageClickListener;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.ui.pin.btn_comment.CommentModalBottomSheet;
import com.example.pinterest_clone_test2.ui.pin.btn_more.PinMoreActionModalBottomSheet;

import java.util.Locale;
import java.util.Objects;

public class PinObjectFragment extends Fragment {
    PinObjectViewModel viewModel;
    private Pin pin;
    FragmentPinObjectBinding binding;
    String source;
    Handler handler = new Handler();
    ActivityResultLauncher<Intent> createBoardActivityLauncher;

    // need this to prevent crash idk why
    public PinObjectFragment() {
    }

    public PinObjectFragment(Pin pin, String source) {
        this.pin = pin;
        this.source = source;
    }

    void fetchAuthorAsync() {
        Thread thread = new Thread(() -> {
            //TODO: fetch author here
            Log.d("pin-object-fragment", "Fetching author info");
            handler.post(this::updateAuthor);
        });
        thread.start();
    }

    void updateAuthor() {
        //TODO: update author info

        //TODO: save author info into viewmodel to survive configuration changes and whatever shiet that makes the data die

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createBoardActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("added", false)) {
                            //TODO: send new board information and pin to database
                            if (data.getBooleanExtra("profile", false)) {
                                //TODO: save pin to profile
                                Toast.makeText(requireContext(), "Saving pin to your profile", Toast.LENGTH_SHORT).show();
                            } else {
                                //TODO: save pin to board
                                String boardName = data.getStringExtra("boardName");
                                boolean isPrivate = data.getBooleanExtra("isPrivate", false);
                                boolean isNew = data.getBooleanExtra("isNew", false);

                                Toast.makeText(
                                        requireContext(),
                                        String.format(Locale.US, "Saving to board: %s, is private: %b, is new: %b", boardName, isPrivate, isNew),
                                        Toast.LENGTH_SHORT
                                ).show();

                                String boardId;
                                if (!isNew) {
                                    boardId = data.getStringExtra("boardId");
                                    Toast.makeText(requireContext(), "Board id to insert: " + boardId, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
        );
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
        initializeRelevantPins();

        fetchAuthorAsync();

        binding.btnComment.setOnClickListener(v -> {
            if (pin != null) {
                CommentModalBottomSheet modalBottomSheet = new CommentModalBottomSheet(pin.getId(), requireContext());
                modalBottomSheet.show(requireActivity().getSupportFragmentManager(), CommentModalBottomSheet.TAG);
            } else {
                Toast.makeText(requireContext(), "Pin is null, idk why", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMore.setOnClickListener(v -> {
            if (pin != null) {
                PinMoreActionModalBottomSheet sheet = new PinMoreActionModalBottomSheet(pin, downloadPinMediaCallback);
                sheet.show(requireActivity().getSupportFragmentManager(), PinMoreActionModalBottomSheet.TAG);
            } else {
                Toast.makeText(requireContext(), "Pin is null, end my suffering", Toast.LENGTH_SHORT).show();
            }
        });

        binding.fabBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });

        binding.btnLove.setOnClickListener(v -> {
            if (pin != null) {
                boolean isLiked = pin.getIsLiked();
                binding.btnLove.setImageResource(isLiked ? R.drawable.ic_favorite_heart : R.drawable.ic_favorite_heart_filled);
                pin.setLikeCount(pin.getLikeCount() + (isLiked ? -1 : 1));
                pin.setIsLiked(!isLiked);
                binding.tvLikeCount.setText(String.valueOf(pin.getLikeCount()));
            } else {
                Toast.makeText(requireContext(), "Pin is null, one must imagine CS students being happy", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            if (pin != null) {
                Intent intent = new Intent(requireActivity(), CreateBoardActivity.class);
                intent.putExtra("pin", pin);
                createBoardActivityLauncher.launch(intent);
            } else {
                Toast.makeText(requireContext(), "Pin is null, i regret studying this major", Toast.LENGTH_SHORT).show();
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

        //TODO: restore author state
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding.rvRelevant.getLayoutManager() != null) {
            viewModel.setScrollState(binding.rvRelevant.getLayoutManager().onSaveInstanceState());
        }
        viewModel.setPinState(pin);
        viewModel.setSourceState(source);
        //TODO: save author into view model
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreStates();

        if (pin == null) {
            Toast.makeText(requireContext(), "COME, WATCH YOUR SCORE, DRIFT INTO A VIVID REALITY, BRING YOUR WHOLE WILL, READY YOURSELF, FOR ANDROID STUPIDITY", Toast.LENGTH_SHORT).show();
            Log.d("pin-error", "pin at PinObjectFragment is null, whyyyyyyyyyyyyyyyyyyyyy?");
        } else {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.karyl)
                    .error(R.drawable.turtle_huh);

            if (pin.getType() == Pin.PinType.IMAGE) {
                Glide.with(binding.ivImage.getContext())
                        .load(pin.getMediaUrl())
                        .fitCenter()
                        .apply(options)
                        .into(binding.ivImage);
            }
            // GIF
            else if (pin.getType() == Pin.PinType.GIF) {
                Glide.with(binding.ivImage.getContext())
                        .asGif()
                        .load(pin.getMediaUrl())
                        .fitCenter()
                        .apply(options)
                        .into(binding.ivImage);
            }
            // VIDEO
            else {
                //TODO: load video
            }

            binding.btnLove.setImageResource(pin.getIsLiked() ? R.drawable.ic_favorite_heart_filled : R.drawable.ic_favorite_heart);
            binding.tvLikeCount.setText(String.valueOf(pin.getLikeCount()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializeRelevantPins() {
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

    public interface DownloadPinMediaCallback {
        void Download();
    }

    void downloadMediaAsync() {
        Thread thread = new Thread(() -> {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(pin.getMediaUrl());
            String mimeType = getMimeType(fileExtension);

            PinMediaDownloader downloader = new PinMediaDownloader(requireContext());
            downloader.DownloadFile(pin.getMediaUrl(), mimeType, String.valueOf(System.currentTimeMillis()));
            handler.post(() -> Toast.makeText(requireContext(), "Download finished", Toast.LENGTH_SHORT).show());
        });
        thread.start();
    }

    @NonNull
    private String getMimeType(String fileExtension) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(fileExtension);

        // fuck this
        if (mimeType == null) {
            if (pin.getType() == Pin.PinType.IMAGE) {
                mimeType = "image/jpg";
            } else if (pin.getType() == Pin.PinType.GIF) {
                mimeType = "image/gif";
            } else {
                mimeType = "video/mp4";
            }
        }
        return mimeType;
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    downloadMediaAsync();
                } else {
                    Toast.makeText(requireContext(), "Permission denied, download failed", Toast.LENGTH_SHORT).show();
                }
            });

    private final DownloadPinMediaCallback downloadPinMediaCallback = () -> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            // no need to ask for these permissions on android 13 and onwards
            else {
                downloadMediaAsync();
            }
        } else {
            downloadMediaAsync();
        }
    };
}
