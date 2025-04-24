package com.example.pinterest_clone_test2.ui.admin.manage_pin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.Pin;
import com.example.pinterest_clone_test2.models.ReportedPin;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ManagePinViewModel extends ViewModel {
    private static final String TAG = "ManagePinViewModel";

    // LiveData cho danh sách báo cáo pin
    private final MutableLiveData<List<ReportedPin>> reportedPins = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Lưu trữ dữ liệu gốc (để phục vụ việc filter mà không cần query lại)
    private List<ReportedPin> originalReportedPins = new ArrayList<>();

    // Biến để lưu trạng thái filter và sort
    private SortOption currentSortOption = SortOption.LAST_REPORTED_DESC;
    private FilterOption currentFilterOption = FilterOption.ALL;
    private String searchQuery = "";
    private Date filterStartDate = null;
    private Date filterEndDate = null;
    private String filterReasonId = null;
    private Pin.PinType filterPinType = null;

    // Enum cho các tùy chọn sắp xếp
    public enum SortOption {
        LAST_REPORTED_ASC,
        LAST_REPORTED_DESC,
        PIN_CREATED_ASC,
        PIN_CREATED_DESC,
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
    public LiveData<List<ReportedPin>> getReportedPins() {
        return reportedPins;
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

    public Pin.PinType getFilterPinType() {
        return filterPinType;
    }

    public void setFilterPinType(Pin.PinType pinType) {
        this.filterPinType = pinType;
        applyFiltersAndSort();
    }

    /**
     * Lấy danh sách báo cáo pin từ Firestore
     */
    public void fetchReportedPins() {
        isLoading.setValue(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Lấy tất cả báo cáo có type là "PIN"
        firestore.collection("reports")
                .whereEqualTo("type", "PIN")
                .get()
                .addOnSuccessListener(reportQuerySnapshot -> {
                    // Tạo map để lưu trữ thông tin pinId -> danh sách báo cáo
                    Map<String, List<DocumentSnapshot>> pinReportsMap = new HashMap<>();

                    // Nhóm báo cáo theo pinId
                    for (DocumentSnapshot reportDoc : reportQuerySnapshot.getDocuments()) {
                        String pinId = reportDoc.getString("typeId");
                        if (pinId != null) {
                            List<DocumentSnapshot> reports = pinReportsMap.getOrDefault(pinId, new ArrayList<>());
                            reports.add(reportDoc);
                            pinReportsMap.put(pinId, reports);
                        }
                    }

                    // Lấy thông tin pin cho từng pinId
                    List<ReportedPin> reportedPinsList = new ArrayList<>();
                    List<Task<Void>> tasks = new ArrayList<>();

                    for (Map.Entry<String, List<DocumentSnapshot>> entry : pinReportsMap.entrySet()) {
                        String pinId = entry.getKey();
                        List<DocumentSnapshot> reportDocs = entry.getValue();

                        // Tạo task để lấy thông tin pin
                        Task<Void> task = firestore.collection("pins")
                                .document(pinId)
                                .get()
                                .continueWith(pinTask -> {
                                    if (pinTask.isSuccessful() && pinTask.getResult().exists()) {
                                        DocumentSnapshot pinDoc = pinTask.getResult();

                                        ReportedPin reportedPin = processPinAndReports(pinDoc, reportDocs);
                                        if (reportedPin != null) {
                                            reportedPinsList.add(reportedPin);
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
                                originalReportedPins = new ArrayList<>(reportedPinsList);
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
     * Xử lý thông tin pin và các báo cáo của nó để tạo đối tượng ReportedPin
     */
    private ReportedPin processPinAndReports(DocumentSnapshot pinDoc, List<DocumentSnapshot> reportDocs) {
        if (pinDoc == null || !pinDoc.exists()) {
            return null;
        }

        ReportedPin reportedPin = new ReportedPin()
                .setPinId(pinDoc.getId())
                .setPinTitle(pinDoc.getString("name"))
                .setPinDescription(pinDoc.getString("description"))
                .setPinAuthorId(pinDoc.getString("authorId"))
                .setMediaUrl(pinDoc.getString("mediaUrl"))
                .setThumbnailUrl(pinDoc.getString("thumbnailUrl"))
                .setPinType(pinDoc.get("type", Pin.PinType.class));

        Long createdAt = pinDoc.getLong("createdAt");
        if (createdAt != null) {
            reportedPin.setPinCreatedAt(createdAt);
        }

        // Lấy thông tin người dùng
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("userId", reportedPin.getPinAuthorId())
                .limit(1)
                .get()
                .addOnSuccessListener(userQuerySnapshot -> {
                    if (!userQuerySnapshot.isEmpty()) {
                        DocumentSnapshot userDoc = userQuerySnapshot.getDocuments().get(0);
                        reportedPin
                                .setPinAuthorName(userDoc.getString("name"))
                                .setPinAuthorAvatar(userDoc.getString("avatarUrl"));

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
                reportedPin.setReportId(reportId);
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
        reportedPin
                .setReportCount(reporterIds.size())
                .setChecked(isChecked)
                .setLastReportedAt(lastReportedAt)
                .setReasonsCount(reasonsCount)
                .setReporterIds(reporterIds)
                .setMostCommonReasonId(mostCommonReasonId)
                .setMostCommonReasonTitle(mostCommonReasonTitle);

        return reportedPin;
    }

    /**
     * Đánh dấu báo cáo pin đã được xử lý
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
                    for (ReportedPin pin : originalReportedPins) {
                        if (reportId.equals(pin.getReportId())) {
                            pin.setChecked(true);
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
     * Xóa pin bị báo cáo
     */
    public void deletePin(String pinId, Consumer<Boolean> callback) {
        if (pinId == null || pinId.isEmpty()) {
            callback.accept(false);
            return;
        }

        isLoading.setValue(true);
        FirebaseFirestore.getInstance().collection("pins")
                .document(pinId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Tiếp tục đánh dấu báo cáo đã xử lý
                    FirebaseFirestore.getInstance().collection("reports")
                            .whereEqualTo("type", "PIN")
                            .whereEqualTo("typeId", pinId)
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
                                            originalReportedPins.removeIf(pin -> pinId.equals(pin.getPinId()));
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
                    errorMessage.setValue("Lỗi khi xóa pin: " + e.getMessage());
                    callback.accept(false);
                });
    }

    /**
     * Gửi thông báo cho người báo cáo và người bị báo cáo
     */
    public void sendNotifications(ReportedPin pin, boolean isViolation, Consumer<Boolean> callback) {
        if (pin == null) {
            callback.accept(false);
            return;
        }

        isLoading.setValue(true);
        List<Task<Void>> notificationTasks = new ArrayList<>();

        // Thông báo cho người báo cáo
        for (String reporterId : pin.getReporterIds()) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", reporterId);
            notification.put("type", "REPORT_PROCESSED");
            notification.put("pinId", pin.getPinId());
            notification.put("createdAt", System.currentTimeMillis());
            notification.put("isViolation", isViolation);
            notification.put("reasonTitle", pin.getMostCommonReasonTitle());

            Task<Void> task = FirebaseFirestore.getInstance().collection("notifications")
                    .add(notification)
                    .continueWith(documentReferenceTask -> null);

            notificationTasks.add(task);
        }

        // Thông báo cho người bị báo cáo
        if (isViolation && pin.getPinAuthorId() != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", pin.getPinAuthorId());
            notification.put("type", "PIN_REMOVED");
            notification.put("pinId", pin.getPinId());
            notification.put("createdAt", System.currentTimeMillis());
            notification.put("reasonTitle", pin.getMostCommonReasonTitle());

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
        List<ReportedPin> filteredList = new ArrayList<>(originalReportedPins);

        // Áp dụng filter
        filteredList = filteredList.stream()
                .filter(this::filterByStatus)
                .filter(this::filterByDate)
                .filter(this::filterByReason)
                .filter(this::filterByPinType)
                .filter(this::filterBySearchQuery)
                .collect(Collectors.toList());

        // Áp dụng sort
        filteredList.sort((p1, p2) -> {
            switch (currentSortOption) {
                case LAST_REPORTED_ASC:
                    return Long.compare(p1.getLastReportedAt(), p2.getLastReportedAt());
                case LAST_REPORTED_DESC:
                    return Long.compare(p2.getLastReportedAt(), p1.getLastReportedAt());
                case PIN_CREATED_ASC:
                    return Long.compare(p1.getPinCreatedAt(), p2.getPinCreatedAt());
                case PIN_CREATED_DESC:
                    return Long.compare(p2.getPinCreatedAt(), p1.getPinCreatedAt());
                case REPORT_COUNT_ASC:
                    return Integer.compare(p1.getReportCount(), p2.getReportCount());
                case REPORT_COUNT_DESC:
                    return Integer.compare(p2.getReportCount(), p1.getReportCount());
                default:
                    return 0;
            }
        });

        reportedPins.setValue(filteredList);
    }

    /**
     * Lọc theo trạng thái đã kiểm tra/chưa kiểm tra
     */
    private boolean filterByStatus(ReportedPin pin) {
        switch (currentFilterOption) {
            case CHECKED:
                return pin.isChecked();
            case UNCHECKED:
                return !pin.isChecked();
            case ALL:
            default:
                return true;
        }
    }

    /**
     * Lọc theo khoảng thời gian
     */
    private boolean filterByDate(ReportedPin pin) {
        if (filterStartDate == null && filterEndDate == null) {
            return true;
        }

        long pinTime = pin.getLastReportedAt();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(pinTime);
        Date pinDate = calendar.getTime();

        if (filterStartDate != null && filterEndDate != null) {
            return !pinDate.before(filterStartDate) && !pinDate.after(filterEndDate);
        } else if (filterStartDate != null) {
            return !pinDate.before(filterStartDate);
        } else {
            return !pinDate.after(filterEndDate);
        }
    }

    /**
     * Lọc theo lý do báo cáo
     */
    private boolean filterByReason(ReportedPin pin) {
        if (filterReasonId == null || filterReasonId.isEmpty()) {
            return true;
        }

        Map<String, Integer> reasonsCount = pin.getReasonsCount();
        return reasonsCount.containsKey(filterReasonId);
    }

    /**
     * Lọc theo loại Pin
     */
    private boolean filterByPinType(ReportedPin pin) {
        if (filterPinType == null) {
            return true;
        }

        return pin.getPinType() == filterPinType;
    }

    /**
     * Lọc theo từ khóa tìm kiếm
     */
    private boolean filterBySearchQuery(ReportedPin pin) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return true;
        }

        String query = searchQuery.toLowerCase();
        return (pin.getPinTitle() != null && pin.getPinTitle().toLowerCase().contains(query)) ||
                (pin.getPinDescription() != null && pin.getPinDescription().toLowerCase().contains(query)) ||
                (pin.getPinAuthorName() != null && pin.getPinAuthorName().toLowerCase().contains(query));
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
        filterPinType = null;
        applyFiltersAndSort();
    }
}