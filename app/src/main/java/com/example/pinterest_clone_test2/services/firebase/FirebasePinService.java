package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.models.Pin;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
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

    public static void getUserProfilePins(@NonNull String userId, @Nullable DocumentSnapshot lastVisible, int perPage, GetProfilePinServiceCallback callback, boolean fetchSavedPins) {
        if (perPage < 1) {
            throw new IllegalArgumentException("Per page number must be greater than 0");
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        List<DocumentSnapshot> result = new ArrayList<>();
        final DocumentSnapshot[] returnLastVisible = new DocumentSnapshot[1];
        returnLastVisible[0] = lastVisible;

        Query createdPinQuery = firestore.collection("pins")
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // this is for pagination, use lastVisible to avoid infinite scrolling
        if (lastVisible != null) {
            createdPinQuery = createdPinQuery.startAfter(lastVisible);
        }

        List<Task<QuerySnapshot>> fetchPinTasks = new ArrayList<>();

        // fetch pin created by user
        fetchPinTasks.add(
                createdPinQuery
                        .limit(perPage)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                            if (!docs.isEmpty()) {
                                result.addAll(docs);
                                returnLastVisible[0] = docs.get(docs.size() - 1);
                            }
                        })
        );

        if (fetchSavedPins) {
            FirebaseUserService.getUserInfos(userId, new FirebaseUserService.GetUserInfoCallback() {
                @Override
                public void OnSuccess(DocumentSnapshot documentSnapshot) {
                    List<String> savedPins = null;
                    try {
                        savedPins = (List<String>) documentSnapshot.get("pins");
                    } catch (Exception e) {
                        //eat exception
                    }

                    if (savedPins != null) {
                        for (String savePinId :
                                savedPins) {
                            // fetch pin saved to profile by user
                            fetchPinTasks.add(
                                    firestore.collection("pins")
                                            .whereEqualTo(FieldPath.documentId(), savePinId)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                                                if (!docs.isEmpty()) {
                                                    result.addAll(docs);
                                                }
                                            })
                            );
                        }
                    }

                    Tasks.whenAllComplete(fetchPinTasks)
                            .addOnSuccessListener(unused -> {
                                callback.OnSuccess(result, returnLastVisible[0]);
                            })
                            .addOnFailureListener(callback::OnFailure);
                }

                @Override
                public void OnFailure(Exception e) {
                    e.printStackTrace();
                    Tasks.whenAllComplete(fetchPinTasks)
                            .addOnSuccessListener(unused -> {
                                callback.OnSuccess(result, returnLastVisible[0]);
                            })
                            .addOnFailureListener(callback::OnFailure);
                }
            });
        } else {
            Tasks.whenAllComplete(fetchPinTasks)
                    .addOnSuccessListener(unused -> {
                        callback.OnSuccess(result, returnLastVisible[0]);
                    })
                    .addOnFailureListener(callback::OnFailure);
        }
    }

    public static void searchPins(@NonNull String searchQuery, @Nullable DocumentSnapshot lastVisible, int perPage, @NonNull SearchPinServiceCallback callback) {
        if (perPage < 1) {
            throw new IllegalArgumentException("Per page number must be greater than 0");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String searchQueryLower = searchQuery.toLowerCase();

        // Xây dựng truy vấn theo tên
        Query nameQuery = db.collection("pins")
                .orderBy("nameNormalized")
                .startAt(searchQueryLower)
                .endAt(searchQueryLower + "\\uf8ff");

        if (lastVisible != null) {
            nameQuery = nameQuery.startAfter(lastVisible);
        }

        // Lưu truy vấn cuối cùng vào biến final để sử dụng trong lambda
        final Query finalNameQuery = nameQuery.limit(perPage);

        // Xây dựng truy vấn theo mô tả
        Query descriptionQuery = db.collection("pins")
                .orderBy("descriptionNormalized")
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
                List<DocumentSnapshot> resultDocuments = tryFilterDocuments(nameResults, descriptionResults);
                Map<String, DocumentSnapshot> uniqueResults = new HashMap<>();

                for (DocumentSnapshot doc : resultDocuments) {
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
                            .setMediaUrl(doc.getString("mediaUrl"))
                            .setThumbnailUrl(doc.getString("thumbnailUrl"))
                            .setType(doc.get("type", Pin.PinType.class));

                    String description = doc.getString("description");
                    String name = doc.getString("name");
                    pin.setName(name != null ? name : "");
                    pin.setDescription(description != null ? description : "");

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

    @NonNull
    private static List<DocumentSnapshot> tryFilterDocuments(QuerySnapshot nameResults, QuerySnapshot descriptionResults) {
        List<DocumentSnapshot> nameDocuments = nameResults.getDocuments();
        List<DocumentSnapshot> descriptionDocuments = descriptionResults.getDocuments();

        List<DocumentSnapshot> resultDocuments = new ArrayList<>();
        if (!nameDocuments.isEmpty()) {
            resultDocuments.addAll(nameDocuments);
        }
        if (!descriptionDocuments.isEmpty()) {
            resultDocuments.addAll(descriptionDocuments);
        }

        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        if (currentUserDocument != null) {
            List<String> blockedPins = null;
            List<String> blockedUsers = null;

            try {
                blockedPins = (List<String>) currentUserDocument.get("blockedPins");
                blockedUsers = (List<String>) currentUserDocument.get("blockedUsers");
            } catch (Exception e) {
                // eat exception
            }

            if (blockedPins != null) {
                List<String> finalBlockedPins = blockedPins;
                resultDocuments.removeIf(doc -> finalBlockedPins.contains(doc.getId()));
            }
            if (blockedUsers != null) {
                List<String> finalBlockedUsers = blockedUsers;
                resultDocuments.removeIf(doc -> finalBlockedUsers.contains(doc.getString("authorId")));
            }
        }
        return resultDocuments;
    }

    public static void getPinLikeCount(@NonNull String pinId, GetPinLikeCountCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("likes")
                .whereEqualTo("type", "PIN")
                .whereEqualTo("typeId", pinId)
                .get()
                .addOnSuccessListener(callback::OnSuccess)
                .addOnFailureListener(callback::OnFailure);
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

    public interface GetProfilePinServiceCallback {
        void OnSuccess(List<DocumentSnapshot> documentSnapshots, DocumentSnapshot lastVisible);

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
