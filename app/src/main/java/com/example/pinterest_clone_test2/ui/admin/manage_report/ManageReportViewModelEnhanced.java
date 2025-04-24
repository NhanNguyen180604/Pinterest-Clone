package com.example.pinterest_clone_test2.ui.admin.manage_report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.PriorityReport;
import com.example.pinterest_clone_test2.models.ReportSeverity;
import com.example.pinterest_clone_test2.models.ReportSummary;
import com.example.pinterest_clone_test2.services.firebase.ReportDashboardService;
import com.example.pinterest_clone_test2.services.firebase.ReportDashboardServiceEnhanced;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * ViewModel nâng cao cho quản lý báo cáo với các tính năng mới
 */
public class ManageReportViewModelEnhanced extends ViewModel {
    private static final String TAG = "ManageReportViewModelEnhanced";

    // LiveData cho thống kê báo cáo
    private final MutableLiveData<ReportSummary> reportSummary = new MutableLiveData<>();

    // LiveData cho danh sách báo cáo ưu tiên
    private final MutableLiveData<List<PriorityReport>> priorityReports = new MutableLiveData<>(new ArrayList<>());

    // LiveData cho báo cáo đã lọc theo mức độ nghiêm trọng
    private final MutableLiveData<List<PriorityReport>> filteredReports = new MutableLiveData<>(new ArrayList<>());

    // LiveData cho dữ liệu heatmap
    private final MutableLiveData<int[][]> heatmapData = new MutableLiveData<>();

    // LiveData cho thống kê báo cáo theo ngày trong tuần
    private final MutableLiveData<int[]> dayOfWeekStats = new MutableLiveData<>();

    // LiveData cho trạng thái loading
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // LiveData cho thông báo lỗi
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Khoảng thời gian hiện tại
    private ReportDashboardService.TimeRange currentTimeRange = ReportDashboardService.TimeRange.LAST_WEEK;

    // Bộ lọc mức độ nghiêm trọng
    private EnumSet<ReportSeverity> severityFilters = EnumSet.allOf(ReportSeverity.class);

    // Getters cho LiveData
    public LiveData<ReportSummary> getReportSummary() {
        return reportSummary;
    }

    public LiveData<List<PriorityReport>> getPriorityReports() {
        return priorityReports;
    }

    public LiveData<List<PriorityReport>> getFilteredReports() {
        return filteredReports;
    }

    public LiveData<int[][]> getHeatmapData() {
        return heatmapData;
    }

    public LiveData<int[]> getDayOfWeekStats() {
        return dayOfWeekStats;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public ReportDashboardService.TimeRange getCurrentTimeRange() {
        return currentTimeRange;
    }

    /**
     * Thiết lập khoảng thời gian và làm mới dữ liệu
     */
    public void setCurrentTimeRange(ReportDashboardService.TimeRange timeRange) {
        this.currentTimeRange = timeRange;
        loadDashboardData();
    }

    /**
     * Thiết lập bộ lọc mức độ nghiêm trọng
     */
    public void setSeverityFilters(EnumSet<ReportSeverity> severityFilters) {
        this.severityFilters = severityFilters;
        applyFilters();
    }

    /**
     * Cập nhật trạng thái lọc cho một mức độ nghiêm trọng cụ thể
     */
    public void toggleSeverityFilter(ReportSeverity severity, boolean enabled) {
        if (enabled) {
            severityFilters.add(severity);
        } else {
            severityFilters.remove(severity);
        }
        applyFilters();
    }

    /**
     * Áp dụng các bộ lọc đến danh sách báo cáo
     */
    private void applyFilters() {
        List<PriorityReport> allReports = priorityReports.getValue();
        if (allReports == null) {
            return;
        }

        List<PriorityReport> filtered = allReports.stream()
                .filter(report -> severityFilters.contains(report.getSeverity()))
                .collect(Collectors.toList());

        filteredReports.setValue(filtered);
    }

    /**
     * Tải tất cả dữ liệu cho bảng điều khiển
     */
    public void loadDashboardData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Tải thống kê báo cáo cơ bản
        loadReportSummary();

        // Tải danh sách báo cáo ưu tiên
        loadPriorityReports();

        // Tải dữ liệu heatmap
        loadHeatmapData();

        // Tải thống kê theo ngày trong tuần
        loadDayOfWeekStats();
    }

    /**
     * Tải thống kê báo cáo
     */
    private void loadReportSummary() {
        ReportDashboardService.getReportSummary(currentTimeRange, new ReportDashboardService.GetReportSummaryCallback() {
            @Override
            public void onSuccess(ReportSummary summary) {
                reportSummary.setValue(summary);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Lỗi khi tải thống kê báo cáo: " + e.getMessage());
            }
        });
    }

    /**
     * Tải danh sách báo cáo ưu tiên
     */
    private void loadPriorityReports() {
        ReportDashboardServiceEnhanced.getPriorityReports(currentTimeRange, new ReportDashboardServiceEnhanced.GetPriorityReportsCallback() {
            @Override
            public void onSuccess(List<PriorityReport> reports) {
                priorityReports.setValue(reports);
                applyFilters();
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Lỗi khi tải báo cáo ưu tiên: " + e.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Tải dữ liệu heatmap
     */
    private void loadHeatmapData() {
        ReportDashboardServiceEnhanced.getReportHeatmapData(currentTimeRange, new ReportDashboardServiceEnhanced.GetHeatmapDataCallback() {
            @Override
            public void onSuccess(int[][] data) {
                heatmapData.setValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Lỗi khi tải dữ liệu heatmap: " + e.getMessage());
            }
        });
    }

    /**
     * Tải thống kê theo ngày trong tuần
     */
    private void loadDayOfWeekStats() {
        ReportDashboardServiceEnhanced.getReportsByDayOfWeek(currentTimeRange, new ReportDashboardServiceEnhanced.GetDayOfWeekStatsCallback() {
            @Override
            public void onSuccess(int[] dayOfWeekCounts) {
                dayOfWeekStats.setValue(dayOfWeekCounts);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Lỗi khi tải thống kê theo ngày: " + e.getMessage());
            }
        });
    }

    /**
     * Tính phần trăm so với tổng số
     */
    public int calculatePercentage(int value, int total) {
        return total > 0 ? (value * 100 / total) : 0;
    }

    /**
     * Format phần trăm thay đổi với dấu (+/-)
     */
    public String formatChangePercent(float percent) {
        return (percent >= 0 ? "+" : "") + String.format(Locale.getDefault(), "%.1f%%", percent);
    }
}