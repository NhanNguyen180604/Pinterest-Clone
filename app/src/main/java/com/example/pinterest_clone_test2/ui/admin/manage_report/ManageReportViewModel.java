package com.example.pinterest_clone_test2.ui.admin.manage_report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pinterest_clone_test2.models.LatestReport;
import com.example.pinterest_clone_test2.models.ReportSummary;
import com.example.pinterest_clone_test2.services.firebase.ReportDashboardService;

import java.util.ArrayList;
import java.util.List;

public class ManageReportViewModel extends ViewModel {
    private static final String TAG = "ManageReportViewModel";

    // LiveData cho thống kê báo cáo
    private final MutableLiveData<ReportSummary> reportSummary = new MutableLiveData<>();

    // LiveData cho danh sách báo cáo mới nhất
    private final MutableLiveData<List<LatestReport>> latestReports = new MutableLiveData<>(new ArrayList<>());

    // LiveData cho trạng thái loading
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // LiveData cho thông báo lỗi
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Khoảng thời gian hiện tại
    private ReportDashboardService.TimeRange currentTimeRange = ReportDashboardService.TimeRange.LAST_WEEK;

    // Getters cho LiveData
    public LiveData<ReportSummary> getReportSummary() {
        return reportSummary;
    }

    public LiveData<List<LatestReport>> getLatestReports() {
        return latestReports;
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

    public void setCurrentTimeRange(ReportDashboardService.TimeRange timeRange) {
        this.currentTimeRange = timeRange;
        loadDashboardData();
    }

    /**
     * Tải dữ liệu cho dashboard
     */
    public void loadDashboardData() {
        isLoading.setValue(true);

        // Tải thống kê báo cáo
        ReportDashboardService.getReportSummary(currentTimeRange, new ReportDashboardService.GetReportSummaryCallback() {
            @Override
            public void onSuccess(ReportSummary summary) {
                reportSummary.setValue(summary);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Lỗi khi tải thống kê báo cáo: " + e.getMessage());
                isLoading.setValue(false);
            }
        });

        // Tải danh sách báo cáo mới nhất
        loadLatestReports();
    }

    /**
     * Tải danh sách báo cáo mới nhất
     */
    public void loadLatestReports() {
        isLoading.setValue(true);

        ReportDashboardService.getLatestReports(5, new ReportDashboardService.GetLatestReportsCallback() {
            @Override
            public void onSuccess(List<LatestReport> reports) {
                latestReports.setValue(reports);
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Lỗi khi tải báo cáo mới nhất: " + e.getMessage());
                isLoading.setValue(false);
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
        return (percent >= 0 ? "+" : "") + String.format("%.1f%%", percent);
    }

    /**
     * Chuyển đến màn hình xử lý báo cáo
     */
    public void navigateToReportDetail(LatestReport report) {
        // TODO: Implement navigation to report detail screen
    }
}