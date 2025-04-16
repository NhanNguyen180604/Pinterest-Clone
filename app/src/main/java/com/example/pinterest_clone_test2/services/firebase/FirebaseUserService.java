package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import com.example.pinterest_clone_test2.models.User;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FirebaseUserService {
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
    public interface GetUserInfoCallback {
        void OnSuccess(DocumentSnapshot documentSnapshot);

        void OnFailure(Exception e);
    }


    //================================== Phần này của LÊ TRƯỜNG
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("users");
    private CollectionReference bannedRef = db.collection("bannedUser");

    // 1. Lấy toàn bộ user
    public void getAllUsers(OnCompleteListener<QuerySnapshot> listener) {
        userRef.get().addOnCompleteListener(listener);
    }

    // 2. Lấy user theo ID - đã sửa để dùng document ID trực tiếp
    public void getUserById(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        userRef.document(userId).get().addOnCompleteListener(listener);
    }

    // 3. Chỉnh role user theo ID
    public void editRoleUser(String userId, String newRole, OnSuccessListener<Void> listener, OnFailureListener failListener) {
        userRef.document(userId)
                .update("role", newRole)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failListener);
    }

    // 4. Thêm user - đã sửa để đảm bảo userId là document ID
    public void addUser(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        // Tạo document reference trước
        DocumentReference newUserRef = userRef.document();

        // Gán ID từ document reference làm userId
        String userId = newUserRef.getId();
        user.setUserId(userId);

        // Lưu user vào document với ID đã tạo
        newUserRef.set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // 5. Lấy toàn bộ userId bị ban
    public void getBannedUserId(OnCompleteListener<QuerySnapshot> listener) {
        bannedRef.get().addOnCompleteListener(listener);
    }

    // 6. Lấy danh sách user bị ban
    public void getBannedUsers(OnCompleteListener<List<User>> listener) {
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
                    tasks.add(userRef.document(id).get()
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
                        .addOnCompleteListener(allTasks -> {
                            listener.onComplete(Tasks.forResult(users));
                        });
            } else {
                listener.onComplete(Tasks.forException(task.getException()));
            }
        });
    }

    // 7. Lấy user không bị cấm - đã sửa
    public void getNormalUsers(OnCompleteListener<List<User>> listener) {
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
                userRef.get().addOnCompleteListener(userTask -> {
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
    public void addBannedUser(String userId, OnSuccessListener<DocumentReference> listener, OnFailureListener failListener) {
        Map<String, Object> banData = new HashMap<>();
        banData.put("userId", userId);
        bannedRef.add(banData)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failListener);
    }

    // 9. Xóa user khỏi danh sách bị cấm
    public void removeBannedUser(String userId, OnSuccessListener<Void> listener, OnFailureListener failListener) {
        bannedRef.whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
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
}