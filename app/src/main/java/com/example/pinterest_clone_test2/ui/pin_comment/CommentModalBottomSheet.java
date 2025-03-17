package com.example.pinterest_clone_test2.ui.pin_comment;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.List;
import java.util.Locale;

public class CommentModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "CommentBottomSheet";
    String _pinId;
    ICommentDao _commentDao = null;
    Handler myHandler = new Handler();

    CommentModalBottomSheetBinding binding;
    CommentListAdapter commentListAdapter;

    public CommentModalBottomSheet(String pinId) {
        _pinId = pinId;
    }

    // update the UI with the comments
    void populateComments(List<Comment> comments, boolean append) {
        if (commentListAdapter == null) {
            commentListAdapter = new CommentListAdapter(comments);
            binding.rvComments.setAdapter(commentListAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            binding.rvComments.setLayoutManager(layoutManager);
        } else {
            commentListAdapter.setComments(comments, append);
        }

        if (comments.size() > 1) {
            binding.tvCount.setText(String.format(Locale.US, "Showing %d comments", comments.size()));
        } else {
            binding.tvCount.setText(String.format(Locale.US, "Showing %d comment", comments.size()));
        }

        Log.d("comment-modal-populating", "good shiet");
    }

    void fetchCommentsTask(int page, int perPage) {
        if (_commentDao == null) {
            _commentDao = new MockCommentDao();
        }

        List<Comment> comments;
        if (page == 0 || perPage == 0) {
            comments = _commentDao.getComments();
        } else comments = _commentDao.getComments(page, perPage);

        myHandler.post(() -> {
            populateComments(comments, true);
        });
    }

    void fetchCommentsAsync(int page, int perPage) {
        Thread thread = new Thread(() -> {
            fetchCommentsTask(page, perPage);
        });
        thread.start();
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
        if (view != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {

                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
            CoordinatorLayout layout = view.findViewById(R.id.comment_layout_container);
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            params.height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.9);
            layout.setLayoutParams(params);
            layout.post(() -> fetchCommentsAsync(1, 5));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
