package com.example.pinterest_clone_test2.models;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.services.firebase.FirebaseReportService;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseReport {
    protected Map<String, Object> reportData = new HashMap<>();
    private final Context _context;

    public BaseReport(@NonNull List<ReportReason> reasons, @NonNull String reportOwnerId, @NonNull String reportedId, Context context) {
        _context = context;
        reportData.put("typeId", reportedId);
        reportData.put("userId", reportOwnerId);
        reportData.put("isChecked", false);
        reportData.put("reasons", reasons);
    }

    public void sendReportToDatabase() {
        reportData.put("createdAt", System.currentTimeMillis());
        FirebaseReportService.uploadReport(reportData, uploadReportCallback);
    }

    protected final FirebaseReportService.UploadReportCallback uploadReportCallback = new FirebaseReportService.UploadReportCallback() {
        @Override
        public void OnSuccess(DocumentReference documentReference) {
            Log.d("PinReport", "Sent report successfully, id: " + documentReference.getId());
            Toast.makeText(_context, _context.getResources().getString(R.string.report_success), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void OnFailure(Exception e) {
            Log.e("PinReport", "Failed to send report:\n" + e.getMessage());
            Toast.makeText(_context, _context.getResources().getString(R.string.report_failure), Toast.LENGTH_SHORT).show();
        }
    };
}
