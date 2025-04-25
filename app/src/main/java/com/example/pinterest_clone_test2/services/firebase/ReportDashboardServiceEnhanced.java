package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.PriorityReport;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportSeverity;
import com.example.pinterest_clone_test2.models.ReportSummary;
import com.example.pinterest_clone_test2.services.firebase.ReportDashboardService.TimeRange;
import com.example.pinterest_clone_test2.utils.DateRangeHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service mở rộng cho bảng điều khiển báo cáo
 * Cung cấp thêm các phương thức để phân tích báo cáo theo mức độ nghiêm trọng và heatmap
 */
public class ReportDashboardServiceEnhanced {
    private static final String TAG = "ReportDashboardEnhanced";

    /**
     * Lấy danh sách báo cáo ưu tiên cần xử lý
     */
    public static void getPriorityReports(TimeRange timeRange, GetPriorityReportsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Tính toán khoảng thời gian
        Date[] dateRange = DateRangeHelper.calculateDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        final long startTimestamp = startDate.getTime();
        final long endTimestamp = endDate.getTime();

        // Query các báo cáo chưa được kiểm tra - không sử dụng whereGreaterThan và orderBy để tránh cần composite index
        firestore.collection("reports")
                .whereEqualTo("isChecked", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> reportDocs = querySnapshot.getDocuments();

                    // Lọc theo thời gian ở phía client thay vì server
                    reportDocs = reportDocs.stream()
                            .filter(doc -> {
                                Long createdAt = doc.getLong("createdAt");
                                return createdAt != null &&
                                        createdAt >= startTimestamp &&
                                        createdAt <= endTimestamp;
                            })
                            .sorted((doc1, doc2) -> {
                                Long time1 = doc1.getLong("createdAt");
                                Long time2 = doc2.getLong("createdAt");
                                if (time1 == null) return -1;
                                if (time2 == null) return 1;
                                return time1.compareTo(time2); // Sắp xếp theo thời gian tăng dần (cũ nhất trước)
                            })
                            .collect(Collectors.toList());

                    // Nhóm báo cáo theo contentId và type để tính số lượng
                    Map<String, List<DocumentSnapshot>> reportGroups = reportDocs.stream()
                            .collect(Collectors.groupingBy(doc ->
                                    doc.getString("type") + ":" + doc.getString("typeId")));

                    List<PriorityReport> priorityReports = new ArrayList<>();
                    List<Task<?>> tasks = new ArrayList<>();

                    // Xử lý từng nhóm báo cáo
                    for (Map.Entry<String, List<DocumentSnapshot>> entry : reportGroups.entrySet()) {
                        List<DocumentSnapshot> reports = entry.getValue();
                        if (reports.isEmpty()) continue;

                        // Lấy thông tin từ báo cáo đầu tiên
                        DocumentSnapshot firstReport = reports.get(0);
                        String type = firstReport.getString("type");
                        String contentId = firstReport.getString("typeId");

                        // Tạo đối tượng báo cáo ưu tiên
                        PriorityReport priorityReport = new PriorityReport()
                                .setReportId(firstReport.getId())
                                .setReportType("PIN".equals(type) ?
                                        PriorityReport.ReportType.PIN : PriorityReport.ReportType.COMMENT)
                                .setContentId(contentId)
                                .setReportCount(reports.size())
                                .setFirstReportedAt(reports.get(0).getLong("createdAt"))
                                .setLastReportedAt(reports.get(reports.size() - 1).getLong("createdAt"));

                        // Tìm lý do báo cáo phổ biến nhất
                        Map<String, Integer> reasonCounts = new HashMap<>();
                        for (DocumentSnapshot report : reports) {
                            List<Map<String, Object>> reasons = (List<Map<String, Object>>) report.get("reasons");
                            if (reasons != null && !reasons.isEmpty()) {
                                String reasonId = (String) reasons.get(0).get("id");
                                String reasonTitle = (String) reasons.get(0).get("title");
                                reasonCounts.put(reasonId, reasonCounts.getOrDefault(reasonId, 0) + 1);

                                // Lưu tiêu đề của lý do
                                if (priorityReport.getMainReasonId() == null ||
                                        reasonCounts.get(reasonId) > reasonCounts.getOrDefault(priorityReport.getMainReasonId(), 0)) {
                                    priorityReport.setMainReasonId(reasonId);
                                    priorityReport.setMainReasonTitle(reasonTitle);
                                }
                            }
                        }

                        // Xác định mức độ nghiêm trọng
                        if (priorityReport.getMainReasonId() != null) {
                            ReportSeverity severity = ReportSeverity.determineSeverity(
                                    priorityReport.getMainReasonId(), priorityReport.getReportCount());
                            priorityReport.setSeverity(severity);
                        } else {
                            priorityReport.setSeverity(ReportSeverity.LOW);
                        }

                        // Lấy thông tin nội dung (pin hoặc comment)
                        if ("PIN".equals(type)) {
                            Task<DocumentSnapshot> pinTask = firestore.collection("pins")
                                    .document(contentId)
                                    .get()
                                    .addOnSuccessListener(pinDoc -> {
                                        if (pinDoc.exists()) {
                                            priorityReport.setContentTitle(pinDoc.getString("name"))
                                                    .setContentThumbnail(pinDoc.getString("thumbnailUrl"))
                                                    .setAuthorId(pinDoc.getString("userId"));

                                            // Lấy thông tin người tạo pin
                                            String authorId = pinDoc.getString("userId");
                                            if (authorId != null) {
                                                Task<QuerySnapshot> authorTask = firestore.collection("users")
                                                        .whereEqualTo("userId", authorId)
                                                        .limit(1)
                                                        .get()
                                                        .addOnSuccessListener(userDocs -> {
                                                            if (!userDocs.isEmpty()) {
                                                                DocumentSnapshot userDoc = userDocs.getDocuments().get(0);
                                                                priorityReport.setAuthorName(userDoc.getString("name"))
                                                                        .setAuthorAvatar(userDoc.getString("avatarUrl"));
                                                            }
                                                        });
                                                tasks.add(authorTask);
                                            }
                                        }
                                    });
                            tasks.add(pinTask);
                        } else {
                            Task<DocumentSnapshot> commentTask = firestore.collection("comments")
                                    .document(contentId)
                                    .get()
                                    .addOnSuccessListener(commentDoc -> {
                                        if (commentDoc.exists()) {
                                            priorityReport.setContentTitle(commentDoc.getString("content"))
                                                    .setContentThumbnail(commentDoc.getString("attachmentThumbnailUrl"))
                                                    .setAuthorId(commentDoc.getString("userId"));

                                            // Lấy thông tin người tạo comment
                                            String authorId = commentDoc.getString("userId");
                                            if (authorId != null) {
                                                Task<QuerySnapshot> authorTask = firestore.collection("users")
                                                        .whereEqualTo("userId", authorId)
                                                        .limit(1)
                                                        .get()
                                                        .addOnSuccessListener(userDocs -> {
                                                            if (!userDocs.isEmpty()) {
                                                                DocumentSnapshot userDoc = userDocs.getDocuments().get(0);
                                                                priorityReport.setAuthorName(userDoc.getString("name"))
                                                                        .setAuthorAvatar(userDoc.getString("avatarUrl"));
                                                            }
                                                        });
                                                tasks.add(authorTask);
                                            }
                                        }
                                    });
                            tasks.add(commentTask);
                        }

                        priorityReports.add(priorityReport);
                    }

                    // Sắp xếp báo cáo theo độ nghiêm trọng (cao nhất trước)
                    priorityReports.sort((r1, r2) -> {
                        // Sắp xếp theo mức độ nghiêm trọng (giảm dần)
                        int severityCompare = Integer.compare(
                                r2.getSeverity().getValue(),
                                r1.getSeverity().getValue());

                        if (severityCompare != 0) {
                            return severityCompare;
                        }

                        // Nếu cùng mức độ, sắp xếp theo số lượng báo cáo (giảm dần)
                        int countCompare = Integer.compare(r2.getReportCount(), r1.getReportCount());

                        if (countCompare != 0) {
                            return countCompare;
                        }

                        // Nếu cùng số lượng, sắp xếp theo thời gian đầu tiên báo cáo (tăng dần)
                        return Long.compare(r1.getFirstReportedAt(), r2.getFirstReportedAt());
                    });

                    // Chờ tất cả các truy vấn phụ hoàn thành
                    Tasks.whenAllComplete(tasks)
                            .addOnSuccessListener(t -> callback.onSuccess(priorityReports))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Lấy dữ liệu heatmap cho báo cáo theo ngày trong tuần và giờ
     */
    public static void getReportHeatmapData(TimeRange timeRange, GetHeatmapDataCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Tính toán khoảng thời gian
        Date[] dateRange = DateRangeHelper.calculateDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        final long startTimestamp = startDate.getTime();
        final long endTimestamp = endDate.getTime();

        // Query báo cáo - bỏ điều kiện thời gian để tránh cần index
        firestore.collection("reports")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Khởi tạo dữ liệu heatmap [dayOfWeek][hour]
                    int[][] heatmapData = new int[7][24]; // 7 ngày x 24 giờ

                    // Xử lý từng báo cáo và lọc theo thời gian ở client
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Long createdAt = doc.getLong("createdAt");
                        if (createdAt != null && createdAt >= startTimestamp && createdAt <= endTimestamp) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(createdAt);

                            // Lấy ngày trong tuần (0 = Chủ nhật, 1 = Thứ 2, ..., 6 = Thứ 7)
                            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                            // Lấy giờ trong ngày (0-23)
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);

                            // Tăng giá trị tại vị trí tương ứng
                            heatmapData[dayOfWeek][hour]++;
                        }
                    }

                    callback.onSuccess(heatmapData);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Lấy thống kê báo cáo theo ngày trong tuần
     */
    public static void getReportsByDayOfWeek(TimeRange timeRange, GetDayOfWeekStatsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Tính toán khoảng thời gian
        Date[] dateRange = DateRangeHelper.calculateDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        final long startTimestamp = startDate.getTime();
        final long endTimestamp = endDate.getTime();

        // Query báo cáo - bỏ điều kiện thời gian để tránh cần index
        firestore.collection("reports")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Khởi tạo mảng dữ liệu (từ Chủ nhật đến Thứ 7)
                    int[] dayOfWeekCounts = new int[7];

                    // Xử lý từng báo cáo, lọc theo thời gian ở client
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Long createdAt = doc.getLong("createdAt");
                        if (createdAt != null && createdAt >= startTimestamp && createdAt <= endTimestamp) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(createdAt);

                            // Lấy ngày trong tuần (0 = Chủ nhật, 1 = Thứ 2, ..., 6 = Thứ 7)
                            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

                            // Tăng giá trị tương ứng
                            dayOfWeekCounts[dayOfWeek]++;
                        }
                    }

                    callback.onSuccess(dayOfWeekCounts);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Callback cho việc lấy danh sách báo cáo ưu tiên
     */
    public interface GetPriorityReportsCallback {
        void onSuccess(List<PriorityReport> reports);
        void onFailure(Exception e);
    }

    /**
     * Callback cho việc lấy dữ liệu heatmap
     */
    public interface GetHeatmapDataCallback {
        void onSuccess(int[][] heatmapData);
        void onFailure(Exception e);
    }

    /**
     * Callback cho việc lấy thống kê theo ngày trong tuần
     */
    public interface GetDayOfWeekStatsCallback {
        void onSuccess(int[] dayOfWeekCounts);
        void onFailure(Exception e);
    }
}