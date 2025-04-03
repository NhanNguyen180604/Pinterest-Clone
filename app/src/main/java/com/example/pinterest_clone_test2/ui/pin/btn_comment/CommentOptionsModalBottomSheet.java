package com.example.pinterest_clone_test2.ui.pin.btn_comment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.databinding.CommentOptionsModalBottomSheetBinding;
import com.example.pinterest_clone_test2.interfaces.ReportModalCallbacks;
import com.example.pinterest_clone_test2.models.Comment;
import com.example.pinterest_clone_test2.models.CommentReport;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.ui.report.ReportModalBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Locale;

public class CommentOptionsModalBottomSheet extends BottomSheetDialogFragment {
    CommentOptionsModalBottomSheetBinding binding;
    public static String TAG = "CommentOptionsModalBottomSheet";
    private final Comment _comment;

    public CommentOptionsModalBottomSheet(@NonNull Comment comment) {
        _comment = comment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CommentOptionsModalBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.tvClickableReport.setOnClickListener(v -> {
            ReportModalBottomSheet bottomSheet = new ReportModalBottomSheet(reportModalCallback);
            bottomSheet.show(requireActivity().getSupportFragmentManager(), ReportModalBottomSheet.TAG);
            dismiss();
        });
        binding.tvClickableBlockUser.setOnClickListener(v -> {
            String userToBeBlockedId = _comment.getAuthorId();
            // TODO: block user on the database
            Toast.makeText(requireContext(), String.format(Locale.US, "Blocked user: %s", userToBeBlockedId), Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    ReportModalCallbacks reportModalCallback = new ReportModalCallbacks() {
        @Override
        public void CreateReport(@NonNull List<ReportReason> reasons) {
            CommentReport _commentReport = new CommentReport(reasons, "default-id", _comment);
            _commentReport.sendReportToDatabase();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
