package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinterest_clone_test2.models.Board;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.Tag;
import com.example.pinterest_clone_test2.ui.pin.edit.BoardBooleanPair;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    public static void fetchPinsFromIds(List<String> pinIds, OnPinsFetchedFromIdsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (pinIds == null || pinIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String pinId : pinIds) {
            tasks.add(firestore.collection("pins").document(pinId).get());
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    List<Pin> pins = new ArrayList<>();
                    for (Object result : results) {
                        if (result instanceof DocumentSnapshot) {
                            DocumentSnapshot doc = (DocumentSnapshot) result;
                            Pin pin = doc.toObject(Pin.class);
                            if (pin != null) {
                                pin.setId(doc.getId());
                                pins.add(pin);
                            }
                        }
                    }
                    callback.onSuccess(pins);
                })
                .addOnFailureListener(callback::onFailure);
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
        finalNameQuery.get().addOnSuccessListener(nameResults -> finalDescriptionQuery.get().addOnSuccessListener(descriptionResults -> {
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
        }).addOnFailureListener(callback::onSearchFailure)).addOnFailureListener(callback::onSearchFailure);
    }

    public static void getRelevantPinIdsByTags(@NonNull Pin pin, GetRelevantPinIdsByTagCallback callback) {
        List<Task<QuerySnapshot>> fetchTagTasks = new ArrayList<>();
        List<String> tagNames = pin.getTags();
        if (tagNames == null || tagNames.isEmpty()) {
            callback.OnComplete(new ArrayList<>());
            return;
        }

        for (String tagName : tagNames) {
            if (tagName.isBlank())
                continue;
            fetchTagTasks.add(FirebaseTagService.getPinsByTagName(tagName));
        }

        Set<String> pinIdSet = new HashSet<>();
        Tasks.whenAllComplete(fetchTagTasks)
                .addOnCompleteListener(listTask -> {
                    for (Task<QuerySnapshot> task : fetchTagTasks) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> docs = task.getResult().getDocuments();
                            if (docs.isEmpty())
                                return;

                            Tag tag = docs.get(0).toObject(Tag.class);
                            if (tag == null)
                                continue;

                            List<String> pinIds = tag.getPinIds();
                            if (pinIds != null && !pinIds.isEmpty()) {
                                pinIds.remove(pin.getId());
                                pinIdSet.addAll(pinIds);
                            }
                        }
                    }
                    List<String> result = new ArrayList<>(pinIdSet);
                    Collections.shuffle(result);
                    callback.OnComplete(result);
                });
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
                    .addOnSuccessListener(aVoid -> Log.d("FirebasePinService-UpdateLike", "like removed successfully"))
                    .addOnFailureListener(e -> {
                        Log.e("FirebasePinService-UpdateLike", "like failed to remove: ", e);
                        callback.OnFailure(e);
                    });
        }
    }

    public static void uploadPin(@NonNull Pin pin, UploadPinServiceCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        pin.setAuthorId(currentUser.getUid());

        firestore.collection("pins")
                .add(pin)
                .addOnSuccessListener(documentReference -> {
                    callback.OnSuccess(documentReference);
                    FirebaseUserService.savePinToProfile(documentReference.getId(), new FirebaseUserService.SavePinToProfileCallback() {
                        @Override
                        public void OnSuccess() {
                            Log.d("FirebaseUserService", "Saved pin to profile successfully");
                        }

                        @Override
                        public void OnFailure(Exception e) {
                            printExceptionMessage("Failed to saved pin to profile", e);
                        }
                    });
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void updatePinWithBoards(@NonNull Pin pin, Map<String, BoardBooleanPair> boardMap, UpdatePinWithBoardsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> pinUpdateMap = new HashMap<>();
        pinUpdateMap.put("name", pin.getName());
        pinUpdateMap.put("nameNormalized", pin.getNameNormalized());
        pinUpdateMap.put("description", pin.getDescription());
        pinUpdateMap.put("descriptionNormalized", pin.getDescriptionNormalized());
        pinUpdateMap.put("allowComment", pin.getAllowComment());

        Task<?> updatePinTask = firestore.collection("pins")
                .document(pin.getId())
                .update(pinUpdateMap)
                .addOnSuccessListener(unused -> Log.d("FirebasePinService", "Updated pin values successfully"))
                .addOnFailureListener(e -> printExceptionMessage("Failed to update pin values", e));

        List<Task<?>> updateBoardTasks = new ArrayList<>();
        for (Map.Entry<String, BoardBooleanPair> entry : boardMap.entrySet()) {
            Board board = entry.getValue().getBoard();
            String boardId = board.getId();
            if (entry.getValue().isIncluded()) {
                updateBoardTasks.add(firestore.collection("boards")
                        .document(boardId)
                        .update("pins", FieldValue.arrayUnion(pin.getId()))
                        .addOnSuccessListener(unused -> Log.d("FirebasePinService", "Added pin to board " + entry.getValue().getBoard().getName()))
                        .addOnFailureListener(e -> printExceptionMessage("Failed to add pin to board " + entry.getValue().getBoard().getName(), e))
                );
            } else {
                updateBoardTasks.add(firestore.collection("boards")
                        .document(boardId)
                        .update("pins", FieldValue.arrayRemove(pin.getId()))
                        .addOnSuccessListener(unused -> Log.d("FirebasePinService", "Removed pin from board " + entry.getValue().getBoard().getName()))
                        .addOnFailureListener(e -> printExceptionMessage("Failed to remove pin from board " + entry.getValue().getBoard().getName(), e))
                );
            }
        }

        List<Task<?>> allTasks = new ArrayList<>();
        allTasks.add(updatePinTask);
        allTasks.addAll(updateBoardTasks);
        Tasks.whenAllComplete(allTasks)
                .addOnCompleteListener(runnable -> {
                    boolean pinUpdateSuccess = updatePinTask.isSuccessful();
                    boolean boardUpdateSuccess = updateBoardTasks.stream().allMatch(Task::isSuccessful);
                    callback.Callback(pinUpdateSuccess, boardUpdateSuccess);
                });
    }

    // there will be a rule on firebase to check if is admin or is owner
    public static void deletePin(@NonNull String pinId, DeletePinCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("pins")
                .document(pinId)
                .delete()
                .addOnSuccessListener(unused -> {
                    callback.OnSuccess();
                    FirebaseUserService.removePinFromProfile(pinId);
                    FirebaseCommentService.deleteCommentsOfPin(pinId);
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void checkPinExists(@NonNull String pinId, CheckPinExistsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("pins")
                .document(pinId)
                .get()
                .addOnSuccessListener(documentSnapshot -> callback.OnComplete(documentSnapshot.exists()))
                .addOnFailureListener(e -> printExceptionMessage("Failed to check if pin exist", e));
    }

    private static void printExceptionMessage(String message, Exception e) {
        Log.e("FirebasePinService", message);
        if (e.getMessage() != null) {
            Log.e("FirebasePinService", e.getMessage());
        } else {
            e.printStackTrace();
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

    public interface OnPinsFetchedFromIdsCallback {
        void onSuccess(List<Pin> pins);

        void onFailure(Exception e);
    }

    public interface UploadPinServiceCallback {
        void OnSuccess(DocumentReference documentReference);

        void OnFailure(Exception e);
    }

    public interface UpdatePinWithBoardsCallback {
        void Callback(boolean updatePinSuccess, boolean updateBoardSuccess);
    }

    public interface DeletePinCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface CheckPinExistsCallback {
        void OnComplete(boolean exist);
    }

    public interface GetRelevantPinIdsByTagCallback {
        void OnComplete(List<String> pinIds);
    }
}
