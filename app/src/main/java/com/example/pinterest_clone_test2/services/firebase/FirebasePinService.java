package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class FirebasePinService {
    public static void getPins(@Nullable DocumentSnapshot lastVisible, int perPage, Filter filter, GetPinServiceCallback callback) {
        if (perPage < 1) {
            throw new IllegalArgumentException("Per page number must be greater than 0");
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Query query = firestore.collection("pins");

        if (filter != null) {
            query = query.where(filter);
        }

        query = query.orderBy("createdAt", Query.Direction.DESCENDING);

        // this is for pagination, use lastVisible to avoid infinite scrolling
        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.limit(perPage)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        callback.OnSuccess(queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.OnFailure(e);
                    }
                });
    }

    public static void getPinLikeCount(@NonNull String pinId, GetPinLikeCountCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("likes")
                .whereEqualTo("type", "PIN")
                .whereEqualTo("typeId", pinId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        callback.OnSuccess(queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.OnFailure(e);
                    }
                });
    }

    public static void updateLike(@NonNull String pinId, boolean isLiked, UpdateLikeCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        if (isLiked) {
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("userId", currentUser.getUid());
            likeData.put("type", "PIN");
            likeData.put("typeId", pinId);
            likeData.put("createdAt", System.currentTimeMillis());
            firestore.collection("likes")
                    .add(likeData)
                    .addOnSuccessListener(documentReference -> Log.d("FirebasePinService-UpdateLike", "add like successfully"))
                    .addOnFailureListener(callback::OnFailure);
        } else {
            // fetch existing like
            firestore.collection("likes")
                    .whereEqualTo("userId", currentUser.getUid())
                    .whereEqualTo("type", "PIN")
                    .whereEqualTo("typeId", pinId)
                    .get()
                    // then delete it
                    .continueWithTask(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();

                            if (!documentSnapshots.isEmpty()) {
                                DocumentReference reference = documentSnapshots.get(0).getReference();
                                return reference.delete();
                            } else {
                                return Tasks.forResult(null);
                            }
                        } else {
                            return Tasks.forException(Objects.requireNonNull(task.getException()));
                        }
                    })
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebasePinService-UpdateLike", "like removed successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebasePinService-UpdateLike", "like failed to remove: ", e);
                        callback.OnFailure(e);
                    });
        }
    }

    public interface GetPinServiceCallback {
        void OnSuccess(QuerySnapshot querySnapshot);

        void OnFailure(Exception e);
    }

    public interface GetPinLikeCountCallback {
        void OnSuccess(QuerySnapshot querySnapshot);

        void OnFailure(Exception e);
    }

    public interface UpdateLikeCallback {
        void OnFailure(Exception e);
    }
}
