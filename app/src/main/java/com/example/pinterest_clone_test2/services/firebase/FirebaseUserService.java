package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FirebaseUserService {
    private static final String TAG = "FirebaseUserService";
    private static DocumentSnapshot currentUserDocument;
    private static long lastUpdateTime = 0;

    public static DocumentSnapshot getCurrentUserDocument() {
        return currentUserDocument;
    }

    public static long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public static void initUserDocument() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        firestore.collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, Objects.requireNonNull(error.getMessage()));
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentUserDocument = documentSnapshot;
                        lastUpdateTime = System.currentTimeMillis();
                        Log.d(TAG, "User info updated");
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

        removePinFromProfileAndBoards(pinId, firestore, currentUser);
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

        // remove pins of blocked users from profile, boards
        firestore.collection("pins")
                .whereEqualTo("authorId", userId)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        List<DocumentSnapshot> pinDocuments = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot pinDoc :
                                pinDocuments) {
                            removePinFromProfileAndBoards(pinDoc.getId(), firestore, currentUser);
                        }
                    }
                    return Tasks.forResult(null);
                });
    }

    private static void removePinFromProfileAndBoards(@NonNull String pinId, FirebaseFirestore firestore, FirebaseUser currentUser) {
        firestore.collection("users")
                .document(currentUser.getUid())
                .update("pins", FieldValue.arrayRemove(pinId))
                .addOnSuccessListener(unused -> Log.d(TAG, "Removed blocked pin from profile"));

        firestore.collection("boards")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot boardDoc :
                                documentSnapshots) {
                            firestore.collection("boards")
                                    .document(boardDoc.getId())
                                    .update("pins", FieldValue.arrayRemove(pinId))
                                    .addOnSuccessListener(unused -> {
                                        if (boardDoc.getString("name") != null) {
                                            Log.d(TAG, "Removed blocked pin from board: " + boardDoc.getString("name"));
                                        } else {
                                            Log.d(TAG, "Removed blocked pin from board id: " + boardDoc.getId());
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (boardDoc.getString("name") != null) {
                                            Log.e(TAG, "Failed to removed blocked pin from board: " + boardDoc.getString("name"));
                                        } else {
                                            Log.e(TAG, "Failed to remove blocked pin from board id: " + boardDoc.getId());
                                        }
                                        e.printStackTrace();
                                    });
                        }
                    } else {
                        Log.d(TAG, "Failed to remove fetch board document");
                    }
                    return Tasks.forResult(null);
                });
    }

    public static void removePinFromProfileAndBoards(@NonNull String pinId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        removePinFromProfileAndBoards(pinId, firestore, currentUser);
    }

    public static void updateGender(@NonNull String gender, UpdateGenderCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .update("gender", gender)
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public static void updateEmail(@NonNull String email, UpdateEmailCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .update("email", email)
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public static void updateBirthdate(@NonNull String birthdate, UpdateBirthdateCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .update("birthdate", birthdate)
                .addOnSuccessListener(unused -> callback.OnSuccess())
                .addOnFailureListener(callback::OnFailure);
    }

    public static void updatePassword(@NonNull String oldPassword, @NonNull String newPassword, UpdatePasswordCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.OnFailure(new Exception("Người dùng không hợp lệ"));
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(authResult -> user.updatePassword(newPassword)
                        .addOnSuccessListener(unused -> callback.OnSuccess())
                        .addOnFailureListener(callback::OnFailure))
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
                                            Pin pin = doc.toObject(Pin.class);
                                            if (pin == null)
                                                continue;

                                            pin.setId(doc.getId());
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

    public static void removePinFromProfile(@NonNull String pinId) {
        assert currentUserDocument != null;
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .document(currentUserDocument.getId())
                .update("pins", FieldValue.arrayRemove(pinId))
                .addOnSuccessListener(unused -> Log.d(TAG, String.format(Locale.US, "Removed pin %s from profile", pinId)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, String.format(Locale.US, "Failed to remove pin %s from profile", pinId));
                    if (e.getMessage() != null) {
                        Log.e(TAG, e.getMessage());
                    } else {
                        e.printStackTrace();
                    }
                });
    }


    //================================== Admin Functions
    // 1. Lấy toàn bộ user
    public static void getAllUsers(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(listener);
    }

    // 2. Lấy user theo ID - đã sửa để dùng document ID trực tiếp
    public static void getUserById(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnCompleteListener(listener);
    }

    // 3. Chỉnh role user theo ID
    public static void editRoleUser(String userId, String newRole, OnSuccessListener<Void> listener, OnFailureListener failListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .update("role", newRole)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failListener);
    }

    // 4. Thêm user - đã sửa để đảm bảo userId là document ID
    public static void addUser(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Tạo document reference trước
        DocumentReference newUserRef = db.collection("users").document();

        // Gán ID từ document reference làm userId
        String userId = newUserRef.getId();
        user.setUserId(userId);

        // Lưu user vào document với ID đã tạo
        newUserRef.set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // 5. Lấy toàn bộ userId bị ban
    public static void getBannedUserId(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bannedUsers").get().addOnCompleteListener(listener);
    }

    // 6. Lấy danh sách user bị ban
    public static void getBannedUsers(OnCompleteListener<List<User>> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        getBannedUserId(task -> {
            if (task.isSuccessful()) {
                List<String> bannedIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String bannedId = doc.getString("userId");
                    if (bannedId != null && !bannedId.isEmpty()) {
                        bannedIds.add(bannedId);
                    }
                }

                if (bannedIds.isEmpty()) {
                    listener.onComplete(Tasks.forResult(Collections.emptyList()));
                    return;
                }

                // Lấy thông tin các user bị ban bằng document ID trực tiếp
                List<User> users = new ArrayList<>();
                List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                for (String id : bannedIds) {
                    tasks.add(db.collection("users").document(id).get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    User user = snapshot.toObject(User.class);
                                    if (user != null) {
                                        // Đảm bảo userId được gán đúng
                                        user.setUserId(snapshot.getId());
                                        users.add(user);
                                    }
                                }
                            })
                    );
                }

                // Đợi tất cả các tasks hoàn thành
                Tasks.whenAllComplete(tasks)
                        .addOnCompleteListener(allTasks -> listener.onComplete(Tasks.forResult(users)));
            } else {
                listener.onComplete(Tasks.forException(task.getException()));
            }
        });
    }

    // 7. Lấy user không bị cấm - đã sửa
    public static void getNormalUsers(OnCompleteListener<List<User>> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        getBannedUserId(task -> {
            if (task.isSuccessful()) {
                List<String> bannedIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String bannedId = doc.getString("userId");
                    if (bannedId != null && !bannedId.isEmpty()) {
                        bannedIds.add(bannedId);
                    }
                }

                // Lấy tất cả users
                db.collection("users").get().addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        List<User> normalUsers = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : userTask.getResult()) {
                            // Sử dụng document ID làm userId
                            String userId = doc.getId();

                            // Chuyển đổi document sang User object
                            User user = doc.toObject(User.class);

                            // Đảm bảo userId trong object trùng khớp với document ID
                            user.setUserId(userId);

                            Log.d("DEBUG_USER", "userId: " + userId);

                            // Kiểm tra xem user có trong danh sách banned không
                            if (!bannedIds.contains(userId)) {
                                normalUsers.add(user);
                            } else {
                                Log.d("DEBUG_USER", "BỊ BAN: " + userId);
                            }
                        }
                        listener.onComplete(Tasks.forResult(normalUsers));
                    } else {
                        listener.onComplete(Tasks.forException(userTask.getException()));
                    }
                });
            } else {
                listener.onComplete(Tasks.forException(task.getException()));
            }
        });
    }

    // 8. Thêm user vào danh sách bị cấm
    public static void addBannedUser(String userId, OnSuccessListener<DocumentReference> listener, OnFailureListener failListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> banData = new HashMap<>();
        banData.put("userId", userId);
        db.collection("bannedUsers").add(banData)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failListener);
    }

    // 9. Xóa user khỏi danh sách bị cấm
    public static void removeBannedUser(String userId, OnSuccessListener<Void> listener, OnFailureListener failListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bannedUsers").whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                WriteBatch batch = db.batch();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    batch.delete(doc.getReference());
                }
                batch.commit().addOnSuccessListener(listener).addOnFailureListener(failListener);
            } else if (task.isSuccessful() && task.getResult().isEmpty()) {
                // Không tìm thấy banned user để xóa
                listener.onSuccess(null);
            } else {
                failListener.onFailure(task.getException());
            }
        });
    }

    // Interfaces
    public interface SavePinToProfileCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface HidePinCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface UpdateGenderCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface UpdateEmailCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface UpdatePasswordCallback {
        void OnSuccess();

        void OnFailure(Exception e);
    }

    public interface UpdateBirthdateCallback {
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