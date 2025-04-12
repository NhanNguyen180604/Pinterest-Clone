package com.example.pinterest_clone_test2.ui.pin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pinterest_clone_test2.ChooseBoardActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentPinObjectBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.User;
import com.example.pinterest_clone_test2.services.download.PinMediaDownloader;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.ui.pin.btn_comment.CommentModalBottomSheet;
import com.example.pinterest_clone_test2.ui.pin.btn_more.PinMoreActionModalBottomSheet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PinObjectFragment extends Fragment {
    PinObjectViewModel viewModel;
    private Pin pin;
    boolean isBlocked = false;
    User author = new User();
    FragmentPinObjectBinding binding;
    String source;
    Handler handler = new Handler();
    ActivityResultLauncher<Intent> chooseBoardActivityLauncher;

    final int perPage = 20;
    boolean isOnLastPage = false;
    boolean isLoading = false;
    DocumentSnapshot lastVisible;  // for pagination
    List<Pin> relevantPins = new ArrayList<>();
    PinListAdapter relevantPinAdapter;
    ExoPlayer exoPlayer;

    // need this to prevent crash idk why
    public PinObjectFragment() {
    }

    public PinObjectFragment(Pin pin, String source) {
        this.pin = pin;
        this.source = source;
    }

    void fetchAuthorAsync() {
        Thread thread = new Thread(() -> {
            FirebaseUserService.getUserInfos(pin.getAuthorId(), getAuthorInfoCallback);
            Log.d("PinObjectFragment", "Fetching author info");
        });
        thread.start();
    }

    final FirebaseUserService.GetUserInfoCallback getAuthorInfoCallback = new FirebaseUserService.GetUserInfoCallback() {
        @Override
        public void OnSuccess(DocumentSnapshot documentSnapshot) {
            author.setFirstName(documentSnapshot.getString("name"));
            author.setAvatarUrl(documentSnapshot.getString("avatarUrl"));
            if (author.getAvatarUrl() != null) {
                Glide.with(binding.ivAuthorAvatar.getContext())
                        .load(author.getAvatarUrl())
                        .fitCenter()
                        .into(binding.ivAuthorAvatar);
            }
        }

        @Override
        public void OnFailure(Exception e) {
            e.printStackTrace();
        }
    };

    void fetchPinLikesAsync() {
        Thread thread = new Thread(() -> FirebasePinService.getPinLikeCount(pin.getId(), getPinLikeCountCallback));
        thread.start();
    }

    final FirebasePinService.GetPinLikeCountCallback getPinLikeCountCallback = new FirebasePinService.GetPinLikeCountCallback() {
        @Override
        public void OnSuccess(QuerySnapshot querySnapshot) {
            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
            pin.setLikeCount(documents.size());
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            assert currentUser != null;
            if (documents.stream().anyMatch(documentSnapshot -> Objects.equals(documentSnapshot.getString("userId"), currentUser.getUid()))) {
                pin.setIsLiked(true);
            }

            binding.btnLove.setImageResource(pin.getIsLiked() ? R.drawable.ic_favorite_heart_filled : R.drawable.ic_favorite_heart);
        }

        @Override
        public void OnFailure(Exception e) {
            e.printStackTrace();
        }
    };

    void fetchRelevantPinsAsync() {
        Thread thread = new Thread(() -> {
            if (isOnLastPage || isLoading)
                return;

            Log.d("PinObjectFragment", "Fetching relevant pins");
            isLoading = true;

            // pretend to have an algorithm that fetch pins based on this board's content
            // no way we can do this
            FirebasePinService.getPins(lastVisible, perPage, null, getRelevantPinsCallback);
        });
        thread.start();
    }

    final FirebasePinService.GetPinServiceCallback getRelevantPinsCallback = new FirebasePinService.GetPinServiceCallback() {
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
            Log.d("PinObjectFragmentLastVisible", lastVisible.getId());

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

            handler.post(() -> addRelevantPins(newPins, true));
        }

        @Override
        public void OnFailure(Exception e) {
            e.printStackTrace();
            isLoading = false;
        }
    };

    void addRelevantPins(List<Pin> newPins, boolean append) {
        if (!append) {
            relevantPins.clear();
        }
        int startPos = relevantPins.size();
        relevantPins.addAll(newPins);
        relevantPinAdapter.notifyItemRangeInserted(startPos, newPins.size());

        isLoading = false;
    }

    // use this to check if this pin is saved inside a board
    void fetchBoardsAsync() {
        Thread thread = new Thread(() -> {
            QuerySnapshot currentUserBoardSnapshot = FirebaseBoardService.getCurrentUserBoardSnapshot();
            if (currentUserBoardSnapshot == null) {
                FirebaseBoardService.getUserBoards(getBoardServiceCallback);
            } else {
                getBoardServiceCallback.OnSuccess(currentUserBoardSnapshot);
            }
        });
        thread.start();
    }

    final FirebaseBoardService.GetBoardServiceCallback getBoardServiceCallback = new FirebaseBoardService.GetBoardServiceCallback() {
        @Override
        public void OnSuccess(QuerySnapshot querySnapshot) {
            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
            for (DocumentSnapshot document :
                    documents) {
                Board board = document.toObject(Board.class);
                if (board == null) {
                    continue;
                }
                board.setId(document.getId());
                if (pin != null && board.getPins() != null && board.getPins().contains(pin.getId())) {
                    handler.post(() -> {
                        binding.btnSave.setText(getString(R.string.saved));
                        binding.btnSave.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.gray_button_pinterest));
                        binding.btnSave.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                    });
                    break;
                } else if (pin == null) {
                    Log.d("PinObjectFragment", "Pin is null again bitch");
                }
            }
        }

        @Override
        public void OnFailure(Exception e) {
            Log.e("PinObjectFragment", "Failed to fetch boards");
            e.printStackTrace();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chooseBoardActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }

                        if (data.getBooleanExtra("profile", false)) {
                            FirebaseUserService.savePinToProfile(pin.getId(), new FirebaseUserService.SavePinToProfileCallback() {
                                @Override
                                public void OnSuccess() {
                                    Toast.makeText(requireContext(), getResources().getString(R.string.save_pin_to_profile_sucess), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void OnFailure(Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(requireContext(), getResources().getString(R.string.save_pin_to_profile_failure), Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }

                        // idk if this gonna happen or not, just to make sure
                        if (pin == null) {
                            Toast.makeText(requireContext(), "Give praise, for android has no equal", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String boardName = data.getStringExtra("boardName");
                        String boardId = data.getStringExtra("boardId");
                        if (boardId != null && boardName != null) {
                            FirebaseBoardService.savePinToBoard(pin.getId(), boardId, new FirebaseBoardService.SavePinToBoardServiceCallback() {
                                @Override
                                public void OnSuccess() {
                                    Toast.makeText(
                                            requireContext(),
                                            String.format(getResources().getString(R.string.pin_save_to_board_template), boardName),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }

                                @Override
                                public void OnFailure(Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(
                                            requireContext(),
                                            String.format(getResources().getString(R.string.pin_save_to_board_failure_template), boardName),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPinObjectBinding.inflate(inflater, container, false);
        binding.setPinViewModel(pin);
        binding.setAuthorViewModel(author);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity().getApplication(), this)).get(PinObjectViewModel.class);
        initRecyclerViewRelevantPins();

        binding.btnLove.setOnClickListener(v -> {
            if (pin != null) {
                pin.setIsLiked(!pin.getIsLiked());
                pin.setLikeCount(pin.getLikeCount() + (pin.getIsLiked() ? 1 : -1));
                binding.btnLove.setImageResource(pin.getIsLiked() ? R.drawable.ic_favorite_heart_filled : R.drawable.ic_favorite_heart);
                // update like on database
                FirebasePinService.updateLike(pin.getId(), pin.getIsLiked(), updateLikeCallback);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnComment.setOnClickListener(v -> {
            if (pin != null) {
                CommentModalBottomSheet modalBottomSheet = new CommentModalBottomSheet(pin.getId(), requireContext());
                modalBottomSheet.show(requireActivity().getSupportFragmentManager(), CommentModalBottomSheet.TAG);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnShare.setOnClickListener(v -> {
            if (pin != null) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String sharedLink = String.format(Locale.US, getResources().getString(R.string.pin_deep_link_string_template), pin.getId());
                sendIntent.putExtra(Intent.EXTRA_TEXT, sharedLink);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMore.setOnClickListener(v -> {
            if (pin != null) {
                PinMoreActionModalBottomSheet sheet = new PinMoreActionModalBottomSheet(pin, requireContext(), downloadPinMediaCallback, hidePinCallback);
                sheet.show(requireActivity().getSupportFragmentManager(), PinMoreActionModalBottomSheet.TAG);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });

        binding.fabBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });

        binding.btnSave.setOnClickListener(v -> {
            if (pin != null) {
                Intent intent = new Intent(requireActivity(), ChooseBoardActivity.class);
                intent.putExtra("pin", pin);
                intent.putExtra("suggestNewBoard", true);
                chooseBoardActivityLauncher.launch(intent);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    final FirebasePinService.UpdateLikeCallback updateLikeCallback = new FirebasePinService.UpdateLikeCallback() {
        @Override
        public void OnFailure(Exception e) {
            // revert the like/unlike action
            pin.setIsLiked(!pin.getIsLiked());
            pin.setLikeCount(pin.getLikeCount() + (pin.getIsLiked() ? 1 : -1));
            binding.btnLove.setImageResource(pin.getIsLiked() ? R.drawable.ic_favorite_heart_filled : R.drawable.ic_favorite_heart);
            Toast.makeText(requireContext(), getResources().getString(R.string.pin_reaction_bug), Toast.LENGTH_SHORT).show();
        }
    };

    private void restoreStates() {
        Pin pin_state = viewModel.getPinState();
        if (pin_state != null) {
            pin = pin_state;
        }

        String source_state = viewModel.getSource();
        if (source_state != null) {
            source = source_state;
        }

        User authorState = viewModel.getAuthorState();
        if (authorState != null) {
            author = authorState;
        }

        List<Pin> relevantPinState = viewModel.getRelevantPinState();
        if (relevantPinState == null || relevantPinState.isEmpty()) {
            fetchRelevantPinsAsync();
        } else if (relevantPins.isEmpty()) {
            addRelevantPins(relevantPinState, false);
        }

        Parcelable scroll_state = viewModel.getScrollState();
        if (scroll_state != null && binding.rvRelevant.getLayoutManager() != null) {
            binding.rvRelevant.getLayoutManager().onRestoreInstanceState(scroll_state);
        }

        fetchBoardsAsync();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding.rvRelevant.getLayoutManager() != null) {
            viewModel.setScrollState(binding.rvRelevant.getLayoutManager().onSaveInstanceState());
        }
        viewModel.setPinState(pin);
        viewModel.setSourceState(source);
        viewModel.setAuthorState(author);
        viewModel.setRelevantPinState(relevantPins);

        stopAndStoreVideoState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        // only release when view is destroyed, if put this in onPause, it will look like shit
        releaseExoPlayer();
    }

    void stopAndStoreVideoState() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            viewModel.setVideoPositionState(exoPlayer.getCurrentPosition());
        }
    }

    void releaseExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreStates();

        if ((author.getFirstName() == null || author.getAvatarUrl() == null) && pin != null) {
            fetchAuthorAsync();
        } else {
            binding.setAuthorViewModel(author);
            Glide.with(binding.ivAuthorAvatar.getContext())
                    .load(author.getAvatarUrl())
                    .fitCenter()
                    .into(binding.ivAuthorAvatar);
        }

        if (pin == null) {
            Log.d("PinObjectFragment", "pin is null, why?");
        } else {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.turtle_huh);

            if (pin.getType() == Pin.PinType.VIDEO) {
                binding.ivImage.setVisibility(View.GONE);
                binding.videoView.setVisibility(View.VISIBLE);
                binding.fabBgRemoval.setVisibility(View.GONE);
            } else {
                binding.ivImage.setVisibility(View.VISIBLE);
                binding.videoView.setVisibility(View.GONE);
            }

            // handle blocked pin case when user navigate up
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

                if (blockedPins != null && blockedPins.contains(pin.getId()) || blockedUsers != null && blockedUsers.contains(pin.getAuthorId())) {
                    hidePinContentAndDisableInteractions(options);
                    return;
                }
            }

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

            fetchPinLikesAsync();
            binding.setPinViewModel(pin);
        }
    }

    private void hidePinContentAndDisableInteractions(RequestOptions options) {
        binding.ivImage.setVisibility(View.VISIBLE);
        Glide.with(binding.ivImage.getContext())
                .load(R.drawable.hidden_image)
                .fitCenter()
                .apply(options)
                .into(binding.ivImage);

        // pin title is still visible, fuck me, idk why
        binding.setPinViewModel(null);

        binding.btnSave.setVisibility(View.GONE);
        binding.btnLove.setVisibility(View.GONE);
        binding.btnComment.setVisibility(View.GONE);
        binding.btnShare.setVisibility(View.GONE);
        binding.btnMore.setVisibility(View.GONE);
        binding.fabBgRemoval.setVisibility(View.GONE);
        binding.fabBgRemoval.setVisibility(View.GONE);
        binding.tvLikeCount.setVisibility(View.GONE);
        binding.tvPinDescription.setVisibility(View.GONE);
        binding.tvPinTitle.setVisibility(View.GONE);
        binding.tvAuthor.setVisibility(View.GONE);
        binding.ivAuthorAvatar.setVisibility(View.GONE);

        isBlocked = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // load video here because onViewStateRestored won't be called every single time
        if (!isBlocked && pin != null && pin.getType() == Pin.PinType.VIDEO) {
            if (exoPlayer == null) {
                exoPlayer = new ExoPlayer.Builder(requireContext()).build();
                binding.videoView.setPlayer(exoPlayer);
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(pin.getMediaUrl()));
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
            }

            exoPlayer.setPlayWhenReady(true);
            long oldPositionState = viewModel.getVideoPositionState();
            if (oldPositionState > 0) {
                exoPlayer.seekTo(oldPositionState);
            }
        }
    }

    private void initRecyclerViewRelevantPins() {
        relevantPinAdapter = new PinListAdapter(requireContext(), relevantPins, relevantPinClickListener);
        relevantPinAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT);
        binding.rvRelevant.setAdapter(relevantPinAdapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        binding.rvRelevant.setHasFixedSize(true);
        binding.rvRelevant.setLayoutManager(layoutManager);

        binding.rvRelevant.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    Log.d("PinObjectFragment", "end of recyclerview reached, fetching more relevant pins");
                    fetchRelevantPinsAsync();
                }
            }
        });
    }

    private final PinClickListener relevantPinClickListener = new PinClickListener() {
        @Override
        public void OnClick(int position, View v) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_pin_deep_link);
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            bundle.putString("source", source);
            bundle.putParcelableArrayList("pins", new ArrayList<>(relevantPins));

            int action = 0;
            if (Objects.equals(source, "home")) {
                action = R.id.action_pinFragment_self;
            } else if (Objects.equals(source, "search")) {
                action = R.id.action_pinFragment2_self;
            } else if (Objects.equals(source, "account")) {
                action = R.id.action_pinFragment3_self;
            } else if (Objects.equals(source, "pinDeepLink")) {
                action = R.id.action_pinFragmentDeepLink_self;
            }

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

    public interface HidePinCallback {
        void Hide();
    }

    private final HidePinCallback hidePinCallback = () -> {
        if (pin == null) {
            Toast.makeText(requireContext(), getResources().getString(R.string.pin_hide_failure), Toast.LENGTH_SHORT).show();
            return;
        }

        // send hide request to the database

        FirebaseUserService.hidePin(pin.getId(), new FirebaseUserService.HidePinCallback() {
            @Override
            public void OnSuccess() {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigateUp();
            }

            @Override
            public void OnFailure(Exception e) {
                Toast.makeText(requireContext(), getResources().getString(R.string.pin_hide_failure), Toast.LENGTH_SHORT).show();
                Log.e("PinObjectFragment", "hide pin failed:\n" + e.getMessage());
            }
        });
    };
}
