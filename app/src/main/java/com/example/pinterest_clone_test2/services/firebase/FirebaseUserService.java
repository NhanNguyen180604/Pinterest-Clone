package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public abstract class FirebaseUserService {
    private static DocumentSnapshot currentUserDocument;

    public static DocumentSnapshot getCurrentUserDocument() {
        return currentUserDocument;
    }

    public static void initUserDocument() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("FirebaseUserService", Objects.requireNonNull(error.getMessage()));
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentUserDocument = documentSnapshot;
                        Log.d("FirebaseUserService", "User info updated");
                    }
                });
    }

    public static void getUserInfos(String userId, GetUserInfoCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    if (documents.isEmpty()) {
                        callback.OnFailure(new Exception("User not found"));
                        return;
                    }
                    callback.OnSuccess(documents.get(0));
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void savePinToProfile(@NonNull String pinId, SavePinToProfileCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(currentUser.getUid())
                .update("pins", FieldValue.arrayUnion(pinId))
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public static void hidePin(@NonNull String pinId, HidePinCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(currentUser.getUid())
                .update("blockedPins", FieldValue.arrayUnion(pinId))
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public static void blockUser(@NonNull String userId, HidePinCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(currentUser.getUid())
                .update("blockedUsers", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public interface GetUserInfoCallback {
        void OnSuccess(DocumentSnapshot documentSnapshot);

        void OnFailure(Exception e);
    }

    public interface SavePinToProfileCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface HidePinCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }
}
