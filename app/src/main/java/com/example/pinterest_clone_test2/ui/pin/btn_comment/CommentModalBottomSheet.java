package com.example.pinterest_clone_test2.ui.pin.btn_comment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.CommentListAdapter;
import com.example.pinterest_clone_test2.databinding.CommentModalBottomSheetBinding;
import com.example.pinterest_clone_test2.models.Comment;
import com.example.pinterest_clone_test2.services.firebase.FirebaseCommentService;
import com.example.pinterest_clone_test2.services.firebase.FirebaseUserService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommentModalBottomSheet extends BottomSheetDialogFragment {
    private final Context _context;
    public static String TAG = "CommentModalBottomSheet";
    String _pinId;
    UserCommentModel userCommentModel;
    Handler handler = new Handler();

    List<Comment> comments = new ArrayList<>();

    CommentModalBottomSheetBinding binding;
    CommentListAdapter commentListAdapter;

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public CommentModalBottomSheet(String pinId, Context context) {
        _pinId = pinId;
        userCommentModel = new UserCommentModel(context, _pinId);
        _context = context;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    // update the UI with the comments
    void initializeCommentRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(_context, LinearLayoutManager.VERTICAL, false);
        binding.rvComments.setLayoutManager(layoutManager);

        commentListAdapter = new CommentListAdapter(comments);
        binding.rvComments.setAdapter(commentListAdapter);

        commentListAdapter.setReactionClickListener(comment -> {
            FirebaseCommentService.UpdateLikeCallback updateLikeCallback = e -> {
                // revert the like/unlike action
                comment.setIsLiked(!comment.getIsLiked());
                comment.setLikeCount(comment.getLikeCount() + (comment.getIsLiked() ? 1 : -1));
                Toast.makeText(_context, getResources().getString(R.string.pin_comment_reaction_bug), Toast.LENGTH_SHORT).show();
            };

            comment.setIsLiked(!comment.getIsLiked());
            comment.setLikeCount(comment.getLikeCount() + (comment.getIsLiked() ? 1 : -1));
            FirebaseCommentService.updateLike(comment.getId(), comment.getIsLiked(), updateLikeCallback);
        });

        commentListAdapter.setReplyClickListener(comment -> {
            if (comment.getReplyCommentId() != null) {
                userCommentModel.setReplyToId(comment.getReplyCommentId());
            } else {
                userCommentModel.setReplyToId(comment.getId());
            }
            userCommentModel.setReplyToName(comment.getAuthorName());
        });

        commentListAdapter.setMoreClickListener(comment -> {
            CommentOptionsModalBottomSheet bottomSheet = new CommentOptionsModalBottomSheet(comment, _context, new BlockUserCallback() {
                @Override
                public void Block(@NonNull String userToBeBlockedId) {
                    FirebaseUserService.blockUser(userToBeBlockedId, new FirebaseUserService.HidePinCallback() {
                        @Override
                        public void OnSuccess() {
                            Toast.makeText(_context, _context.getResources().getString(R.string.user_blocked_success), Toast.LENGTH_SHORT).show();
                            handler.post(() -> removeBlockedComments(userToBeBlockedId));
                        }

                        @Override
                        public void OnFailure(Exception e) {
                            Toast.makeText(_context, _context.getResources().getString(R.string.user_blocked_failure), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            bottomSheet.show(requireActivity().getSupportFragmentManager(), CommentOptionsModalBottomSheet.TAG);
        });

        commentListAdapter.setAttachmentClickListener(comment -> {
            CommentAttachmentDetailModal bottomSheet = new CommentAttachmentDetailModal(comment);
            bottomSheet.show(requireActivity().getSupportFragmentManager(), CommentAttachmentDetailModal.TAG);
        });
    }

    public interface BlockUserCallback {
        void Block(@NonNull String userToBeBlockedId);
    }

    void removeBlockedComments(String userToBeBlockedId) {
        List<Comment> blockedComments = comments.stream().filter(comment -> Objects.equals(comment.getAuthorId(), userToBeBlockedId)).collect(Collectors.toList());
        // remove blocked comments
        comments.removeIf(blockedComments::contains);
        // remove comments replying to blocked comments
        comments.removeIf(comment -> blockedComments.stream().anyMatch(blockedComment -> Objects.equals(blockedComment.getId(), comment.getReplyCommentId())));
        commentListAdapter.notifyDataSetChanged();
        binding.tvCount.setText(getCommentCountString());
    }

    void fetchCommentsAsync() {
        Thread thread = new Thread(() -> {
            FirebaseCommentService.getPinComments(_pinId, null, getCommentServiceCallback, _context);
        });
        thread.start();
    }

    private final FirebaseCommentService.GetCommentServiceCallback getCommentServiceCallback = new FirebaseCommentService.GetCommentServiceCallback() {
        @Override
        public void OnSuccess(List<Comment> commentList) {
            handler.post(() -> {
                binding.progressBar.setVisibility(View.GONE);
                int startPos = comments.size();
                comments.addAll(commentList);
                commentListAdapter.notifyItemRangeInserted(startPos, commentList.size());
                binding.tvCount.setText(getCommentCountString());
            });
        }

        @Override
        public void OnFailure(Exception e) {
            e.printStackTrace();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("CommentPinPhotoPicker", "Selected URI: " + uri);
                        assert userCommentModel != null;
                        userCommentModel.setAttachmentUri(uri);
                    } else {
                        Log.d("CommentPinPhotoPicker", "No media selected");
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CommentModalBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        fetchCommentsAsync();
        initializeCommentRecyclerView();

        binding.setUserCommentModel(userCommentModel);
        binding.etCommentInput.setOnFocusChangeListener((v, hasFocus) -> userCommentModel.setIsFocused(hasFocus));
        binding.tvCancelReplying.setOnClickListener(v -> {
            userCommentModel.setReplyToName("");
            userCommentModel.setReplyToId(null);
        });
        binding.fabAddAttachment.setOnClickListener(v -> {
            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        binding.ibtnRemoveAttachment.setOnClickListener(v -> userCommentModel.setAttachmentUri(null));
        binding.btnPostComment.setOnClickListener(v -> {
            Log.d("listener", "ibtnPostComment clicked");
            binding.etCommentInput.clearFocus();
            createCommentAsync();
        });
    }

    // create new comment from user's input
    // behold, callback hell
    private void createCommentAsync() {
        Thread thread = new Thread(() -> {
            Comment comment = userCommentModel.createComment();
            FirebaseCommentService.UploadCommentServiceCallback uploadCommentServiceCallback = e -> {
                e.printStackTrace();
                // remove the added comment
                handler.post(() -> {
                    int index = comments.indexOf(comment);
                    if (index > -1) {
                        comments.remove(index);
                        commentListAdapter.notifyItemRemoved(index);
                        binding.tvCount.setText(getCommentCountString());
                    }
                    Toast.makeText(_context, getResources().getString(R.string.create_pin_comment_failed), Toast.LENGTH_SHORT).show();
                });
            };

            DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
            if (currentUserDocument != null) {
                comment.setAuthorId(currentUserDocument.getString("userId"));
                comment.setAuthorName(currentUserDocument.getString("name"));
                comment.setAuthorAvatarUrl(currentUserDocument.getString("avatarUrl"));
                // upload to database
                FirebaseCommentService.uploadPinComment(comment, uploadCommentServiceCallback);
                handler.post(() -> addNewlyPostedComment(comment));
            } else {
                Toast.makeText(_context, getResources().getString(R.string.create_pin_comment_failed), Toast.LENGTH_SHORT).show();
            }
        });
        thread.start();
    }

    // add newly created comment to the UI
    private void addNewlyPostedComment(Comment comment) {
        int newIndex = 0;

        String replyingId = comment.getReplyCommentId();
        if (replyingId == null) {
            comments.add(newIndex, comment);
        }
        // look for the final position of the replying comments and insert it there
        else {
            while (!Objects.equals(comments.get(newIndex).getId(), replyingId)) {
                newIndex++;
            }
            do {
                newIndex++;
                if (newIndex >= comments.size()) {
                    comments.add(comment);
                    commentListAdapter.notifyItemInserted(comments.size() - 1);

                    // clear comment input
                    userCommentModel.setContent("");
                    userCommentModel.setAttachmentUri(null);
                    userCommentModel.setReplyToId(null);
                    userCommentModel.setReplyToName(null);
                    binding.tvCount.setText(getCommentCountString());

                    return;
                }
            } while (comments.get(newIndex).getReplyCommentId() != null);
            comments.add(newIndex, comment);
        }
        commentListAdapter.notifyDataSetChanged();

        // clear comment input
        userCommentModel.setContent("");
        userCommentModel.setAttachmentUri(null);
        userCommentModel.setReplyToId(null);
        userCommentModel.setReplyToName(null);
        binding.tvCount.setText(getCommentCountString());
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        assert view != null;

        setupModalHeight(view);
    }

    private void setupModalHeight(View view) {
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        ViewGroup.LayoutParams params = binding.commentLayoutContainer.getLayoutParams();
        params.height = (int) (displayMetrics.heightPixels * 0.9);
        binding.commentLayoutContainer.setLayoutParams(params);
    }

    private String getCommentCountString() {
        int count = comments != null ? comments.size() : 0;
        return String.format(Locale.US, getResources().getString(R.string.comment_count_template), count);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
