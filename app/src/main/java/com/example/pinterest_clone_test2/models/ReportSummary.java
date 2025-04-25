package com.example.pinterest_clone_test2.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Model để lưu trữ tổng hợp thống kê báo cáo
 */
public class ReportSummary {
    // Tổng số lượng báo cáo
    private int totalReports;
    private int totalPendingReports;
    private long averageProcessingTimeInHours;

    // So sánh với kỳ trước
    private float totalReportsChangePercent;
    private float averageProcessingTimeChangePercent;

    // Số lượng theo loại
    private int pinReports;
    private int commentReports;

    // Số lượng theo trạng thái
    private int checkedReports;
    private int uncheckedReports;

    // Số lượng theo lý do
    private Map<String, Integer> reportReasonCounts;

    // Dữ liệu xu hướng
    private Map<String, Integer> trendData;

    public ReportSummary() {
        reportReasonCounts = new HashMap<>();
        trendData = new HashMap<>();
    }

    // Getters
    public int getTotalReports() {
        return totalReports;
    }

    public int getTotalPendingReports() {
        return totalPendingReports;
    }

    public long getAverageProcessingTimeInHours() {
        return averageProcessingTimeInHours;
    }

    public float getTotalReportsChangePercent() {
        return totalReportsChangePercent;
    }

    public float getAverageProcessingTimeChangePercent() {
        return averageProcessingTimeChangePercent;
    }

    public int getPinReports() {
        return pinReports;
    }

    public int getCommentReports() {
        return commentReports;
    }

    public int getCheckedReports() {
        return checkedReports;
    }

    public int getUncheckedReports() {
        return uncheckedReports;
    }

    public Map<String, Integer> getReportReasonCounts() {
        return reportReasonCounts;
    }

    public Map<String, Integer> getTrendData() {
        return trendData;
    }

    // Setters (Builder pattern)
    public ReportSummary setTotalReports(int totalReports) {
        this.totalReports = totalReports;
        return this;
    }

    public ReportSummary setTotalPendingReports(int totalPendingReports) {
        this.totalPendingReports = totalPendingReports;
        return this;
    }

    public ReportSummary setAverageProcessingTimeInHours(long averageProcessingTimeInHours) {
        this.averageProcessingTimeInHours = averageProcessingTimeInHours;
        return this;
    }

    public ReportSummary setTotalReportsChangePercent(float totalReportsChangePercent) {
        this.totalReportsChangePercent = totalReportsChangePercent;
        return this;
    }

    public ReportSummary setAverageProcessingTimeChangePercent(float averageProcessingTimeChangePercent) {
        this.averageProcessingTimeChangePercent = averageProcessingTimeChangePercent;
        return this;
    }

    public ReportSummary setPinReports(int pinReports) {
        this.pinReports = pinReports;
        return this;
    }

    public ReportSummary setCommentReports(int commentReports) {
        this.commentReports = commentReports;
        return this;
    }

    public ReportSummary setCheckedReports(int checkedReports) {
        this.checkedReports = checkedReports;
        return this;
    }

    public ReportSummary setUncheckedReports(int uncheckedReports) {
        this.uncheckedReports = uncheckedReports;
        return this;
    }

    public ReportSummary setReportReasonCounts(Map<String, Integer> reportReasonCounts) {
        this.reportReasonCounts = reportReasonCounts;
        return this;
    }

    public ReportSummary setTrendData(Map<String, Integer> trendData) {
        this.trendData = trendData;
        return this;
    }

    public void addReportReasonCount(String reasonId, int count) {
        reportReasonCounts.put(reasonId, count);
    }

    public void addTrendDataPoint(String date, int count) {
        trendData.put(date, count);
    }
}