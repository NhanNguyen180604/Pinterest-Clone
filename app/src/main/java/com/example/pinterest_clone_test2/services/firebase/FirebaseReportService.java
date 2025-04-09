package com.example.pinterest_clone_test2.services.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
}
