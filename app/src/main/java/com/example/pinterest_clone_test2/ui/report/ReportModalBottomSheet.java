package com.example.pinterest_clone_test2.ui.report;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pinterest_clone_test2.adapters.ReportReasonAdapter;
import com.example.pinterest_clone_test2.daos.IReportReasonDao;
import com.example.pinterest_clone_test2.daos.MockReportReasonDao;
import com.example.pinterest_clone_test2.databinding.ReportModalBottomSheetBinding;
import com.example.pinterest_clone_test2.interfaces.ReportModalCallbacks;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportModalBottomSheet extends BottomSheetDialogFragment {
    ReportModalBottomSheetBinding binding;
    public static String TAG = "CommentReportModalBottomSheet";

    private final List<ReportReason> _reportReasons;
    private final List<Boolean> _checkedList;

    ReportReasonAdapter reportReasonAdapter;
    ReportModalCallbacks _callback;

    public ReportModalBottomSheet(@NonNull ReportModalCallbacks callback) {
        IReportReasonDao _reportReasonDao = new MockReportReasonDao();
        _reportReasons = _reportReasonDao.getReasons();  // get deep copy list
        assert !_reportReasons.isEmpty();
        _checkedList = new ArrayList<>(_reportReasons.size());
        for (int i = 0; i < _reportReasons.size(); i++) {
            _checkedList.add(false);
        }
        _callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ReportModalBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportReasonAdapter = new ReportReasonAdapter(_reportReasons, _checkedList);
        binding.rvReportReasons.setAdapter(reportReasonAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvReportReasons.setLayoutManager(layoutManager);

        binding.btnSubmit.setOnClickListener(v -> {
            List<ReportReason> reasons = new ArrayList<ReportReason>();
            for (int i = 0; i < _reportReasons.size(); i++) {
                ReportReason reason = _reportReasons.get(i);
                if (_checkedList.get(i)) {
                    reasons.add(reason);
                    Log.d("report-reason", String.format(Locale.US, "Report for\nTitle: %s\nDescription: %s", reason.getTitle(), reason.getDescription()));
                }
            }

            if (reasons.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

                // Add the buttons.
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                // set title
                builder.setMessage("Please select at least 1 reason")
                        .setTitle("Invalid report");

                // Create the AlertDialog.
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                _callback.CreateReport(reasons);
                dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        assert view != null;

        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        ViewGroup.LayoutParams params = binding.commentReportLayoutContainer.getLayoutParams();
        params.height = (int) (displayMetrics.heightPixels * 0.9);
        binding.commentReportLayoutContainer.setLayoutParams(params);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
