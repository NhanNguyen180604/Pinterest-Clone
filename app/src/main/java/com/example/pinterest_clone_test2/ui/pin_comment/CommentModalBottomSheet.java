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
import androidx.recyclerview.widget.RecyclerView;

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

public class CommentModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "CommentBottomSheet";
    String _pinId;
    ICommentDao _commentDao = null;
    List<Comment> _comments;
    Handler myHandler = new Handler();
    int currentPage = 1;
    final int perPage = 5;
    boolean isOnLastPage = false;

    CommentModalBottomSheetBinding binding;
    CommentListAdapter commentListAdapter;

    public CommentModalBottomSheet(String pinId) {
        _pinId = pinId;
        _commentDao = new MockCommentDao();
        _comments = new ArrayList<>();
    }

    // update the UI with the comments
    void populateComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvComments.setLayoutManager(layoutManager);

        commentListAdapter = new CommentListAdapter(_comments, binding.rvComments);
        binding.rvComments.setAdapter(commentListAdapter);

        commentListAdapter.setOnLoadMoreListener(new CommentListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (isOnLastPage) {
                    return;
                }

                _comments.add(null);
                commentListAdapter.notifyItemInserted(_comments.size() - 1);

                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        _comments.remove(_comments.size() - 1);
                        commentListAdapter.notifyItemRemoved(_comments.size());

                        currentPage++;
                        List<Comment> newComments = _commentDao.getComments(currentPage, perPage);

                        if (newComments.isEmpty()) {
                            isOnLastPage = true;
                            return;
                        }

                        _comments.addAll(newComments);
                        commentListAdapter.notifyItemRangeInserted(_comments.size() - newComments.size(), newComments.size());

                        if (commentListAdapter.getItemCount() > 1) {
                            binding.tvCount.setText(String.format(Locale.US, "Showing %d comments", commentListAdapter.getItemCount()));
                        } else {
                            binding.tvCount.setText(String.format(Locale.US, "Showing %d comment", commentListAdapter.getItemCount()));
                        }
                    }
                }, 2000);
            }
        });

        if (_comments.size() > 1) {
            binding.tvCount.setText(String.format(Locale.US, "Showing %d comments", _comments.size()));
        } else {
            binding.tvCount.setText(String.format(Locale.US, "Showing %d comment", _comments.size()));
        }
    }

    void fetchCommentsAsync() {
        Thread thread = new Thread(() -> {
            List<Comment> newComments = _commentDao.getComments(currentPage, perPage);
            _comments.addAll(newComments);
            myHandler.post(this::populateComments);
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
            CoordinatorLayout layout = view.findViewById(R.id.comment_layout_container);
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            params.height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.9);
            layout.setLayoutParams(params);
            layout.post(this::fetchCommentsAsync);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
