package com.example.pinterest_clone_test2.services.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseReportService {
    public static void uploadReport(@NonNull Map<String, Object> reportData, UploadReportCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("reports")
                .add(reportData)
                .addOnSuccessListener(callback::OnSuccess)
                .addOnFailureListener(callback::OnFailure);
    }

    public interface UploadReportCallback {
        void OnSuccess(DocumentReference documentReference);

        void OnFailure(Exception e);
    }

    /**
     * Lấy danh sách báo cáo cho comment
     */
    public static void getReportsForComment(String commentId, GetReportsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("reports")
                .whereEqualTo("type", "COMMENT")
                .whereEqualTo("typeId", commentId)
                .get()
                .addOnSuccessListener(callback::OnSuccess)
                .addOnFailureListener(callback::OnFailure);
    }

    /**
     * Đánh dấu báo cáo đã được kiểm tra
     */
    public static void markReportAsChecked(String reportId, MarkReportCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("reports")
                .document(reportId)
                .update("isChecked", true)
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    /**
     * Gửi thông báo cho người báo cáo và người bị báo cáo
     */
    public static void sendReportNotifications(List<String> reporterIds, String authorId,
                                               String commentId, boolean isViolation,
                                               String reasonTitle, SendNotificationsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        List<Task<DocumentReference>> tasks = new ArrayList<>();

        // Thông báo cho người báo cáo
        for (String reporterId : reporterIds) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", reporterId);
            notification.put("type", "REPORT_PROCESSED");
            notification.put("commentId", commentId);
            notification.put("createdAt", System.currentTimeMillis());
            notification.put("isViolation", isViolation);
            notification.put("reasonTitle", reasonTitle);

            tasks.add(firestore.collection("notifications").add(notification));
        }

        // Thông báo cho người bị báo cáo
        if (isViolation && authorId != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", authorId);
            notification.put("type", "COMMENT_REMOVED");
            notification.put("commentId", commentId);
            notification.put("createdAt", System.currentTimeMillis());
            notification.put("reasonTitle", reasonTitle);

            tasks.add(firestore.collection("notifications").add(notification));
        }

        Tasks.whenAllComplete(tasks)
                .addOnSuccessListener(taskList -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public interface GetReportsCallback {
        void OnSuccess(QuerySnapshot querySnapshot);
        void OnFailure(Exception e);
    }

    public interface MarkReportCallback {
        void OnSuccess();
        void OnFailure(Exception e);
    }

    public interface SendNotificationsCallback {
        void OnSuccess();
        void OnFailure(Exception e);
    }
}
