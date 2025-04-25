package com.example.pinterest_clone_test2.ui.admin.manage_comment;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Comment;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportedComment;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ManageCommentViewModel extends ViewModel {
    private static final String TAG = "ManageCommentViewModel";

    // LiveData cho danh sách báo cáo comment
    private final MutableLiveData<List<ReportedComment>> reportedComments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Lưu trữ dữ liệu gốc (để phục vụ việc filter mà không cần query lại)
    private List<ReportedComment> originalReportedComments = new ArrayList<>();

    // Biến để lưu trạng thái filter và sort
    private SortOption currentSortOption = SortOption.LAST_REPORTED_DESC;
    private FilterOption currentFilterOption = FilterOption.ALL;
    private String searchQuery = "";
    private Date filterStartDate = null;
    private Date filterEndDate = null;
    private String filterReasonId = null;

    // Enum cho các tùy chọn sắp xếp
    public enum SortOption {
        LAST_REPORTED_ASC,
        LAST_REPORTED_DESC,
        COMMENT_CREATED_ASC,
        COMMENT_CREATED_DESC,
        REPORT_COUNT_ASC,
        REPORT_COUNT_DESC
    }

    // Enum cho các tùy chọn lọc
    public enum FilterOption {
        ALL,
        CHECKED,
        UNCHECKED
    }

    // Getters cho LiveData
    public LiveData<List<ReportedComment>> getReportedComments() {
        return reportedComments;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Getter và Setter cho filter và sort options
    public SortOption getCurrentSortOption() {
        return currentSortOption;
    }

    public void setCurrentSortOption(SortOption option) {
        this.currentSortOption = option;
        applyFiltersAndSort();
    }

    public FilterOption getCurrentFilterOption() {
        return currentFilterOption;
    }

    public void setCurrentFilterOption(FilterOption option) {
        this.currentFilterOption = option;
        applyFiltersAndSort();
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query;
        applyFiltersAndSort();
    }

    public Date getFilterStartDate() {
        return filterStartDate;
    }

    public void setFilterStartDate(Date date) {
        this.filterStartDate = date;
        applyFiltersAndSort();
    }

    public Date getFilterEndDate() {
        return filterEndDate;
    }

    public void setFilterEndDate(Date date) {
        this.filterEndDate = date;
        applyFiltersAndSort();
    }

    public String getFilterReasonId() {
        return filterReasonId;
    }

    public void setFilterReasonId(String reasonId) {
        this.filterReasonId = reasonId;
        applyFiltersAndSort();
    }

    /**
     * Lấy danh sách báo cáo comment từ Firestore
     */
    public void fetchReportedComments() {
        isLoading.setValue(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Lấy tất cả báo cáo có type là "COMMENT"
        firestore.collection("reports")
                .whereEqualTo("type", "COMMENT")
                .get()
                .addOnSuccessListener(reportQuerySnapshot -> {
                    // Tạo map để lưu trữ thông tin commentId -> danh sách báo cáo
                    Map<String, List<DocumentSnapshot>> commentReportsMap = new HashMap<>();

                    // Nhóm báo cáo theo commentId
                    for (DocumentSnapshot reportDoc : reportQuerySnapshot.getDocuments()) {
                        String commentId = reportDoc.getString("typeId");
                        if (commentId != null) {
                            List<DocumentSnapshot> reports = commentReportsMap.getOrDefault(commentId, new ArrayList<>());
                            reports.add(reportDoc);
                            commentReportsMap.put(commentId, reports);
                        }
                    }

                    // Lấy thông tin comment cho từng commentId
                    List<ReportedComment> reportedCommentsList = new ArrayList<>();
                    List<Task<Void>> tasks = new ArrayList<>();

                    for (Map.Entry<String, List<DocumentSnapshot>> entry : commentReportsMap.entrySet()) {
                        String commentId = entry.getKey();
                        List<DocumentSnapshot> reportDocs = entry.getValue();

                        // Tạo task để lấy thông tin comment
                        Task<Void> task = firestore.collection("comments")
                                .document(commentId)
                                .get()
                                .continueWith(commentTask -> {
                                    if (commentTask.isSuccessful() && commentTask.getResult().exists()) {
                                        DocumentSnapshot commentDoc = commentTask.getResult();

                                        ReportedComment reportedComment = processCommentAndReports(commentDoc, reportDocs);
                                        if (reportedComment != null) {
                                            reportedCommentsList.add(reportedComment);
                                        }
                                    }
                                    return null;
                                });

                        tasks.add(task);
                    }

                    // Đợi tất cả các task hoàn thành
                    Tasks.whenAllComplete(tasks)
                            .addOnSuccessListener(voids -> {
                                isLoading.setValue(false);
                                originalReportedComments = new ArrayList<>(reportedCommentsList);
                                applyFiltersAndSort();
                            })
                            .addOnFailureListener(e -> {
                                isLoading.setValue(false);
                                errorMessage.setValue("Lỗi khi tải dữ liệu: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Lỗi khi tải dữ liệu: " + e.getMessage());
                });
    }

    /**
     * Xử lý thông tin comment và các báo cáo của nó để tạo đối tượng ReportedComment
     */
    private ReportedComment processCommentAndReports(DocumentSnapshot commentDoc, List<DocumentSnapshot> reportDocs) {
        if (commentDoc == null || !commentDoc.exists()) {
            return null;
        }

        ReportedComment reportedComment = new ReportedComment()
                .setCommentId(commentDoc.getId())
                .setCommentContent(commentDoc.getString("content"))
                .setCommentAuthorId(commentDoc.getString("userId"))
                .setAttachmentUrl(commentDoc.getString("attachmentUrl"))
                .setPinId(commentDoc.getString("pin"));

        Long createdAt = commentDoc.getLong("createdAt");
        if (createdAt != null) {
            reportedComment.setCommentCreatedAt(createdAt);
        }

        // Lấy thông tin người dùng
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("userId", reportedComment.getCommentAuthorId())
                .limit(1)
                .get()
                .addOnSuccessListener(userQuerySnapshot -> {
                    if (!userQuerySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = userQuerySnapshot.getDocuments().get(0);
                        reportedComment
                                .setCommentAuthorName(userDoc.getString("name"))
                                .setCommentAuthorAvatar(userDoc.getString("avatarUrl"));

                        // Cập nhật LiveData
                        applyFiltersAndSort();
                    }
                });

        // Xử lý các báo cáo
        long lastReportedAt = 0;
        boolean isChecked = true;
        Map<String, Integer> reasonsCount = new HashMap<>();
        List<String> reporterIds = new ArrayList<>();

        for (DocumentSnapshot reportDoc : reportDocs) {
            String reportId = reportDoc.getId();
            String reporterId = reportDoc.getString("userId");
            Long reportCreatedAt = reportDoc.getLong("createdAt");
            Boolean reportChecked = reportDoc.getBoolean("isChecked");

            if (reportId != null && reportDoc.getId().equals(reportDocs.get(0).getId())) {
                reportedComment.setReportId(reportId);
            }

            // Cập nhật trạng thái đã kiểm tra
            if (reportChecked != null && !reportChecked) {
                isChecked = false;
            }

            // Cập nhật thời gian báo cáo gần nhất
            if (reportCreatedAt != null && reportCreatedAt > lastReportedAt) {
                lastReportedAt = reportCreatedAt;
            }

            // Thêm người báo cáo
            if (reporterId != null && !reporterIds.contains(reporterId)) {
                reporterIds.add(reporterId);
            }

            // Xử lý lý do báo cáo
            List<Map<String, Object>> reasons = (List<Map<String, Object>>) reportDoc.get("reasons");
            if (reasons != null) {
                for (Map<String, Object> reason : reasons) {
                    String reasonId = (String) reason.get("id");
                    if (reasonId != null) {
                        Integer count = reasonsCount.getOrDefault(reasonId, 0);
                        reasonsCount.put(reasonId, count + 1);
                    }
                }
            }
        }

        // Tìm lý do phổ biến nhất
        String mostCommonReasonId = "";
        String mostCommonReasonTitle = "";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : reasonsCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonReasonId = entry.getKey();
            }
        }

        // Lấy tiêu đề của lý do phổ biến nhất
        for (DocumentSnapshot reportDoc : reportDocs) {
            List<Map<String, Object>> reasons = (List<Map<String, Object>>) reportDoc.get("reasons");
            if (reasons != null) {
                for (Map<String, Object> reason : reasons) {
                    String reasonId = (String) reason.get("id");
                    if (reasonId != null && reasonId.equals(mostCommonReasonId)) {
                        mostCommonReasonTitle = (String) reason.get("title");
                        break;
                    }
                }
                if (!mostCommonReasonTitle.isEmpty()) {
                    break;
                }
            }
        }

        // Cập nhật thông tin báo cáo
        reportedComment
                .setReportCount(reporterIds.size())
                .setChecked(isChecked)
                .setLastReportedAt(lastReportedAt)
                .setReasonsCount(reasonsCount)
                .setReporterIds(reporterIds)
                .setMostCommonReasonId(mostCommonReasonId)
                .setMostCommonReasonTitle(mostCommonReasonTitle);

        return reportedComment;
    }

    /**
     * Đánh dấu báo cáo comment đã được xử lý
     */
    public void markReportAsChecked(String reportId, Consumer<Boolean> callback) {
        if (reportId == null || reportId.isEmpty()) {
            callback.accept(false);
            return;
        }

        isLoading.setValue(true);
        FirebaseFirestore.getInstance().collection("reports")
                .document(reportId)
                .update("isChecked", true)
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    callback.accept(true);

                    // Cập nhật local data
                    for (ReportedComment comment : originalReportedComments) {
                        if (reportId.equals(comment.getReportId())) {
                            comment.setChecked(true);
                            break;
                        }
                    }
                    applyFiltersAndSort();
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Lỗi khi đánh dấu báo cáo: " + e.getMessage());
                    callback.accept(false);
                });
    }

    /**
     * Xóa comment bị báo cáo
     */
    public void deleteComment(String commentId, Consumer<Boolean> callback) {
        if (commentId == null || commentId.isEmpty()) {
            callback.accept(false);
            return;
        }

        isLoading.setValue(true);
        FirebaseFirestore.getInstance().collection("comments")
                .document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Tiếp tục đánh dấu báo cáo đã xử lý
                    FirebaseFirestore.getInstance().collection("reports")
                            .whereEqualTo("type", "COMMENT")
                            .whereEqualTo("typeId", commentId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<Task<Void>> markTasks = new ArrayList<>();

                                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                    Task<Void> task = doc.getReference().update("isChecked", true);
                                    markTasks.add(task);
                                }

                                Tasks.whenAllComplete(markTasks)
                                        .addOnSuccessListener(voids -> {
                                            isLoading.setValue(false);
                                            callback.accept(true);

                                            // Cập nhật local data
                                            originalReportedComments.removeIf(comment -> commentId.equals(comment.getCommentId()));
                                            applyFiltersAndSort();
                                        })
                                        .addOnFailureListener(e -> {
                                            isLoading.setValue(false);
                                            errorMessage.setValue("Lỗi khi đánh dấu báo cáo: " + e.getMessage());
                                            callback.accept(false);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                isLoading.setValue(false);
                                errorMessage.setValue("Lỗi khi tìm báo cáo: " + e.getMessage());
                                callback.accept(false);
                            });
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Lỗi khi xóa comment: " + e.getMessage());
                    callback.accept(false);
                });
    }

    /**
     * Gửi thông báo cho người báo cáo và người bị báo cáo
     */
    public void sendNotifications(ReportedComment comment, boolean isViolation, Consumer<Boolean> callback) {
        if (comment == null) {
            callback.accept(false);
            return;
        }

        isLoading.setValue(true);
        List<Task<Void>> notificationTasks = new ArrayList<>();

        // Thông báo cho người báo cáo
        for (String reporterId : comment.getReporterIds()) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", reporterId);
            notification.put("type", "REPORT_PROCESSED");
            notification.put("commentId", comment.getCommentId());
            notification.put("createdAt", System.currentTimeMillis());
            notification.put("isViolation", isViolation);
            notification.put("reasonTitle", comment.getMostCommonReasonTitle());

            Task<Void> task = FirebaseFirestore.getInstance().collection("notifications")
                    .add(notification)
                    .continueWith(documentReferenceTask -> null);

            notificationTasks.add(task);
        }

        // Thông báo cho người bị báo cáo
        if (isViolation && comment.getCommentAuthorId() != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", comment.getCommentAuthorId());
            notification.put("type", "COMMENT_REMOVED");
            notification.put("commentId", comment.getCommentId());
            notification.put("createdAt", System.currentTimeMillis());
            notification.put("reasonTitle", comment.getMostCommonReasonTitle());

            Task<Void> task = FirebaseFirestore.getInstance().collection("notifications")
                    .add(notification)
                    .continueWith(documentReferenceTask -> null);

            notificationTasks.add(task);
        }

        Tasks.whenAllComplete(notificationTasks)
                .addOnSuccessListener(voids -> {
                    isLoading.setValue(false);
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Lỗi khi gửi thông báo: " + e.getMessage());
                    callback.accept(false);
                });
    }

    /**
     * Áp dụng filter và sắp xếp cho danh sách báo cáo
     */
    private void applyFiltersAndSort() {
        List<ReportedComment> filteredList = new ArrayList<>(originalReportedComments);

        // Áp dụng filter
        filteredList = filteredList.stream()
                .filter(this::filterByStatus)
                .filter(this::filterByDate)
                .filter(this::filterByReason)
                .filter(this::filterBySearchQuery)
                .collect(Collectors.toList());

        // Áp dụng sort
        filteredList.sort((c1, c2) -> {
            switch (currentSortOption) {
                case LAST_REPORTED_ASC:
                    return Long.compare(c1.getLastReportedAt(), c2.getLastReportedAt());
                case LAST_REPORTED_DESC:
                    return Long.compare(c2.getLastReportedAt(), c1.getLastReportedAt());
                case COMMENT_CREATED_ASC:
                    return Long.compare(c1.getCommentCreatedAt(), c2.getCommentCreatedAt());
                case COMMENT_CREATED_DESC:
                    return Long.compare(c2.getCommentCreatedAt(), c1.getCommentCreatedAt());
                case REPORT_COUNT_ASC:
                    return Integer.compare(c1.getReportCount(), c2.getReportCount());
                case REPORT_COUNT_DESC:
                    return Integer.compare(c2.getReportCount(), c1.getReportCount());
                default:
                    return 0;
            }
        });

        reportedComments.setValue(filteredList);
    }

    /**
     * Lọc theo trạng thái đã kiểm tra/chưa kiểm tra
     */
    private boolean filterByStatus(ReportedComment comment) {
        switch (currentFilterOption) {
            case CHECKED:
                return comment.isChecked();
            case UNCHECKED:
                return !comment.isChecked();
            case ALL:
            default:
                return true;
        }
    }

    /**
     * Lọc theo khoảng thời gian
     */
    private boolean filterByDate(ReportedComment comment) {
        if (filterStartDate == null && filterEndDate == null) {
            return true;
        }

        long commentTime = comment.getLastReportedAt();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(commentTime);
        Date commentDate = calendar.getTime();

        if (filterStartDate != null && filterEndDate != null) {
            return !commentDate.before(filterStartDate) && !commentDate.after(filterEndDate);
        } else if (filterStartDate != null) {
            return !commentDate.before(filterStartDate);
        } else {
            return !commentDate.after(filterEndDate);
        }
    }

    /**
     * Lọc theo lý do báo cáo
     */
    private boolean filterByReason(ReportedComment comment) {
        if (filterReasonId == null || filterReasonId.isEmpty()) {
            return true;
        }

        Map<String, Integer> reasonsCount = comment.getReasonsCount();
        return reasonsCount.containsKey(filterReasonId);
    }

    /**
     * Lọc theo từ khóa tìm kiếm
     */
    private boolean filterBySearchQuery(ReportedComment comment) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return true;
        }

        String query = searchQuery.toLowerCase();
        return (comment.getCommentContent() != null && comment.getCommentContent().toLowerCase().contains(query)) ||
                (comment.getCommentAuthorName() != null && comment.getCommentAuthorName().toLowerCase().contains(query));
    }

    /**
     * Reset tất cả các bộ lọc
     */
    public void resetFilters() {
        currentFilterOption = FilterOption.ALL;
        searchQuery = "";
        filterStartDate = null;
        filterEndDate = null;
        filterReasonId = null;
        applyFiltersAndSort();
    }
}