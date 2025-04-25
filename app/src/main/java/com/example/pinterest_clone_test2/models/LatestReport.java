package com.example.pinterest_clone_test2.models;

/**
 * Model đại diện cho báo cáo hiển thị trong danh sách báo cáo mới nhất
 */
public class LatestReport {
    public enum ReportType {
        PIN,
        COMMENT
    }

    private String reportId;
    private ReportType reportType;
    private String contentId; // ID của pin hoặc comment
    private String contentTitle; // Tiêu đề của pin hoặc nội dung của comment
    private String contentDescription; // Mô tả pin hoặc null nếu là comment
    private String contentThumbnail; // URL hình thu nhỏ
    private String reporterId; // ID người báo cáo
    private String reporterName; // Tên người báo cáo
    private String reporterAvatar; // URL avatar người báo cáo
    private long reportDate; // Thời gian báo cáo
    private boolean isChecked; // Đã kiểm tra hay chưa
    private String reportReason; // Lý do báo cáo phổ biến nhất

    public LatestReport() {
    }

    // Getters
    public String getReportId() {
        return reportId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getContentId() {
        return contentId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public String getContentThumbnail() {
        return contentThumbnail;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getReporterAvatar() {
        return reporterAvatar;
    }

    public long getReportDate() {
        return reportDate;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public String getReportReason() {
        return reportReason;
    }

    // Setters (Builder pattern)
    public LatestReport setReportId(String reportId) {
        this.reportId = reportId;
        return this;
    }

    public LatestReport setReportType(ReportType reportType) {
        this.reportType = reportType;
        return this;
    }

    public LatestReport setContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    public LatestReport setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
        return this;
    }

    public LatestReport setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
        return this;
    }

    public LatestReport setContentThumbnail(String contentThumbnail) {
        this.contentThumbnail = contentThumbnail;
        return this;
    }

    public LatestReport setReporterId(String reporterId) {
        this.reporterId = reporterId;
        return this;
    }

    public LatestReport setReporterName(String reporterName) {
        this.reporterName = reporterName;
        return this;
    }

    public LatestReport setReporterAvatar(String reporterAvatar) {
        this.reporterAvatar = reporterAvatar;
        return this;
    }

    public LatestReport setReportDate(long reportDate) {
        this.reportDate = reportDate;
        return this;
    }

    public LatestReport setChecked(boolean checked) {
        isChecked = checked;
        return this;
    }

    public LatestReport setReportReason(String reportReason) {
        this.reportReason = reportReason;
        return this;
    }
}