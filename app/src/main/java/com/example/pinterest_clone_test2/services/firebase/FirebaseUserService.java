package com.example.pinterest_clone_test2.services.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public abstract class FirebaseUserService {
    private static DocumentSnapshot currentUserDocument;

    public static DocumentSnapshot getCurrentUserDocument() {
        return currentUserDocument;
    }

    public static void initUserDocument() {
        getCurrentUserInfo(new GetUserInfoCallback() {
            @Override
            public void OnSuccess(DocumentSnapshot documentSnapshot) {
                currentUserDocument = documentSnapshot;
            }

            @Override
            public void OnFailure(Exception e) {
                e.printStackTrace();
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

    public static void getCurrentUserInfo(GetUserInfoCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        getUserInfos(currentUser.getUid(), callback);
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

    public interface GetUserInfoCallback {
        void OnSuccess(DocumentSnapshot documentSnapshot);

        void OnFailure(Exception e);
    }

    public interface SavePinToProfileCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }
}
