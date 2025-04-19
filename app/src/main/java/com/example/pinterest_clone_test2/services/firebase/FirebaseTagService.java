package com.example.pinterest_clone_test2.services.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.R;
import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.Tag;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class FirebaseTagService {
    private static final String TAG = "FirebaseTagService";
    private static ArrayList<String> FIXED_TAGS;
    private static final int MAX_TAGS_PER_PIN = 6;

    private static final String PREFS_NAME = "TagServicePrefs";
    private static final String FIXED_TAGS_SAVED_KEY = "fixed_tags_saved";

    // Initialize fixed tags list from resources
    public static void initFixedTags(Context context) {
        if (FIXED_TAGS == null) {
            String[] tagArray = {
                    context.getString(R.string.anime).toLowerCase(),
                    context.getString(R.string.art).toLowerCase(),
                    context.getString(R.string.animal).toLowerCase(),
                    context.getString(R.string.photography).toLowerCase(),
                    context.getString(R.string.graphic_design).toLowerCase(),
                    context.getString(R.string.quotes).toLowerCase(),
                    context.getString(R.string.football).toLowerCase(),
                    context.getString(R.string.cars).toLowerCase(),
                    context.getString(R.string.illustration).toLowerCase(),
                    context.getString(R.string.technology).toLowerCase(),
                    context.getString(R.string.celebrity).toLowerCase(),
                    context.getString(R.string.flowers).toLowerCase(),
                    context.getString(R.string.travel).toLowerCase(),
                    context.getString(R.string.food).toLowerCase(),
                    context.getString(R.string.fashion).toLowerCase(),
                    context.getString(R.string.beauty).toLowerCase(),
                    context.getString(R.string.education).toLowerCase(),
                    context.getString(R.string.decor).toLowerCase(),
                    context.getString(R.string.wedding).toLowerCase(),
                    context.getString(R.string.landscape).toLowerCase(),
                    context.getString(R.string.music).toLowerCase(),
                    context.getString(R.string.science).toLowerCase()

            };

            FIXED_TAGS = new ArrayList<>(Arrays.asList(tagArray));
        }
    }

    public static boolean areFixedTagsSaved(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(FIXED_TAGS_SAVED_KEY, false);
    }

    public static void saveFixedTagsIfNeeded(Context context, SaveFixedTagsCallback callback) {
        // Check if fixed tags have already been saved
        if (areFixedTagsSaved(context)) {
            Log.d(TAG, "Fixed tags already saved, skipping");
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        // If not saved yet, proceed with saving
        saveFixedTagsToFirestore(context, new SaveFixedTagsCallback() {
            @Override
            public void onSuccess() {
                // Mark as saved when successful
                markFixedTagsAsSaved(context);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onPartialSuccess(int successCount, List<String> failedTags) {
                if (callback != null) {
                    callback.onPartialSuccess(successCount, failedTags);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public static void markFixedTagsAsSaved(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FIXED_TAGS_SAVED_KEY, true);
        editor.apply();
    }
    //Process tags to prioritize fixed tags and limit to MAX_TAGS_PER_PIN
    public static List<String> processTags(@NonNull List<String> detectedTags) {
        if (detectedTags == null || detectedTags.isEmpty()) {
            return new ArrayList<>();
        }

        // Convert all tags to lowercase
        List<String> lowerDetectedTags = new ArrayList<>();
        for (String tag : detectedTags) {
            if (tag != null && !tag.isEmpty()) {
                lowerDetectedTags.add(tag.toLowerCase().trim());
            }
        }

        // Use LinkedHashSet to preserve order and ensure uniqueness
        Set<String> resultSet = new LinkedHashSet<>();

        //Add fixed tags that exist in the detected tags
        if (FIXED_TAGS != null) {
            for (String detectedTag : lowerDetectedTags) {
                if (FIXED_TAGS.contains(detectedTag)) {
                    resultSet.add(detectedTag);
                    if (resultSet.size() >= MAX_TAGS_PER_PIN) {
                        return new ArrayList<>(resultSet);
                    }
                }
            }
        }

        // Add remaining tags until we reach the limit
        for (String detectedTag : lowerDetectedTags) {
            resultSet.add(detectedTag);
            if (resultSet.size() >= MAX_TAGS_PER_PIN) {
                break;
            }
        }

        return new ArrayList<>(resultSet);
    }


     //Save tags to Firestore, updating count for existing tags
    public static void saveTagsToFirestore(@NonNull List<String> tags, String pinId) {
        if (tags.isEmpty() || pinId == null || pinId.isEmpty()) {
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        for (String tag : tags) {
            final String normalizedTag = tag.toLowerCase().trim();

            firestore.collection("tags")
                    .whereEqualTo("name", normalizedTag)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                        if (documents.isEmpty()) {
                            // Add new tag
                            Map<String, Object> tagData = new HashMap<>();
                            tagData.put("name", normalizedTag);
                            tagData.put("count", 1);
                            tagData.put("createdAt", System.currentTimeMillis());

                            // Initialize pinIds array with the current pin
                            List<String> pinIds = new ArrayList<>();
                            pinIds.add(pinId);
                            tagData.put("pinIds", pinIds);

                            firestore.collection("tags")
                                    .add(tagData)
                                    .addOnSuccessListener(documentReference ->
                                            Log.d(TAG, "New tag added: " + normalizedTag + " with pinId: " + pinId))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error adding tag: " + e.getMessage()));
                        } else {
                            // Update existing tag
                            DocumentSnapshot tagDoc = documents.get(0);
                            Long currentCountLong = tagDoc.getLong("count");
                            int currentCount = currentCountLong != null ? currentCountLong.intValue() : 0;

                            // Get current pinIds list or create a new one
                            List<String> pinIds = (List<String>) tagDoc.get("pinIds");
                            if (pinIds == null) {
                                pinIds = new ArrayList<>();
                            }

                            // add pinId if it's not already in the list
                            if (!pinIds.contains(pinId)) {
                                pinIds.add(pinId);

                                // Update tag document with new pinId and increased count
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("pinIds", pinIds);
                                updates.put("count", currentCount + 1);

                                tagDoc.getReference()
                                        .update(updates)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "Tag updated: " + normalizedTag + ", count: " + (currentCount + 1) + ", added pinId: " + pinId))
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Error updating tag: " + e.getMessage()));
                            } else {
                                Log.d(TAG, "Pin already associated with tag: " + normalizedTag);
                            }
                        }
                    })
                    .addOnFailureListener(e ->{
                        Log.e(TAG, "Error checking tag: " + e.getMessage(), e); // Log stack trace đầy đủ
                        String errorMsg = e.getMessage();
                        if (errorMsg != null) {
                            Log.e(TAG, "Error details: " + errorMsg);
                        }
                        if (e.getCause() != null) {
                            Log.e(TAG, "Error cause: " + e.getCause().getMessage());
                        }
                    });
        }
    }


     // overloaded to calling saveTagsToFirestore with null pinId

    public static void saveTagsToFirestore(@NonNull List<String> tags) {
        saveTagsToFirestore(tags, null);
    }

    public static boolean isFixedTag(String tag) {
        return tag != null && FIXED_TAGS != null && FIXED_TAGS.contains(tag.toLowerCase().trim());
    }

    public static List<String> getFixedTags() {
        return FIXED_TAGS != null ? new ArrayList<>(FIXED_TAGS) : new ArrayList<>();
    }

    public static void getPopularTags(int limit, GetPopularTagsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("tags")
                .orderBy("count", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> tags = new ArrayList<>();
                    List<Tag> tagObjects = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String tagName = doc.getString("name");
                        if (tagName != null) {
                            tags.add(tagName);

                            // Create Tag object for more complete data
                            Long countLong = doc.getLong("count");
                            int count = countLong != null ? countLong.intValue() : 0;

                            Long createdAtLong = doc.getLong("createdAt");
                            long createdAt = createdAtLong != null ? createdAtLong : 0;

                            Tag tagObj = new Tag(doc.getId(), tagName, count, createdAt);
                            tagObjects.add(tagObj);
                        }
                    }

                    callback.onSuccess(tags, tagObjects);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void searchTags(@NonNull String query, int limit, SearchTagsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String queryLower = query.toLowerCase().trim();

        firestore.collection("tags")
                .orderBy("name")
                .startAt(queryLower)
                .endAt(queryLower + "\uf8ff")
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Tag> tags = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String tagName = doc.getString("name");
                        Long countLong = doc.getLong("count");
                        int count = countLong != null ? countLong.intValue() : 0;
                        Long createdAtLong = doc.getLong("createdAt");
                        long createdAt = createdAtLong != null ? createdAtLong : 0;

                        Tag tag = new Tag(doc.getId(), tagName, count, createdAt);
                        tags.add(tag);
                    }

                    callback.onSuccess(tags);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getPinsByTag(String tagName, GetPinsByTagCallback callback) {
        if (tagName == null || tagName.isEmpty()) {
            callback.onFailure(new Exception("Tag name cannot be empty"));
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("tags")
                .whereEqualTo("name", tagName.toLowerCase().trim())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                    if (documents.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    DocumentSnapshot tagDoc = documents.get(0);
                    List<String> pinIds = (List<String>) tagDoc.get("pinIds");

                    if (pinIds == null || pinIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Fetch pins by their IDs
                    FirebasePinService.fetchPinsFromIds(pinIds, new FirebasePinService.OnPinsFetchedFromIdsCallback() {
                        @Override
                        public void onSuccess(List<Pin> pins) {
                            callback.onSuccess(pins);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void removePinFromTag(String tagName, String pinId, UpdateTagCallback callback) {
        if (tagName == null || tagName.isEmpty() || pinId == null || pinId.isEmpty()) {
            callback.onFailure(new Exception("Tag name and pin ID cannot be empty"));
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("tags")
                .whereEqualTo("name", tagName.toLowerCase().trim())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                    if (documents.isEmpty()) {
                        callback.onFailure(new Exception("Tag not found"));
                        return;
                    }

                    DocumentSnapshot tagDoc = documents.get(0);
                    List<String> pinIds = (List<String>) tagDoc.get("pinIds");

                    if (pinIds != null && pinIds.contains(pinId)) {
                        pinIds.remove(pinId);

                        Long currentCountLong = tagDoc.getLong("count");
                        int currentCount = currentCountLong != null ? currentCountLong.intValue() : 0;
                        int newCount = Math.max(0, currentCount - 1);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("pinIds", pinIds);
                        updates.put("count", newCount);

                        tagDoc.getReference()
                                .update(updates)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(callback::onFailure);
                    } else {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface GetPopularTagsCallback {
        void onSuccess(List<String> tagNames, List<Tag> tags);
        void onFailure(Exception e);
    }

    public interface SearchTagsCallback {
        void onSuccess(List<Tag> tags);
        void onFailure(Exception e);
    }

    public interface GetPinsByTagCallback {
        void onSuccess(List<Pin> pins);
        void onFailure(Exception e);
    }

    public interface UpdateTagCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static void saveFixedTagsToFirestore(Context context, SaveFixedTagsCallback callback) {
        // Đảm bảo rằng các tag cố định đã được khởi tạo
        if (FIXED_TAGS == null) {
            initFixedTags(context);
        }

        if (FIXED_TAGS == null || FIXED_TAGS.isEmpty()) {
            if (callback != null) {
                callback.onFailure(new Exception("Các tag cố định chưa được khởi tạo hoặc rỗng"));
            }
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final int[] tagCount = {FIXED_TAGS.size()};
        final int[] successCount = {0};
        final List<String> failedTags = new ArrayList<>();

        for (String tag : FIXED_TAGS) {
            final String normalizedTag = tag.toLowerCase().trim();

            firestore.collection("tags")
                    .whereEqualTo("name", normalizedTag)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                        if (documents.isEmpty()) {
                            // Thêm tag cố định mới
                            Map<String, Object> tagData = new HashMap<>();
                            tagData.put("name", normalizedTag);
                            tagData.put("count", 0);
                            tagData.put("createdAt", System.currentTimeMillis());
                            tagData.put("isFixed", true);
                            tagData.put("pinIds", new ArrayList<String>());

                            firestore.collection("tags")
                                    .add(tagData)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "Đã thêm tag cố định: " + normalizedTag);
                                        synchronized (successCount) {
                                            successCount[0]++;
                                            checkCompletion(tagCount[0], successCount[0], failedTags, callback);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Lỗi khi thêm tag cố định: " + e.getMessage());
                                        synchronized (failedTags) {
                                            failedTags.add(normalizedTag);
                                            checkCompletion(tagCount[0], successCount[0], failedTags, callback);
                                        }
                                    });
                        } else {
                            // Tag đã tồn tại, cập nhật nếu cần
                            DocumentSnapshot tagDoc = documents.get(0);
                            Boolean isFixed = tagDoc.getBoolean("isFixed");

                            if (isFixed == null || !isFixed) {
                                tagDoc.getReference()
                                        .update("isFixed", true)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Đã cập nhật tag thành cố định: " + normalizedTag);
                                            synchronized (successCount) {
                                                successCount[0]++;
                                                checkCompletion(tagCount[0], successCount[0], failedTags, callback);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Lỗi khi cập nhật tag thành cố định: " + e.getMessage());
                                            synchronized (failedTags) {
                                                failedTags.add(normalizedTag);
                                                checkCompletion(tagCount[0], successCount[0], failedTags, callback);
                                            }
                                        });
                            } else {
                                // Đã được đánh dấu là cố định, tính là thành công
                                synchronized (successCount) {
                                    successCount[0]++;
                                    checkCompletion(tagCount[0], successCount[0], failedTags, callback);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi kiểm tra tag: " + e.getMessage());
                        synchronized (failedTags) {
                            failedTags.add(normalizedTag);
                            checkCompletion(tagCount[0], successCount[0], failedTags, callback);
                        }
                    });
        }
    }

    private static void checkCompletion(int totalCount, int successCount, List<String> failedTags,
                                        SaveFixedTagsCallback callback) {
        if (successCount + failedTags.size() >= totalCount && callback != null) {
            if (failedTags.isEmpty()) {
                callback.onSuccess();
            } else {
                callback.onPartialSuccess(successCount, failedTags);
            }
        }
    }

    public interface SaveFixedTagsCallback {
        void onSuccess();
        void onPartialSuccess(int successCount, List<String> failedTags);
        void onFailure(Exception e);
    }
}