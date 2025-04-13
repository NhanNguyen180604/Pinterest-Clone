package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.Pin;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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

    public static void followUser(@NonNull String userId, FollowUserCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        if (userId.equals(currentUser.getUid())) {
            callback.OnFailure(new Exception("Cannot follow yourself"));
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Add userId to current user's following list
        firestore.collection("users")
                .document(currentUser.getUid())
                .update("following", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> {
                    // Add current user to target user's followers list
                    firestore.collection("users")
                            .document(userId)
                            .update("followers", FieldValue.arrayUnion(currentUser.getUid()))
                            .addOnSuccessListener(unused1 -> callback.OnSuccess())
                            .addOnFailureListener(callback::OnFailure);
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void unfollowUser(@NonNull String userId, FollowUserCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Remove userId from current user's following list
        firestore.collection("users")
                .document(currentUser.getUid())
                .update("following", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(unused -> {
                    // Remove current user from target user's followers list
                    firestore.collection("users")
                            .document(userId)
                            .update("followers", FieldValue.arrayRemove(currentUser.getUid()))
                            .addOnSuccessListener(unused1 -> callback.OnSuccess())
                            .addOnFailureListener(callback::OnFailure);
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public static void checkIfFollowing(@NonNull String userId, CheckFollowStatusCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        if (userId.equals(currentUser.getUid())) {
            callback.OnSuccess(false); // Can't follow yourself
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        boolean isFollowing = (following != null && following.contains(userId));
                        callback.OnSuccess(isFollowing);
                    } else {
                        callback.OnFailure(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(callback::OnFailure);
    }

    // Get user's pins
    public static void getUserPins(@NonNull String userId, GetUserPinsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // First get the user document to get the list of pinIds
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> pinIds = (List<String>) documentSnapshot.get("pins");

                        if (pinIds == null || pinIds.isEmpty()) {
                            callback.OnSuccess(new ArrayList<>());
                            return;
                        }

                        // Split pinIds into batches of 30
                        List<List<String>> pinIdBatches = new ArrayList<>();
                        for (int i = 0; i < pinIds.size(); i += 30) {
                            pinIdBatches.add(pinIds.subList(i, Math.min(i + 30, pinIds.size())));
                        }

                        // Fetch pins in batches
                        List<Pin> allPins = new ArrayList<>();
                        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

                        for (List<String> batch : pinIdBatches) {
                            Task<QuerySnapshot> task = firestore.collection("pins")
                                    .whereIn("__name__", batch)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                            Pin pin = new Pin()
                                                    .setId(doc.getId())
                                                    .setAllowComment(Boolean.TRUE.equals(doc.getBoolean("allowComment")))
                                                    .setAuthorId(doc.getString("authorId"))
                                                    .setMediaUrl(doc.getString("mediaUrl"))
                                                    .setThumbnailUrl(doc.getString("thumbnailUrl"))
                                                    .setType(doc.get("type", Pin.PinType.class));

                                            String description = doc.getString("description");
                                            String name = doc.getString("name");
                                            pin.setDescription(description != null ? description : "")
                                                    .setName(name != null ? name : "");

                                            Long createdAt = doc.getLong("createdAt");
                                            Integer likeCount = doc.get("likeCount", Integer.class);
                                            pin.setCreatedAt(createdAt != null ? createdAt : 0);
                                            pin.setLikeCount(likeCount != null ? likeCount : 0);

                                            allPins.add(pin);
                                        }
                                    });
                            tasks.add(task);
                        }

                        Tasks.whenAllComplete(tasks)
                                .addOnSuccessListener(taskResults -> {
                                    // Sort pins by creation time in descending order
                                    allPins.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                                    callback.OnSuccess(allPins);
                                })
                                .addOnFailureListener(callback::OnFailure);
                    } else {
                        callback.OnFailure(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(callback::OnFailure);
    }

    public interface SavePinToProfileCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface HidePinCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface FollowUserCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface CheckFollowStatusCallback {
        void OnSuccess(boolean isFollowing);

        void OnFailure(Exception e);
    }

    public interface GetUserPinsCallback {
        void OnSuccess(List<Pin> pins);

        void OnFailure(Exception e);
    }
    public interface GetUserInfoCallback {
        void OnSuccess(DocumentSnapshot documentSnapshot);

        void OnFailure(Exception e);
    }
}
