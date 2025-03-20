package com.example.pinterest_clone_test2.ui.pin_comment;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
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

public class CommentModalBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "CommentBottomSheet";
    String _pinId;
    ICommentDao _commentDao;
    CommentModalViewModel viewModel;
    Handler myHandler = new Handler();
    int currentPage = 1;
    final int perPage = 5;
    boolean isOnLastPage = false;

    CommentModalBottomSheetBinding binding;
    CommentListAdapter commentListAdapter;

    public CommentModalBottomSheet(String pinId) {
        _pinId = pinId;
        _commentDao = new MockCommentDao();
    }

    // update the UI with the comments
    void populateComments(List<Comment> newComments) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvComments.setLayoutManager(layoutManager);

        viewModel.addComments(newComments);
        commentListAdapter = new CommentListAdapter(viewModel.getComments().getValue(), binding.rvComments);
        binding.rvComments.setAdapter(commentListAdapter);

        commentListAdapter.setOnLoadMoreListener(new CommentListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (isOnLastPage) {
                    return;
                }

                viewModel.addComments(null);

                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewModel.removeLastComment();

                        currentPage++;
                        List<Comment> newComments = _commentDao.getComments(currentPage, perPage);

                        if (newComments.isEmpty()) {
                            isOnLastPage = true;
                            return;
                        }

                        viewModel.addComments(newComments);
                        commentListAdapter.setLoaded();
                    }
                });
            }
        });

        viewModel.getComments().observe(this, comments -> {
            commentListAdapter.notifyDataSetChanged();
            binding.tvCount.setText(viewModel.getCommentCountString());
        });
    }

    void fetchCommentsAsync() {
        Thread thread = new Thread(() -> {
            List<Comment> newComments = _commentDao.getComments(currentPage, perPage);
            myHandler.post(() -> populateComments(newComments));
        });
        thread.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommentModalViewModel.class);
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
        CoordinatorLayout layout = view.findViewById(R.id.comment_layout_container);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.9);
        layout.setLayoutParams(params);
        layout.post(this::fetchCommentsAsync);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
