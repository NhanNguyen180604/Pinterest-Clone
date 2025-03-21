package com.example.pinterest_clone_test2.ui.pin_comment;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.adapters.CommentListAdapter;
import com.example.pinterest_clone_test2.daos.ICommentDao;
import com.example.pinterest_clone_test2.daos.MockCommentDao;
import com.example.pinterest_clone_test2.databinding.CommentModalBottomSheetBinding;
import com.example.pinterest_clone_test2.models.Comment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CommentModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "CommentModalBottomSheet";
    String _pinId;
    ICommentDao _commentDao;
    UserCommentModel userCommentModel;
    Handler myHandler = new Handler();

    List<Comment> _comments;

    CommentModalBottomSheetBinding binding;
    CommentListAdapter commentListAdapter;

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public CommentModalBottomSheet(String pinId) {
        _pinId = pinId;
        _commentDao = new MockCommentDao();
        _comments = new ArrayList<>();
        userCommentModel = new UserCommentModel();
    }

    // update the UI with the comments
    void populateComments(List<Comment> newComments) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvComments.setLayoutManager(layoutManager);

//        viewModel.setComments(newComments);
        _comments = newComments;
        commentListAdapter = new CommentListAdapter(newComments);
        binding.rvComments.setAdapter(commentListAdapter);
        binding.tvCount.setText(getCommentCountString());

        commentListAdapter.setReactionClickListener(comment -> {
            comment.setIsLiked(!comment.getIsLiked());
            comment.setLikeCount(comment.getLikeCount() + (comment.getIsLiked() ? 1 : -1));
            // TODO: update database here

            // TODO: update database here
        });

        commentListAdapter.setReplyClickListener(comment -> {
            userCommentModel.setReplyToId(comment.getId());
            userCommentModel.setReplyToName(comment.getAuthorName());
        });

        commentListAdapter.setMoreClickListener(comment -> {
            CommentOptionsModalBottomSheet bottomSheet = new CommentOptionsModalBottomSheet(comment);
            bottomSheet.show(requireActivity().getSupportFragmentManager(), CommentOptionsModalBottomSheet.TAG);
        });
    }

    void fetchCommentsAsync() {
        Thread thread = new Thread(() -> {
            List<Comment> newComments = _commentDao.getComments();
            myHandler.post(() -> populateComments(newComments));
        });
        thread.start();
    }

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
    public void onStart() {
        super.onStart();

        View view = getView();
        assert view != null;

        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        ViewGroup.LayoutParams params = binding.commentLayoutContainer.getLayoutParams();
        params.height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.9);
        binding.commentLayoutContainer.setLayoutParams(params);

        binding.commentLayoutContainer.post(this::fetchCommentsAsync);

        binding.setUserCommentModel(userCommentModel);
        binding.etCommentInput.setOnFocusChangeListener((v, hasFocus) -> userCommentModel.setIsFocused(hasFocus));
        binding.tvCancelReplying.setOnClickListener(v -> {
            userCommentModel.setReplyToName("");
            userCommentModel.setReplyToId(null);
        });
        binding.fabAddAttachment.setOnClickListener(v -> {
            // Include only one of the following calls to launch(), depending on the types
            // of media that you want to let the user choose from.

            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        binding.ibtnRemoveAttachment.setOnClickListener(v -> userCommentModel.setAttachmentUri(null));
        binding.ibtnPostComment.setOnClickListener(v -> {
            Log.d("listener", "ibtnPostComment clicked");
            binding.etCommentInput.clearFocus();

            // TODO: upload comment to database here

            // TODO: upload comment to database here

            // fake new comment, for demonstration only
            Comment comment = userCommentModel.createComment();

            int newIndex = 0;

            String replyingId = comment.getReplyCommentId();
            if (replyingId == null) {
                _comments.add(newIndex, comment);
            }
            // look for the final position of the replying comments and insert it there
            else {
                while (!Objects.equals(_comments.get(newIndex).getId(), replyingId)) {
                    newIndex++;
                }
                do {
                    newIndex++;
                    if (newIndex >= _comments.size()) {
                        _comments.add(comment);
                        commentListAdapter.notifyDataSetChanged();
                        return;
                    }
                } while (_comments.get(newIndex).getReplyCommentId() != null);
                _comments.add(newIndex, comment);
            }
            commentListAdapter.notifyDataSetChanged();

            binding.tvCount.setText(getCommentCountString());

            // clear comment input
            userCommentModel.setContent("");
            userCommentModel.setAttachmentUri(null);
            userCommentModel.setReplyToId(null);
            userCommentModel.setReplyToName(null);
        });
    }

    private String getCommentCountString() {
        int count = _comments != null ? _comments.size() : 0;
        String result = String.format(Locale.US, "Now showing %d comment", count);
        return result + ((count > 1) ? "s" : "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
