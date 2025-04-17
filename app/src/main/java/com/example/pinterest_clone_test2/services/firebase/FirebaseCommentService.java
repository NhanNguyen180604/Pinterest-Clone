package com.example.pinterest_clone_test2.services.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.pinterest_clone_test2.models.Comment;
import com.example.pinterest_clone_test2.utils.CloudinaryManager;
import com.google.android.gms.tasks.Task;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public abstract class FirebaseCommentService {
    // use this function for a pin comment only, admin should have a separate function that doesn't filter out anyone
    public static void getPinComments(@NonNull String pinId, @Nullable Filter filter, GetCommentServiceCallback callback, Context context) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        Query query = firestore.collection("comments")
                .whereEqualTo("pin", pinId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        if (filter != null) {
            query = query.where(filter);
        }

        query.get()
                .addOnSuccessListener(commentDocumentSnapshots -> {
                    List<DocumentSnapshot> commentDocuments = tryFilterBlockedUsers(commentDocumentSnapshots);

                    if (commentDocuments.isEmpty()) {
                        callback.OnSuccess(Collections.emptyList());
                        return;
                    }

                    List<Task<QuerySnapshot>> fetchUserTasks = new ArrayList<>();
                    List<Task<QuerySnapshot>> fetchLikeTasks = new ArrayList<>();
                    List<Comment> comments = new ArrayList<>();

                    for (DocumentSnapshot commentDocument :
                            commentDocuments) {
                        String userId = commentDocument.getString("userId");
                        Comment comment = new Comment(context)
                                .setId(commentDocument.getId())
                                .setAuthorId(userId)
                                .setPinId(pinId)
                                .setContent(commentDocument.getString("content"))
                                .setAttachmentUrl(commentDocument.getString("attachmentUrl"))
                                .setAttachmentThumbnailUrl(commentDocument.getString("attachmentThumbnailUrl"))
                                .setReplyCommentId(commentDocument.getString("replyTo"));

                        Long createdAt = commentDocument.getLong("createdAt");
                        comment.setCreatedAt(createdAt != null ? createdAt : 0);

                        // for each comment, we fetch its author's information
                        Task<QuerySnapshot> userTask = firestore.collection("users")
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<DocumentSnapshot> userDocuments = queryDocumentSnapshots.getDocuments();
                                    if (!userDocuments.isEmpty()) {
                                        DocumentSnapshot userDocument = userDocuments.get(0);
                                        comment.setAuthorName(userDocument.getString("name"))
                                                .setAuthorAvatarUrl(userDocument.getString("avatarUrl"));
                                        comments.add(comment);
                                    }
                                });
                        fetchUserTasks.add(userTask);

                        // for each comment, we fetch its like documents
                        Task<QuerySnapshot> likeTask = firestore.collection("likes")
                                .whereEqualTo("type", "COMMENT")
                                .whereEqualTo("typeId", comment.getId())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<DocumentSnapshot> likeDocuments = queryDocumentSnapshots.getDocuments();
                                    comment.setLikeCount(likeDocuments.size());
                                    comment.setIsLiked(likeDocuments.stream().anyMatch(document -> Objects.equals(document.getString("userId"), currentUser.getUid())));
                                });
                        fetchLikeTasks.add(likeTask);
                    }

                    List<Task<?>> allTasks = new ArrayList<>();
                    allTasks.addAll(fetchUserTasks);
                    allTasks.addAll(fetchLikeTasks);

                    // then we wait for all of the fetching tasks to finish
                    Tasks.whenAllComplete(allTasks)
                            .addOnSuccessListener(runnable -> {
                                // re-order the comments based on their replying IDs
                                Map<String, List<Comment>> commentMap = new HashMap<>();
                                List<Comment> nonReplyingComments = new ArrayList<>();
                                for (Comment comment :
                                        comments) {
                                    if (comment.getReplyCommentId() == null) {
                                        commentMap.computeIfAbsent(comment.getId(), k -> new ArrayList<>());
                                        nonReplyingComments.add(comment);
                                    } else {
                                        List<Comment> commentChain = commentMap.computeIfAbsent(comment.getReplyCommentId(), k -> new ArrayList<>());
                                        commentChain.add(comment);
                                    }
                                }

                                nonReplyingComments.sort((c1, c2) -> Long.compare(c2.getCreatedAt(), c1.getCreatedAt()));
                                List<Comment> result = new ArrayList<>();
                                for (Comment comment :
                                        nonReplyingComments) {
                                    result.add(comment);
                                    List<Comment> commentChain = commentMap.get(comment.getId());
                                    if (commentChain != null) {
                                        commentChain.sort(Comparator.comparingLong(Comment::getCreatedAt));
                                        result.addAll(commentChain);
                                    }
                                }

                                callback.OnSuccess(result);
                            })
                            .addOnFailureListener(callback::OnFailure);
                })
                .addOnFailureListener(callback::OnFailure);
    }

    @NonNull
    private static List<DocumentSnapshot> tryFilterBlockedUsers(QuerySnapshot commentDocumentSnapshots) {
        List<DocumentSnapshot> commentDocuments = commentDocumentSnapshots.getDocuments();

        DocumentSnapshot currentUserDocument = FirebaseUserService.getCurrentUserDocument();
        if (currentUserDocument != null) {
            List<String> blockedUsers = null;

            try {
                blockedUsers = (List<String>) currentUserDocument.get("blockedUsers");
            } catch (Exception e) {
                // eat exception
            }

            if (blockedUsers != null) {
                List<String> finalBlockedUsers = blockedUsers;
                commentDocuments.removeIf(doc -> finalBlockedUsers.contains(doc.getString("userId")));
            }
        }
        return commentDocuments;
    }

    public static void uploadPinComment(@NonNull Comment comment, UploadCommentServiceCallback callback) {
        if (!comment.isValidComment()) {
            throw new IllegalArgumentException("Comment must have pinId, content and authorId");
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userId", comment.getAuthorId());
        commentData.put("pin", comment.getPinId());
        commentData.put("replyTo", comment.getReplyCommentId());
        commentData.put("content", comment.getContent());
        commentData.put("createdAt", System.currentTimeMillis());

        if (comment.getAttachmentUri() != null) {
            CloudinaryManager.uploadMedia(comment.getAttachmentUri(), "image/gif", new UploadCallback() {
                @Override
                public void onStart(String requestId) {

                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {

                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    String url = (String) resultData.get("secure_url");
                    if (url == null) {
                        callback.OnFailure(new Exception("Failed to upload attachment url"));
                        return;
                    }

                    commentData.put("attachmentUrl", url);
                    String thumbnailUrl = url.replace("/upload/", "/upload/c_thumb,w_200/");
                    commentData.put("attachmentThumbnailUrl", thumbnailUrl);

                    firestore.collection("comments")
                            .add(commentData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("FirebaseCommentService", "added comment successfully, " + documentReference.getId());
                                comment.setId(documentReference.getId());
                            })
                            .addOnFailureListener(callback::OnFailure);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    callback.OnFailure(new Exception(error.getDescription()));
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {

                }
            });
        } else {
            firestore.collection("comments")
                    .add(commentData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("FirebaseCommentService", "added comment successfully, " + documentReference.getId());
                        comment.setId(documentReference.getId());
                    })
                    .addOnFailureListener(callback::OnFailure);
        }
    }

    public static void updateLike(@NonNull String commentId, boolean isLiked, UpdateLikeCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        if (isLiked) {
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("userId", currentUser.getUid());
            likeData.put("type", "COMMENT");
            likeData.put("typeId", commentId);
            likeData.put("createdAt", System.currentTimeMillis());
            firestore.collection("likes")
                    .add(likeData)
                    .addOnSuccessListener(documentReference -> Log.d("FirebaseCommentService", "add like successfully"))
                    .addOnFailureListener(callback::OnFailure);
        } else {
            // fetch existing like
            firestore.collection("likes")
                    .whereEqualTo("userId", currentUser.getUid())
                    .whereEqualTo("type", "COMMENT")
                    .whereEqualTo("typeId", commentId)
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
                    .addOnSuccessListener(aVoid -> Log.d("FirebaseCommentService", "like removed successfully"))
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseCommentService", "like failed to remove: ", e);
                        callback.OnFailure(e);
                    });
        }
    }

    public static void deleteCommentsOfPin(@NonNull String pinId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("comments")
                .whereEqualTo("pin", pinId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                    for (DocumentSnapshot doc : documentSnapshots) {
                        firestore.collection("comments")
                                .document(doc.getId())
                                .delete()
                                .addOnSuccessListener(unused -> Log.e("FirebaseCommentService", String.format(Locale.US, "Deleted comment %s from pin %s", doc.getId(), pinId)))
                                .addOnFailureListener(e -> logExceptionMessage(String.format(Locale.US, "Failed to delete comment %s", pinId), e));
                    }
                })
                .addOnFailureListener(e -> logExceptionMessage(String.format(Locale.US, "Failed to fetch comments of pin %s", pinId), e));
    }

    private static void logExceptionMessage(String message, Exception e) {
        Log.e("FirebaseCommentService", message);
        if (e.getMessage() != null) {
            Log.e("FirebaseCommentService", e.getMessage());
        } else {
            e.printStackTrace();
        }
    }

    public interface GetCommentServiceCallback {
        void OnSuccess(List<Comment> commentList);

        void OnFailure(Exception e);
    }

    public interface UploadCommentServiceCallback {
        void OnFailure(Exception e);
    }

    public interface UpdateLikeCallback {
        void OnFailure(Exception e);
    }
}
