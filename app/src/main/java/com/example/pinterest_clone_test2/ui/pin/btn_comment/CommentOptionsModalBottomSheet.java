package com.example.pinterest_clone_test2.ui.pin.btn_comment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.databinding.CommentOptionsModalBottomSheetBinding;
import com.example.pinterest_clone_test2.interfaces.ReportModalCallbacks;
import com.example.pinterest_clone_test2.models.Comment;
import com.example.pinterest_clone_test2.models.CommentReport;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.ui.report.ReportModalBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class CommentOptionsModalBottomSheet extends BottomSheetDialogFragment {
    CommentOptionsModalBottomSheetBinding binding;
    public static String TAG = "CommentOptionsModalBottomSheet";
    private final Comment _comment;
    private final Context _context;
    private final CommentModalBottomSheet.BlockUserCallback _blockUserCallback;

    public CommentOptionsModalBottomSheet(@NonNull Comment comment, Context context, CommentModalBottomSheet.BlockUserCallback blockUserCallback) {
        _comment = comment;
        _context = context;
        _blockUserCallback = blockUserCallback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CommentOptionsModalBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.tvClickableReport.setOnClickListener(v -> {
            ReportModalBottomSheet bottomSheet = new ReportModalBottomSheet(_context, reportModalCallback);
            bottomSheet.show(requireActivity().getSupportFragmentManager(), ReportModalBottomSheet.TAG);
            dismiss();
        });
        binding.tvClickableBlockUser.setOnClickListener(v -> {
            String userToBeBlockedId = _comment.getAuthorId();
            _blockUserCallback.Block(userToBeBlockedId);
            dismiss();
        });
    }

    ReportModalCallbacks reportModalCallback = new ReportModalCallbacks() {
        @Override
        public void CreateReport(@NonNull List<ReportReason> reasons) {
            CommentReport _commentReport = new CommentReport(reasons, "default-id", _comment, _context);
            _commentReport.sendReportToDatabase();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
