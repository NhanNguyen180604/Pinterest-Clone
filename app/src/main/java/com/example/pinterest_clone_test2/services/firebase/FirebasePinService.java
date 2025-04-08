package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.models.Pin;
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

import java.util.ArrayList;
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
                .addOnSuccessListener(callback::OnSuccess)
                .addOnFailureListener(callback::OnFailure);
    }

    public static void searchPins(@NonNull String searchQuery, @Nullable DocumentSnapshot lastVisible, int perPage, @NonNull SearchPinServiceCallback callback) {
        if (perPage < 1) {
            throw new IllegalArgumentException("Per page number must be greater than 0");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String searchQueryLower = searchQuery.toLowerCase();

        // Xây dựng truy vấn theo tên
        Query nameQuery = db.collection("pins")
                .orderBy("name")
                .startAt(searchQueryLower)
                .endAt(searchQueryLower + "\\uf8ff");

        if (lastVisible != null) {
            nameQuery = nameQuery.startAfter(lastVisible);
        }

        // Lưu truy vấn cuối cùng vào biến final để sử dụng trong lambda
        final Query finalNameQuery = nameQuery.limit(perPage);

        // Xây dựng truy vấn theo mô tả
        Query descriptionQuery = db.collection("pins")
                .orderBy("description")
                .startAt(searchQueryLower)
                .endAt(searchQueryLower + "\\uf8ff");

        if (lastVisible != null) {
            descriptionQuery = descriptionQuery.startAfter(lastVisible);
        }

        // Lưu truy vấn cuối cùng vào biến final để sử dụng trong lambda
        final Query finalDescriptionQuery = descriptionQuery.limit(perPage);

        // Thực hiện các truy vấn
        finalNameQuery.get().addOnSuccessListener(nameResults -> {
            finalDescriptionQuery.get().addOnSuccessListener(descriptionResults -> {
                Map<String, DocumentSnapshot> uniqueResults = new HashMap<>();

                for (DocumentSnapshot doc : nameResults.getDocuments()) {
                    uniqueResults.put(doc.getId(), doc);
                }

                for (DocumentSnapshot doc : descriptionResults.getDocuments()) {
                    uniqueResults.put(doc.getId(), doc);
                }

                // Chuyển đổi DocumentSnapshot thành đối tượng Pin
                List<Pin> pinResults = new ArrayList<>();
                DocumentSnapshot lastDoc = null;

                for (DocumentSnapshot doc : uniqueResults.values()) {
                    Pin pin = new Pin()
                            .setId(doc.getId())
                            .setAllowComment(Boolean.TRUE.equals(doc.getBoolean("allowComment")))
                            .setAuthorId(doc.getString("authorId"))
                            .setDescription(doc.getString("description"))
                            .setName(doc.getString("name"))
                            .setMediaUrl(doc.getString("mediaUrl"))
                            .setThumbnailUrl(doc.getString("thumbnailUrl"))
                            .setType(doc.get("type", Pin.PinType.class));

                    Long createdAt = doc.getLong("createdAt");
                    Integer likeCount = doc.get("likeCount", Integer.class);
                    pin.setCreatedAt(createdAt != null ? createdAt : 0);
                    pin.setLikeCount(likeCount != null ? likeCount : 0);

                    pinResults.add(pin);

                    // Cập nhật lastDoc cho phân trang
                    lastDoc = doc;
                }

                if (pinResults.size() > perPage) {
                    pinResults = pinResults.subList(0, perPage);
                }

                callback.onSearchSuccess(pinResults, lastDoc);
            }).addOnFailureListener(callback::onSearchFailure);
        }).addOnFailureListener(callback::onSearchFailure);
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


    public interface SearchPinServiceCallback {
        void onSearchSuccess(List<Pin> results, DocumentSnapshot lastVisible);

        void onSearchFailure(Exception e);
    }
}
