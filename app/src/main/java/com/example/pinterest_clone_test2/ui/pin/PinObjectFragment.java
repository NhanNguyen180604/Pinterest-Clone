package com.example.pinterest_clone_test2.ui.pin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Base64;
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
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.pinterest_clone_test2.ChooseBoardActivity;
import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.RemoveBgActivity;
import com.example.pinterest_clone_test2.adapters.PinListAdapter;
import com.example.pinterest_clone_test2.databinding.FragmentPinObjectBinding;
import com.example.pinterest_clone_test2.interfaces.PinClickListener;
import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.User;
import com.example.pinterest_clone_test2.services.cloudinary.CloudinaryManager;
import com.example.pinterest_clone_test2.services.download.PinMediaDownloader;
import com.example.pinterest_clone_test2.services.firebase.FirebaseBoardService;
import com.example.pinterest_clone_test2.services.firebase.FirebasePinService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseTagService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.example.pinterest_clone_test2.services.remove_image_bg.RemoveBgService;
import com.example.pinterest_clone_test2.ui.pin.btn_comment.CommentModalBottomSheet;
import com.example.pinterest_clone_test2.ui.pin.btn_more.PinAuthorMoreActionModal;
import com.example.pinterest_clone_test2.ui.pin.btn_more.PinNormalMoreActionModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PinObjectFragment extends Fragment {
    // need this to prevent crash idk why
    public PinObjectFragment() {
    }

    PinObjectViewModel viewModel;
    private Pin pin;
    boolean isBlocked = false;
    boolean savedToBoard = false;
    User author = new User();
    FragmentPinObjectBinding binding;
    String source;
    Handler handler = new Handler();
    ActivityResultLauncher<Intent> chooseBoardActivityLauncher;
    ActivityResultLauncher<Intent> editPinActivityLauncher;
    ActivityResultLauncher<Intent> removeBgActivityLauncher;

    int page = 1;
    int totalPage = 0;
    final int perPage = 20;
    boolean isFetchingFirstTime = false;
    boolean isFetchingRelevantPins = false;
    boolean isFetchingRelevantPinIds = false;
    List<String> relevantPinIds = new ArrayList<>();
    List<Pin> relevantPins = new ArrayList<>();
    PinListAdapter relevantPinAdapter;
    ExoPlayer exoPlayer;

    boolean isCheckingPinDeleted = false;
    final int checkPinDeletedDelay = 10000;  // check every 10 seconds

    private void checkPinExistsAndExitIfNeededAlsoRemovePinFromProfile() {
        FirebasePinService.checkPinExists(pin.getId(), exist -> {
            Log.d("PinObjectFragment", String.format(Locale.US, "Pin %s exists: %b", pin.getId(), exist));
            if (!exist) {
                handler.post(() -> {
                    stopCheckingPinDeleted();
                    createDeletedDialog();
                    hidePinContentAndDisableInteractions();
                });
                // this pin might be saved to the user's profile, just delete it to make sure
                FirebaseUserService.removePinFromProfile(pin.getId());
            }
        });
    }

    private final Runnable checkPinDeletedRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCheckingPinDeleted) {
                checkPinExistsAndExitIfNeededAlsoRemovePinFromProfile();
                handler.postDelayed(this, checkPinDeletedDelay);
            }
        }
    };

    void startCheckingPinDeleted() {
        isCheckingPinDeleted = true;
        checkPinDeletedRunnable.run();
    }

    void stopCheckingPinDeleted() {
        isCheckingPinDeleted = false;
        handler.removeCallbacks(checkPinDeletedRunnable);
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
            author.setName(documentSnapshot.getString("name"));
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
            printExceptionMessage("Couldn't get author info", e);
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
            printExceptionMessage("Couldn't fetch pin's like count", e);
        }
    };

    void fetchPinsFirstTime() {
        isFetchingRelevantPinIds = true;
        isFetchingFirstTime = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        Thread thread = new Thread(() -> FirebasePinService.getRelevantPinIdsByTags(pin, pinIds -> {
            relevantPinIds = pinIds;
            page = 1;
            setTotalPageCount();
            isFetchingRelevantPinIds = false;
            if (pinIds.isEmpty()) {
                binding.progressBar.setVisibility(View.GONE);
                return;
            }
            fetchRelevantPinsAsync();
        }));
        thread.start();
    }

    void setTotalPageCount() {
        totalPage = (int) Math.ceil((double) relevantPinIds.size() / perPage);
    }

    void fetchRelevantPinsAsync() {
        Thread thread = new Thread(() -> {
            if (outOfPins() || isFetchingRelevantPins)
                return;

            Log.d("PinObjectFragment", "Fetching relevant pins");
            isFetchingRelevantPins = true;

            FirebasePinService.fetchPinsFromIds(relevantPinIds.subList((page - 1) * perPage, Math.min(page * perPage, relevantPinIds.size())), getRelevantPinsCallback);
        });
        thread.start();
    }

    final FirebasePinService.OnPinsFetchedFromIdsCallback getRelevantPinsCallback = new FirebasePinService.OnPinsFetchedFromIdsCallback() {
        @Override
        public void onSuccess(List<Pin> newPins) {
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
                    newPins.removeIf(newPin -> finalBlockedPins.contains(newPin.getId()));
                }
                if (blockedUsers != null) {
                    List<String> finalBlockedUsers = blockedUsers;
                    newPins.removeIf(newPin -> finalBlockedUsers.contains(newPin.getAuthorId()));
                }
            } else {
                showToastMessage(getResources().getString(R.string.pin_filter_failure));
            }

            handler.post(() -> addRelevantPins(newPins, !isFetchingFirstTime));
            page++;
        }

        @Override
        public void onFailure(Exception e) {
            printExceptionMessage("Couldn't fetch relevant pins", e);
            isFetchingRelevantPins = false;
        }
    };

    boolean outOfPins() {
        return page > totalPage;
    }

    void addRelevantPins(List<Pin> newPins, boolean append) {
        if (binding == null) {
            return;
        }

        if (!append) {
            int oldSize = relevantPins.size();
            relevantPins.clear();
            relevantPinAdapter.notifyItemRangeRemoved(0, oldSize);
        }
        int startPos = relevantPins.size();
        relevantPins.addAll(newPins);
        relevantPinAdapter.notifyItemRangeInserted(startPos, newPins.size());

        isFetchingRelevantPins = false;
        isFetchingFirstTime = false;
        binding.progressBar.setVisibility(View.GONE);
    }

    // use this to check if this pin is saved inside a board
    void checkSavedPinAndSetButtonText() {
        Thread thread = new Thread(() -> {
            DocumentSnapshot currentUserSnapshot = FirebaseUserService.getCurrentUserDocument();
            assert currentUserSnapshot != null;

            List<String> pinIds = null;
            try {
                pinIds = (List<String>) currentUserSnapshot.get("pins");
            } catch (Exception e) {
                //eat exception
            }
            if (pinIds != null && !pinIds.isEmpty() && pinIds.contains(pin.getId())) {
                savedToBoard = true;
                handler.post(() -> {
                    binding.btnSave.setText(getString(R.string.saved));
                    binding.btnSave.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.gray_button_pinterest));
                    binding.btnSave.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                });
                return;
            }

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
                        savedToBoard = true;
                        binding.btnSave.setText(getString(R.string.saved));
                        binding.btnSave.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.gray_button_pinterest));
                        binding.btnSave.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                    });
                    break;
                } else if (pin == null) {
                    Log.e("PinObjectFragment", "Pin is null again bitch");
                }
            }
        }

        @Override
        public void OnFailure(Exception e) {
            Log.e("PinObjectFragment", "Failed to fetch boards");
            printExceptionMessage("Couldn't fetch user's boards to check if this pin belongs to any of them", e);
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

                        savedToBoard = true;

                        // idk if this gonna happen or not, just to make sure
                        if (pin == null) {
                            Log.e("PinObjectFragment", "pin is fucking null, at on create");
                            return;
                        }

                        // seems like pinterest always saves to profile
                        FirebaseUserService.savePinToProfile(pin.getId(), new FirebaseUserService.SavePinToProfileCallback() {
                            @Override
                            public void OnSuccess() {
                                if (data.getBooleanExtra("profile", false)) {
                                    showToastMessage(getResources().getString(R.string.save_pin_to_profile_sucess));
                                }
                            }

                            @Override
                            public void OnFailure(Exception e) {
                                printExceptionMessage("Couldn't save pin to profile", e);
                                showToastMessage(getResources().getString(R.string.save_pin_to_profile_failure));
                            }
                        });

                        if (data.getBooleanExtra("profile", false)) {
                            return;
                        }

                        String boardName = data.getStringExtra("boardName");
                        String boardId = data.getStringExtra("boardId");
                        if (boardId != null && boardName != null) {
                            FirebaseBoardService.savePinToBoard(pin.getId(), boardId, new FirebaseBoardService.SavePinToBoardServiceCallback() {
                                @Override
                                public void OnSuccess() {
                                    showToastMessage(String.format(getResources().getString(R.string.pin_save_to_board_template), boardName));
                                }

                                @Override
                                public void OnFailure(Exception e) {
                                    printExceptionMessage("Couldn't save pin to chosen board", e);
                                    showToastMessage(String.format(getResources().getString(R.string.pin_save_to_board_failure_template), boardName));
                                }
                            });
                        }
                    }
                }
        );

        editPinActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }

                        if (data.getBooleanExtra("delete", false)) {
                            getNavController().navigateUp();
                            return;
                        }

                        if (data.getBooleanExtra("nonAuthorDelete", false)) {
                            binding.btnSave.setText(getString(R.string.btn_save));
                            binding.btnSave.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.red_button_pinterest));
                            binding.btnSave.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                            return;
                        }

                        Pin updatedPin = data.getParcelableExtra("pin");
                        if (updatedPin == null) {
                            return;
                        }

                        pin.setName(updatedPin.getName());
                        pin.setDescription(updatedPin.getDescription());
                        pin.setAllowComment(updatedPin.getAllowComment());
                    }
                }
        );

        // domain expansion: LIMITLESS CALLBACKS
        removeBgActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }

                        // idk if this gonna happen or not, just to make sure
                        if (pin == null) {
                            Log.e("PinObjectFragment", "pin is fucking null, at on create");
                            return;
                        }

                        String boardId = data.getStringExtra("boardId");
                        String boardName = data.getStringExtra("boardName");

                        String processedImageB64 = RemoveBgService.getProcessedImageB64();
                        RemoveBgService.clearProcessedImageB64();

                        handler.post(() -> Toast.makeText(requireContext(), getResources().getString(R.string.pin_uploading), Toast.LENGTH_SHORT).show());

                        CloudinaryManager.uploadMedia(Base64.decode(processedImageB64, Base64.DEFAULT), new UploadCallback() {
                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                List<String> rawTags = extractTagsFromResult(resultData);
                                List<String> processedTags = FirebaseTagService.processTags(rawTags);
                                Log.d("Cloudinary", "Raw detected tags: " + rawTags);
                                Log.d("Cloudinary", "Processed tags: " + processedTags);

                                String url = (String) resultData.get("secure_url");
                                if (url == null) {
                                    showToastMessage(getResources().getString(R.string.upload_image_failure));
                                } else {
                                    String thumbnailUrl = url.replace("/upload/", "/upload/w_200/");
                                    Pin newPin = new Pin()
                                            .setName(pin.getName())
                                            .setDescription(pin.getDescription())
                                            .setMediaUrl(url)
                                            .setThumbnailUrl(thumbnailUrl)
                                            .setType(Pin.PinType.IMAGE)
                                            .setAllowComment(pin.getAllowComment())
                                            .setCreatedAt(System.currentTimeMillis());

                                    if (pin.getTags() != null && !pin.getTags().isEmpty()) {
                                        newPin.setTags(new ArrayList<>(pin.getTags()));
                                    } else {
                                        newPin.setTags(processedTags);
                                    }

                                    FirebasePinService.uploadPin(newPin, new FirebasePinService.UploadPinServiceCallback() {
                                        @Override
                                        public void OnSuccess(DocumentReference documentReference) {
                                            String newPinId = documentReference.getId();
                                            if (boardId != null && boardName != null) {
                                                FirebaseBoardService.savePinToBoard(newPinId, boardId, new FirebaseBoardService.SavePinToBoardServiceCallback() {
                                                    @Override
                                                    public void OnSuccess() {
                                                        showToastMessage(String.format(getResources().getString(R.string.pin_save_to_board_template), boardName));
                                                    }

                                                    @Override
                                                    public void OnFailure(Exception e) {
                                                        printExceptionMessage("Couldn't save pin to chosen board", e);
                                                        showToastMessage(String.format(getResources().getString(R.string.pin_save_to_board_failure_template), boardName));
                                                    }
                                                });
                                            }
                                            FirebaseTagService.saveTagsToFirestore(processedTags, newPinId);
                                        }

                                        @Override
                                        public void OnFailure(Exception e) {
                                            printExceptionMessage("Couldn't upload pin to firebase", e);
                                            showToastMessage(getResources().getString(R.string.upload_pin_failure));
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onStart(String requestId) {
                                Log.d("Cloudinary", "Upload start");
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                                Log.d("Cloudinary", "Upload progress: " + bytes + "/" + totalBytes);
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                showToastMessage(getResources().getString(R.string.media_upload_failure));
                                Log.e("Cloudinary", "Error: " + error.getDescription());
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                                Log.d("Cloudinary", "Rescheduled");
                            }
                        });
                    } else {
                        RemoveBgService.clearProcessedImageB64();
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

        restoreStates();

        if ((author.getName() == null || author.getAvatarUrl() == null) && pin != null) {
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
            return;
        }

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.turtle_huh);

        if (pin.getType() == Pin.PinType.VIDEO) {
            binding.ivImage.setVisibility(View.GONE);
            binding.videoView.setVisibility(View.VISIBLE);
            binding.fabBgRemoval.setVisibility(View.GONE);
        } else {
            if (pin.getType() == Pin.PinType.GIF) {
                binding.fabBgRemoval.setVisibility(View.GONE);
            }
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
                hidePinContentAndDisableInteractions();
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

        initButtonInteractions();
    }

    private void initButtonInteractions() {
        binding.btnLove.setOnClickListener(v -> {
            if (pin != null) {
                pin.setIsLiked(!pin.getIsLiked());
                pin.setLikeCount(pin.getLikeCount() + (pin.getIsLiked() ? 1 : -1));
                binding.btnLove.setImageResource(pin.getIsLiked() ? R.drawable.ic_favorite_heart_filled : R.drawable.ic_favorite_heart);
                // update like on database
                FirebasePinService.updateLike(pin.getId(), pin.getIsLiked(), updateLikeCallback);
            } else {
                showToastMessage(getResources().getString(R.string.unknown_error));
            }
        });

        binding.btnComment.setOnClickListener(v -> {
            if (pin != null) {
                CommentModalBottomSheet modalBottomSheet = new CommentModalBottomSheet(pin.getId(), requireContext());
                modalBottomSheet.show(requireActivity().getSupportFragmentManager(), CommentModalBottomSheet.TAG);
            } else {
                showToastMessage(getResources().getString(R.string.unknown_error));
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
                showToastMessage(getResources().getString(R.string.unknown_error));
            }
        });

        if (source.startsWith("admin")) {
            binding.btnMore.setOnClickListener(v -> {
                if (pin != null) {
                    DocumentSnapshot currentUserDoc = FirebaseUserService.getCurrentUserDocument();
                    assert currentUserDoc != null;

                    // display author's exclusive dialog to manage pin
                    if (currentUserDoc.getId().equals(pin.getAuthorId())) {
                        PinAuthorMoreActionModal sheet = new PinAuthorMoreActionModal(pin, requireContext(), downloadPinMediaCallback, editPinActivityLauncher);
                        sheet.show(requireActivity().getSupportFragmentManager(), PinAuthorMoreActionModal.TAG);
                    }
                    // display normal dialog for viewers
                    else {
                        PinNormalMoreActionModal sheet = new PinNormalMoreActionModal(pin, requireContext(), savedToBoard, editPinActivityLauncher, downloadPinMediaCallback, hidePinCallback);
                        sheet.show(requireActivity().getSupportFragmentManager(), PinNormalMoreActionModal.TAG);
                    }
                } else {
                    showToastMessage(getResources().getString(R.string.unknown_error));
                }
            });

            binding.fabBgRemoval.setOnClickListener(v -> {
                if (pin != null) {
                    Intent intent = new Intent(requireActivity(), RemoveBgActivity.class);
                    intent.putExtra("imageUrl", pin.getMediaUrl());
                    removeBgActivityLauncher.launch(intent);
                } else {
                    showToastMessage(getResources().getString(R.string.unknown_error));
                }
            });
        } else {
            binding.btnMore.setVisibility(View.GONE);
            binding.fabBgRemoval.setVisibility(View.GONE);
        }

        binding.fabBack.setOnClickListener(v -> {
            NavController navController = getNavController();
            navController.navigateUp();
        });

        binding.btnSave.setOnClickListener(v -> {
            if (pin != null) {
                Intent intent = new Intent(requireActivity(), ChooseBoardActivity.class);
                intent.putExtra("pin", pin);
                intent.putExtra("suggestNewBoard", true);
                chooseBoardActivityLauncher.launch(intent);
            } else {
                showToastMessage(getResources().getString(R.string.unknown_error));
            }
        });
        binding.ivAuthorAvatar.setOnClickListener(v -> {
            if (pin != null && pin.getAuthorId() != null) {
                navigateToUserProfile(pin.getAuthorId());
            }
        });
        binding.tvAuthor.setOnClickListener(v -> {
            if (pin != null && pin.getAuthorId() != null) {
                navigateToUserProfile(pin.getAuthorId());
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
            showToastMessage(getResources().getString(R.string.pin_reaction_bug));
        }
    };

    private void restoreStates() {
        Pin pin_state = viewModel.getPinState();
        if (pin_state != null) {
            pin = pin_state;
            binding.setPinViewModel(pin);
        }

        String source_state = viewModel.getSource();
        if (source_state != null) {
            source = source_state;
        }

        User authorState = viewModel.getAuthorState();
        if (authorState != null) {
            author = authorState;
            binding.setAuthorViewModel(author);
        }

        boolean oldBoardSaved = viewModel.getBoardSavedState();
        if (!savedToBoard) {
            savedToBoard = oldBoardSaved;
        }

        int oldPageState = viewModel.getPageState();
        if (oldPageState > 0) {
            page = oldPageState;
        }

        if (pin != null) {
            // idk, this looks really buggy
            List<String> relevantPinIdsState = viewModel.getRelevantPinIdsState();
            if (relevantPinIdsState == null || relevantPinIdsState.isEmpty()) {
                fetchPinsFirstTime();
            } else {
                relevantPinIds = relevantPinIdsState;
                setTotalPageCount();
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
            }

            checkSavedPinAndSetButtonText();
        }
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
        viewModel.setPageState(page);
        viewModel.setRelevantPinIdsState(relevantPinIds);
        viewModel.setBoardSavedState(savedToBoard);

        stopAndStoreVideoState();
        stopCheckingPinDeleted();
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

    private void hidePinContentAndDisableInteractions() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.turtle_huh);

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

        if (pin == null) {
            return;
        }

        startCheckingPinDeleted();

        // load video here because onViewStateRestored won't be called every single time
        if (!isBlocked && pin.getType() == Pin.PinType.VIDEO) {
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

    void createDeletedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("This Pin is no longer available")
                .setTitle("Pin was deleted")
                .setPositiveButton("Confirm", (dialogInterface, i) -> getNavController().navigateUp())
                .setCancelable(false);
        builder.create().show();
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
                if (dy <= 0 || outOfPins() || isFetchingRelevantPins)
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
            NavController navController = getNavController();

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
                    showToastMessage(getResources().getString(R.string.download_permission_denied));
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
            showToastMessage(getResources().getString(R.string.pin_hide_failure));
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
                showToastMessage(getResources().getString(R.string.pin_hide_failure));
                Log.e("PinObjectFragment", "hide pin failed:\n" + e.getMessage());
            }
        });
    };

    private void navigateToUserProfile(String userId) {
        NavController navController = getNavController();

        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("source", source);

        int action = R.id.action_pinFragment_to_userProfileFragment;
        if (Objects.equals(source, "search")) {
            action = R.id.action_pinFragment2_to_userProfileFragment;
        } else if (Objects.equals(source, "account")) {
            action = R.id.action_pinFragment3_to_userProfileFragment;
        } else if (Objects.equals(source, "pinDeepLink")) {
            action = R.id.action_pinFragmentDeepLink_to_userProfileFragmentDeepLink;
        } else if (source.startsWith("admin")) {
            if (source.equals("admin_pin")){
                action = R.id.action_pinFragment_to_userProfileFragment2;
            }
        }

        navController.navigate(action, args, null, null);
    }

    @NonNull
    private NavController getNavController() {
        NavController navController;
        if (Objects.equals(source, "pinDeepLink")) {
            navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_pin_deep_link);
        } else if (source.startsWith("admin")) {
            navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_admin);
        } else {
            navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        }
        return navController;
    }

    private void printExceptionMessage(String message, Exception e) {
        Log.e("PinObjectFragment", message);
        if (e.getMessage() != null) {
            Log.e("PinObjectFragment", e.getMessage());
        }
    }

    void showToastMessage(String message) {
        handler.post(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
    }

    private List<String> extractTagsFromResult(Map<String, Object> resultData) {
        List<String> tags = new ArrayList<>();

        try {
            if (resultData.containsKey("tags")) {
                Object tagsObj = resultData.get("tags");
                if (tagsObj instanceof List) {
                    tags.addAll((List<String>) tagsObj);
                    Log.d("Cloudinary", "Tags extracted: " + tags);
                }
            }
        } catch (Exception e) {
            Log.e("Cloudinary", "Error extracting tags: " + e.getMessage());
        }

        // Xử lý tags (ưu tiên fixed tags và giới hạn số lượng)
        return FirebaseTagService.processTags(tags);
    }
}
