package com.example.pinterest_clone_test2.services.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pinterest_clone_test2.models.LatestReport;
import com.example.pinterest_clone_test2.models.ReportReason;
import com.example.pinterest_clone_test2.models.ReportSummary;
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
import java.util.stream.Collectors;

public class ReportDashboardService {
    private static final String TAG = "ReportDashboardService";

    /**
     * Lấy thông tin tổng hợp báo cáo theo khoảng thời gian
     */
    public static void getReportSummary(TimeRange timeRange, GetReportSummaryCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Tính toán khoảng thời gian
        Date[] dateRange = calculateDateRange(timeRange);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        Date previousStartDate = dateRange[2];
        Date previousEndDate = dateRange[3];

        long startTimestamp = startDate.getTime();
        long endTimestamp = endDate.getTime();
        long previousStartTimestamp = previousStartDate.getTime();
        long previousEndTimestamp = previousEndDate.getTime();

        // Query tất cả báo cáo trong khoảng thời gian hiện tại
        Task<QuerySnapshot> currentPeriodTask = firestore.collection("reports")
                .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
                .whereLessThanOrEqualTo("createdAt", endTimestamp)
                .get();

        // Query tất cả báo cáo trong khoảng thời gian trước đó để so sánh
        Task<QuerySnapshot> previousPeriodTask = firestore.collection("reports")
                .whereGreaterThanOrEqualTo("createdAt", previousStartTimestamp)
                .whereLessThanOrEqualTo("createdAt", previousEndTimestamp)
                .get();

        // Chờ cả hai query hoàn thành
        Tasks.whenAllComplete(currentPeriodTask, previousPeriodTask)
                .addOnSuccessListener(tasks -> {
                    try {
                        ReportSummary summary = new ReportSummary();

                        // Xử lý dữ liệu kỳ hiện tại
                        if (currentPeriodTask.isSuccessful()) {
                            QuerySnapshot currentSnapshot = currentPeriodTask.getResult();
                            List<DocumentSnapshot> currentReports = currentSnapshot.getDocuments();

                            processCurrentPeriodReports(summary, currentReports, startTimestamp, endTimestamp);
                        }

                        // Xử lý dữ liệu kỳ trước để tính toán phần trăm thay đổi
                        if (previousPeriodTask.isSuccessful()) {
                            QuerySnapshot previousSnapshot = previousPeriodTask.getResult();
                            int previousTotal = previousSnapshot.size();

                            // Tính phần trăm thay đổi
                            if (previousTotal > 0) {
                                float changePercent = ((float) summary.getTotalReports() - previousTotal) / previousTotal * 100;
                                summary.setTotalReportsChangePercent(changePercent);
                            }

                            // TODO: Tính phần trăm thay đổi thời gian xử lý trung bình
                        }

                        callback.onSuccess(summary);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing report summary", e);
                        callback.onFailure(e);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Xử lý dữ liệu báo cáo kỳ hiện tại
     */
    private static void processCurrentPeriodReports(ReportSummary summary, List<DocumentSnapshot> reports,
                                                    long startTimestamp, long endTimestamp) {
        // Tổng số báo cáo
        summary.setTotalReports(reports.size());

        // Đếm theo loại
        int pinReports = 0;
        int commentReports = 0;

        // Đếm theo trạng thái
        int checkedReports = 0;
        int uncheckedReports = 0;

        // Đếm thời gian xử lý trung bình
        long totalProcessingTime = 0;
        int processedReportCount = 0;

        // Đếm theo lý do
        Map<String, Integer> reasonCounts = new HashMap<>();

        // Dữ liệu xu hướng theo ngày
        Map<String, Integer> trendData = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Duyệt qua từng báo cáo
        for (DocumentSnapshot reportDoc : reports) {
            // Phân loại theo loại báo cáo
            String type = reportDoc.getString("type");
            if ("PIN".equals(type)) {
                pinReports++;
            } else if ("COMMENT".equals(type)) {
                commentReports++;
            }

            // Phân loại theo trạng thái
            Boolean isChecked = reportDoc.getBoolean("isChecked");
            if (isChecked != null && isChecked) {
                checkedReports++;

                // Tính thời gian xử lý nếu đã kiểm tra
                Long createdAt = reportDoc.getLong("createdAt");
                Long checkedAt = reportDoc.getLong("checkedAt");
                if (createdAt != null && checkedAt != null) {
                    long processingTime = checkedAt - createdAt;
                    totalProcessingTime += processingTime;
                    processedReportCount++;
                }
            } else {
                uncheckedReports++;
            }

            // Phân loại theo lý do
            List<Map<String, Object>> reasons = (List<Map<String, Object>>) reportDoc.get("reasons");
            if (reasons != null) {
                for (Map<String, Object> reason : reasons) {
                    String reasonId = (String) reason.get("id");
                    if (reasonId != null) {
                        reasonCounts.put(reasonId, reasonCounts.getOrDefault(reasonId, 0) + 1);
                    }
                }
            }

            // Dữ liệu xu hướng theo ngày
            Long createdAt = reportDoc.getLong("createdAt");
            if (createdAt != null) {
                String dateKey = dateFormat.format(new Date(createdAt));
                trendData.put(dateKey, trendData.getOrDefault(dateKey, 0) + 1);
            }
        }

        // Cập nhật summary
        summary.setPinReports(pinReports)
                .setCommentReports(commentReports)
                .setCheckedReports(checkedReports)
                .setUncheckedReports(uncheckedReports)
                .setTotalPendingReports(uncheckedReports)
                .setReportReasonCounts(reasonCounts)
                .setTrendData(trendData);

        // Tính thời gian xử lý trung bình (giờ)
        if (processedReportCount > 0) {
            long avgTimeInHours = (totalProcessingTime / processedReportCount) / (1000 * 60 * 60);
            summary.setAverageProcessingTimeInHours(avgTimeInHours);
        }
    }

    /**
     * Lấy danh sách các báo cáo mới nhất
     */
    public static void getLatestReports(int limit, GetLatestReportsCallback callback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Query báo cáo mới nhất
        firestore.collection("reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> reportDocs = querySnapshot.getDocuments();
                    List<LatestReport> latestReports = new ArrayList<>();
                    List<Task<?>> tasks = new ArrayList<>();

                    for (DocumentSnapshot reportDoc : reportDocs) {
                        String type = reportDoc.getString("type");
                        String contentId = reportDoc.getString("typeId");
                        String reporterId = reportDoc.getString("userId");

                        LatestReport report = new LatestReport()
                                .setReportId(reportDoc.getId())
                                .setReportType("PIN".equals(type) ? LatestReport.ReportType.PIN : LatestReport.ReportType.COMMENT)
                                .setContentId(contentId)
                                .setReporterId(reporterId)
                                .setReportDate(reportDoc.getLong("createdAt"))
                                .setChecked(Boolean.TRUE.equals(reportDoc.getBoolean("isChecked")));

                        // Xử lý lý do báo cáo
                        List<Map<String, Object>> reasons = (List<Map<String, Object>>) reportDoc.get("reasons");
                        if (reasons != null && !reasons.isEmpty()) {
                            report.setReportReason((String) reasons.get(0).get("title"));
                        }

                        // Lấy thông tin nội dung
                        if ("PIN".equals(type)) {
                            Task<DocumentSnapshot> pinTask = firestore.collection("pins")
                                    .document(contentId)
                                    .get()
                                    .addOnSuccessListener(pinDoc -> {
                                        if (pinDoc.exists()) {
                                            report.setContentTitle(pinDoc.getString("name"))
                                                    .setContentDescription(pinDoc.getString("description"))
                                                    .setContentThumbnail(pinDoc.getString("thumbnailUrl"));
                                        }
                                    });
                            tasks.add(pinTask);
                        } else {
                            Task<DocumentSnapshot> commentTask = firestore.collection("comments")
                                    .document(contentId)
                                    .get()
                                    .addOnSuccessListener(commentDoc -> {
                                        if (commentDoc.exists()) {
                                            report.setContentTitle(commentDoc.getString("content"))
                                                    .setContentThumbnail(commentDoc.getString("attachmentThumbnailUrl"));
                                        }
                                    });
                            tasks.add(commentTask);
                        }

                        // Lấy thông tin người báo cáo
                        Task<QuerySnapshot> reporterTask = firestore.collection("users")
                                .whereEqualTo("userId", reporterId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(userDocs -> {
                                    if (!userDocs.isEmpty()) {
                                        DocumentSnapshot userDoc = userDocs.getDocuments().get(0);
                                        report.setReporterName(userDoc.getString("name"))
                                                .setReporterAvatar(userDoc.getString("avatarUrl"));
                                    }
                                });
                        tasks.add(reporterTask);

                        latestReports.add(report);
                    }

                    // Chờ tất cả các truy vấn phụ hoàn thành
                    Tasks.whenAllComplete(tasks)
                            .addOnSuccessListener(t -> callback.onSuccess(latestReports))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Tính toán khoảng thời gian dựa trên TimeRange
     * Trả về mảng [startDate, endDate, previousStartDate, previousEndDate]
     */
    private static Date[] calculateDateRange(TimeRange timeRange) {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime(); // Thời điểm hiện tại

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date startDate;
        Date previousStartDate;
        Date previousEndDate;

        switch (timeRange) {
            case LAST_WEEK:
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                startDate = calendar.getTime();

                // Khoảng thời gian trước đó (tuần trước nữa)
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;

            case LAST_MONTH:
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();

                // Khoảng thời gian trước đó (tháng trước nữa)
                calendar.add(Calendar.MONTH, -1);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.MONTH, 1);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;

            case LAST_QUARTER:
                calendar.add(Calendar.MONTH, -3);
                startDate = calendar.getTime();

                // Khoảng thời gian trước đó (quý trước nữa)
                calendar.add(Calendar.MONTH, -3);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.MONTH, 3);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;

            case LAST_YEAR:
                calendar.add(Calendar.YEAR, -1);
                startDate = calendar.getTime();

                // Khoảng thời gian trước đó (năm trước nữa)
                calendar.add(Calendar.YEAR, -1);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.YEAR, 1);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;

            case TODAY:
            default:
                startDate = calendar.getTime(); // Đầu ngày hôm nay

                // Khoảng thời gian trước đó (hôm qua)
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                previousStartDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                previousEndDate = new Date(startDate.getTime() - 1);
                break;
        }

        return new Date[]{startDate, endDate, previousStartDate, previousEndDate};
    }

    /**
     * Các khoảng thời gian thống kê
     */
    public enum TimeRange {
        TODAY,
        LAST_WEEK,
        LAST_MONTH,
        LAST_QUARTER,
        LAST_YEAR
    }

    /**
     * Callback cho việc lấy thông tin tổng hợp báo cáo
     */
    public interface GetReportSummaryCallback {
        void onSuccess(ReportSummary summary);
        void onFailure(Exception e);
    }

    /**
     * Callback cho việc lấy danh sách báo cáo mới nhất
     */
    public interface GetLatestReportsCallback {
        void onSuccess(List<LatestReport> reports);
        void onFailure(Exception e);
    }
}