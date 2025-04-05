package com.example.pinterest_clone_test2.services.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class FirebaseUserService {
    public static void getUserInfos(String userId, GetUserInfoCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    callback.OnSuccess(documentSnapshot);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.OnFailure(e);
                    }
                });
    }

    public static void getCurrentUserInfo(GetUserInfoCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .whereEqualTo("userId", currentUser.getUid())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    callback.OnSuccess(documentSnapshot);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.OnFailure(e);
                    }
                });
    }

    public interface GetUserInfoCallback {
        void OnSuccess(DocumentSnapshot documentSnapshot);

        void OnFailure(Exception e);
    }
}
